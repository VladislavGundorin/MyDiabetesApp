package com.example.mydiabetesapp.feature.sync.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mydiabetesapp.feature.glucose.data.GlucoseEntry
import com.example.mydiabetesapp.feature.glucose.data.GlucoseRepository
import com.example.mydiabetesapp.feature.sync.ui.SyncFragment
import kotlinx.coroutines.launch

class SyncViewModel(private val repo: GlucoseRepository) : ViewModel() {
    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

    fun updateStatus(text: String) = _status.postValue(text)

    fun saveMeasurement(m: SyncFragment.Measurement) {
        viewModelScope.launch {
            repo.insert(
                GlucoseEntry(
                userId       = 1,
                date         = m.date,
                time         = m.time,
                glucoseLevel = m.value,
                category     = m.context
            )
            )
            Log.d("SyncViewModel", "Inserted glucose: ${m.date} ${m.time} = ${m.value}")
        }
    }
}
