package com.example.expensivemanagement.model

data class LoaiChi(
    var id: String = "",
    var name: String = "",
    var userId: String = ""
) {
    // Trả về tên của LoaiChi
    fun getNameLoaiChi(): String {
        return name
    }

    // Trả về ID của LoaiChi
    fun getIdLoaiChi(): String {
        return id
    }
}
