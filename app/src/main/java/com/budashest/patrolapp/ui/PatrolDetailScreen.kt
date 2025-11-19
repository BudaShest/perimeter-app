package com.budashest.patrolapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.budashest.patrolapp.data.Point
import com.budashest.patrolapp.data.Schedule

@Composable
fun PatrolDetailScreen(
    schedule: Schedule,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовок с кнопкой назад
        Row(modifier = Modifier.padding(16.dp)) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
            Text(
                text = schedule.name ?: "Без названия",
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Информация об обходе
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Время: ${schedule.timeRange ?: "Не указано"}")
                Text(text = "Маршрут: ${schedule.route?.name ?: "Без названия"}")
                Text(text = "Зона: ${schedule.route?.area ?: "Не указана"}")
                Text(text = "Количество точек: ${schedule.route?.points?.size ?: 0}")
            }
        }

        // Список точек
        Text(
            text = "Точки обхода:",
            modifier = Modifier.padding(16.dp)
        )

        // ★★★ БЕЗОПАСНЫЙ СПИСОК ТОЧЕК ★★★
        val points = schedule.route?.points ?: emptyList()

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(points) { point ->
                PointCard(point = point)
            }
        }
    }
}

@Composable
fun PointCard(point: Point) {
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = point.name ?: "Без названия")
            Text(text = "UID: ${point.uid ?: "Без UID"}")
            Text(text = "Порядок: ${point.stepOrder}")
        }
    }
}