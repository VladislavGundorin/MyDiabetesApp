package com.example.mydiabetesapp.core

import android.app.Application
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import com.github.mikephil.charting.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MyDiabetesApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
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
