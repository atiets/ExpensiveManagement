package com.example.expensivemanagement.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
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
    private lateinit var expenseDailyAdapterIncome: IncomeDailyAdapter
    private lateinit var expenseDailyAdapterExpense: ExpenseDailyAdapter

    private lateinit var tvThuNhap: TextView
    private lateinit var tvChiTieu: TextView
    private lateinit var tvTong: TextView

    private val khoanThuIncomeList = mutableListOf<KhoanThu>()
    private val khoanChiExpenseList = mutableListOf<KhoanChi>()

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private var lastSelectedDate: String? = null

    private var totalExpenseValue = 0.0
    private var totalIncomeValue = 0.0
    private var TotalValue = 0.0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        recyclerViewIncome = view.findViewById(R.id.recyclerViewIncome)
        recyclerViewExpense = view.findViewById(R.id.recyclerViewExpense)

        tvThuNhap = view.findViewById(R.id.tv_ThuNhap)
        tvChiTieu = view.findViewById(R.id.tv_ChiTieu)
        tvTong = view.findViewById(R.id.tv_Tong)

        expenseDailyAdapterExpense = ExpenseDailyAdapter(khoanChiExpenseList)
        expenseDailyAdapterIncome = IncomeDailyAdapter(khoanThuIncomeList)

        recyclerViewIncome.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewIncome.adapter = expenseDailyAdapterIncome

        recyclerViewExpense.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewExpense.adapter = expenseDailyAdapterExpense

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        val currentDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        loadKhoanChiByDate(currentUser.uid, currentDate)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            if (selectedDate != lastSelectedDate) {
                lastSelectedDate = selectedDate

                loadKhoanChiByDate(currentUser.uid, selectedDate)
                loadKhoanThuByDate(currentUser.uid, selectedDate)

                updateTotal(totalExpenseValue, totalIncomeValue)
            }
        }
        tvThuNhap.setOnClickListener {
            recyclerViewIncome.visibility = View.VISIBLE
            recyclerViewExpense.visibility = View.GONE
            loadKhoanThuByDate(currentUser.uid, lastSelectedDate ?: "")
        }

        tvChiTieu.setOnClickListener {
            recyclerViewIncome.visibility = View.GONE
            recyclerViewExpense.visibility = View.VISIBLE
            loadKhoanChiByDate(currentUser.uid, lastSelectedDate ?: "")
        }
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
                        val storedDate = khoanChi.thoiDiemChi
                        if (isSameDate(storedDate, date)) {
                            chiList.add(khoanChi)
                            totalExpense += khoanChi.soTien
                        }
                    }
                }

                expenseDailyAdapterExpense.updateKhoanChiList(chiList)
                totalExpenseValue = totalExpense
                updateTotal(totalExpenseValue, totalIncomeValue)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải danh sách KhoanChi!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadKhoanThuByDate(userId: String, date: String) {
        val khoanThuRef = FirebaseDatabase.getInstance().getReference("KhoanThu")

        khoanThuRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val thuList = mutableListOf<KhoanThu>()
                var totalIncome = 0.0

                for (dataSnapshot in snapshot.children) {
                    val khoanThu = dataSnapshot.getValue(KhoanThu::class.java)
                    if (khoanThu != null) {
                        val storedDate = khoanThu.thoiDiemThu
                        if (isSameDate(storedDate, date)) {
                            thuList.add(khoanThu)
                            totalIncome += khoanThu.soTien
                        }
                    }
                }
                expenseDailyAdapterIncome.updateKhoanThuList(thuList)
                totalIncomeValue = totalIncome
                updateTotal(totalExpenseValue, totalIncomeValue)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi tải danh sách KhoanThu!", Toast.LENGTH_SHORT).show()
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

    private fun updateTotal(totalExpense: Double, totalIncome: Double) {
        val netTotal = totalIncome - totalExpense

        // Cập nhật tổng chi tiêu
        tvChiTieu.text = formatMoney(totalExpense.toInt())

        // Cập nhật tổng thu nhập
        tvThuNhap.text = formatMoney(totalIncome.toInt())

        // Cập nhật tổng thu nhập sau khi trừ chi tiêu
        tvTong.text = formatMoney(netTotal.toInt())
    }

    private fun formatMoney(amount: Int): String {
        return if (amount >= 1_000_000) {
            val moneyInMillions = amount / 1_000_000.0
            val formatted = String.format("%,.2f", moneyInMillions)
            "$formatted Tr VNĐ"
        } else {
            String.format("%,.0f VNĐ", amount.toDouble()).replace(',', '.')
        }
    }

}
