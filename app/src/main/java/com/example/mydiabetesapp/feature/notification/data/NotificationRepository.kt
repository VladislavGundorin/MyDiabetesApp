package com.example.mydiabetesapp.feature.notification.data

import kotlinx.coroutines.flow.Flow

class NotificationRepository(private val dao: NotificationDao) {

    fun getNotifications(): Flow<List<NotificationEntry>> = dao.getAllNotifications()

    suspend fun addNotification(entry: NotificationEntry): Long {
        return dao.insertNotification(entry)
    }

    suspend fun updateNotification(entry: NotificationEntry) {
        dao.updateNotification(entry)
    }

    suspend fun deleteNotification(entry: NotificationEntry) {
        dao.deleteNotification(entry.id)
    }
}
