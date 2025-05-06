package com.example.mydiabetesapp.feature.weight.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.weight.data.WeightEntry
import com.example.mydiabetesapp.databinding.FragmentEditWeightBinding
import com.example.mydiabetesapp.feature.weight.data.WeightRepository
import com.example.mydiabetesapp.feature.weight.viewmodel.WeightViewModel
import com.example.mydiabetesapp.feature.weight.viewmodel.WeightViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditWeightFragment : Fragment() {

    private var _binding: FragmentEditWeightBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WeightViewModel

    private val args: EditWeightFragmentArgs by navArgs()
    private var entryId: Int = -1
    private var currentUserId: Int = -1

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        entryId = args.entryId

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val dao = AppDatabase.getDatabase(requireContext()).weightDao()
        val repository = WeightRepository(dao)
        val factory = WeightViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(WeightViewModel::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { allEntries ->
                val currentEntry = allEntries.find { it.id == entryId }
                if (currentEntry != null) {
                    fillFields(currentEntry)
                }
            }
        }

        binding.tvDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%02d.%02d.%d", dayOfMonth, month + 1, year)
                    binding.tvDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.tvTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.tvTime.text = selectedTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        binding.btnSave.setOnClickListener {
            val date = binding.tvDate.text.toString().trim()
            val time = binding.tvTime.text.toString().trim()
            val weight = binding.etWeightInput.text.toString().toFloatOrNull()
            if (entryId != -1 && date.isNotEmpty() && time.isNotEmpty() && weight != null && currentUserId != -1) {
                val updatedEntry = WeightEntry(
                    id = entryId,
                    userId = currentUserId,
                    date = date,
                    time = time,
                    weight = weight
                )
                viewModel.updateEntry(updatedEntry)
                Toast.makeText(requireContext(), "Запись обновлена", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fillFields(entry: WeightEntry) {
        currentUserId = entry.userId
        binding.tvDate.text = entry.date
        binding.tvTime.text = entry.time
        binding.etWeightInput.setText(entry.weight.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
