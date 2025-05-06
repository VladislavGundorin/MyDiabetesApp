package com.example.mydiabetesapp.ui.export

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.ReportEntry
import com.example.mydiabetesapp.databinding.FragmentExportBinding
import com.example.mydiabetesapp.repository.ReportRepository
import com.example.mydiabetesapp.ui.export.ReportAdapter
import com.example.mydiabetesapp.ui.viewmodel.ReportVMFactory
import com.example.mydiabetesapp.ui.viewmodel.ReportViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ExportFragment : Fragment() {

    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: ReportViewModel

    private val adapter = ReportAdapter(
        onOpen     = ::openFile,
        onDownload = ::downloadReport,
        onDelete   = { vm.delete(it) }
    )

    private var start: LocalDate = LocalDate.now()
    private var end: LocalDate   = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val db   = AppDatabase.getDatabase(requireContext())
        val repo = ReportRepository(
            db.reportDao(),
            db.userProfileDao(),
            db.glucoseDao(),
            db.weightDao(),
            db.bloodPressureDao(),
            db.hba1cDao()
        )
        vm = ViewModelProvider(this, ReportVMFactory(repo))[ReportViewModel::class.java]

        binding.startDate.setOnClickListener { pickDate(true) }
        binding.endDate  .setOnClickListener { pickDate(false) }

        binding.btnGenerate.setOnClickListener {
            vm.generate(requireContext(), start, end)
        }

        binding.rvReports.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReports.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            vm.reports.collect { list ->
                adapter.submitList(list)
            }
        }

        updateUi()
    }

    private fun pickDate(isStart: Boolean) {
        val date = if (isStart) start else end
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val picked = LocalDate.of(y, m + 1, d)
                if (isStart) start = picked else end = picked
                updateUi()
            },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        ).show()
    }

    private fun updateUi() {
        binding.apply {
            startDate.setText(start.format(DATE_FMT))
            endDate  .setText(end  .format(DATE_FMT))
            tvDaysCount.text =
                "Выбрано дней: ${ChronoUnit.DAYS.between(start, end) + 1}"
        }
    }

    private fun openFile(report: ReportEntry) {
        val srcFile = File(Uri.parse(report.uri).path!!)
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            srcFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Открыть отчёт"))
    }

    private fun downloadReport(report: ReportEntry) {
        try {
            val srcFile = File(Uri.parse(report.uri).path!!)
            val downloadsDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dstFile = File(downloadsDir, report.fileName)
            srcFile.copyTo(dstFile, overwrite = true)
            Toast.makeText(
                requireContext(),
                "Сохранено: ${dstFile.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Ошибка при сохранении: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}
