package com.example.expensivemanagement.model

data class KhoanThu(
    var idThu: String = "",
    var tenThu: String = "",
    var loaiThu: String = "",
    var thoiDiemThu: String = "",
    var soTien: Int = 0,
    var danhGia: Int = 0,
    var deleteFlag: Int = 0,
    var idLoaiThu: String = ""
)