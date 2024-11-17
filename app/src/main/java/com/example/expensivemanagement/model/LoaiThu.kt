package com.example.expensivemanagement.model

data class LoaiThu(
    var id: String = "",
    var name: String = "",
    var userId: String = ""
) {
    fun getNameLoaiThu(): String {
        return name
    }

    fun getIdLoaiThu(): String{
        return id
    }
}