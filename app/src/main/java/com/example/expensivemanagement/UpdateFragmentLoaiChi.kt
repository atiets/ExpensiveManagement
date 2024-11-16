package com.example.expensivemanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.expensivemanagement.Adapter.LoaiChiAdapter
import com.example.expensivemanagement.Model.LoaiChi
import java.util.ArrayList

class UpdateFragmentLoaiChi : ComponentActivity() {

    private lateinit var listViewLoaiChi: ListView
    private lateinit var btnThemLoaiChi: Button
    private val loaiChiList = mutableListOf<LoaiChi>()
    private lateinit var adapter: LoaiChiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_loai_chi)

        listViewLoaiChi = findViewById(R.id.listViewLoaiChi)
        btnThemLoaiChi = findViewById(R.id.btnThemLoaiChi)

        // Khởi tạo adapter với context và danh sách loaiChiList
        adapter = LoaiChiAdapter(this, ArrayList(loaiChiList))
        listViewLoaiChi.adapter = adapter

        // Thêm loại chi mới
        btnThemLoaiChi.setOnClickListener {
            val intent = Intent(this, AddLoaiChiActivity::class.java)
            startActivity(intent)
        }

        // Xử lý sự kiện click vào từng item trong ListView
        listViewLoaiChi.setOnItemClickListener { parent, view, position, id ->
//            val loaiChi = loaiChiList[position]
//            showEditDeleteDialog(loaiChi, position)
        }
    }
}


