package com.example.expensivemanagement.Fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensivemanagement.Adapter.LoaiChiAdapter
import com.example.expensivemanagement.Model.LoaiChi
import com.example.expensivemanagement.R

class LoaiChiFragment : Fragment() {

    private lateinit var listViewLoaiChi: ListView
    private var loaiChiList: ArrayList<LoaiChi> = ArrayList()
    private lateinit var btnThem: Button
    private lateinit var adapter: LoaiChiAdapter
    private var firebaseDB: FirebaseDB = FirebaseDB()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_loai_chi, container, false)

        // Lấy đối tượng ListView và Button bằng findViewById
        listViewLoaiChi = rootView.findViewById(R.id.listViewLoaiChi)
        btnThem = rootView.findViewById(R.id.btnThemLoaiChi)

        // Khởi tạo Adapter cho ListView
        adapter = LoaiChiAdapter(requireContext(), loaiChiList)
        listViewLoaiChi.adapter = adapter

        // Khởi tạo Firebase Database
        firebaseDB.initializeDatabase()

        // Lấy danh sách LoaiChi từ Firebase và cập nhật ListView
        firebaseDB.getAllLoaiChi()

        // Handle thêm loại chi
        btnThem.setOnClickListener {
            val intent = Intent(context, AddLoaiChiActivity::class.java)
            startActivity(intent)
        }

        return rootView
    }

    // Hàm để cập nhật ListView khi Firebase trả về dữ liệu
    private fun updateListView(loaiChis: List<LoaiChi>) {
        loaiChiList.clear()  // Xóa dữ liệu cũ trước khi thêm dữ liệu mới
        loaiChiList.addAll(loaiChis)  // Thêm dữ liệu mới vào danh sách
        adapter.notifyDataSetChanged()  // Cập nhật adapter với dữ liệu mới
    }
}