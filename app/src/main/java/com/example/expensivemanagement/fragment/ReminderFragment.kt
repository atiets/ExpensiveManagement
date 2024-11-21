package com.example.expensivemanagement.fragment

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.adapter.ReminderAdapter
import com.example.expensivemanagement.model.Reminder
import com.example.expensivemanagement.receiver.ReminderReceiver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import android.provider.Settings

class ReminderFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddReminder: Button
    private lateinit var database: DatabaseReference
    private lateinit var reminderList: MutableList<Reminder>
    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reminder, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewLoaiThu)
        btnAddReminder = view.findViewById(R.id.btnThemReminder)
        database = FirebaseDatabase.getInstance().getReference("Reminders")
        reminderList = mutableListOf()

        adapter = ReminderAdapter(reminderList,
            onEditClick = { showEditDialog(it) },
            onDeleteClick = { deleteReminder(it) })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        btnAddReminder.setOnClickListener { showAddDialog() }

        loadRemindersData()
        return view
    }

    private fun loadRemindersData() {
        // Initialize database reference correctly
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().getReference("reminders").child(uid)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reminderList.clear()  // Clear the existing data before loading new data
                for (child in snapshot.children) {
                    val reminder = child.getValue(Reminder::class.java)
                    if (reminder != null) {
                        reminderList.add(reminder)
                    }
                }

                // Log the reminder list to confirm data fetching
                Log.d("Reminder", "Loaded reminders: ${reminderList.size} items")

                // Notify adapter to update UI
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Log the error for debugging
                Log.e("Reminder", "Error fetching data: ${error.message}")
                Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun scheduleReminder(reminder: Reminder) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.name) // Lấy tiêu đề từ name
            putExtra("message", reminder.note) // Lấy nội dung từ note
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(), // Sử dụng hashCode để đảm bảo ID duy nhất
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Chuyển đổi ngày và giờ thành thời gian tính bằng milliseconds
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val reminderTime = dateTimeFormat.parse("${reminder.date} ${reminder.time}")?.time
        if (reminderTime == null) {
            Toast.makeText(requireContext(), "Lỗi khi phân tích ngày giờ!", Toast.LENGTH_SHORT).show()
            return
        }

        // Kiểm tra API level và sử dụng phương thức phù hợp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31)
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Request permission to schedule exact alarms
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            } else {
                // Proceed with setting the alarm
                setExactAlarm(reminder)
            }
        } else {
            // For older versions (API level 24 and below), you can directly set the alarm
            setExactAlarm(reminder)
        }

        Toast.makeText(requireContext(), "Đã lên lịch nhắc nhở: ${reminder.name}", Toast.LENGTH_SHORT).show()
    }

    private fun setExactAlarm(reminder: Reminder) {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.name) // Lấy tiêu đề từ name
            putExtra("message", reminder.note) // Lấy nội dung từ note
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(), // Sử dụng hashCode để đảm bảo ID duy nhất
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Chuyển đổi ngày và giờ thành thời gian tính bằng milliseconds
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val reminderTime = dateTimeFormat.parse("${reminder.date} ${reminder.time}")?.time
        if (reminderTime == null) {
            Toast.makeText(requireContext(), "Lỗi khi phân tích ngày giờ!", Toast.LENGTH_SHORT).show()
            return
        }

        // Thiết lập báo thức chính xác
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
        )

        Toast.makeText(requireContext(), "Đã lên lịch nhắc nhở: ${reminder.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.diag_add_reminder, null)
        val dialog = AlertDialog.Builder(context ?: throw IllegalStateException("Context không được phép null"))
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val frequencies = arrayOf("Một lần", "Hàng ngày", "Hàng tuần", "Mỗi 2 tuần", "Hàng tháng", "Mỗi 2 tháng", "Hàng quý", "Mỗi năm")

        val spinner = dialogView.findViewById<Spinner>(R.id.sp_reminder_frequency)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Lấy tham chiếu đến EditText và Spinner
        val edtReminderName = dialogView.findViewById<EditText>(R.id.edt_reminder_name)
        val edtReminderDate = dialogView.findViewById<EditText>(R.id.edt_reminder_date) // EditText cho ngày
        val edtReminderTime = dialogView.findViewById<EditText>(R.id.edt_reminder_time) // EditText cho giờ
        val edtReminderNote = dialogView.findViewById<EditText>(R.id.edt_reminder_note) // EditText cho ghi chú

        // Cài đặt sự kiện cho nút "Chọn ngày"
        edtReminderDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                edtReminderDate.setText("${selectedDay}/${selectedMonth + 1}/$selectedYear")
            }, year, month, day)
            datePickerDialog.show()
        }

        // Xử lý khi nhấn nút "Thêm Reminder"
        dialogView.findViewById<Button>(R.id.btnThemReminder).setOnClickListener {
            val name = edtReminderName.text.toString().trim()
            val frequency = spinner.selectedItem.toString()
            val date = edtReminderDate.text.toString().trim() // Lấy ngày từ EditText
            val time = edtReminderTime.text.toString().trim() // Lấy thời gian từ EditText
            val note = edtReminderNote.text.toString().trim() // Lấy ghi chú từ EditText

            // Kiểm tra dữ liệu hợp lệ
            if (name.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lấy UID từ FirebaseAuth
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }

            // Truy vấn "user" để lấy userId
            val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)
            userRef.get().addOnSuccessListener { snapshot ->
                val userId = snapshot.child("userId").getValue(String::class.java)
                if (userId.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Không tìm thấy userId!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@addOnSuccessListener
                }

                // Tạo nhắc nhở mới
                val id = FirebaseDatabase.getInstance().reference.push().key ?: return@addOnSuccessListener
                val reminder = Reminder(id, name, frequency, date, time, note)

                // Log before saving to Firebase
                Log.d("Reminder22222", "Saving reminder: $reminder")

                // Lưu vào Firebase dưới userId
                FirebaseDatabase.getInstance().getReference("reminders")
                    .child(userId)
                    .child(id)
                    .setValue(reminder)
                    .addOnSuccessListener {
                        // Log success
                        Log.d("Reminder", "Reminder added successfully!")

                        Toast.makeText(requireContext(), "Thêm nhắc nhở thành công!", Toast.LENGTH_SHORT).show()
                        scheduleReminder(reminder)
                        dialog.dismiss()

                        loadRemindersData() // Ensure this is called to load the new data
                    }
                    .addOnFailureListener {
                        // Log failure
                        Log.e("Reminder", "Failed to add reminder: ${it.message}")

                        Toast.makeText(requireContext(), "Thêm nhắc nhở thất bại!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            }.addOnFailureListener {
                Log.e("Reminder", "Error fetching userId: ${it.message}")
                Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        // Xử lý khi nhấn nút "Hủy"
        dialogView.findViewById<Button>(R.id.btnHuyThemReminder).setOnClickListener {
            dialog.dismiss()
        }

        // Hiển thị Dialog
        dialog.show()
    }

    private fun showEditDialog(reminder: Reminder) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.diag_update_reminder, null)
        val dialog = AlertDialog.Builder(context ?: throw IllegalStateException("Context không được phép null"))
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Thiết lập các trường EditText với giá trị hiện tại của reminder
        dialogView.findViewById<EditText>(R.id.edt_reminder_nameUpdate).setText(reminder.name)
        dialogView.findViewById<EditText>(R.id.edt_reminder_dateUpdate).setText(reminder.date)
        dialogView.findViewById<EditText>(R.id.edt_reminder_timeUpdate).setText(reminder.time)
        dialogView.findViewById<EditText>(R.id.edt_reminder_noteUpdate).setText(reminder.note)

        // Cài đặt Spinner tần suất và chọn giá trị hiện tại
        val frequencies = arrayOf("Một lần", "Hàng ngày", "Hàng tuần", "Mỗi 2 tuần", "Hàng tháng", "Mỗi 2 tháng", "Hàng quý", "Mỗi năm")
        val spinner = dialogView.findViewById<Spinner>(R.id.sp_reminder_frequencyUpdate)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Chọn giá trị tần suất hiện tại
        val frequencyPosition = frequencies.indexOf(reminder.frequency)
        spinner.setSelection(frequencyPosition)

        // Cài đặt sự kiện cho nút "Chọn ngày" (cập nhật ngày)
        dialogView.findViewById<EditText>(R.id.edt_reminder_dateUpdate).setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                dialogView.findViewById<EditText>(R.id.edt_reminder_dateUpdate).setText("${selectedDay}/${selectedMonth + 1}/$selectedYear")
            }, year, month, day)

            datePickerDialog.show()
        }

        // Cài đặt sự kiện cho nút "Cập nhật Reminder"
        dialogView.findViewById<Button>(R.id.btnUpdateReminder).setOnClickListener {
            // Cập nhật giá trị của reminder từ các EditText và Spinner
            reminder.name = dialogView.findViewById<EditText>(R.id.edt_reminder_nameUpdate).text.toString()
            reminder.date = dialogView.findViewById<EditText>(R.id.edt_reminder_dateUpdate).text.toString()
            reminder.time = dialogView.findViewById<EditText>(R.id.edt_reminder_timeUpdate).text.toString()
            reminder.note = dialogView.findViewById<EditText>(R.id.edt_reminder_noteUpdate).text.toString()
            reminder.frequency = spinner.selectedItem.toString() // Cập nhật tần suất từ Spinner

            // Lấy UID của người dùng
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                // Truy vấn "user" để lấy userId
                val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)
                userRef.get().addOnSuccessListener { snapshot ->
                    val userId = snapshot.child("userId").getValue(String::class.java)
                    if (!userId.isNullOrEmpty()) {
                        // Cập nhật nhắc nhở trong node "reminders" dưới userId
                        FirebaseDatabase.getInstance().getReference("reminders")
                            .child(userId) // Dùng userId thay cho reminder.id
                            .child(reminder.id ?: return@addOnSuccessListener) // Sử dụng id của reminder
                            .setValue(reminder)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Cập nhật nhắc nhở thành công!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Cập nhật nhắc nhở thất bại!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy userId!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        // Cài đặt sự kiện cho nút "Hủy"
        dialogView.findViewById<Button>(R.id.btnHuyUpdateReinder).setOnClickListener {
            dialog.dismiss()
        }

        // Hiển thị Dialog
        dialog.show()
    }

    private fun deleteReminder(id: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Truy vấn để lấy userId từ bảng "user"
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)
        userRef.get().addOnSuccessListener { snapshot ->
            val userId = snapshot.child("userId").getValue(String::class.java)
            if (userId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Không tìm thấy userId!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Thực hiện xóa reminder theo userId
            val remindersRef = FirebaseDatabase.getInstance().getReference("reminders").child(userId)
            remindersRef.child(id).removeValue().addOnSuccessListener {
                Toast.makeText(requireContext(), "Xóa nhắc nhở thành công!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Xóa nhắc nhở thất bại!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}