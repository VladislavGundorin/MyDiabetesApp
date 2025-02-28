package com.example.mydiabetesapp.ui.journal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.data.database.GlucoseEntry
import com.example.mydiabetesapp.databinding.ItemGlucoseEntryBinding

interface OnGlucoseEntryClickListener {
    fun onEdit(entry: GlucoseEntry)
    fun onDelete(entry: GlucoseEntry)
}

class GlucoseAdapter(
    private var entries: List<GlucoseEntry>,
    private val listener: OnGlucoseEntryClickListener
) : RecyclerView.Adapter<GlucoseAdapter.GlucoseViewHolder>() {

    inner class GlucoseViewHolder(val binding: ItemGlucoseEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: GlucoseEntry) {
            binding.tvDate.text = entry.date
            binding.tvTime.text = entry.time
            binding.tvGlucoseLevel.text = "Глюкоза: ${entry.glucoseLevel}"
            binding.tvCategory.text = "Категория: ${entry.category}"

            binding.btnEdit.setOnClickListener {
                listener.onEdit(entry)
            }
            binding.btnDelete.setOnClickListener {
                listener.onDelete(entry)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlucoseViewHolder {
        val binding = ItemGlucoseEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GlucoseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GlucoseViewHolder, position: Int) {
        val entry = entries[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = entries.size

    fun updateList(newEntries: List<GlucoseEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
