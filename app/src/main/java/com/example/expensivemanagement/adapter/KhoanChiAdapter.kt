package com.example.expensivemanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.KhoanChi

class KhoanChiAdapter(
    private val khoanChiList: List<KhoanChi>,
    private val onDeleteClick: (KhoanChi) -> Unit,
    private val onEditClick: (KhoanChi) -> Unit,
    private val onViewClick: (KhoanChi) -> Unit
) : RecyclerView.Adapter<KhoanChiAdapter.KhoanChiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KhoanChiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_khoanchi_layout, parent, false)
        return KhoanChiViewHolder(view)
    }

    override fun onBindViewHolder(holder: KhoanChiViewHolder, position: Int) {
        val khoanChi = khoanChiList[position]
        holder.nameTextView.text = khoanChi.name
        holder.amountTextView.text = khoanChi.soTien.toString()

        // Set click listeners for each action
        holder.deleteImageView.setOnClickListener { onDeleteClick(khoanChi) }
        holder.editImageView.setOnClickListener { onEditClick(khoanChi) }
        holder.itemView.setOnClickListener { onViewClick(khoanChi) }
    }

    override fun getItemCount(): Int = khoanChiList.size

    inner class KhoanChiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewChi)
        val amountTextView: TextView = itemView.findViewById(R.id.textViewTienChi)
        val deleteImageView: ImageView = itemView.findViewById(R.id.imageViewDeleteChi)
        val editImageView: ImageView = itemView.findViewById(R.id.imageViewEditChi)
    }
}