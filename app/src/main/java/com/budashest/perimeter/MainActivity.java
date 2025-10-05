package com.budashest.perimeter; // замените на ваш package name

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private HttpLogger httpLogger;
    private TextView logTextView;
    private StringBuilder logHistory;

    // ЗАМЕНИТЕ ЭТОТ URL НА ВАШ СЕРВЕР!
    private static final String LOG_SERVER_URL = "https://your-webhook-server.com/api/nfc-logs";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logTextView = findViewById(R.id.logTextView);
        logHistory = new StringBuilder();
        addToLog("Приложение запущено. Готово к чтению NFC меток.");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC не поддерживается на этом устройстве", Toast.LENGTH_LONG).show();
            addToLog("Ошибка: NFC не поддерживается");
            finish();
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "Включите NFC в настройках", Toast.LENGTH_LONG).show();
            addToLog("Предупреждение: NFC отключен");
        }

        httpLogger = new HttpLogger();
        addToLog("HTTP логгер инициализирован");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                new NfcReaderTask().execute(tag);
            }
        }
    }

    private class NfcReaderTask extends AsyncTask<Tag, Void, String> {
        private String tagId;

        @Override
        protected String doInBackground(Tag... tags) {
            Tag tag = tags[0];

            // Получаем ID метки
            byte[] tagIdBytes = tag.getId();
            tagId = bytesToHex(tagIdBytes);

            // Пытаемся прочитать NDEF данные
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                try {
                    ndef.connect();
                    NdefMessage ndefMessage = ndef.getNdefMessage();
                    if (ndefMessage != null) {
                        NdefRecord[] records = ndefMessage.getRecords();
                        for (NdefRecord record : records) {
                            if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN) {
                                // Текстовая запись
                                if (java.util.Arrays.equals(record.getType(), NdefRecord.RTD_TEXT)) {
                                    byte[] payload = record.getPayload();
                                    String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
                                    int languageCodeLength = payload[0] & 0x3F;
                                    return new String(payload, languageCodeLength + 1,
                                            payload.length - languageCodeLength - 1,
                                            textEncoding);
                                }
                                // URI запись
                                else if (java.util.Arrays.equals(record.getType(), NdefRecord.RTD_URI)) {
                                    byte[] payload = record.getPayload();
                                    String uriPrefix = getUriPrefix(payload[0]);
                                    String uri = uriPrefix + new String(payload, 1, payload.length - 1, StandardCharsets.UTF_8);
                                    return uri;
                                }
                            }
                        }
                    }
                    ndef.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Ошибка чтения NDEF: " + e.getMessage();
                }
            }

            return "RAW_DATA:" + tagId;
        }

        @Override
        protected void onPostExecute(String result) {
            String logMessage = "NFC метка прочитана:\nID: " + tagId + "\nДанные: " + result;
            addToLog(logMessage);

            // Отправляем лог на сервер
            httpLogger.sendLog(tagId, result, new HttpLogger.LogCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Лог отправлен на сервер", Toast.LENGTH_SHORT).show();
                        addToLog("✓ Лог успешно отправлен на сервер");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Ошибка отправки лога", Toast.LENGTH_SHORT).show();
                        addToLog("✗ Ошибка отправки лога: " + error);
                    });
                }
            });
        }

        private String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }

        private String getUriPrefix(byte prefixByte) {
            switch (prefixByte) {
                case 0x00: return "";
                case 0x01: return "http://www.";
                case 0x02: return "https://www.";
                case 0x03: return "http://";
                case 0x04: return "https://";
                case 0x05: return "tel:";
                case 0x06: return "mailto:";
                case 0x07: return "ftp://";
                default: return "";
            }
        }
    }

    private static class HttpLogger {
        private OkHttpClient client;
        private String deviceId;

        public interface LogCallback {
            void onSuccess();
            void onError(String error);
        }

        public HttpLogger() {
            this.client = new OkHttpClient();
            this.deviceId = UUID.randomUUID().toString();
        }

        public void sendLog(String tagId, String nfcData, LogCallback callback) {
            try {
                JSONObject json = new JSONObject();
                json.put("device_id", deviceId);
                json.put("tag_id", tagId);
                json.put("nfc_data", nfcData);
                json.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                json.put("event_type", "nfc_scan");

                RequestBody body = RequestBody.create(json.toString(), JSON);
                Request request = new Request.Builder()
                        .url(LOG_SERVER_URL)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("User-Agent", "NFC-Logger-Android")
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        callback.onError(e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onError("HTTP " + response.code() + ": " + response.message());
                        }
                    }
                });

            } catch (JSONException e) {
                callback.onError("JSON error: " + e.getMessage());
            }
        }
    }

    private void addToLog(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String logEntry = "[" + timestamp + "] " + message + "\n\n";
        logHistory.insert(0, logEntry);

        runOnUiThread(() -> {
            logTextView.setText(logHistory.toString());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                    this, 0, intent,
                    android.app.PendingIntent.FLAG_MUTABLE
            );

            String[][] techLists = new String[][]{
                    {Ndef.class.getName()},
                    {android.nfc.tech.NfcA.class.getName()},
                    {android.nfc.tech.NfcB.class.getName()},
                    {android.nfc.tech.NfcF.class.getName()},
                    {android.nfc.tech.NfcV.class.getName()}
            };

            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techLists);
            addToLog("NFC слушатель активирован");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
            addToLog("NFC слушатель деактивирован");
        }
    }
}