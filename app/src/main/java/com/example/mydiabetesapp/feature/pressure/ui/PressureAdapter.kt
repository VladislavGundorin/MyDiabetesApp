package com.example.mydiabetesapp.feature.pressure.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureEntry
import com.example.mydiabetesapp.databinding.ItemPressureEntryBinding

class PressureAdapter(
    private var list: List<BloodPressureEntry>,
    private val listener: OnPressureClickListener
) : RecyclerView.Adapter<PressureAdapter.VH>() {

    inner class VH(val b: ItemPressureEntryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: BloodPressureEntry) = with(b) {
            tvDate.text = e.date
            tvTime.text = e.time
            tvValue.text = "${e.systolic} / ${e.diastolic} мм рт. ст.   П: ${e.pulse}"
            btnEdit.setOnClickListener { listener.onEdit(e) }
            btnDelete.setOnClickListener { listener.onDelete(e) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup,v:Int)=
        VH(ItemPressureEntryBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun getItemCount()=list.size
    override fun onBindViewHolder(h: VH, i: Int)=h.bind(list[i])
    fun submit(new: List<BloodPressureEntry>){ list = new; notifyDataSetChanged() }
}
