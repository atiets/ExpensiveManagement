package com.example.expensivemanagement

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.expensivemanagement.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.EmailAuthProvider

class AccountActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    private lateinit var emailTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var changePasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        currentUser.let {
            // Log thông tin user
            Log.d("UserInfo", "User ID: ${it.uid}")
            Log.d("UserInfo", "User Email: ${it.email}")
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("user")

        emailTextView = findViewById(R.id.emailTextView)
        nameEditText = findViewById(R.id.nameEditText)
        updateButton = findViewById(R.id.updateButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        emailTextView.setText(currentUser.email)
        loadUserData()
        updateButton.setOnClickListener {
            val newName = nameEditText.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateUserProfile(newName)
            } else {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show()
            }
        }

        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadUserData() {
        val userId = currentUser.uid
        databaseReference.child(userId).get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            Log.d("UserInfo", "Snapshot: ${snapshot.value}")
            if (user != null) {
                nameEditText.setText(user.name)
            } else {
                Toast.makeText(this, "Không tìm thấy người dùng trong database", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserProfile(newName: String) {
        val userId = currentUser.uid

        // Cập nhật tên trong Firebase Authentication
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        currentUser.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updatedUser = User(
                    userId = userId,
                    email = currentUser.email ?: "",
                    role = "user",
                    name = newName
                )
                databaseReference.child(userId).setValue(updatedUser).addOnSuccessListener {
                    Toast.makeText(this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Lỗi khi lưu thông tin vào Database", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Cập nhật thông tin thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        dialogBuilder.setView(dialogView)

        val currentPasswordEditText = dialogView.findViewById<EditText>(R.id.currentPasswordEditText)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
        val confirmPasswordEditText = dialogView.findViewById<EditText>(R.id.confirmPasswordEditText)
        val changePasswordButton = dialogView.findViewById<Button>(R.id.changePasswordButton)

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        changePasswordButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Tất cả các trường đều phải được điền", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra mật khẩu hiện tại
            val user = FirebaseAuth.getInstance().currentUser
            val credential = EmailAuthProvider.getCredential(user!!.email!!, currentPassword)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Mật khẩu hiện tại đúng, cập nhật mật khẩu mới
                    user.updatePassword(newPassword).addOnCompleteListener { passwordUpdateTask ->
                        if (passwordUpdateTask.isSuccessful) {
                            Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                            alertDialog.dismiss()
                        } else {
                            Toast.makeText(this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Mật khẩu hiện tại không đúng
                    Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
