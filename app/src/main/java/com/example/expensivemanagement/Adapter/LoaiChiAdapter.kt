package com.example.expensivemanagement.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.Model.LoaiChi
import com.example.expensivemanagement.R
import com.google.android.material.textfield.TextInputEditText

class LoaiChiAdapter(private val loaiChis: ArrayList<LoaiChi>) :
    RecyclerView.Adapter<LoaiChiAdapter.LoaiChiHolder>() {

        class LoaiChiHolder(view: View) : RecyclerView.ViewHolder(view) {
            val labelInput : TextInputEditText = view.findViewById(R.id.labelEdtNameLoaiChi)
            val description : TextInputEditText = view.findViewById(R.id.edtNameLoaiChi)
        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoaiChiHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.diag_add_loachi, parent, false)
        return LoaiChiHolder(view)
    }
    override fun onBindViewHolder(holder: LoaiChiHolder, position: Int) {
        val loaiChi = loaiChis[position]

        // Gán giá trị vào các View trong item
        holder.labelInput.setText(loaiChi.label)
        holder.description.setText(loaiChi.description)
    }
    override fun getItemCount(): Int {
        return loaiChis.size
    }
}