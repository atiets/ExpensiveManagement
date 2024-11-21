package com.example.expensivemanagement.fragment

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.expensivemanagement.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class ChartFragment : Fragment() {
    private lateinit var txtYear: TextView
    private var txtMonth: TextView? = null
    private var txtNoData: TextView? = null
    private lateinit var btnPrevYear: ImageView
    private lateinit var btnNextYear: ImageView
    private var currentYear = 0
    private var currentMonth = 0
    private var pieChart: PieChart? = null
    private var database: FirebaseDatabase? = null
    private var listChi: DatabaseReference? = null
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_chart, container, false)
        database = FirebaseDatabase.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            listChi = database?.getReference("KhoanChi")?.child(currentUser!!.uid)
            listChi?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Log.d("FirebaseData", "Dữ liệu KhoanChi: ${snapshot.value}")
                    } else {
                        Log.d("FirebaseData", "Không có dữ liệu KhoanChi.")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseData", "Lỗi truy vấn Firebase: ${error.message}")
                }
            })
        }
        txtYear = view.findViewById<TextView>(R.id.txt_year)
        txtMonth = view.findViewById<TextView>(R.id.txt_month)
        btnPrevYear = view.findViewById<ImageView>(R.id.btn_prev_year)
        btnNextYear = view.findViewById<ImageView>(R.id.btn_next_year)
        pieChart = view.findViewById<PieChart>(R.id.pie_chart)
        txtNoData = view.findViewById<TextView>(R.id.txt_no_data)

        // Lấy năm và tháng hiện tại
        val calendar = Calendar.getInstance()
        currentYear = calendar[Calendar.YEAR]
        currentMonth = calendar[Calendar.MONTH]

        // Hiển thị năm và tháng hiện tại
        updateYearAndMonth()
        txtYear.setOnClickListener {
            showMonthPickerDialog()
            updateYearAndMonth()
            drawPieChart()
        }

        btnPrevYear.setOnClickListener {
            currentYear--
            updateYearAndMonth()
            drawPieChart()
        }

        btnNextYear.setOnClickListener {
            currentYear++
            updateYearAndMonth()
            drawPieChart()
        }
        drawPieChart()
        return view
    }

    private fun updateYearAndMonth() {
        txtYear.text = currentYear.toString()
        txtMonth!!.text = getMonthName(currentMonth)
    }

    private fun showMonthPickerDialog() {
        val dateSetListener =
            OnDateSetListener { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                currentYear = year
                currentMonth = month
                updateYearAndMonth()
                drawPieChart()
            }
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            dateSetListener,
            currentYear,
            currentMonth,
            1
        )
        datePickerDialog.show()
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
            "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        )
        return monthNames[month]
    }

    private fun drawPieChart() {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val startDate = sdf.format(calendar.time)

        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.DATE, -1)
        val endDate = sdf.format(calendar.time)

        listChi?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("DateRange", "startDate: $startDate, endDate: $endDate")
                val entries: MutableList<PieEntry> = ArrayList()
                val colors: MutableList<Int> = ArrayList()
                var colorIndex = 0
                val categoryTotals = mutableMapOf<String, Double>()
                val categoryDetails = mutableMapOf<String, MutableList<Double>>()
                for (data in snapshot.children) {
                    val date = data.child("thoiDiemChi").getValue(String::class.java)
                    val loaiChi = data.child("loaiChi").getValue(String::class.java)
                    val soTien = data.child("soTien").getValue(Double::class.java)
                    Log.d("KhoanChi", "date: $date, loaiChi: $loaiChi, soTien: $soTien")
                    if (date != null && loaiChi != null && soTien != null) {
                        // Parse thoiDiemChi thành Date để so sánh với startDate và endDate
                        try {
                            val thoiDiemChiDate = sdf.parse(date)
                            val startDateObj = sdf.parse(startDate)
                            val endDateObj = sdf.parse(endDate)

                            // Kiểm tra nếu thoiDiemChi nằm trong khoảng thời gian startDate và endDate
                            if (thoiDiemChiDate != null && thoiDiemChiDate.after(startDateObj) && thoiDiemChiDate.before(endDateObj)) {
                                categoryTotals[loaiChi] = categoryTotals.getOrDefault(loaiChi, 0.0) + soTien
                                categoryDetails.getOrPut(loaiChi) { mutableListOf() }.add(soTien)
                            }
                        } catch (e: ParseException) {
                            Log.e("ParseError", "Lỗi khi phân tích ngày tháng: ${e.message}")
                        }
                    }
                }
                Log.d("CategoryTotals", "Tổng tiền theo từng loại chi: $categoryTotals")

                // Log chi tiết khoản chi trong mỗi loại
                Log.d("CategoryDetails", "Chi tiết khoản chi trong từng loại: $categoryDetails")
                if (categoryTotals.isNotEmpty()) {
                    for ((loaiChi, totalAmount) in categoryTotals) {
                        entries.add(PieEntry(totalAmount.toFloat(), loaiChi))
                        colors.add(ColorTemplate.MATERIAL_COLORS[colorIndex % ColorTemplate.MATERIAL_COLORS.size])
                        colorIndex++
                    }
                    val dataSet = PieDataSet(entries, "Biểu đồ chi tiêu tháng ${getMonthName(currentMonth)} $currentYear")
                    dataSet.colors = colors
                    val data = PieData(dataSet)
                    pieChart?.data = data
                    val description = Description()
                    description.text = "Biểu đồ chi tiêu tháng ${getMonthName(currentMonth)} $currentYear"
                    pieChart?.description = description
                    pieChart?.isDrawHoleEnabled = false
                    pieChart?.setEntryLabelTextSize(14f)
                    pieChart?.animateY(1400)
                    pieChart?.invalidate()
                    pieChart?.visibility = View.VISIBLE
                    txtNoData?.visibility = View.GONE
                } else {
                    pieChart?.visibility = View.GONE
                    txtNoData?.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChartFragment", "Lỗi khi truy vấn Firebase: ${error.message}")
                pieChart?.visibility = View.GONE
                txtNoData?.visibility = View.VISIBLE
            }
        })
    }
}
