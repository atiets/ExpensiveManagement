package com.example.expensivemanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.expensivemanagement.Data.FirebaseDB
import com.example.expensivemanagement.Model.LoaiChi


class UpdateDeleteSingleItemLoaiChi: ComponentActivity() {
    // có 2 ImageView viết chì để update, cái thùng rác để giục

    private lateinit var firebaseDB: FirebaseDB
    private lateinit var loaiChi: LoaiChi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.list_loaichi_layout)

        // Khởi tạo đối tượng FirebaseDB
        firebaseDB = FirebaseDB()
        firebaseDB.initializeDatabase()

        // phần tử UI
        val textViewLoaiChi = findViewById<TextView>(R.id.textViewLoaiChi)
        val editButton = findViewById<ImageView>(R.id.imageViewEditLoaiChi)
        val deleteButton = findViewById<ImageView>(R.id.imageViewDeleteLoaiChi)

        // Lấy dữ liệu LoaiChi từ Intent
        loaiChi = intent.getStringExtra("nameLoaiChi") as LoaiChi

        // Hiển thị tên Loại Chi
        textViewLoaiChi.text = loaiChi.nameLoaiChi

        editButton.setOnClickListener {
            val intent = Intent(this, UpdateDialogLoaiChiActivity::class.java)
            //startActivity(intent)


        }

        deleteButton.setOnClickListener {
            // Xóa LoaiChi khỏi Firebase theo ID
            loaiChi.id?.let { id ->
                firebaseDB.deleteLoaiChi(id)
                Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show()
                finish()  // Sau khi xóa, quay lại Activity trước đó
            }
        }
    }
}

