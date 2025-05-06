package com.example.mydiabetesapp.feature.export.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.mydiabetesapp.feature.export.data.ReportEntry
import com.example.mydiabetesapp.databinding.ItemReportBinding

class ReportAdapter(
    private val onOpen:     (ReportEntry) -> Unit,
    private val onDownload: (ReportEntry) -> Unit,
    private val onDelete:   (ReportEntry) -> Unit
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
            root.setOnClickListener     { onOpen(r) }
            btnDownload.setOnClickListener { onDownload(r) }
            btnDelete.setOnClickListener   { onDelete(r) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ReportEntry>() {
            override fun areItemsTheSame(old: ReportEntry, new: ReportEntry) = old.id == new.id
            override fun areContentsTheSame(old: ReportEntry, new: ReportEntry) = old == new
        }
    }
}
