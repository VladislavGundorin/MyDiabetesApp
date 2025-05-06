package com.example.mydiabetesapp.core

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureDao
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureEntry
import com.example.mydiabetesapp.feature.glucose.data.GlucoseDao
import com.example.mydiabetesapp.feature.glucose.data.GlucoseEntry
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cDao
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry
import com.example.mydiabetesapp.feature.notification.data.NotificationDao
import com.example.mydiabetesapp.feature.notification.data.NotificationEntry
import com.example.mydiabetesapp.feature.export.data.ReportDao
import com.example.mydiabetesapp.feature.export.data.ReportEntry
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import com.example.mydiabetesapp.feature.profile.data.UserProfileDao
import com.example.mydiabetesapp.feature.weight.data.WeightDao
import com.example.mydiabetesapp.feature.weight.data.WeightEntry

@Database(entities = [UserProfile::class, GlucoseEntry::class, WeightEntry::class, NotificationEntry::class, ReportEntry::class, Hba1cEntry::class, BloodPressureEntry::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun glucoseDao(): GlucoseDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightDao(): WeightDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reportDao(): ReportDao
    abstract fun hba1cDao(): Hba1cDao
    abstract fun bloodPressureDao(): BloodPressureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glucose_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                Log.d("AppDatabase","DB Created: $instance")
                INSTANCE = instance
                    instance
            }
        }
    }
}
