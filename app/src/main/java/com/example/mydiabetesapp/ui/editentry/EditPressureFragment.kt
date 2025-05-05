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
import com.example.mydiabetesapp.data.database.BloodPressureEntry
import com.example.mydiabetesapp.databinding.FragmentEditPressureBinding
import com.example.mydiabetesapp.repository.BloodPressureRepository
import com.example.mydiabetesapp.ui.viewmodel.BloodPressureViewModel
import com.example.mydiabetesapp.ui.viewmodel.BloodPressureViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditPressureFragment : Fragment() {

    private var _binding: FragmentEditPressureBinding? = null
    private val b get() = _binding!!

    private lateinit var vm: BloodPressureViewModel
    private val args: EditPressureFragmentArgs by navArgs()

    private var entryId = -1
    private var currentUserId = -1

    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        i: LayoutInflater, c: ViewGroup?, s: Bundle?
    ): View {
        _binding = FragmentEditPressureBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        entryId = args.entryId

        b.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val repo = BloodPressureRepository(
            AppDatabase.getDatabase(requireContext()).bloodPressureDao()
        )
        vm = ViewModelProvider(
            this,
            BloodPressureViewModelFactory(repo)
        )[BloodPressureViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            vm.entries.collect { list ->
                list.find { it.id == entryId }?.let { fillFields(it) }
            }
        }

        b.containerDate.setOnClickListener { showDatePicker() }
        b.containerTime.setOnClickListener { showTimePicker() }
        b.btnSave.setOnClickListener { saveChanges() }
    }

    private fun fillFields(e: BloodPressureEntry) {
        currentUserId = e.userId
        b.tvDate.text = e.date
        b.tvTime.text = e.time
        b.etSystolic.setText(e.systolic.toString())
        b.etDiastolic.setText(e.diastolic.toString())
        b.etPulse.setText(e.pulse.toString())
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                b.tvDate.text = "%02d.%02d.%d".format(d, m + 1, y)
            },
            cal[Calendar.YEAR], cal[Calendar.MONTH], cal[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, h, m ->
                b.tvTime.text = "%02d:%02d".format(h, m)
            },
            cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE], true
        ).show()
    }

    private fun saveChanges() {
        val date = b.tvDate.text.toString().trim()
        val time = b.tvTime.text.toString().trim()
        val sys = b.etSystolic.text.toString().toIntOrNull()
        val dia = b.etDiastolic.text.toString().toIntOrNull()
        val pul = b.etPulse.text.toString().toIntOrNull()

        if (entryId != -1 && currentUserId != -1 &&
            date.isNotEmpty() && time.isNotEmpty() &&
            sys != null && dia != null && pul != null
        ) {
            val updated = BloodPressureEntry(
                id = entryId,
                userId = currentUserId,
                date = date,
                time = time,
                systolic = sys,
                diastolic = dia,
                pulse = pul
            )
            vm.update(updated)
            Toast.makeText(requireContext(), "Запись обновлена", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        } else {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
