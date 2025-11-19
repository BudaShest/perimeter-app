package com.budashest.patrolapp.data

import android.util.Log

class ScheduleRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getTodaySchedules(): List<Schedule> {
        return try {
            Log.d("API", "🔄 Запрос к: http://10.0.2.2/api/schedules/today")

            val response = apiService.getTodaySchedules()

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true) {
                    val schedules = apiResponse.schedules ?: emptyList()

                    Log.d("API", "✅ УСПЕХ! Получено обходов: ${schedules.size}")

                    // ★★★ ЛОГИРУЕМ С ИСПРАВЛЕННОЙ СТРУКТУРОЙ ★★★
                    schedules.forEachIndexed { index, schedule ->
                        Log.d("API", "--- Обход #${index + 1} ---")
                        Log.d("API", "ID: ${schedule.id}")
                        Log.d("API", "Название: ${schedule.name}")
                        Log.d("API", "Время: ${schedule.timeRange}")
                        Log.d("API", "Маршрут: ${schedule.route?.name}")
                        Log.d("API", "Количество точек: ${schedule.points?.size ?: 0}")

                        // Логируем точки
                        schedule.points?.forEachIndexed { pointIndex, point ->
                            Log.d("API", "   Точка #${pointIndex + 1}:")
                            Log.d("API", "      ID: ${point.id}")
                            Log.d("API", "      Название: ${point.name}")
                            Log.d("API", "      UID: ${point.uid}")
                            Log.d("API", "      StepOrder: ${point.getStepOrder()}")
                        }
                    }

                    schedules
                } else {
                    Log.e("API", "❌ API вернуло success=false")
                    emptyList()
                }
            } else {
                Log.e("API", "❌ HTTP ошибка: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API", "❌ Ошибка: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}