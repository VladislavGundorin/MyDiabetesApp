package com.example.mydiabetesapp.feature.weight.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.databinding.FragmentWeightStatisticsBinding
import com.example.mydiabetesapp.feature.weight.data.WeightEntry
import com.example.mydiabetesapp.feature.weight.data.WeightRepository
import com.example.mydiabetesapp.feature.weight.viewmodel.WeightViewModel
import com.example.mydiabetesapp.feature.weight.viewmodel.WeightViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WeightStatisticsFragment : Fragment(), OnWeightEntryClickListener {

    private var _binding: FragmentWeightStatisticsBinding? = null
    private val b get() = _binding!!

    private lateinit var vm: WeightViewModel
    private lateinit var chart: LineChart
    private lateinit var adapter: WeightAdapter

    private var allEntries: List<WeightEntry> = emptyList()
    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val MS_PER_DAY = 86_400_000f
    private var start: Date = Date()
    private var end: Date = Date()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightStatisticsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val dao = AppDatabase.getDatabase(requireContext()).weightDao()
        vm = androidx.lifecycle.ViewModelProvider(
            this,
            WeightViewModelFactory(WeightRepository(dao))
        )[WeightViewModel::class.java]

        chart = b.lineChart.apply {
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                labelCount = 6
                labelRotationAngle = -45f
                setAvoidFirstLastClipping(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val ms = (value * MS_PER_DAY).toLong()
                        return dateFmt.format(Date(ms))
                    }
                }
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
        }

        adapter = WeightAdapter(emptyList(), this)
        b.rvWeightList.layoutManager = LinearLayoutManager(requireContext())
        b.rvWeightList.adapter = adapter

        b.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> presetRange(7)
                    1 -> presetRange(30)
                    2 -> presetRange(90)
                    3 -> presetRange(180)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                onTabSelected(tab)
            }
        })
        b.tabLayout.getTabAt(0)?.select()

        b.startDateInput.setOnClickListener { pickDate { d -> start = d.startOfDay(); refresh() } }
        b.endDateInput.setOnClickListener   { pickDate { d -> end   = d.endOfDay();   refresh() } }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                vm.entries.collectLatest { list ->
                    allEntries = list
                    refresh()
                }
            }
        }
    }

    private fun presetRange(days: Int) {
        val cal = Calendar.getInstance()
        end = cal.time.endOfDay()
        cal.add(Calendar.DAY_OF_YEAR, -days + 1)
        start = cal.time.startOfDay()
        refresh()
    }

    private fun pickDate(cb: (Date) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                Calendar.getInstance()
                    .apply { set(y, m, d) }
                    .time
                    .let(cb)
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun refresh() {
        b.startDateInput.setText(dateFmt.format(start))
        b.endDateInput.setText(dateFmt.format(end))

        val filtered = allEntries
            .mapNotNull { dateFmt.parse(it.date)?.let { d -> d to it } }
            .filter { (d, _) -> !d.before(start) && !d.after(end) }
            .sortedBy { it.first }
            .map { it.second }

        val points = filtered.map {
            Entry(it.date.toEpochMillis(dateFmt) / MS_PER_DAY, it.weight)
        }

        val ds = LineDataSet(points, "Вес").apply {
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)

            setDrawFilled(true)
        }

        chart.data = LineData(ds)
        chart.xAxis.apply {
            axisMinimum = start.time / MS_PER_DAY
            axisMaximum = end.time / MS_PER_DAY
        }
        chart.axisLeft.apply {
            if (filtered.isNotEmpty()) {
                val ws = filtered.map { it.weight }
                axisMinimum = (ws.minOrNull()!! - 1f).coerceAtLeast(0f)
                axisMaximum = ws.maxOrNull()!! + 1f
            } else {
                resetAxisMinimum()
                resetAxisMaximum()
            }
        }
        chart.invalidate()

        if (filtered.isNotEmpty()) {
            val ws = filtered.map { it.weight }
            b.tvMin.text = "Мин: ${"%.1f".format(ws.minOrNull())}"
            b.tvMax.text = "Макс: ${"%.1f".format(ws.maxOrNull())}"
            b.tvAvg.text = "Ср:  ${"%.1f".format(ws.average())}"
        } else {
            b.tvMin.text = "Мин: –"
            b.tvMax.text = "Макс: –"
            b.tvAvg.text = "Ср:  –"
        }

        adapter.updateList(filtered)
    }

    override fun onEdit(entry: WeightEntry) {
        findNavController().navigate(
            WeightStatisticsFragmentDirections
                .actionWeightStatisticsFragmentToEditWeightFragment(entry.id)
        )
    }

    override fun onDelete(entry: WeightEntry) {
        vm.deleteEntry(entry)
        Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun Date.startOfDay(): Date = Calendar.getInstance().apply {
        time = this@startOfDay
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.time

    private fun Date.endOfDay(): Date = Calendar.getInstance().apply {
        time = this@endOfDay
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.time

    private fun String.toEpochMillis(fmt: SimpleDateFormat): Float =
        fmt.parse(this)?.time?.toFloat() ?: 0f
}
