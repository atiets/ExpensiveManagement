package com.example.expensivemanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.KhoanThu

class IncomeDailyAdapter(private val khoanThuList: List<KhoanThu>) :
    RecyclerView.Adapter<IncomeDailyAdapter.IncomeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item_thongketheoloai, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val (_, tenThu, _, _, soTien) = khoanThuList[position]
        holder.tvIncomeTitle.text = tenThu
        holder.tvIncomeAmount.text = soTien.toString()
    }

    override fun getItemCount(): Int {
        return khoanThuList.size
    }

    class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvIncomeTitle: TextView
        var tvIncomeAmount: TextView

        init {
            tvIncomeTitle = itemView.findViewById<TextView>(R.id.tv_dmKhoan)
            tvIncomeAmount = itemView.findViewById<TextView>(R.id.tv_sotien)
        }
    }
}