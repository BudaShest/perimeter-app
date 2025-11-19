package com.budashest.patrolapp.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.budashest.patrolapp.data.Schedule

@Composable
fun ScheduleListScreen(
    onScheduleClick: (Schedule) -> Unit = {}
) {
    val viewModel: ScheduleViewModel = viewModel()
    val schedules by viewModel.schedules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSchedules()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Обходы на сегодня",
            modifier = Modifier.padding(16.dp)
        )

        // ★★★ БЕЗОПАСНЫЙ ИНДИКАТОР ★★★
        Text(
            text = if (schedules.isEmpty()) "📭 Нет данных" else "✅ Данные с сервера: ${schedules.size} обходов",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Загрузка с сервера...", modifier = Modifier.padding(top = 16.dp))
                }
            } else if (schedules.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Нет обходов для отображения")
                    Text("Возможно:")
                    Text("• На сегодня нет расписаний")
                    Text("• Сервер недоступен")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(schedules) { schedule ->
                        // ★★★ БЕЗОПАСНАЯ ПРОВЕРКА ОБЯЗАТЕЛЬНЫХ ПОЛЕЙ ★★★
                        if (schedule.id != null && schedule.name != null && schedule.route != null) {
                            ScheduleCard(
                                schedule = schedule,
                                onClick = { onScheduleClick(schedule) }
                            )
                        } else {
                            // Пропускаем обходы с отсутствующими обязательными полями
                            Log.w("UI", "Пропущен обход с отсутствующими полями: $schedule")
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.loadSchedules() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Обновить с сервера")
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: Schedule,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = schedule.name ?: "Без названия")
            Text(text = "Время: ${schedule.timeRange ?: "Не указано"}")
            Text(text = "Маршрут: ${schedule.route?.name ?: "Без названия"}")
            // ★★★ ИСПОЛЬЗУЕМ points ИЗ Schedule ★★★
            Text(text = "Точек: ${schedule.points?.size ?: 0}")
        }
    }
}