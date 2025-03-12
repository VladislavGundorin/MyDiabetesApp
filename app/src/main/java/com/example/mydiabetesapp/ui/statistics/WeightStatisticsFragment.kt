package com.example.mydiabetesapp.ui.weightstatistics

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.WeightEntry
import com.example.mydiabetesapp.databinding.FragmentWeightStatisticsBinding
import com.example.mydiabetesapp.repository.WeightRepository
import com.example.mydiabetesapp.ui.journal.OnWeightEntryClickListener
import com.example.mydiabetesapp.ui.journal.WeightAdapter
import com.example.mydiabetesapp.ui.viewmodel.WeightViewModel
import com.example.mydiabetesapp.ui.viewmodel.WeightViewModelFactory
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

class WeightStatisticsFragment : Fragment(), OnWeightEntryClickListener {

    private var _binding: FragmentWeightStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WeightViewModel
    private lateinit var lineChart: LineChart
    private lateinit var adapter: WeightAdapter

    private var _allEntries: List<WeightEntry> = emptyList()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var startDate: Date = Date()
    private var endDate: Date = Date()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        val dao = AppDatabase.getDatabase(requireContext()).weightDao()
        val repository = WeightRepository(dao)
        val factory = WeightViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(WeightViewModel::class.java)

        lineChart = binding.lineChart
        lineChart.setDragEnabled(true)
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        adapter = WeightAdapter(emptyList(), this)
        binding.rvWeightList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWeightList.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> setDateRange(days = 7)
                        1 -> setDateRange(days = 30)
                        2 -> setDateRange(days = 90)
                        3 -> setDateRange(days = 180)
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
                updateChartAndList()
            }
        }

        binding.endDateInput.setOnClickListener {
            showDatePicker { date ->
                endDate = getEndOfDay(date)
                binding.endDateInput.setText(dateFormat.format(endDate))
                updateChartAndList()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.entries.collect { entries ->
                _allEntries = entries
                updateChartAndList()
            }
        }

        binding.tabLayout.getTabAt(0)?.select()
        setDateRange(days = 7)
    }

    private fun setTodayRange() {
        val now = Date()
        startDate = getStartOfDay(now)
        endDate = getEndOfDay(now)
        binding.startDateInput.setText(dateFormat.format(startDate))
        binding.endDateInput.setText(dateFormat.format(endDate))
        updateChartAndList()
    }

    private fun setDateRange(days: Int) {
        val calendar = Calendar.getInstance()
        endDate = getEndOfDay(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        startDate = getStartOfDay(calendar.time)
        binding.startDateInput.setText(dateFormat.format(startDate))
        binding.endDateInput.setText(dateFormat.format(endDate))
        updateChartAndList()
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
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

    private fun updateChartAndList() {
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

        val isToday = dateFormat.format(startDate) == dateFormat.format(endDate)

        val sortedEntries = if (isToday) {
            filteredEntries.sortedBy { parseTimeToFloat(it.time) }
        } else {
            filteredEntries.sortedBy { entry ->
                try {
                    dateFormat.parse(entry.date)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        }

        val chartPoints = if (isToday) {
            sortedEntries.map { entry ->
                Entry(parseTimeToFloat(entry.time), entry.weight)
            }
        } else {
            sortedEntries.map { entry ->
                val entryDate = try {
                    dateFormat.parse(entry.date)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
                Entry(entryDate.toFloat() / 86_400_000f, entry.weight)
            }
        }

        val dataSet = LineDataSet(chartPoints, "Вес").apply {
            lineWidth = 2f
            circleRadius = 4f
        }
        lineChart.data = LineData(dataSet)

        val xAxis = lineChart.xAxis
        if (chartPoints.isNotEmpty()) {
            val minX = chartPoints.minOf { it.x }
            val maxX = chartPoints.maxOf { it.x }
            if (minX == maxX) {
                xAxis.axisMinimum = minX - 1f
                xAxis.axisMaximum = maxX + 1f
            } else {
                val rangeX = maxX - minX
                val marginX = rangeX * 0.1f
                xAxis.axisMinimum = minX - marginX
                xAxis.axisMaximum = maxX + marginX
            }
        } else {
            if (isToday) {
                xAxis.axisMinimum = 0f
                xAxis.axisMaximum = 24f
            } else {
                xAxis.axisMinimum = 0f
                xAxis.axisMaximum = 31f
            }
        }
        xAxis.granularity = 1f
        xAxis.setGranularityEnabled(true)
        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (isToday) {
                    val hour = value.toInt()
                    val minute = ((value - hour) * 60).toInt()
                    String.format("%02d:%02d", hour, minute)
                } else {
                    val millis = (value * 86_400_000L).toLong()
                    val formatter = SimpleDateFormat("dd.MM", Locale.getDefault())
                    formatter.format(Date(millis))
                }
            }
        }

        lineChart.axisRight.isEnabled = false
        val leftAxis = lineChart.axisLeft
        if (chartPoints.isNotEmpty()) {
            val minY = chartPoints.minOf { it.y }
            val maxY = chartPoints.maxOf { it.y }
            if (minY == maxY) {
                leftAxis.axisMinimum = minY - 1f
                leftAxis.axisMaximum = maxY + 1f
            } else {
                val rangeY = maxY - minY
                val marginY = rangeY * 0.1f
                leftAxis.axisMinimum = minY - marginY
                leftAxis.axisMaximum = maxY + marginY
            }
        } else {
            leftAxis.axisMinimum = 0f
            leftAxis.axisMaximum = 10f
        }
        leftAxis.setLabelCount(6, false)

        lineChart.invalidate()

        // Обновление списка записей (без блока статистики типа "Мин, Макс, Ср")
        adapter.updateList(sortedEntries)
    }

    private fun parseTimeToFloat(timeStr: String): Float {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = format.parse(timeStr) ?: return 0f
            val cal = Calendar.getInstance()
            cal.time = date
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            hour.toFloat() + minute.toFloat() / 60f
        } catch (e: Exception) {
            0f
        }
    }

    override fun onEdit(entry: WeightEntry) {
        val action = WeightStatisticsFragmentDirections
            .actionWeightStatisticsFragmentToEditWeightFragment(entry.id)
        findNavController().navigate(action)
        Toast.makeText(requireContext(), "Редактировать: ${entry.id}", Toast.LENGTH_SHORT).show()
    }

    override fun onDelete(entry: WeightEntry) {
        viewModel.deleteEntry(entry)
        Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
