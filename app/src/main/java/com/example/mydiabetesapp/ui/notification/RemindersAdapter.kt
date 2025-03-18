package com.example.mydiabetesapp.ui.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.databinding.ItemReminderOptionBinding

class RemindersAdapter(
    private val items: List<String>
) : RecyclerView.Adapter<RemindersAdapter.ViewHolder>() {

    // Индекс выбранного элемента
    var selectedPosition = 0

    inner class ViewHolder(private val binding: ItemReminderOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String, isSelected: Boolean) {
            binding.tvOption.text = text
            // Можно менять цвет фона или текста, если элемент выбран
            if (isSelected) {
                // Например, выделим жирным шрифтом или меняем фон
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
