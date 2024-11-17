package com.example.expensivemanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.LoaiThu

class LoaiThuAdapter(
    private val loaiThuList: MutableList<LoaiThu>,
    private val onEdit: (LoaiThu) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<LoaiThuAdapter.LoaiThuViewHolder>() {

    inner class LoaiThuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.textViewLoaiThu)
        val btnEdit: ImageView = itemView.findViewById(R.id.imageViewEditLoaiThu)
        val btnDelete: ImageView = itemView.findViewById(R.id.imageViewDeleteLoaiThu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoaiThuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loaithu_layout, parent, false)
        return LoaiThuViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoaiThuViewHolder, position: Int) {
        val loaiThu = loaiThuList[position]
        holder.tvName.text = loaiThu.name
        holder.btnEdit.setOnClickListener { onEdit(loaiThu) }
        holder.btnDelete.setOnClickListener { onDelete(loaiThu.id) }
    }

    override fun getItemCount(): Int = loaiThuList.size
}