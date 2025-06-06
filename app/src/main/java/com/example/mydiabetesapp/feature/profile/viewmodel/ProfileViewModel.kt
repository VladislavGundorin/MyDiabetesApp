package com.example.mydiabetesapp.feature.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import com.example.mydiabetesapp.feature.profile.data.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    val profile: StateFlow<UserProfile?> = repository.getProfile()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
        }
    }

    fun insertProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.insertProfile(profile)
        }
    }
}
