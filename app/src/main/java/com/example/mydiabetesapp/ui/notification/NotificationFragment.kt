package com.example.mydiabetesapp.ui.notification

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.Fragment
import com.example.mydiabetesapp.databinding.FragmentNotificationBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var selectedDate: Date? = null
    private var selectedTime: Date? = null

    private val reminderOptions = listOf(
        "внесении данных",
        "измерении глюкозы",
        "длинном инсулине",
        "отчете врачу",
        "другом"
    )

    private var selectedReminderIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        setupReminderRecycler()

        binding.btnSelectDate.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date
                binding.tvSelectedDate.text = dateFormat.format(date)
            }
        }

        binding.btnSelectTime.setOnClickListener {
            showTimePicker { time ->
                selectedTime = time
                binding.tvSelectedTime.text = timeFormat.format(time)
            }
        }

        binding.btnSetNotification.setOnClickListener {
            val message = binding.etNotificationMessage.text.toString().trim()
            if (selectedDate == null || selectedTime == null || message.isEmpty()) {
                Toast.makeText(requireContext(), "Выберите дату, время и введите текст уведомления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedReminderText = reminderOptions[selectedReminderIndex]

            val fullMessage = "$message ($selectedReminderText)"

            val intervalMinutes = binding.etInterval.text.toString().toLongOrNull() ?: 0L
            val repeatDaily = binding.cbRepeatDaily.isChecked
            val autoCancel = binding.switchAutoCancel.isChecked

            val calendar = Calendar.getInstance().apply {
                time = selectedDate!!
                val timeCal = Calendar.getInstance().apply { time = selectedTime!! }
                set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            scheduleNotification(
                timeInMillis = calendar.timeInMillis,
                message = fullMessage,
                withSound = true,
                autoCancel = autoCancel,
                repeatDaily = repeatDaily,
                intervalMinutes = intervalMinutes
            )
        }
    }

    private fun setupReminderRecycler() {
        val adapter = RemindersAdapter(reminderOptions)
        adapter.selectedPosition = selectedReminderIndex

        binding.rvReminderOptions.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        binding.rvReminderOptions.adapter = adapter

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvReminderOptions)

        binding.rvReminderOptions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(recyclerView.layoutManager) ?: return
                    val position = recyclerView.layoutManager?.getPosition(centerView) ?: return

                    selectedReminderIndex = position
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                }
            }
        })
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

    private fun showTimePicker(onTimeSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onTimeSelected(selectedCalendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun scheduleNotification(
        timeInMillis: Long,
        message: String,
        withSound: Boolean,
        autoCancel: Boolean,
        repeatDaily: Boolean,
        intervalMinutes: Long
    ) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(requireContext(), "Точные будильники не разрешены. Включите их в настройках.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            return
        }

        val intent = Intent(requireContext(), MyNotificationReceiver::class.java).apply {
            putExtra("notification_message", message)
            putExtra("withSound", withSound)
            putExtra("autoCancel", autoCancel)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            timeInMillis.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            when {
                repeatDaily -> {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                }
                intervalMinutes > 0 -> {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        intervalMinutes * 60 * 1000,
                        pendingIntent
                    )
                }
                else -> {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                }
            }
            Toast.makeText(requireContext(), "Уведомление установлено", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "Ошибка: точные будильники не разрешены", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
