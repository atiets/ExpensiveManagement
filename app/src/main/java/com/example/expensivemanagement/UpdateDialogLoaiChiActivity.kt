package com.example.expensivemanagement

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.expensivemanagement.Data.FirebaseDB
import com.example.expensivemanagement.Model.LoaiChi



class UpdateDialogLoaiChiActivity : ComponentActivity() {

    private var loaiChiList = mutableListOf<String>()
    private var currentLoaiChiId : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.diag_update_loaichi)

        // phần tử UI
        val btnCNLoaiChi = findViewById<Button>(R.id.btnCNLoaiChi)
        val btnHuyCNLoaiChi = findViewById<Button>(R.id.btnHuyCNLoaiChi)
        val edtNameLoaiChi = findViewById<EditText>(R.id.edtNameLoaiChi)

        // khởi tạo FirebaseDB
        val firebaseDB = FirebaseDB()

        // Lấy thông tin từ Intent nếu có
        val loaiChiName = intent.getStringExtra("loaiChiName")
        currentLoaiChiId = intent.getStringExtra("loaiChiId")

        // Nếu có tên loại chi thì hiển thị nó trong EditText
        if (loaiChiName != null && currentLoaiChiId != null) {
            edtNameLoaiChi.setText(loaiChiName)
        }

        btnCNLoaiChi.setOnClickListener {
            val updatedNameLoaiChi = edtNameLoaiChi.text.toString().trim()

            if (updatedNameLoaiChi.isNotEmpty() && currentLoaiChiId != null) {
                // Tạo đối tượng LoaiChi mới với tên mới
                val updatedLoaiChi = LoaiChi(currentLoaiChiId, updatedNameLoaiChi)

                // Cập nhật loại chi trong Firebase
                firebaseDB.updateLoaiChi(updatedLoaiChi)

                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()

                finish()  // Kết thúc Activity sau khi cập nhật
            } else {
                Toast.makeText(this, "Tên loại chi không được để trống!", Toast.LENGTH_SHORT).show()
            }
        }

        btnHuyCNLoaiChi.setOnClickListener {
            finish()        // đóng activity khi nhấn Huỷ
        }
    }

    // Hàm này được gọi để truyền dữ liệu từ Activity cha
    fun updateLoaiChiList(loaiChiList: MutableList<String>) {
        this.loaiChiList = loaiChiList
    }
}

