package com.budashest.patrolapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.budashest.patrolapp.data.Schedule
import com.budashest.patrolapp.ui.PatrolDetailScreen
import com.budashest.patrolapp.ui.ScheduleListScreen
import com.budashest.patrolapp.ui.theme.PatrolAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PatrolAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PatrolApp()
                }
            }
        }
    }
}

@Composable
fun PatrolApp() {
    val currentScreen = remember { mutableStateOf<Screen>(Screen.ScheduleList) }
    val selectedSchedule = remember { mutableStateOf<Schedule?>(null) }

    when (val screen = currentScreen.value) {
        is Screen.ScheduleList -> {
            ScheduleListScreen(
                onScheduleClick = { schedule ->
                    selectedSchedule.value = schedule
                    currentScreen.value = Screen.PatrolDetail
                }
            )
        }
        is Screen.PatrolDetail -> {
            selectedSchedule.value?.let { schedule ->
                PatrolDetailScreen(
                    schedule = schedule,
                    onBackClick = {
                        currentScreen.value = Screen.ScheduleList
                    }
                )
            }
        }
    }
}

sealed class Screen {
    object ScheduleList : Screen()
    object PatrolDetail : Screen()
}

@Preview(showBackground = true)
@Composable
fun PatrolAppPreview() {
    PatrolAppTheme {
        PatrolApp()
    }
}