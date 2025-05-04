package com.example.mydiabetesapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.data.database.Hba1cEntry
import com.example.mydiabetesapp.repository.Hba1cRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class Hba1cViewModel(private val repo: Hba1cRepository) : ViewModel() {

    val entries: StateFlow<List<Hba1cEntry>> =
        repo.getEntries().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    suspend fun get(id:Int)       = repo.get(id)
    fun     delete(e:Hba1cEntry)  = viewModelScope.launch { repo.delete(e) }

    fun add(e:Hba1cEntry)    = viewModelScope.launch { repo.add(e) }
    fun update(e:Hba1cEntry) = viewModelScope.launch { repo.update(e) }
}

class Hba1cViewModelFactory(private val repo: Hba1cRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        Hba1cViewModel(repo) as T
}
