package com.example.mydiabetesapp.ui.addweight

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
import com.example.mydiabetesapp.data.database.WeightEntry
import com.example.mydiabetesapp.databinding.FragmentAddWeightBinding
import com.example.mydiabetesapp.repository.WeightRepository
import com.example.mydiabetesapp.ui.viewmodel.WeightViewModel
import com.example.mydiabetesapp.ui.viewmodel.WeightViewModelFactory
import java.util.Calendar

class AddWeightFragment : Fragment() {

    private var _binding: FragmentAddWeightBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WeightViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWeightBinding.inflate(inflater, container, false)
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

        val dao = AppDatabase.getDatabase(requireContext()).weightDao()
        val repository = WeightRepository(dao)
        val factory = WeightViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(WeightViewModel::class.java)

        binding.btnSave.setOnClickListener {
            val date = binding.tvDate.text.toString().trim()
            val time = binding.tvTime.text.toString().trim()
            val weight = binding.etWeightInput.text.toString().toFloatOrNull()
            if (date.isNotEmpty() && time.isNotEmpty() && weight != null) {
                val entry = WeightEntry(
                    userId = 1,
                    date = date,
                    time = time,
                    weight = weight
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
