package com.example.expensivemanagement

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensivemanagement.Adapter.LoaiChiAdapter
import com.example.expensivemanagement.Model.LoaiChi
import com.example.expensivemanagement.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var loaiChis: ArrayList<LoaiChi>
    private lateinit var loaiChiAdapter: LoaiChiAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val addBtn = findViewById<FloatingActionButton>(R.id.addBtn)

        // Kết nối Activity với View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // khởi tạo danh sách loại chi
        loaiChis = arrayListOf(
            LoaiChi("Thực phẩm", "Cái gì nhai và uống được đều tính là thực phẩm, trừ đi cà phê cà pháo"),
            LoaiChi("Y tế", "Mua thuốc, khám bệnh, thực phẩm chức năng gom vô đây hết"),
            LoaiChi("Mua sắm", "Quần áo, giày dép, balo, túi xách, săn sale Shoppe tính luôn"),
            LoaiChi("Xã hội", "Cà phê, cà pháo, tiền mừng cưới hỏi, sinh nhật")
        )

        // khởi tạo adapter và layout manager cho RecyclerView
        loaiChiAdapter = LoaiChiAdapter(loaiChis)
        linearLayoutManager = LinearLayoutManager(this)

        // Gán adapter và layout manager cho RecyclerView
        binding.recyclerview.apply {
            adapter = loaiChiAdapter
            layoutManager = linearLayoutManager
        }

        addBtn.setOnClickListener {
            val intent = Intent(this, AddLoaiChiActivity::class.java)
            startActivity(intent)
        }
    }
}