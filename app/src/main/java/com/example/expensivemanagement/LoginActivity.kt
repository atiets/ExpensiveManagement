package com.example.expensivemanagement

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var loginUsername: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signupRedirectText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginUsername = findViewById(R.id.editTextUsername)
        loginPassword = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.bt_login)
        signupRedirectText = findViewById(R.id.tv_signup)

        loginButton.setOnClickListener {
            if (!validateUsername() || !validatePassword()) {
                // Không làm gì nếu có lỗi
            } else {
                checkUser()
            }
        }

        signupRedirectText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateUsername(): Boolean {
        val valUsername = loginUsername.text.toString()
        return if (valUsername.isEmpty()) {
            loginUsername.error = "Username cannot be empty"
            false
        } else {
            loginUsername.error = null
            true
        }
    }

    private fun validatePassword(): Boolean {
        val valPassword = loginPassword.text.toString()
        return if (valPassword.isEmpty()) {
            loginPassword.error = "Password cannot be empty"
            false
        } else if (valPassword.length < 6) { // Kiểm tra mật khẩu có ít nhất 6 ký tự
            loginPassword.error = "Password must be at least 6 characters long"
            false
        } else {
            loginPassword.error = null
            true
        }
    }

    private fun checkUser() {
        val userUsername = loginUsername.text.toString().trim()
        val userPassword = loginPassword.text.toString().trim()

        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        val checkUserDatabase: Query = reference.orderByChild("username").equalTo(userUsername)

        checkUserDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    loginUsername.error = null
                    val passwordFromDB = snapshot.child(userUsername).child("password").getValue(String::class.java)

                    if (passwordFromDB == userPassword) {
                        loginUsername.error = null

                        val emailFromDB = snapshot.child(userUsername).child("email").getValue(String::class.java)
                        val usernameFromDB = snapshot.child(userUsername).child("username").getValue(String::class.java)

                        // Chuyển sang MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                            putExtra("email", emailFromDB)
                            putExtra("username", usernameFromDB)
                            putExtra("password", passwordFromDB)
                        }

                        startActivity(intent)
                        finish() // Kết thúc LoginActivity sau khi chuyển sang MainActivity
                    } else {
                        loginPassword.error = "Invalid Credentials"
                        loginPassword.requestFocus()
                    }
                } else {
                    loginUsername.error = "User does not exist"
                    loginUsername.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })
    }
}
