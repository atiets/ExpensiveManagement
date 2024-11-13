package com.example.expensivemanagement.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.Model.LoaiChi
import com.example.expensivemanagement.R
import com.google.android.material.textfield.TextInputEditText

class LoaiChiAdapter(
    private val context: Context,
    private val loaiChis: ArrayList<LoaiChi>
) : android.widget.BaseAdapter() {

    // Tạo ViewHolder để tối ưu hóa việc tìm kiếm view trong item
    private class ViewHolder {
        lateinit var textViewLoaiChi: TextView
        lateinit var imageViewEdit: ImageView
        lateinit var imageViewDelete: ImageView
    }

    override fun getCount(): Int {
        return loaiChis.size
    }

    override fun getItem(position: Int): Any {
        return loaiChis[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        // Kiểm tra xem có thể tái sử dụng view hay không
        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_loaichi_layout, parent, false)

            // Tạo và gán ViewHolder cho item
            viewHolder = ViewHolder()
            viewHolder.textViewLoaiChi = view.findViewById(R.id.textViewLoaiChi)
            viewHolder.imageViewEdit = view.findViewById(R.id.imageViewEditLoaiChi)
            viewHolder.imageViewDelete = view.findViewById(R.id.imageViewDeleteLoaiChi)

            view.tag = viewHolder  // Lưu trữ viewHolder vào tag của view
        } else {
            // Lấy lại viewHolder nếu có sẵn
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Lấy đối tượng LoaiChi tương ứng với vị trí
        val loaiChi = loaiChis[position]

        // Cập nhật nội dung của item
        viewHolder.textViewLoaiChi.text = loaiChi.nameLoaiChi

        // Xử lý sự kiện edit
        viewHolder.imageViewEdit.setOnClickListener {
            Toast.makeText(context, "Chỉnh sửa ${loaiChi.nameLoaiChi}", Toast.LENGTH_SHORT).show()
            // Thực hiện logic chỉnh sửa ở đây
        }

        // Xử lý sự kiện delete
        viewHolder.imageViewDelete.setOnClickListener {
            Toast.makeText(context, "Xóa ${loaiChi.nameLoaiChi}", Toast.LENGTH_SHORT).show()
            // Thực hiện logic xóa ở đây
        }

        return view
    }
}