package com.example.mydiabetesapp.feature.hba1c.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Hba1cViewModel(private val repo: Hba1cRepository) : ViewModel() {

    val entries: StateFlow<List<Hba1cEntry>> =
        repo.getEntries().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun get(id:Int)       = repo.get(id)
    fun     delete(e: Hba1cEntry)  = viewModelScope.launch { repo.delete(e) }

    fun add(e: Hba1cEntry)    = viewModelScope.launch { repo.add(e) }
    fun update(e: Hba1cEntry) = viewModelScope.launch { repo.update(e) }
}
