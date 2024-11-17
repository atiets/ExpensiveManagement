package com.example.expensivemanagement.model

data class KhoanChi(
    var id: String = "",
    var name: String = "",
    var loaiChi: String = "",
    var thoiDiemChi: String = "",
    var soTien: Int = 0,
    var danhGia: Int = 0,
    var deleteFlag: Int = 0,
    var idLoaiChi: String = ""
)