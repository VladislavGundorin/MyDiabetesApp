package com.example.mydiabetesapp.feature.pressure.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.databinding.FragmentPulseStatisticsBinding
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureEntry
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureRepository
import com.example.mydiabetesapp.feature.pressure.viewmodel.BloodPressureViewModel
import com.example.mydiabetesapp.feature.pressure.viewmodel.BloodPressureViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry as ChartEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PulseStatisticsFragment : Fragment(), OnPressureClickListener {

    private var _binding: FragmentPulseStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BloodPressureViewModel
    private lateinit var chart: LineChart
    private lateinit var adapter: PressureAdapter

    private val dateFmt     = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val MS_PER_DAY  = 86_400_000f

    private var dateFrom   : Date = Date()
    private var dateTo     : Date = Date()
    private var allEntries : List<BloodPressureEntry> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPulseStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val dao = AppDatabase.getDatabase(requireContext()).bloodPressureDao()
        viewModel = ViewModelProvider(
            this,
            BloodPressureViewModelFactory(BloodPressureRepository(dao))
        )[BloodPressureViewModel::class.java]

        chart = binding.lineChart.apply {
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position            = XAxis.XAxisPosition.BOTTOM
                granularity         = 1f
                setDrawGridLines(false)
                textColor           = ContextCompat.getColor(requireContext(), R.color.chartAxisColor)
                labelRotationAngle  = 45f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val ms = (value * MS_PER_DAY).toLong()
                        return dateFmt.format(Date(ms))
                    }
                }
            }

            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 50f
                axisMaximum = 160f
                granularity = 10f
                textColor   = ContextCompat.getColor(requireContext(), R.color.chartAxisColor)
                gridColor   = ContextCompat.getColor(requireContext(), R.color.chartGridColor)

                listOf(
                    60f  to R.color.secondaryColor,
                    90f  to R.color.accentColor,
                    140f to R.color.errorColor
                ).forEach { (value, clr) ->
                    addLimitLine(
                        LimitLine(value, "").apply {
                            lineWidth = 1.5f
                            enableDashedLine(10f, 10f, 0f)
                            lineColor = ContextCompat.getColor(requireContext(), clr)
                        }
                    )
                }
            }

            legend.apply {
                isEnabled  = true
                form       = Legend.LegendForm.LINE
                textColor  = ContextCompat.getColor(requireContext(), R.color.chartAxisColor)
            }
        }

        adapter = PressureAdapter(emptyList(), this)
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter        = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> presetRange(7)
                    1 -> presetRange(30)
                    2 -> presetRange(90)
                    3 -> presetRange(180)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.startDateInput.setOnClickListener {
            pickDate { d -> dateFrom = d.startOfDay(); refresh() }
        }
        binding.endDateInput.setOnClickListener {
            pickDate { d -> dateTo   = d.endOfDay();   refresh() }
        }

        binding.tabLayout.getTabAt(0)?.select()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.entries.collectLatest { list ->
                    allEntries = list
                    presetRange(7)
                }
            }
        }
    }

    private fun presetRange(days: Int) {
        val cal = Calendar.getInstance()
        dateTo   = cal.time.endOfDay()
        cal.add(Calendar.DAY_OF_YEAR, -days + 1)
        dateFrom = cal.time.startOfDay()
        refresh()
    }

    private fun pickDate(onDate: (Date) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> Calendar.getInstance().apply { set(y, m, d) }.time.let(onDate) },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun refresh() {
        binding.startDateInput.setText(dateFmt.format(dateFrom))
        binding.endDateInput  .setText(dateFmt.format(dateTo))

        val filtered = allEntries
            .mapNotNull { e ->
                dateFmt.parse(e.date)?.let { d -> d to e }
            }
            .filter { (d, _) -> !d.before(dateFrom) && !d.after(dateTo) }
            .sortedBy { it.first }
            .map { it.second }

        val sysEntries = filtered.map {
            val days = dateFmt.parse(it.date)!!.time / MS_PER_DAY
            ChartEntry(days, it.systolic.toFloat())
        }
        val diaEntries = filtered.map {
            val days = dateFmt.parse(it.date)!!.time / MS_PER_DAY
            ChartEntry(days, it.diastolic.toFloat())
        }

        val sysDs = LineDataSet(sysEntries, "Систолическое").apply {
            color = ContextCompat.getColor(requireContext(), R.color.errorColor)
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }
        val diaDs = LineDataSet(diaEntries, "Диастолическое").apply {
            color = ContextCompat.getColor(requireContext(), R.color.secondaryColor)
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        chart.data = LineData(sysDs, diaDs)
        chart.xAxis.axisMinimum = dateFrom.time / MS_PER_DAY
        chart.xAxis.axisMaximum = dateTo.time   / MS_PER_DAY
        chart.invalidate()

        if (filtered.isNotEmpty()) {
            val sysVals = filtered.map { it.systolic }
            val diaVals = filtered.map { it.diastolic }
            binding.tvMin.text = "Сист мин: ${sysVals.minOrNull()}  Диаст мин: ${diaVals.minOrNull()}"
            binding.tvMax.text = "Сист макс: ${sysVals.maxOrNull()}  Диаст макс: ${diaVals.maxOrNull()}"
            binding.tvAvg.text = "Сист ср: %.1f  Диаст ср: %.1f".format(
                sysVals.average(), diaVals.average()
            )
        } else {
            binding.tvMin.text = "Нет данных"
            binding.tvMax.text = "Нет данных"
            binding.tvAvg.text = "Нет данных"
        }

        adapter.submit(filtered)
    }

    override fun onEdit(e: BloodPressureEntry) {
        findNavController().navigate(
            PulseStatisticsFragmentDirections
                .actionPulseStatisticsFragmentToEditPressureFragment(e.id)
        )
    }
    override fun onDelete(e: BloodPressureEntry) {
        viewModel.delete(e)
    }

    private fun Date.startOfDay(): Date = Calendar.getInstance().apply {
        time = this@startOfDay
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE,      0)
        set(Calendar.SECOND,      0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun Date.endOfDay(): Date = Calendar.getInstance().apply {
        time = this@endOfDay
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE,      59)
        set(Calendar.SECOND,      59)
        set(Calendar.MILLISECOND, 999)
    }.time

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
