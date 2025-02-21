package com.example.mydiabetesapp.ui.journal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.data.database.GlucoseEntry
import com.example.mydiabetesapp.databinding.ItemGlucoseEntryBinding

class GlucoseAdapter(
    private var entries: List<GlucoseEntry>
) : RecyclerView.Adapter<GlucoseAdapter.GlucoseViewHolder>() {

    inner class GlucoseViewHolder(val binding: ItemGlucoseEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlucoseViewHolder {
        val binding = ItemGlucoseEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GlucoseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GlucoseViewHolder, position: Int) {
        val entry = entries[position]
        with(holder.binding) {
            tvDate.text = entry.date
            tvTime.text = entry.time
            tvGlucoseLevel.text = "Глюкоза: ${entry.glucoseLevel}"
        }
    }

    override fun getItemCount(): Int = entries.size

    fun updateList(newEntries: List<GlucoseEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }
}
