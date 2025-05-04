package com.example.mydiabetesapp.ui.hba1cstatistics

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.Hba1cEntry
import com.example.mydiabetesapp.databinding.FragmentHba1cStatisticsBinding
import com.example.mydiabetesapp.repository.Hba1cRepository
import com.example.mydiabetesapp.ui.viewmodel.Hba1cViewModel
import com.example.mydiabetesapp.ui.viewmodel.Hba1cViewModelFactory
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.tabs.TabLayout
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
    private var allEntries: List<Hba1cEntry> = emptyList()
    private var fromDate: Date = Date()
    private var toDate: Date = Date()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHba1cStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

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

        chart = binding.lineChart.apply {
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
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
            pickDate { date ->
                fromDate = date.startOfDay()
                refresh()
            }
        }
        binding.endDateInput.setOnClickListener {
            pickDate { date ->
                toDate = date.endOfDay()
                refresh()
            }
        }

        lifecycleScope.launch {
            viewModel.entries.collect { list ->
                allEntries = list
                presetRange(90)
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

    private fun pickDate(onDate: (Date) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                onDate(Calendar.getInstance().apply { set(y, m, d) }.time)
            },
            now[Calendar.YEAR],
            now[Calendar.MONTH],
            now[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun refresh() {
        binding.startDateInput.setText(dateFormat.format(fromDate))
        binding.endDateInput.setText(dateFormat.format(toDate))

        val filtered = allEntries.filter { entry ->
            val d = dateFormat.parse(entry.date) ?: return@filter false
            !d.before(fromDate) && !d.after(toDate)
        }.sortedBy { dateFormat.parse(it.date) }

        val points = filtered.mapIndexed { i, e ->
            Entry(i.toFloat(), e.hba1c)
        }
        chart.data = LineData(LineDataSet(points, "HbA1c, %").apply {
            lineWidth = 2f
            circleRadius = 4f
        })
        chart.invalidate()

        if (filtered.isNotEmpty()) {
            val vals = filtered.map { it.hba1c }
            binding.tvMin.text = "Мин: %.2f".format(vals.minOrNull())
            binding.tvMax.text = "Макс: %.2f".format(vals.maxOrNull())
            binding.tvAvg.text = "Ср:  %.2f".format(vals.average())
        } else {
            binding.tvMin.text = "Нет данных"
            binding.tvMax.text = "Нет данных"
            binding.tvAvg.text = "Нет данных"
        }

        adapter.update(filtered)
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

    override fun onEdit(entry: Hba1cEntry) {
        val action = Hba1cStatisticsFragmentDirections
            .actionHba1cStatisticsFragmentToEditHba1cFragment(entry.id)
        findNavController().navigate(action)
    }

    override fun onDelete(entry: Hba1cEntry) {
        viewModel.delete(entry)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
