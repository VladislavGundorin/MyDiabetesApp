package com.example.mydiabetesapp

import android.app.Application
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyDiabetesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            val userProfileDao = db.userProfileDao()
            if (userProfileDao.getUserById(1) == null) {
                val defaultProfile = UserProfile(
                    id = 1,
                    name = "",
                    gender = "",
                    age = 0,
                    height = 0f,
                    weight = 0f
                )
                userProfileDao.insertUserProfile(defaultProfile)
            }
        }
    }
}
