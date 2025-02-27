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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: GlucoseViewModel
    private lateinit var lineChart: LineChart
    private lateinit var adapter: GlucoseAdapter

    private var _allEntries: List<GlucoseEntry> = emptyList()

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


        val dao = AppDatabase.getDatabase(requireContext()).glucoseDao()
        val repository = GlucoseRepository(dao)
        val factory = GlucoseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[GlucoseViewModel::class.java]

        lineChart = binding.lineChart
        adapter = GlucoseAdapter(emptyList())
        binding.rvStatisticsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStatisticsList.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> setDateRange(days = 7)
                        1 -> setDateRange(days = 30)
                        2 -> setDateRange(days = 90)
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.startDateInput.setOnClickListener {
            showDatePicker { date ->
                startDate = getStartOfDay(date)
                binding.startDateInput.setText(dateFormat.format(startDate))
                updateChartAndStatistics()
            }
        }

        binding.endDateInput.setOnClickListener {
            showDatePicker { date ->
                endDate = getEndOfDay(date)
                binding.endDateInput.setText(dateFormat.format(endDate))
                updateChartAndStatistics()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                _allEntries = entries
                Log.d("StatisticsFragment", "Собрали из БД: $_allEntries")
                updateChartAndStatistics()
            }
        }

        setTodayRange()
    }

    private fun setTodayRange() {
        val now = Date()
        startDate = getStartOfDay(now)
        endDate = getEndOfDay(now)

        binding.startDateInput.setText(dateFormat.format(startDate))
        binding.endDateInput.setText(dateFormat.format(endDate))
        updateChartAndStatistics()
    }

    private fun setDateRange(days: Int) {
        val calendar = Calendar.getInstance()
        endDate = getEndOfDay(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, -days)
        startDate = getStartOfDay(calendar.time)

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

    private fun getStartOfDay(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun getEndOfDay(date: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.time
    }

    private fun updateChartAndStatistics() {

        val filteredEntries = _allEntries.filter { entry ->
            try {
                val entryDate = dateFormat.parse(entry.date)
                entryDate != null &&
                        !entryDate.before(startDate) &&
                        !entryDate.after(endDate)
            } catch (e: Exception) {
                false
            }
        }

        Log.d("StatisticsFragment", "Отфильтрованные записи: $filteredEntries")

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
