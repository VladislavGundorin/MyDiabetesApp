package com.example.mydiabetesapp.ui.notification

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.NotificationEntry
import com.example.mydiabetesapp.databinding.FragmentAddNotificationBinding
import com.example.mydiabetesapp.repository.NotificationRepository
import com.example.mydiabetesapp.ui.viewmodel.NotificationViewModel
import com.example.mydiabetesapp.ui.viewmodel.NotificationViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NotificationFragment : Fragment() {

    private var _binding: FragmentAddNotificationBinding? = null
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

    private lateinit var viewModel: NotificationViewModel
    private var isEditMode = false
    private var currentNotification: NotificationEntry? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        setupReminderRecycler()
        setupPickers()

        val dao = AppDatabase.getDatabase(requireContext()).notificationDao()
        val repo = NotificationRepository(dao)
        viewModel = ViewModelProvider(this, NotificationViewModelFactory(repo))
            .get(NotificationViewModel::class.java)

        val editId = arguments?.getInt("notification_id", -1) ?: -1
        if (editId != -1) {
            isEditMode = true
            lifecycleScope.launch {
                dao.getNotificationById(editId)?.let { entry ->
                    currentNotification = entry
                    binding.etNotificationMessage.setText(entry.message.substringBefore(" ("))
                    selectedDate = dateFormat.parse(entry.date)
                    selectedTime = timeFormat.parse(entry.time)
                    binding.tvSelectedDate.text = entry.date
                    binding.tvSelectedTime.text = entry.time
                    binding.etInterval.setText(entry.intervalMinutes.toString())
                    binding.cbRepeatDaily.isChecked = entry.repeatDaily
                    binding.switchAutoCancel.isChecked = entry.autoCancel
                    selectedReminderIndex =
                        reminderOptions.indexOf(entry.reminderType).coerceAtLeast(0)
                    binding.btnSetNotification.text = "Обновить"
                    binding.btnDeleteNotification.visibility = View.VISIBLE
                }
            }
        }

        binding.btnSetNotification.setOnClickListener {
            val text = binding.etNotificationMessage.text.toString().trim()
            if (selectedDate == null || selectedTime == null || text.isEmpty()) {
                Toast.makeText(requireContext(),
                    "Выберите дату, время и введите текст уведомления",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullMsg = "$text (${reminderOptions[selectedReminderIndex]})"
            val intervalMin = binding.etInterval.text.toString().toLongOrNull() ?: 0L
            val repeatDaily = binding.cbRepeatDaily.isChecked
            val autoCancel = binding.switchAutoCancel.isChecked
            val cal = Calendar.getInstance().apply {
                time = selectedDate!!
                val t = Calendar.getInstance().apply { time = selectedTime!! }
                set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, t.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val firstTrigger = cal.timeInMillis

            if (isEditMode && currentNotification != null) {
                val updated = currentNotification!!.copy(
                    message = fullMsg,
                    date = dateFormat.format(cal.time),
                    time = timeFormat.format(cal.time),
                    repeatDaily = repeatDaily,
                    intervalMinutes = intervalMin,
                    autoCancel = autoCancel,
                    reminderType = reminderOptions[selectedReminderIndex]
                )
                lifecycleScope.launch {
                    viewModel.updateNotification(updated)
                    scheduleNotification(
                        firstTrigger, fullMsg, true, autoCancel,
                        repeatDaily, intervalMin, updated.id
                    )
                    Toast.makeText(requireContext(), "Уведомление обновлено",
                        Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } else {
                val entry = NotificationEntry(
                    message = fullMsg,
                    date = dateFormat.format(cal.time),
                    time = timeFormat.format(cal.time),
                    repeatDaily = repeatDaily,
                    intervalMinutes = intervalMin,
                    autoCancel = autoCancel,
                    enabled = true,
                    reminderType = reminderOptions[selectedReminderIndex]
                )
                lifecycleScope.launch {
                    val newId = viewModel.addNotificationAndReturnId(entry).toInt()
                    scheduleNotification(
                        firstTrigger, fullMsg, true, autoCancel,
                        repeatDaily, intervalMin, newId
                    )
                    Toast.makeText(requireContext(), "Уведомление установлено",
                        Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }

        binding.btnDeleteNotification.setOnClickListener {
            currentNotification?.let { entry ->
                lifecycleScope.launch {
                    viewModel.deleteNotification(entry)
                    cancelAlarm(entry)
                    Toast.makeText(requireContext(), "Уведомление удалено",
                        Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupReminderRecycler() {
        val adapter = RemindersAdapter(reminderOptions).apply {
            selectedPosition = selectedReminderIndex
        }
        binding.rvReminderOptions.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvReminderOptions.adapter = adapter
        val snap = androidx.recyclerview.widget.LinearSnapHelper()
        snap.attachToRecyclerView(binding.rvReminderOptions)
        binding.rvReminderOptions.addOnScrollListener(
            object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(rv: androidx.recyclerview.widget.RecyclerView,
                                                  newState: Int) {
                    super.onScrollStateChanged(rv, newState)
                    if (newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE) {
                        snap.findSnapView(rv.layoutManager)?.let { v ->
                            val pos = rv.layoutManager!!.getPosition(v)
                            selectedReminderIndex = pos
                            adapter.selectedPosition = pos
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            })
    }

    private fun setupPickers() {
        binding.btnSelectDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    selectedDate = Calendar.getInstance().apply { set(y, m, d) }.time
                    binding.tvSelectedDate.text = dateFormat.format(selectedDate!!)
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.btnSelectTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, h, min ->
                    selectedTime = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, min)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time
                    binding.tvSelectedTime.text = timeFormat.format(selectedTime!!)
                },
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun scheduleNotification(
        timeInMillis: Long,
        message: String,
        withSound: Boolean,
        autoCancel: Boolean,
        repeatDaily: Boolean,
        intervalMinutes: Long,
        requestCode: Int
    ) {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            Toast.makeText(requireContext(),
                "Точные будильники не разрешены. Разрешите в настройках.",
                Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            return
        }

        val intent = Intent(requireContext(), MyNotificationReceiver::class.java).apply {
            putExtra("notification_message", message)
            putExtra("withSound", withSound)
            putExtra("autoCancel", autoCancel)
            putExtra("repeatDaily", repeatDaily)
            putExtra("intervalMinutes", intervalMinutes)
            putExtra("requestCode", requestCode)
        }
        val pi = PendingIntent.getBroadcast(
            requireContext(), requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pi)
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(),
                "Не удалось установить точное напоминание: ${e.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun cancelAlarm(entry: NotificationEntry) {
        val am = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), MyNotificationReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            requireContext(), entry.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
