package com.example.expensivemanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinAuthenticationActivity : AppCompatActivity() {
    private lateinit var pinInput: EditText
    private lateinit var pinSubmitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_authentication)

        pinInput = findViewById(R.id.pinInput)
        pinSubmitButton = findViewById(R.id.pinSubmitButton)

        pinSubmitButton.setOnClickListener {
            val enteredPin = pinInput.text.toString().trim()

            if (isPinCorrect(enteredPin)) {
                // Nếu mã PIN đúng, chuyển hướng đến màn hình chính
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Đóng màn hình nhập mã PIN
            } else {
                // Nếu mã PIN sai, hiển thị thông báo lỗi
                Toast.makeText(this, "Mã PIN không đúng. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Kiểm tra mã PIN người dùng nhập có đúng không
    private fun isPinCorrect(enteredPin: String): Boolean {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val savedPin = sharedPreferences.getString("user_pin", null)
        return enteredPin == savedPin
    }
}
