package com.example.expensivemanagement.Data

data class HelperClass(
    var email: String? = null,
    var username: String? = null,
    var password: String? = null


) {
    override fun toString(): String {
        return "HelperClass(email=$email, username=$username, password=$password)"
    }

    
}
