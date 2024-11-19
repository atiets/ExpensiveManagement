package com.example.expensivemanagement.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.CalendarView.OnDateChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.adapter.ExpenseDailyAdapter
import com.example.expensivemanagement.adapter.IncomeDailyAdapter
import com.example.expensivemanagement.model.KhoanChi
import com.example.expensivemanagement.model.KhoanThu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateFragment : Fragment(R.layout.fragment_date) {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerViewIncome: RecyclerView
    private lateinit var recyclerViewExpense: RecyclerView
    private lateinit var expenseDailyAdapterIncome: ExpenseDailyAdapter
    private lateinit var expenseDailyAdapterExpense: ExpenseDailyAdapter

    private lateinit var tvThuNhap: TextView
    private lateinit var tvChiTieu: TextView
    private lateinit var tvTong: TextView

    private val khoanChiIncomeList = mutableListOf<KhoanChi>()
    private val khoanChiExpenseList = mutableListOf<KhoanChi>()

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
 
        // Khởi tạo RecyclerView và adapter
        recyclerViewIncome = view.findViewById(R.id.recyclerViewIncome)
        recyclerViewExpense = view.findViewById(R.id.recyclerViewExpense)

        tvThuNhap = view.findViewById(R.id.tv_ThuNhap)
        tvChiTieu = view.findViewById(R.id.tv_ChiTieu)
        tvTong = view.findViewById(R.id.tv_Tong)

        expenseDailyAdapterIncome = ExpenseDailyAdapter(khoanChiIncomeList)
        expenseDailyAdapterExpense = ExpenseDailyAdapter(khoanChiExpenseList)

        recyclerViewIncome.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewIncome.adapter = expenseDailyAdapterIncome

        recyclerViewExpense.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewExpense.adapter = expenseDailyAdapterExpense

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        // Gọi hàm loadKhoanChi() để tải dữ liệu từ Firebase
        loadKhoanChiByUser(currentUser.uid)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            loadKhoanChiByDate(currentUser.uid, selectedDate)
        }

        // Tải dữ liệu ban đầu cho ngày hiện tại
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        loadKhoanChiByDate(currentUser.uid, currentDate)
    }

    private fun loadKhoanChiByUser(userId: String) {
        val khoanChiRef = FirebaseDatabase.getInstance().getReference("KhoanChi")

        // Truy cập trực tiếp vào node của userId
        khoanChiRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chiList = mutableListOf<KhoanChi>()
                var totalExpense = 0.0

                // Duyệt qua các khoản chi bên dưới userId
                for (dataSnapshot in snapshot.children) {
                    val khoanChi = dataSnapshot.getValue(KhoanChi::class.java)
                    if (khoanChi != null) {
                        chiList.add(khoanChi)
                        totalExpense += khoanChi.soTien?.toDouble() ?: 0.0
                    }
                }

                // Cập nhật danh sách vào Adapter
                expenseDailyAdapterExpense.updateKhoanChiList(chiList)

                // Hiển thị tổng chi tiêu
                tvChiTieu.text = String.format("%,.2f VNĐ", totalExpense)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải danh sách KhoanChi!", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun loadKhoanChiByDate(userId: String, date: String) {
        val khoanChiRef = FirebaseDatabase.getInstance().getReference("KhoanChi")

        khoanChiRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chiList = mutableListOf<KhoanChi>()
                var totalExpense = 0.0

                for (dataSnapshot in snapshot.children) {
                    val khoanChi = dataSnapshot.getValue(KhoanChi::class.java)
                    if (khoanChi != null) {
                        val storedDate = khoanChi.thoiDiemChi // Giả sử `ngay` lưu dưới dạng "19/11/2024"

                        if (isSameDate(storedDate, date)) { // Kiểm tra ngày có trùng không
                            chiList.add(khoanChi)
                            totalExpense += khoanChi.soTien?.toDouble() ?: 0.0
                        }
                    }
                }

                // Cập nhật danh sách và tổng chi tiêu
                expenseDailyAdapterExpense.updateKhoanChiList(chiList)
                tvChiTieu.text = String.format("%,.2f VNĐ", totalExpense)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải danh sách KhoanChi!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isSameDate(storedDate: String, selectedDate: String): Boolean {
        return try {
            val formatterFirebase = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formatterSelected = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val dateFromFirebase = formatterFirebase.parse(storedDate)
            val dateFromSelected = formatterSelected.parse(selectedDate)

            dateFromFirebase == dateFromSelected
        } catch (e: Exception) {
            Log.e("DateComparison", "Lỗi khi so sánh ngày: ${e.message}")
            false
        }
    }
}
