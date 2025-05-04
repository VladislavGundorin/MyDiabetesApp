package com.example.mydiabetesapp.ui.hba1cstatistics

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.Hba1cEntry
import com.example.mydiabetesapp.databinding.FragmentEditHba1cBinding
import com.example.mydiabetesapp.repository.Hba1cRepository
import com.example.mydiabetesapp.ui.viewmodel.Hba1cViewModel
import com.example.mydiabetesapp.ui.viewmodel.Hba1cViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditHba1cFragment : Fragment() {
    private var _binding: FragmentEditHba1cBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: Hba1cViewModel
    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var entryId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditHba1cBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        entryId = arguments?.getInt("entryId") ?: -1
        if (entryId == -1) {
            Toast.makeText(requireContext(), "Неверный идентификатор записи", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProvider(
            this,
            Hba1cViewModelFactory(
                Hba1cRepository(
                    AppDatabase
                        .getDatabase(requireContext())
                        .hba1cDao()
                )
            )
        )[Hba1cViewModel::class.java]

        binding.containerDate.setOnClickListener { showDatePicker() }

        viewLifecycleOwner.lifecycleScope.launch {
            val list = viewModel.entries.first()
            list.find { it.id == entryId }?.let {
                binding.tvDate.text = it.date
                binding.etHba1c.setText(it.hba1c.toString())
            }
        }

        binding.btnSave.setOnClickListener {
            val value = binding.etHba1c.text.toString().toFloatOrNull()
            if (value == null) {
                Toast.makeText(requireContext(), "Введите значение HbA1c", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updated = Hba1cEntry(
                id = entryId,
                userId = 1,
                date = binding.tvDate.text.toString(),
                hba1c = value
            )
            viewModel.update(updated)
            Toast.makeText(requireContext(), "Запись обновлена", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.tvDate.text = String.format("%02d.%02d.%d", day, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
