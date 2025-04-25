package com.example.mydiabetesapp.ui.export

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.mydiabetesapp.data.database.ReportEntry
import com.example.mydiabetesapp.databinding.ItemReportBinding

class ReportAdapter(
    private val onOpen:   (ReportEntry) -> Unit,
    private val onDelete: (ReportEntry) -> Unit
) : ListAdapter<ReportEntry, ReportAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemReportBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(r: ReportEntry) = with(b) {
            tvName.text   = r.fileName
            tvPeriod.text = "${r.startDate} – ${r.endDate} (${r.daysCount} д.)"
            root.setOnClickListener { onOpen(r) }
            btnDelete.setOnClickListener { onDelete(r) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ReportEntry>() {
            override fun areItemsTheSame(o: ReportEntry, n: ReportEntry) = o.id == n.id
            override fun areContentsTheSame(o: ReportEntry, n: ReportEntry) = o == n
        }
    }
}
