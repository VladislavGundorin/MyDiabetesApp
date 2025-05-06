package com.example.mydiabetesapp.feature.hba1c.ui

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
import androidx.navigation.fragment.navArgs
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.databinding.FragmentEditHba1cBinding
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cRepository
import com.example.mydiabetesapp.feature.hba1c.viewmodel.Hba1cViewModel
import com.example.mydiabetesapp.feature.hba1c.viewmodel.Hba1cViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditHba1cFragment : Fragment() {

    private var _binding: FragmentEditHba1cBinding? = null
    private val binding get() = _binding!!

    private val args: EditHba1cFragmentArgs by navArgs()
    private lateinit var viewModel: Hba1cViewModel

    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditHba1cBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        lifecycleScope.launch {
            viewModel.get(args.entryId)?.let { entry ->
                binding.tvDate.text = entry.date
                binding.etHba1c.setText("%.2f".format(entry.hba1c))
            } ?: run {
                Toast.makeText(requireContext(), "Запись не найдена", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        binding.containerDate.setOnClickListener { showDatePicker() }

        binding.btnSave.setOnClickListener {
            val text = binding.etHba1c.text.toString()
            val value = text.toFloatOrNull()
            if (value == null) {
                Toast.makeText(requireContext(), "Введите корректное значение HbA1c", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updated = Hba1cEntry(
                id = args.entryId,
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
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.tvDate.text = String.format("%02d.%02d.%04d", day, month + 1, year)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
