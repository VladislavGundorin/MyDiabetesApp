package com.example.mydiabetesapp.ui.addentry

import android.app.*
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mydiabetesapp.data.database.*
import com.example.mydiabetesapp.databinding.FragmentAddPressureBinding
import com.example.mydiabetesapp.repository.BloodPressureRepository
import com.example.mydiabetesapp.ui.viewmodel.BloodPressureViewModel
import com.example.mydiabetesapp.ui.viewmodel.BloodPressureViewModelFactory
import java.util.*

class AddPressureFragment : Fragment() {

    private var _binding: FragmentAddPressureBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BloodPressureViewModel
    private val calendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddPressureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        updateDateTime(calendar)

        binding.containerDate.setOnClickListener { pickDate() }
        binding.containerTime.setOnClickListener { pickTime() }

        val repository = BloodPressureRepository(AppDatabase.getDatabase(requireContext()).bloodPressureDao())
        val factory = BloodPressureViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(BloodPressureViewModel::class.java)

        binding.btnSave.setOnClickListener { saveEntry() }
    }

    private fun pickDate() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                updateDateTime(calendar)
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun pickTime() {
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateDateTime(calendar)
            },
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            true
        ).show()
    }

    private fun updateDateTime(calendar: Calendar) {
        binding.tvDate.text = "%02d.%02d.%d".format(
            calendar[Calendar.DAY_OF_MONTH], calendar[Calendar.MONTH] + 1, calendar[Calendar.YEAR])
        binding.tvTime.text = "%02d:%02d".format(calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE])
    }

    private fun saveEntry() {
        val systolic = binding.etSystolic.text.toString().toIntOrNull()
        val diastolic = binding.etDiastolic.text.toString().toIntOrNull()
        val pulse = binding.etPulse.text.toString().toIntOrNull()

        if (systolic == null || diastolic == null || pulse == null) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val entry = BloodPressureEntry(
            userId = 1,
            date = binding.tvDate.text.toString(),
            time = binding.tvTime.text.toString(),
            systolic = systolic,
            diastolic = diastolic,
            pulse = pulse
        )

        viewModel.insert(entry)
        Toast.makeText(requireContext(), "Запись добавлена", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
