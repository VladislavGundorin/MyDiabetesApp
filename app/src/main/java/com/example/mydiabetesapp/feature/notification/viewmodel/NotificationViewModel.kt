package com.example.mydiabetesapp.feature.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.feature.notification.data.NotificationEntry
import com.example.mydiabetesapp.feature.notification.data.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntry>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun addNotificationAndReturnId(entry: NotificationEntry): Long {
        return repository.addNotification(entry)
    }

    fun updateNotification(entry: NotificationEntry) {
        viewModelScope.launch {
            repository.updateNotification(entry)
        }
    }

    fun deleteNotification(entry: NotificationEntry) {
        viewModelScope.launch {
            repository.deleteNotification(entry)
        }
    }
}
