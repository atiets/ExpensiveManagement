package com.example.expensivemanagement

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddLoaiChiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.diag_add_loachi)

        // phần tử UI
        val btnThemLoaiChi = findViewById<Button>(R.id.btnThemLoaiChi)
        val btnHuyThemLoaiChi = findViewById<Button>(R.id.btnHuyThemLoaiChi)
        val edtNameLoaiChi = findViewById<TextInputEditText>(R.id.edtNameLoaiChi)

        // tìm TextInputLayout để hiển thị lỗi
        val labelEditTextLayout = findViewById<TextInputLayout>(R.id.labelEdtNameLoaiChi)

        edtNameLoaiChi.addTextChangedListener {
            if (it!!.count() > 0)
                labelEditTextLayout.error = null
        }

        // Xử lý sự kiện khi nhấn nút "Thêm"
        btnThemLoaiChi.setOnClickListener {
            val label : String = edtNameLoaiChi.text.toString()

            // Reset lỗi trước khi kiểm tra
            labelEditTextLayout.error = null

            if (label.isEmpty())
                labelEditTextLayout.error = "Vui lòng nhập một tiêu đề hợp lệ"
            else
                Toast.makeText(this, "Đã thêm loại chi: $label", Toast.LENGTH_SHORT).show()
        }

        btnHuyThemLoaiChi.setOnClickListener {
            edtNameLoaiChi.setText("")
        }
    }
}