package com.example.mydiabetesapp.ui.weightstatistics

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
    private val b get() = _binding!!

    private lateinit var vm: WeightViewModel
    private lateinit var chart: LineChart
    private lateinit var adapter: WeightAdapter

    private var allEntries: List<WeightEntry> = emptyList()
    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var start: Date = Date()
    private var end  : Date = Date()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentWeightStatisticsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {

        b.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        vm = ViewModelProvider(
            this,
            WeightViewModelFactory(
                WeightRepository(AppDatabase.getDatabase(requireContext()).weightDao())
            )
        )[WeightViewModel::class.java]

        chart = b.lineChart.apply {
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }

        adapter = WeightAdapter(emptyList(), this)
        b.rvWeightList.layoutManager = LinearLayoutManager(requireContext())
        b.rvWeightList.adapter       = adapter

        b.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = when (tab.position) {
                0 -> setRange(7)
                1 -> setRange(30)
                2 -> setRange(90)
                else -> setRange(180)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        b.startDateInput.setOnClickListener { pickDate { d -> start = d.startOfDay(); refresh() } }
        b.endDateInput  .setOnClickListener { pickDate { d -> end   = d.endOfDay();   refresh() } }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.entries.collect { list ->
                allEntries = list
                setRange(7)
                b.tabLayout.getTabAt(0)?.select()
            }
        }
    }

    private fun setRange(days: Int) {
        val cal = Calendar.getInstance()
        end   = cal.time.endOfDay()
        cal.add(Calendar.DAY_OF_YEAR, -days + 1)
        start = cal.time.startOfDay()
        refresh()
    }

    private fun pickDate(cb: (Date) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> cb(Calendar.getInstance().apply { set(y, m, d) }.time) },
            now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun refresh() {

        b.startDateInput.setText(dateFmt.format(start))
        b.endDateInput  .setText(dateFmt.format(end))

        val filtered = allEntries.filter { e ->
            val d = runCatching { dateFmt.parse(e.date) }.getOrNull() ?: return@filter false
            !d.before(start) && !d.after(end)
        }

        val isSingleDay = dateFmt.format(start) == dateFmt.format(end)

        val sorted = if (isSingleDay) {
            filtered.sortedBy { parseTimeToFloat(it.time) }
        } else {
            filtered.sortedBy { runCatching { dateFmt.parse(it.date)?.time }.getOrNull() ?: 0L }
        }

        val points = if (isSingleDay) {
            sorted.map { Entry(parseTimeToFloat(it.time), it.weight) }
        } else {
            sorted.map {
                val x = (dateFmt.parse(it.date)?.time ?: 0L) / 86_400_000f
                Entry(x, it.weight)
            }
        }

        chart.data = LineData(LineDataSet(points, "Вес").apply {
            lineWidth = 2f
            circleRadius = 4f
        })
        chart.axisRight.isEnabled = false
        chart.invalidate()

        if (sorted.isNotEmpty()) {
            val ws  = sorted.map { it.weight }
            val min = ws.minOrNull() ?: 0f
            val max = ws.maxOrNull() ?: 0f
            val avg = ws.average()
            b.tvMin.text = "Мин: ${"%.1f".format(min)}"
            b.tvMax.text = "Макс: ${"%.1f".format(max)}"
            b.tvAvg.text = "Ср:  ${"%.1f".format(avg)}"
        } else {
            b.tvMin.text = "Мин: –"
            b.tvMax.text = "Макс: –"
            b.tvAvg.text = "Ср:  –"
        }


        adapter.updateList(sorted)
    }

    private fun parseTimeToFloat(timeStr: String): Float {
        return try {
            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = fmt.parse(timeStr) ?: return 0f
            val cal = Calendar.getInstance().apply { time = date }
            cal[Calendar.HOUR_OF_DAY] + cal[Calendar.MINUTE] / 60f
        } catch (_: Exception) {
            0f
        }
    }
    private fun Date.startOfDay() = Calendar.getInstance().apply {
        time = this@startOfDay
        set(11, 0); set(12, 0); set(13, 0); set(14, 0)
    }.time

    private fun Date.endOfDay() = Calendar.getInstance().apply {
        time = this@endOfDay
        set(11, 23); set(12, 59); set(13, 59); set(14, 999)
    }.time

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
        super.onDestroyView(); _binding = null
    }
}
