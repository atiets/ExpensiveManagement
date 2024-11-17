package com.example.expensivemanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.KhoanThu

class KhoanThuAdapter(
    private val khoanThuList: List<KhoanThu>,
    private val onDeleteClick: (KhoanThu) -> Unit,
    private val onEditClick: (KhoanThu) -> Unit,
    private val onViewClick: (KhoanThu) -> Unit
) : RecyclerView.Adapter<KhoanThuAdapter.KhoanThuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KhoanThuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_khoanthu_layout, parent, false)
        return KhoanThuViewHolder(view)
    }

    override fun onBindViewHolder(holder: KhoanThuViewHolder, position: Int) {
        val khoanThu = khoanThuList[position]
        holder.nameTextView.text = khoanThu.tenThu
        holder.amountTextView.text = khoanThu.soTien.toString()

        // Set click listeners for each action
        holder.deleteImageView.setOnClickListener { onDeleteClick(khoanThu) }
        holder.editImageView.setOnClickListener { onEditClick(khoanThu) }
        holder.itemView.setOnClickListener { onViewClick(khoanThu) }
    }

    override fun getItemCount(): Int = khoanThuList.size

    inner class KhoanThuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewThu)
        val amountTextView: TextView = itemView.findViewById(R.id.textViewTienThu)
        val deleteImageView: ImageView = itemView.findViewById(R.id.imageViewDeleteThu)
        val editImageView: ImageView = itemView.findViewById(R.id.imageViewEditThu)
    }
}