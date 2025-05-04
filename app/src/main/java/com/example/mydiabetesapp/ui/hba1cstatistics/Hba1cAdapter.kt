package com.example.mydiabetesapp.ui.hba1cstatistics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.data.database.Hba1cEntry
import com.example.mydiabetesapp.databinding.ItemHba1cEntryBinding

class Hba1cAdapter(
    private var items: List<Hba1cEntry>,
    private val listener: OnHba1cEntryClickListener
) : RecyclerView.Adapter<Hba1cAdapter.VH>() {

    inner class VH(private val b: ItemHba1cEntryBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(e: Hba1cEntry) = with(b) {
            tvDate.text  = e.date
            tvValue.text = "%.2f %%".format(e.hba1c)
            btnEdit.setOnClickListener   { listener.onEdit(e) }
            btnDelete.setOnClickListener { listener.onDelete(e) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) =
        VH(ItemHba1cEntryBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, i: Int) = h.bind(items[i])
    override fun getItemCount() = items.size
    fun update(list: List<Hba1cEntry>) { items = list; notifyDataSetChanged() }
}
