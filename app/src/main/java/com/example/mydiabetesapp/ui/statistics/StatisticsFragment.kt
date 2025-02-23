package com.example.mydiabetesapp.ui.statistics

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.GlucoseEntry
import com.example.mydiabetesapp.databinding.FragmentStatisticsBinding
import com.example.mydiabetesapp.repository.GlucoseRepository
import com.example.mydiabetesapp.ui.journal.GlucoseAdapter
import com.example.mydiabetesapp.ui.viewmodel.GlucoseViewModel
import com.example.mydiabetesapp.ui.viewmodel.GlucoseViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GlucoseViewModel
    private lateinit var lineChart: LineChart
    private lateinit var adapter: GlucoseAdapter

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var startDate: Date = Date()
    private var endDate: Date = Date()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> setDateRange(7)
                        1 -> setDateRange(30)
                        2 -> setDateRange(90)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.startDateInput.setOnClickListener {
            showDatePicker { date ->
                binding.startDateInput.setText(dateFormat.format(date))
                startDate = date
                updateChartAndStatistics()
            }
        }
        binding.endDateInput.setOnClickListener {
            showDatePicker { date ->
                binding.endDateInput.setText(dateFormat.format(date))
                endDate = date
                updateChartAndStatistics()
            }
        }

        val dao = AppDatabase.getDatabase(requireContext()).glucoseDao()
        val repository = GlucoseRepository(dao)
        val factory = GlucoseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(GlucoseViewModel::class.java)

        lineChart = binding.lineChart

        adapter = GlucoseAdapter(emptyList())
        binding.rvStatisticsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStatisticsList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                updateChartAndStatistics(entries)
            }
        }

        binding.tabLayout.getTabAt(0)?.select()
    }

    private fun setDateRange(days: Int) {
        val calendar = Calendar.getInstance()
        endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        startDate = calendar.time

        binding.startDateInput.setText(dateFormat.format(startDate))
        binding.endDateInput.setText(dateFormat.format(endDate))
        updateChartAndStatistics()
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(selectedCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateChartAndStatistics(allEntries: List<GlucoseEntry>? = null) {
        val entriesList = allEntries ?: emptyList()

        val filteredEntries = entriesList
        Log.d("StatisticsFragment", "All entries from DB: $entriesList")

//        val filteredEntries = entriesList.filter { entry ->
//            try {
//                val entryDate = dateFormat.parse(entry.date)
//                entryDate != null && !entryDate.before(startDate) && !entryDate.after(endDate)
//            } catch (e: Exception) {
//                false
//            }


        val chartPoints = filteredEntries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.glucoseLevel)
        }
        val dataSet = LineDataSet(chartPoints, "Глюкоза")
        dataSet.color = resources.getColor(android.R.color.holo_blue_dark, requireContext().theme)
        dataSet.setCircleColor(resources.getColor(android.R.color.holo_blue_dark, requireContext().theme))

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()

        if (filteredEntries.isNotEmpty()) {
            val glucoseValues = filteredEntries.map { it.glucoseLevel }
            val minValue = glucoseValues.minOrNull() ?: 0f
            val maxValue = glucoseValues.maxOrNull() ?: 0f
            val avgValue = glucoseValues.average().toFloat()

            binding.tvStatistics.text = "Мин: $minValue, Макс: $maxValue, Ср: $avgValue"
        } else {
            binding.tvStatistics.text = "Нет данных"
        }

        adapter.updateList(filteredEntries)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
