package com.example.expensivemanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.fragment.NganSach

class NganSachListAdapter(
    context: Context,
    private val nganSachList: List<NganSach>,
    private val onDeleteClick: (NganSach) -> Unit,
    private val onEditClick: (NganSach) -> Unit
) : ArrayAdapter<NganSach>(context, 0, nganSachList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nganSach = getItem(position)

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_ngansach_layout, parent, false)

        val textViewTenNganSach = view.findViewById<TextView>(R.id.textViewTenNganSach)
        val textViewThangNam = view.findViewById<TextView>(R.id.textViewThangNam)
        val textViewSoTien = view.findViewById<TextView>(R.id.textViewSoTien)
        val textViewTotalChi = view.findViewById<TextView>(R.id.textViewTotalChi)
        val btnEdit = view.findViewById<Button>(R.id.btnEdit)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        textViewTenNganSach.text = nganSach?.tenNganSach
        textViewThangNam.text = nganSach?.thangNam
        textViewSoTien.text = "Ngân sách: ${nganSach?.soTien}"
        textViewTotalChi.text = "Tổng chi: ${nganSach?.totalChi}"

        // Set click listeners for edit and delete buttons
        btnEdit.setOnClickListener {
            nganSach?.let { onEditClick(it) }
        }

        btnDelete.setOnClickListener {
            nganSach?.let { onDeleteClick(it) }
        }

        return view
    }
} 