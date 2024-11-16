package com.example.expensivemanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.LoaiChi

class LoaiChiAdapter(
    private val loaiChiList: MutableList<LoaiChi>,
    private val onEdit: (LoaiChi) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<LoaiChiAdapter.LoaiChiViewHolder>() {

    inner class LoaiChiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.textViewLoaiChi)
        val btnEdit: ImageView = itemView.findViewById(R.id.imageViewEditLoaiChi)
        val btnDelete: ImageView = itemView.findViewById(R.id.imageViewDeleteLoaiChi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoaiChiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loaichi_layout, parent, false)
        return LoaiChiViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoaiChiViewHolder, position: Int) {
        val loaiChi = loaiChiList[position]
        holder.tvName.text = loaiChi.name
        holder.btnEdit.setOnClickListener { onEdit(loaiChi) }
        holder.btnDelete.setOnClickListener { onDelete(loaiChi.id) }
    }

    override fun getItemCount(): Int = loaiChiList.size
}