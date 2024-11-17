package com.example.expensivemanagement

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signupRedirectText: TextView
    private lateinit var forgotPasswordRedirectText: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Khởi tạo các view
        loginEmail = findViewById(R.id.editTextEmail)
        loginPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.bt_login)
        signupRedirectText = findViewById(R.id.tv_signup)
        forgotPasswordRedirectText = findViewById(R.id.tv_forgotPassword)
        val passwordToggle: ImageView = findViewById(R.id.iv_toggle_password)
        auth = FirebaseAuth.getInstance()

        var isPasswordVisible = false

        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                // Hiển thị mật khẩu
                loginPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.visibility_on) // Cập nhật biểu tượng
            } else {
                // Ẩn mật khẩu
                loginPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.visibility_off) // Cập nhật biểu tượng
            }
            // Đảm bảo con trỏ không bị mất vị trí khi chuyển đổi
            loginPassword.setSelection(loginPassword.text?.length ?: 0)
        }

        loginButton.setOnClickListener {
            if (validateEmail() && validatePassword()) {
                loginUser()
            }
        }

        signupRedirectText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        forgotPasswordRedirectText.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Nếu người dùng đã đăng nhập, chuyển đến MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Không cho phép quay lại LoginActivity
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.diag_forgot, null)
        val emailBox = dialogView.findViewById<EditText>(R.id.editTextEmail)

        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.findViewById<Button>(R.id.buttonSendCode).setOnClickListener {
            val userEmail = emailBox.text.toString().trim()
            if (userEmail.isEmpty() || !isValidEmail(userEmail)) {
                Toast.makeText(this, "Nhập địa chỉ email hợp lệ", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Kiểm tra email để đặt lại mật khẩu", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, task.exception?.message ?: "Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialogView.findViewById<Button>(R.id.buttonCancel).setOnClickListener { dialog.dismiss() }
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.show()
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailPattern.matcher(email).matches()
    }

    private fun validateEmail(): Boolean {
        val email = loginEmail.text.toString().trim()
        return if (email.isEmpty()) {
            loginEmail.error = "Email không được để trống"
            false
        } else {
            loginEmail.error = null
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = loginPassword.text.toString().trim()
        return when {
            password.isEmpty() -> {
                loginPassword.error = "Mật khẩu không được để trống"
                false
            }
            password.length < 6 -> {
                loginPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
                false
            }
            else -> {
                loginPassword.error = null
                true
            }
        }
    }

    private fun loginUser() {
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, task.exception?.message ?: "Đăng nhập thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}