package com.example.expensivemanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.LoaiThu

class SpLoaiThuAdapter(
    context: Context,
    objects: ArrayList<LoaiThu>
) : ArrayAdapter<LoaiThu>(context, R.layout.spinner_item_layout, objects) {

    private val data: ArrayList<LoaiThu> = objects  // Cập nhật kiểu dữ liệu
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    // Custom dropdown view
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    // Custom selected view
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    // Custom view for both dropdown and selected item
    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row: View = convertView ?: inflater.inflate(R.layout.spinner_item_layout, parent, false)
        val txtLoaiThuSp: TextView = row.findViewById(R.id.txtSpinner)

        // Set the name of the item to be displayed in the spinner
        txtLoaiThuSp.text = data[position].getNameLoaiThu()

        return row
    }
}
