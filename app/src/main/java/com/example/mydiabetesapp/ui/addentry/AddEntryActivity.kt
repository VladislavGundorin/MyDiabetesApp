package com.example.mydiabetesapp.ui.addentry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.GlucoseEntry
import com.example.mydiabetesapp.databinding.ActivityAddEntryBinding
import com.example.mydiabetesapp.repository.GlucoseRepository
import com.example.mydiabetesapp.ui.viewmodel.GlucoseViewModel
import com.example.mydiabetesapp.ui.viewmodel.GlucoseViewModelFactory
import java.util.Calendar

class AddEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEntryBinding
    private lateinit var viewModel: GlucoseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendar = Calendar.getInstance()
        updateDateInView(calendar)
        updateTimeInView(calendar)

        binding.tvDate.setOnClickListener {
            val currentDate = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%02d.%02d.%d", dayOfMonth, month + 1, year)
                    binding.tvDate.text = selectedDate
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.tvTime.setOnClickListener {
            val currentTime = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.tvTime.text = selectedTime
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true
            ).show()
        }

        val dao = AppDatabase.getDatabase(this).glucoseDao()
        val repository = GlucoseRepository(dao)
        val factory = GlucoseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(GlucoseViewModel::class.java)

        binding.btnSave.setOnClickListener {
            val date = binding.tvDate.text.toString()
            val time = binding.tvTime.text.toString()
            val glucoseLevel = binding.etGlucose.text.toString().toFloatOrNull()

            if (date.isNotEmpty() && time.isNotEmpty() && glucoseLevel != null) {
                val entry = GlucoseEntry(
                    date = date,
                    time = time,
                    glucoseLevel = glucoseLevel,
                )
                viewModel.addEntry(entry)
                Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateDateInView(calendar: Calendar) {
        val date = String.format(
            "%02d.%02d.%d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
        binding.tvDate.text = date
    }

    private fun updateTimeInView(calendar: Calendar) {
        val time = String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
        binding.tvTime.text = time
    }
}
