package com.example.mydiabetesapp.feature.weight.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.feature.weight.data.WeightEntry
import com.example.mydiabetesapp.databinding.ItemWeightEntryBinding

class WeightAdapter(
    private var entries: List<WeightEntry>,
    private val listener: OnWeightEntryClickListener
) : RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {

    inner class WeightViewHolder(val binding: ItemWeightEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: WeightEntry) {
            binding.tvDate.text = entry.date
            binding.tvTime.text = entry.time
            binding.tvWeight.text = "Вес: ${entry.weight}"
            binding.btnEdit.setOnClickListener { listener.onEdit(entry) }
            binding.btnDelete.setOnClickListener { listener.onDelete(entry) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val binding = ItemWeightEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    fun updateList(newEntries: List<WeightEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
