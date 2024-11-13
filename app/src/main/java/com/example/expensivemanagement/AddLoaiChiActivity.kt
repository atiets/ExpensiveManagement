package com.example.expensivemanagement

import android.content.Context
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
import com.example.expensivemanagement.Data.FirebaseDB
import com.example.expensivemanagement.Model.LoaiChi
import android.view.inputmethod.InputMethodManager


class AddLoaiChiActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.diag_add_loachi)

        // phần tử UI
        val btnThemLoaiChi = findViewById<Button>(R.id.btnThemLoaiChi)
        val btnHuyThemLoaiChi = findViewById<Button>(R.id.btnHuyThemLoaiChi)
        val edtNameLoaiChi = findViewById<EditText>(R.id.edtNameLoaiChi)

        // Đặt focus cho edtNameLoaiChi
        edtNameLoaiChi.requestFocus()

        // Dùng post để chắc chắn bàn phím xuất hiện khi view đã vẽ xong
        edtNameLoaiChi.post {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(edtNameLoaiChi, InputMethodManager.SHOW_IMPLICIT)
        }

        // Xử lý sự kiện khi người dùng nhấn vào edtNameLoaiChi
        edtNameLoaiChi.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Đảm bảo bàn phím sẽ hiển thị khi người dùng click vào edtNameLoaiChi
                edtNameLoaiChi.post {
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(edtNameLoaiChi, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        // Khi người dùng nhập liệu vào edtNameLoaiChi
        edtNameLoaiChi.addTextChangedListener {
            // Nếu có nội dung, xoá lỗi
            if (it!!.count() > 0)
                edtNameLoaiChi.error = null
        }

        // Xử lý sự kiện khi nhấn nút "Thêm"
        btnThemLoaiChi.setOnClickListener {
            val label : String = edtNameLoaiChi.text.toString().trim()

            // Reset lỗi trước khi kiểm tra
            edtNameLoaiChi.error = null

            if (label.isEmpty())
                edtNameLoaiChi.error = "Vui lòng nhập một tiêu đề hợp lệ"
            else {
                // Tạo đối tượng LoaiChi, thêm vào Firebase
                val loaiChi = LoaiChi(null, nameLoaiChi = label)

                FirebaseDB().addLoaiChi(loaiChi)

                Toast.makeText(this, "Đã thêm loại chi: $label", Toast.LENGTH_SHORT).show()
            }
        }

        btnHuyThemLoaiChi.setOnClickListener {
            edtNameLoaiChi.setText("")
        }
    }
}