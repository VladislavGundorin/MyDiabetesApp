package com.example.mydiabetesapp.feature.hba1c.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.core.AppDatabase
import com.example.mydiabetesapp.databinding.FragmentHba1cStatisticsBinding
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cEntry
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cRepository
import com.example.mydiabetesapp.feature.hba1c.viewmodel.Hba1cViewModel
import com.example.mydiabetesapp.feature.hba1c.viewmodel.Hba1cViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry as ChartEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Hba1cStatisticsFragment : Fragment(), OnHba1cEntryClickListener {

    private var _binding: FragmentHba1cStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: Hba1cViewModel
    private lateinit var chart: LineChart
    private lateinit var adapter: Hba1cAdapter

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val MS_PER_DAY = 86_400_000f

    private var allEntries: List<Hba1cEntry> = emptyList()
    private var fromDate: Date = Date()
    private var toDate: Date   = Date()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHba1cStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        viewModel = ViewModelProvider(
            this,
            Hba1cViewModelFactory(
                Hba1cRepository(AppDatabase.getDatabase(requireContext()).hba1cDao())
            )
        )[Hba1cViewModel::class.java]

        chart = binding.lineChart.apply {
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
                        return dateFormat.format(Date(ms))
                    }
                }
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
        }

        adapter = Hba1cAdapter(emptyList(), this)
        binding.rvStatisticsList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStatisticsList.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> presetRange(1)
                    1 -> presetRange(7)
                    2 -> presetRange(30)
                    3 -> presetRange(90)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.startDateInput.setOnClickListener {
            pickDate { d -> fromDate = d.startOfDay(); refresh() }
        }
        binding.endDateInput.setOnClickListener {
            pickDate { d -> toDate = d.endOfDay(); refresh() }
        }

        binding.tabLayout.getTabAt(1)?.select()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.entries.collectLatest { list ->
                    allEntries = list
                    refresh()
                }
            }
        }
    }

    private fun presetRange(days: Int) {
        val cal = Calendar.getInstance()
        toDate = cal.time.endOfDay()
        cal.add(Calendar.DAY_OF_YEAR, -days + 1)
        fromDate = cal.time.startOfDay()
        refresh()
    }

    private fun pickDate(cb: (Date) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> Calendar.getInstance().apply { set(y, m, d) }.time.let(cb) },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun refresh() {
        binding.startDateInput.setText(dateFormat.format(fromDate))
        binding.endDateInput.setText(dateFormat.format(toDate))

        val filtered = allEntries
            .mapNotNull { e -> dateFormat.parse(e.date)?.let { it to e } }
            .filter { (d, _) -> !d.before(fromDate) && !d.after(toDate) }
            .sortedBy { it.first }
            .map { it.second }

        val entries = filtered.map { e ->
            val days = dateFormat.parse(e.date)!!.time / MS_PER_DAY
            ChartEntry(days, e.hba1c)
        }
        val ds = LineDataSet(entries, "HbA₁c, %").apply {
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            setDrawFilled(true)
        }

        chart.data = LineData(ds)
        chart.xAxis.axisMinimum = fromDate.time / MS_PER_DAY
        chart.xAxis.axisMaximum = toDate.time / MS_PER_DAY

        chart.axisLeft.apply {
            val ys = filtered.map { it.hba1c }
            if (ys.isNotEmpty()) {
                axisMinimum = (ys.minOrNull()!! - 1f).coerceAtLeast(0f)
                axisMaximum = ys.maxOrNull()!! + 1f
            } else {
                resetAxisMinimum()
                resetAxisMaximum()
            }
        }

        chart.invalidate()

        if (filtered.isNotEmpty()) {
            val vals = filtered.map { it.hba1c }
            binding.tvMin.text = "Мин:  %.2f".format(vals.minOrNull())
            binding.tvMax.text = "Макс: %.2f".format(vals.maxOrNull())
            binding.tvAvg.text = "Ср:   %.2f".format(vals.average())
        } else {
            binding.tvMin.text = "Мин:  –"
            binding.tvMax.text = "Макс: –"
            binding.tvAvg.text = "Ср:   –"
        }

        adapter.update(filtered)
    }

    override fun onEdit(entry: Hba1cEntry) {
        findNavController().navigate(
            Hba1cStatisticsFragmentDirections
                .actionHba1cStatisticsFragmentToEditHba1cFragment(entry.id)
        )
    }

    override fun onDelete(entry: Hba1cEntry) {
        viewModel.delete(entry)
    }

    private fun Date.startOfDay(): Date = Calendar.getInstance().apply {
        time = this@startOfDay
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun Date.endOfDay(): Date = Calendar.getInstance().apply {
        time = this@endOfDay
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
