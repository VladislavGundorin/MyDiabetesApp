package com.example.mydiabetesapp.ui.editentry

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
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.GlucoseEntry
import com.example.mydiabetesapp.databinding.FragmentEditEntryBinding
import com.example.mydiabetesapp.repository.GlucoseRepository
import com.example.mydiabetesapp.ui.viewmodel.GlucoseViewModel
import com.example.mydiabetesapp.ui.viewmodel.GlucoseViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditEntryFragment : Fragment() {

    private var _binding: FragmentEditEntryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GlucoseViewModel

    private val args: EditEntryFragmentArgs by navArgs()
    private var entryId: Int = -1
    private var currentUserId: Int = -1

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        entryId = args.entryId

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val dao = AppDatabase.getDatabase(requireContext()).glucoseDao()
        val repository = GlucoseRepository(dao)
        val factory = GlucoseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[GlucoseViewModel::class.java]

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
            val glucoseLevel = binding.etGlucoseInput.text.toString().toFloatOrNull()
            val category = binding.spinnerCategory.selectedItem?.toString()?.trim() ?: ""
            if (entryId != -1 && date.isNotEmpty() && time.isNotEmpty() &&
                glucoseLevel != null && category.isNotEmpty() && currentUserId != -1) {

                val updatedEntry = GlucoseEntry(
                    id = entryId,
                    userId = currentUserId,
                    date = date,
                    time = time,
                    glucoseLevel = glucoseLevel,
                    category = category
                )
                viewModel.updateEntry(updatedEntry)
                Toast.makeText(requireContext(), "Запись обновлена", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fillFields(entry: GlucoseEntry) {
        currentUserId = entry.userId
        binding.tvDate.text = entry.date
        binding.tvTime.text = entry.time
        binding.etGlucoseInput.setText(entry.glucoseLevel.toString())
        val categories = resources.getStringArray(com.example.mydiabetesapp.R.array.glucose_categories)
        val index = categories.indexOf(entry.category)
        if (index != -1) {
            binding.spinnerCategory.setSelection(index)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
