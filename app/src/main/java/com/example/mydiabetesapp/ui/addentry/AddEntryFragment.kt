package com.example.mydiabetesapp.ui.addentry

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.GlucoseEntry
import com.example.mydiabetesapp.databinding.FragmentAddEntryBinding
import com.example.mydiabetesapp.repository.GlucoseRepository
import com.example.mydiabetesapp.ui.viewmodel.GlucoseViewModel
import com.example.mydiabetesapp.ui.viewmodel.GlucoseViewModelFactory
import java.util.Calendar

class AddEntryFragment : Fragment() {

    private var _binding: FragmentAddEntryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: GlucoseViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val calendar = Calendar.getInstance()
        updateDateInView(calendar)
        updateTimeInView(calendar)

        binding.tvDate.setOnClickListener {
            val currentDate = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
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
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.tvTime.text = selectedTime
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true
            ).show()
        }

        val dao = AppDatabase.getDatabase(requireContext()).glucoseDao()
        val repository = GlucoseRepository(dao)
        val factory = GlucoseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(GlucoseViewModel::class.java)

        binding.btnSave.setOnClickListener {
            val date = binding.tvDate.text.toString().trim()
            val time = binding.tvTime.text.toString().trim()
            val glucoseLevel = binding.etGlucoseInput.text.toString().toFloatOrNull()
            val selectedCategory = binding.spinnerCategory.selectedItem?.toString()?.trim() ?: ""
            if (date.isNotEmpty() && time.isNotEmpty() && glucoseLevel != null && selectedCategory.isNotEmpty()) {
                val entry = GlucoseEntry(
                    date = date,
                    time = time,
                    glucoseLevel = glucoseLevel,
                    category = selectedCategory
                )
                viewModel.addEntry(entry)
                Toast.makeText(requireContext(), "Запись добавлена", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
