package com.example.mydiabetesapp.feature.notification.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.databinding.ItemReminderOptionBinding

class RemindersAdapter(
    private val items: List<String>
) : RecyclerView.Adapter<RemindersAdapter.ViewHolder>() {

    var selectedPosition = 0

    inner class ViewHolder(private val binding: ItemReminderOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String, isSelected: Boolean) {
            binding.tvOption.text = text
            if (isSelected) {
                binding.tvOption.setBackgroundResource(android.R.color.holo_blue_light)
            } else {
                binding.tvOption.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReminderOptionBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text = items[position]
        holder.bind(text, position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size
}
