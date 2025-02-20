package com.example.mydiabetesapp.ui.addentry

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

class AddEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEntryBinding
    private lateinit var viewModel: GlucoseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getDatabase(this).glucoseDao()
        val repository = GlucoseRepository(dao)
        val factory = GlucoseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(GlucoseViewModel::class.java)

        binding.btnSave.setOnClickListener {
            val date = binding.etDate.text.toString()
            val time = binding.etTime.text.toString()
            val glucoseLevel = binding.etGlucose.text.toString().toFloatOrNull()
            val insulinDose = binding.etInsulin.text.toString().toFloatOrNull()
            val carbs = binding.etCarbs.text.toString().toFloatOrNull()

            if (date.isNotEmpty() && time.isNotEmpty() && glucoseLevel != null) {
                val entry = GlucoseEntry(
                    date = date,
                    time = time,
                    glucoseLevel = glucoseLevel,
                    insulineDose = insulinDose,
                    carbs = carbs
                )
                viewModel.addEntry(entry)
                Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
