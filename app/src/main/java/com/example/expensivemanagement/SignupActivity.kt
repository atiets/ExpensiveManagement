package com.example.expensivemanagement

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.expensivemanagement.Data.HelperClass
import com.example.expensivemanagement.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var signupName: EditText
    private lateinit var signupUsername: EditText
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var loginRedirectText: TextView
    private lateinit var signupButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signupEmail = findViewById(R.id.editTextEmail)
        signupUsername = findViewById(R.id.editTextUsername)
        signupPassword = findViewById(R.id.editTextPassword)
        loginRedirectText = findViewById(R.id.tv_signup)
        signupButton = findViewById(R.id.bt_signup)

        signupButton.setOnClickListener {
            database = FirebaseDatabase.getInstance()
            reference = database.getReference("users")

            val email = signupEmail.text.toString()
            val username = signupUsername.text.toString()
            val password = signupPassword.text.toString()

            val helperClass = HelperClass(email, username, password)
            reference.child(username).setValue(helperClass)

            Toast.makeText(this, "You have signup successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        loginRedirectText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}