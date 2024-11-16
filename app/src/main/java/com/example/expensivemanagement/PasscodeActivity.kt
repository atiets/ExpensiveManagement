package com.example.expensivemanagement

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasscodeActivity : AppCompatActivity() {

    private lateinit var etOldPin: EditText
    private lateinit var etNewPin: EditText
    private lateinit var etConfirmNewPin: EditText
    private lateinit var btnSavePin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passcode)

        // Initialize views
        etOldPin = findViewById(R.id.etOldPin)
        etNewPin = findViewById(R.id.etNewPin)
        etConfirmNewPin = findViewById(R.id.etConfirmNewPin)
        btnSavePin = findViewById(R.id.btnSavePin)

        // Check if PIN already exists
        if (getSavedPin() == null) {
            // No PIN saved, allow user to set a new PIN
            etOldPin.visibility = EditText.GONE  // Hide old PIN field
            etNewPin.hint = "Enter new PIN"
            etConfirmNewPin.hint = "Confirm new PIN"
        } else {
            // PIN exists, require the user to enter old PIN to change it
            etNewPin.hint = "Enter new PIN"
            etConfirmNewPin.hint = "Confirm new PIN"
        }

        btnSavePin.setOnClickListener {
            val oldPin = etOldPin.text.toString()
            val newPin = etNewPin.text.toString()
            val confirmNewPin = etConfirmNewPin.text.toString()

            // Validate input
            if (newPin.isEmpty() || confirmNewPin.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (getSavedPin() != null) {
                // If PIN exists, validate old PIN
                if (oldPin.isEmpty()) {
                    Toast.makeText(this, "Please enter your old PIN", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check if the old PIN is correct
                if (oldPin != getSavedPin()) {
                    Toast.makeText(this, "Old PIN is incorrect", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Check if new PINs match
            if (newPin != confirmNewPin) {
                Toast.makeText(this, "New PINs do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save the new PIN
            savePin(newPin)

            Toast.makeText(this, "PIN saved successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun savePin(pin: String) {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_pin", pin)
        editor.apply()
    }

    private fun getSavedPin(): String? {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_pin", null)
    }
}
