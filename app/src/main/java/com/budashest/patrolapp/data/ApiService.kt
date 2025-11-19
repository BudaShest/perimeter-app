package com.budashest.patrolapp.data

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("schedules/today")
    suspend fun getTodaySchedules(): Response<ApiResponse> // ★★★ Теперь ожидаем ApiResponse, а не List<Schedule> ★★★
}