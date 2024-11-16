package com.example.expensivemanagement.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.LoaiChi

class SpLoaiChiAdapter(
    context: Context,
    objects: ArrayList<LoaiChi>
) : ArrayAdapter<LoaiChi>(context, R.layout.spinner_item_layout, objects) {

    private val data: ArrayList<LoaiChi> = objects
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
        val txtLoaiChiSp: TextView = row.findViewById(R.id.txtSpinner)

        // Set the name of the item to be displayed in the spinner
        txtLoaiChiSp.text = data[position].getNameLoaiChi()

        return row
    }
}