package com.example.mydiabetesapp.feature.hba1c.ui

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
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry
import com.example.mydiabetesapp.databinding.FragmentAddHba1cBinding
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cRepository
import com.example.mydiabetesapp.feature.hba1c.viewmodel.Hba1cViewModel
import com.example.mydiabetesapp.feature.hba1c.viewmodel.Hba1cViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddHba1cFragment : Fragment() {

    private var _binding: FragmentAddHba1cBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: Hba1cViewModel
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHba1cBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val dao = AppDatabase.getDatabase(requireContext()).hba1cDao()
        viewModel = ViewModelProvider(
            this,
            Hba1cViewModelFactory(Hba1cRepository(dao))
        )[Hba1cViewModel::class.java]

        val now = Calendar.getInstance()
        binding.tvDate.text = dateFormat.format(now.time)
        binding.tvTime.text = timeFormat.format(now.time)

        binding.containerDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val sel = Calendar.getInstance().apply {
                        set(y, m, d)
                    }
                    binding.tvDate.text = dateFormat.format(sel.time)
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.containerTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, h, min ->
                    val sel = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, min)
                    }
                    binding.tvTime.text = timeFormat.format(sel.time)
                },
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        }

        binding.btnSave.setOnClickListener {
            val hba = binding.etHba1c.text.toString().toFloatOrNull()
            if (hba == null) {
                Toast.makeText(requireContext(), "Введите значение HbA1c", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val entry = Hba1cEntry(
                userId = 1,
                date = binding.tvDate.text.toString(),
                hba1c = hba
            )
            viewModel.add(entry)
            Toast.makeText(requireContext(), "Запись добавлена", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
