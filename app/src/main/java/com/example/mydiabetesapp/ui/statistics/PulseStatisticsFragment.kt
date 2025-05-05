package com.example.mydiabetesapp.ui.statistics

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.data.database.*
import com.example.mydiabetesapp.databinding.FragmentPulseStatisticsBinding
import com.example.mydiabetesapp.repository.BloodPressureRepository
import com.example.mydiabetesapp.ui.journal.*
import com.example.mydiabetesapp.ui.viewmodel.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PulseStatisticsFragment : Fragment(), OnPressureClickListener {

    private var _binding: FragmentPulseStatisticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BloodPressureViewModel
    private lateinit var chart: LineChart
    private lateinit var adapter: PressureAdapter

    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var dateFrom: Date = Date()
    private var dateTo  : Date = Date()

    private var allEntries: List<BloodPressureEntry> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentPulseStatisticsBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val dao = AppDatabase.getDatabase(requireContext()).bloodPressureDao()
        val factory = BloodPressureViewModelFactory(BloodPressureRepository(dao))
        viewModel = ViewModelProvider(this, factory)[BloodPressureViewModel::class.java]

        chart = binding.lineChart.apply {
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
        }

        adapter = PressureAdapter(emptyList(), this)
        binding.rvList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvList.adapter = adapter

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = when (tab.position) {
                0 -> setPresetRange(7)
                1 -> setPresetRange(30)
                2 -> setPresetRange(90)
                else -> setPresetRange(180)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.startDateInput.setOnClickListener { pickDate { d -> dateFrom = d.startOfDay(); refresh() } }
        binding.endDateInput  .setOnClickListener { pickDate { d -> dateTo   = d.endOfDay();   refresh() } }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entries.collect { list ->
                    allEntries = list
                    setPresetRange(7)
                    binding.tabLayout.getTabAt(0)?.select()
                }
            }
        }
    }

    private fun setPresetRange(days: Int) {
        val cal = Calendar.getInstance()
        dateTo = cal.time.endOfDay()
        cal.add(Calendar.DAY_OF_YEAR, -days + 1)
        dateFrom = cal.time.startOfDay()
        refresh()
    }

    private fun pickDate(callback: (Date) -> Unit) {
        val now = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, y, m, d -> callback(Calendar.getInstance().apply { set(y, m, d) }.time) },
            now[Calendar.YEAR], now[Calendar.MONTH], now[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun refresh() {
        val b = _binding ?: return

        b.startDateInput.setText(dateFmt.format(dateFrom))
        b.endDateInput  .setText(dateFmt.format(dateTo))

        val inRange = allEntries.filter { e ->
            val d = dateFmt.parse(e.date) ?: return@filter false
            !d.before(dateFrom) && !d.after(dateTo)
        }.sortedBy { dateFmt.parse(it.date) }

        val points = inRange.mapIndexed { idx, e -> Entry(idx.toFloat(), e.pulse.toFloat()) }

        chart.data = LineData(LineDataSet(points, "Пульс, уд/мин").apply {
            lineWidth = 2f
            circleRadius = 4f
        })
        chart.invalidate()

        if (inRange.isNotEmpty()) {
            val pulses = inRange.map { it.pulse }
            b.tvMin.text = "Мин: ${pulses.minOrNull()}"
            b.tvMax.text = "Макс: ${pulses.maxOrNull()}"
            b.tvAvg.text = "Ср:  %.1f".format(pulses.average())
        } else {
            b.tvMin.text = "Нет данных"
            b.tvMax.text = "Нет данных"
            b.tvAvg.text = "Нет данных"
        }

        adapter.submit(inRange)
    }

    override fun onEdit(e: BloodPressureEntry) {
        val action = PulseStatisticsFragmentDirections
            .actionPulseStatisticsFragmentToEditPressureFragment(e.id)
        findNavController().navigate(action)
    }

    override fun onDelete(e: BloodPressureEntry) {
        viewModel.delete(e)
        Toast.makeText(requireContext(), "Запись удалена", Toast.LENGTH_SHORT).show()
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
