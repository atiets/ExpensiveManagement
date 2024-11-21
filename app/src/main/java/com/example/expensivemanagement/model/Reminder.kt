package com.example.expensivemanagement.model

data class Reminder(
    var id: String? = null,
    var name: String = "",
    var frequency: String = "",
    var date: String = "",
    var time: String = "",
    var note: String = "",
    var isActive: Boolean = true
)