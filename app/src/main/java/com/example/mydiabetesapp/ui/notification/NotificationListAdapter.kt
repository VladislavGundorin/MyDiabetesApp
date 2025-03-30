package com.example.mydiabetesapp.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.data.database.NotificationEntry
import com.example.mydiabetesapp.databinding.ItemNotificationBinding

class NotificationListAdapter(
    private val onToggle: (NotificationEntry, Boolean) -> Unit,
    private val onItemClick: (NotificationEntry) -> Unit
) : ListAdapter<NotificationEntry, NotificationListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationEntry) {
            binding.tvNotificationTitle.text = item.message

            val repeatText = if (item.repeatDaily) "ежедневно" else "не повторять"
            binding.tvNotificationDescription.text = repeatText

            val dateTimeStr = "Сработает: ${item.date} ${item.time}"
            binding.tvNotificationTime.text = dateTimeStr

            binding.switchEnabled.isChecked = item.enabled
            binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item, isChecked)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NotificationEntry>() {
        override fun areItemsTheSame(oldItem: NotificationEntry, newItem: NotificationEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationEntry, newItem: NotificationEntry): Boolean {
            return oldItem == newItem
        }
    }
}
