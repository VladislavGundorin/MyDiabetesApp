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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        setupReminderRecycler()
        setupPickers()

        val dao = AppDatabase.getDatabase(requireContext()).notificationDao()
        val repository = NotificationRepository(dao)
        val factory = NotificationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)

        val notificationId = arguments?.getInt("notification_id", -1) ?: -1
        if (notificationId != -1) {
            isEditMode = true
            lifecycleScope.launch {
                val loaded = dao.getNotificationById(notificationId)
                loaded?.let { entry ->
                    currentNotification = entry
                    val mainMessage = entry.message.substringBefore(" (")
                    binding.etNotificationMessage.setText(mainMessage)
                    selectedDate = dateFormat.parse(entry.date)
                    selectedTime = timeFormat.parse(entry.time)
                    binding.tvSelectedDate.text = entry.date
                    binding.tvSelectedTime.text = entry.time
                    binding.etInterval.setText(entry.intervalMinutes.toString())
                    binding.cbRepeatDaily.isChecked = entry.repeatDaily
                    binding.switchAutoCancel.isChecked = entry.autoCancel
                    val index = reminderOptions.indexOf(entry.reminderType)
                    if (index >= 0) {
                        selectedReminderIndex = index
                    }
                    binding.btnSetNotification.text = "Сохранить изменения"
                    binding.btnDeleteNotification.visibility = View.VISIBLE
                }
            }
        }

        binding.btnSetNotification.setOnClickListener {
            val messageText = binding.etNotificationMessage.text.toString().trim()
            if (selectedDate == null || selectedTime == null || messageText.isEmpty()) {
                Toast.makeText(requireContext(), "Выберите дату, время и введите текст уведомления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedReminderText = reminderOptions[selectedReminderIndex]
            val fullMessage = "$messageText ($selectedReminderText)"
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
            val scheduledTimeInMillis = calendar.timeInMillis

            if (isEditMode && currentNotification != null) {
                val updatedNotification = currentNotification!!.copy(
                    message = fullMessage,
                    date = dateFormat.format(calendar.time),
                    time = timeFormat.format(calendar.time),
                    repeatDaily = repeatDaily,
                    intervalMinutes = intervalMinutes,
                    autoCancel = autoCancel,
                    reminderType = selectedReminderText
                )
                lifecycleScope.launch {
                    viewModel.updateNotification(updatedNotification)
                    scheduleNotification(
                        timeInMillis = scheduledTimeInMillis,
                        message = fullMessage,
                        withSound = true,
                        autoCancel = autoCancel,
                        repeatDaily = repeatDaily,
                        intervalMinutes = intervalMinutes,
                        requestCode = updatedNotification.id
                    )
                    Toast.makeText(requireContext(), "Уведомление обновлено", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } else {
                val notificationEntry = NotificationEntry(
                    message = fullMessage,
                    date = dateFormat.format(calendar.time),
                    time = timeFormat.format(calendar.time),
                    repeatDaily = repeatDaily,
                    intervalMinutes = intervalMinutes,
                    autoCancel = autoCancel,
                    enabled = true,
                    reminderType = selectedReminderText
                )
                lifecycleScope.launch {
                    val insertedId = viewModel.addNotificationAndReturnId(notificationEntry).toInt()
                    scheduleNotification(
                        timeInMillis = scheduledTimeInMillis,
                        message = fullMessage,
                        withSound = true,
                        autoCancel = autoCancel,
                        repeatDaily = repeatDaily,
                        intervalMinutes = intervalMinutes,
                        requestCode = insertedId
                    )
                    Toast.makeText(requireContext(), "Уведомление установлено", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }

        binding.btnDeleteNotification.setOnClickListener {
            if (isEditMode && currentNotification != null) {
                lifecycleScope.launch {
                    viewModel.deleteNotification(currentNotification!!)
                    cancelAlarm(currentNotification!!)
                    Toast.makeText(requireContext(), "Уведомление удалено", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupReminderRecycler() {
        val adapter = RemindersAdapter(reminderOptions)
        adapter.selectedPosition = selectedReminderIndex

        binding.rvReminderOptions.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext(), androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false)
        binding.rvReminderOptions.adapter = adapter

        val snapHelper = androidx.recyclerview.widget.LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvReminderOptions)

        binding.rvReminderOptions.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(recyclerView.layoutManager) ?: return
                    val position = recyclerView.layoutManager?.getPosition(centerView) ?: return

                    selectedReminderIndex = position
                    adapter.selectedPosition = position
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun setupPickers() {
        binding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    selectedDate = selectedCalendar.time
                    binding.tvSelectedDate.text = dateFormat.format(selectedCalendar.time)
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSelectTime.setOnClickListener {
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
                    selectedTime = selectedCalendar.time
                    binding.tvSelectedTime.text = timeFormat.format(selectedCalendar.time)
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
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(requireContext(), "Точные будильники не разрешены. Включите их в настройках.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            return
        }

        val intent = Intent(requireContext(), MyNotificationReceiver::class.java).apply {
            putExtra("notification_message", message)
            putExtra("withSound", withSound)
            putExtra("autoCancel", autoCancel)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

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
    }

    private fun cancelAlarm(item: NotificationEntry) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), MyNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            item.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
