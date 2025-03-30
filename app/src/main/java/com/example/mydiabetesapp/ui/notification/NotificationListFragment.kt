package com.example.mydiabetesapp.ui.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mydiabetesapp.R
import com.example.mydiabetesapp.data.database.AppDatabase
import com.example.mydiabetesapp.data.database.NotificationEntry
import com.example.mydiabetesapp.databinding.FragmentNotificationBinding
import com.example.mydiabetesapp.repository.NotificationRepository
import com.example.mydiabetesapp.ui.viewmodel.NotificationViewModel
import com.example.mydiabetesapp.ui.viewmodel.NotificationViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationListFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationViewModel
    private lateinit var adapter: NotificationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val dao = AppDatabase.getDatabase(requireContext()).notificationDao()
        val repository = NotificationRepository(dao)
        val factory = NotificationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(NotificationViewModel::class.java)

        adapter = NotificationListAdapter(
            onToggle = { item, isEnabled -> toggleNotification(item, isEnabled) },
            onItemClick = { item -> editNotification(item) }
        )

        binding.rvNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifications.collectLatest { list ->
                adapter.submitList(list)
            }
        }

        binding.btnSave.setOnClickListener {
            findNavController().navigate(R.id.action_nav_notification_to_addNotificationFragment)
        }
    }

    private fun toggleNotification(item: NotificationEntry, isEnabled: Boolean) {
        val updatedItem = item.copy(enabled = isEnabled)
        viewModel.updateNotification(updatedItem)
        if (isEnabled) {
            scheduleAlarm(updatedItem)
        } else {
            cancelAlarm(updatedItem)
        }
    }

    private fun scheduleAlarm(item: NotificationEntry) {
        val partsDate = item.date.split(".")
        val partsTime = item.time.split(":")
        if (partsDate.size < 3 || partsTime.size < 2) return

        val day = partsDate[0].toIntOrNull() ?: return
        val month = partsDate[1].toIntOrNull()?.minus(1) ?: return
        val year = partsDate[2].toIntOrNull() ?: return
        val hour = partsTime[0].toIntOrNull() ?: return
        val minute = partsTime[1].toIntOrNull() ?: return

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month)
            set(java.util.Calendar.DAY_OF_MONTH, day)
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val timeInMillis = calendar.timeInMillis

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), MyNotificationReceiver::class.java).apply {
            putExtra("notification_message", item.message)
            putExtra("withSound", true)
            putExtra("autoCancel", item.autoCancel)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            item.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        when {
            item.repeatDaily -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            item.intervalMinutes > 0 -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    item.intervalMinutes * 60 * 1000,
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

    private fun editNotification(item: NotificationEntry) {
        val bundle = Bundle().apply {
            putInt("notification_id", item.id)
        }
        findNavController().navigate(R.id.action_nav_notification_to_addNotificationFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
