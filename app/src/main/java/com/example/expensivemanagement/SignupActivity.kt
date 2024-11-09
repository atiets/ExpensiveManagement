package com.example.expensivemanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var signupButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Khởi tạo các view
        signupEmail = findViewById(R.id.editTextEmail)
        signupPassword = findViewById(R.id.editTextPassword)
        confirmPassword = findViewById(R.id.editTextConfirmPassword)
        signupButton = findViewById(R.id.bt_signup)
        auth = FirebaseAuth.getInstance()

        signupButton.setOnClickListener {
            if (validateEmail() && validatePassword() && validateConfirmPassword()) {
                registerUser()
            }
        }
    }

    private fun validateConfirmPassword(): Boolean {
        val password = signupPassword.text.toString().trim()
        val confirmPass = confirmPassword.text.toString().trim()

        return when {
            confirmPass.isEmpty() -> {
                confirmPassword.error = "Xác nhận mật khẩu không được để trống"
                false
            }
            confirmPass != password -> {
                confirmPassword.error = "Mật khẩu xác nhận không khớp"
                false
            }
            else -> {
                confirmPassword.error = null
                true
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return emailPattern.matcher(email).matches()
    }

    private fun validateEmail(): Boolean {
        val email = signupEmail.text.toString().trim()
        return if (email.isEmpty()) {
            signupEmail.error = "Email không được để trống"
            false
        } else {
            signupEmail.error = null
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = signupPassword.text.toString().trim()
        return when {
            password.isEmpty() -> {
                signupPassword.error = "Mật khẩu không được để trống"
                false
            }
            password.length < 6 -> {
                signupPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
                false
            }
            else -> {
                signupPassword.error = null
                true
            }
        }
    }

    private fun registerUser() {
        val email = signupEmail.text.toString().trim()
        val password = signupPassword.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, task.exception?.message ?: "Đăng ký thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}