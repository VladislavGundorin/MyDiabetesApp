package com.example.mydiabetesapp.feature.profile.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepository(private val userProfileDao: UserProfileDao) {

    fun getProfile(): Flow<UserProfile?> {
        return userProfileDao.getAllUserProfiles().map { profiles ->
            profiles.find { it.id == 1 }
        }
    }

    suspend fun updateProfile(profile: UserProfile) {
        userProfileDao.updateUserProfile(profile)
    }

    suspend fun insertProfile(profile: UserProfile): Long {
        return userProfileDao.insertUserProfile(profile)
    }
}
