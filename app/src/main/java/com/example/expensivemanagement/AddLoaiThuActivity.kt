package com.example.expensivemanagement

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddLoaiThuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.diag_add_loaithu)

        // phần tử UI
        val btnThemLoaiThu = findViewById<Button>(R.id.btnThemLoaiThu)
        val btnHuyThemLoaiThu = findViewById<Button>(R.id.btnHuyThemLoaiThu)
        val edtNameLoaiThu = findViewById<TextInputEditText>(R.id.edtNameLoaiThu)

        // tìm TextInputLayout để hiển thị lỗi
        val labelEditTextLayout = findViewById<TextInputLayout>(R.id.labelEdtNameLoaiThu)

        edtNameLoaiThu.addTextChangedListener {
            if (it!!.count() > 0)
                labelEditTextLayout.error = null
        }

        // Xử lý sự kiện khi nhấn nút "Thêm"
        btnThemLoaiThu.setOnClickListener {
            val label: String = edtNameLoaiThu.text.toString()

            // Reset lỗi trước khi kiểm tra
            labelEditTextLayout.error = null

            if (label.isEmpty())
                labelEditTextLayout.error = "Vui lòng nhập một tiêu đề hợp lệ"
            else
                Toast.makeText(this, "Đã thêm loại chi: $label", Toast.LENGTH_SHORT).show()
        }

        btnHuyThemLoaiThu.setOnClickListener {
            edtNameLoaiThu.setText("")
        }
    }
}
