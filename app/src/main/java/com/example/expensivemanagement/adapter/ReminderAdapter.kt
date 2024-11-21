package com.example.expensivemanagement.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.model.Reminder

class ReminderAdapter(
    private var reminders: MutableList<Reminder>,
    private val onEditClick: (Reminder) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tx_nameReminder)
        val switch: Switch = itemView.findViewById(R.id.switchReminder)

        fun bind(reminder: Reminder) {
            name.text = reminder.name
            switch.isChecked = reminder.isActive
            switch.setOnCheckedChangeListener { _, isChecked ->
                reminder.isActive = isChecked
            }
            itemView.setOnClickListener { onEditClick(reminder) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder_layout, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(reminders[position])

        val reminder = reminders[position] // Lấy reminder từ vị trí hiện tại
        // Sửa trong onBindViewHolder
        holder.itemView.setOnLongClickListener {
            // Hiển thị hộp thoại xác nhận khi nhấn giữ
            AlertDialog.Builder(holder.itemView.context)
                .setMessage("Bạn có chắc chắn muốn xóa nhắc nhở này?")
                .setPositiveButton("Xóa") { _, _ ->
                    // Kiểm tra id có phải null không trước khi xóa
                    reminder.id?.let {
                        onDeleteClick(it) // Gọi onDeleteClick chỉ khi id không phải null
                        Toast.makeText(holder.itemView.context, "Đã xóa nhắc nhở", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(holder.itemView.context, "ID không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()

            true // Trả về true để sự kiện không tiếp tục truyền đi
        }
    }

    override fun getItemCount(): Int = reminders.size
}