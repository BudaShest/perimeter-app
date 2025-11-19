package com.budashest.patrolapp.data

import com.google.gson.annotations.SerializedName

// ★★★ БЕЗОПАСНЫЕ МОДЕЛИ С NULLABLE ПОЛЯМИ ★★★
data class ApiResponse(
    @SerializedName("success") val success: Boolean? = false,
    @SerializedName("date") val date: String? = "",
    @SerializedName("day_of_week") val dayOfWeek: Int? = 0,
    @SerializedName("current_time") val currentTime: String? = "",
    @SerializedName("schedules") val schedules: List<Schedule>? = emptyList()
)

data class Schedule(
    @SerializedName("id") val id: Int? = 0,
    @SerializedName("name") val name: String? = "",
    @SerializedName("time_range") val timeRange: String? = "",
    @SerializedName("route") val route: Route? = null
)

data class Route(
    @SerializedName("id") val id: Int? = 0,
    @SerializedName("name") val name: String? = "",
    @SerializedName("area") val area: String? = "",
    @SerializedName("points") val points: List<Point>? = emptyList()
)

data class Point(
    @SerializedName("id") val id: Int? = 0,
    @SerializedName("name") val name: String? = "",
    @SerializedName("uid") val uid: String? = "",
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lon") val lon: Double? = null,
    @SerializedName("pivot") val pivot: PointPivot? = null
) {
    // ★★★ БЕЗОПАСНОЕ ПОЛУЧЕНИЕ stepOrder ★★★
    val stepOrder: Int
        get() = pivot?.stepOrder ?: 0
}

data class PointPivot(
    @SerializedName("route_version_id") val routeVersionId: Int? = 0,
    @SerializedName("point_id") val pointId: Int? = 0,
    @SerializedName("step_order") val stepOrder: Int? = 0
)