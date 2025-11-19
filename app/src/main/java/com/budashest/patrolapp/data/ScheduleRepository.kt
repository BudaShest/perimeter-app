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

                // ★★★ БЕЗОПАСНАЯ ПРОВЕРКА ВСЕХ ПОЛЕЙ ★★★
                if (apiResponse?.success == true) {
                    // Безопасно извлекаем schedules
                    val schedules = apiResponse.schedules ?: emptyList()

                    Log.d("API", "✅ УСПЕХ! Получено обходов: ${schedules.size}")

                    // Безопасно логируем каждый обход
                    schedules.forEach { schedule ->
                        val pointsCount = schedule.route?.points?.size ?: 0
                        Log.d("API", "   📋 ${schedule.name ?: "Без названия"} - $pointsCount точек")

                        // Безопасно логируем точки
                        schedule.route?.points?.forEach { point ->
                            Log.d("API", "      • ${point.stepOrder}. ${point.name ?: "Без названия"} (UID: ${point.uid ?: "Без UID"})")
                        }
                    }

                    schedules
                } else {
                    Log.e("API", "❌ API вернуло success=false или null")
                    emptyList()
                }
            } else {
                Log.e("API", "❌ HTTP ошибка: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("API", "❌ Ошибка подключения: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}