package com.budashest.patrolapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budashest.patrolapp.data.Schedule
import com.budashest.patrolapp.data.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {
    private val repository = ScheduleRepository()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadSchedules() {
        _isLoading.value = true
        viewModelScope.launch {
            _schedules.value = repository.getTodaySchedules()
            _isLoading.value = false
        }
    }
}