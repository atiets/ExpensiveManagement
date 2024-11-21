package com.example.expensivemanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.KhoanChi

class ExpenseDailyAdapter(private val khoanChiList: MutableList<KhoanChi>) :
    RecyclerView.Adapter<ExpenseDailyAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_thongketheoloai, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val khoanChi = khoanChiList[position]
         holder.tvExpenseTitle.text = khoanChi.name
        holder.tvExpenseAmount.text = khoanChi.soTien.toString()
    }

    override fun getItemCount(): Int {
        return khoanChiList.size
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvExpenseTitle: TextView = itemView.findViewById(R.id.tv_dmKhoan)
        var tvExpenseAmount: TextView = itemView.findViewById(R.id.tv_sotien)
    }

    // Hàm để cập nhật dữ liệu trong RecyclerView
    fun updateKhoanChiList(newList: List<KhoanChi>) {
        khoanChiList.clear()
        khoanChiList.addAll(newList)
        notifyDataSetChanged()
    }
}