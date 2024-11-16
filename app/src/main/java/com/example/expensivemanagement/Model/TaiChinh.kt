package com.example.expensivemanagement.Model

class TaiChinh(
    var id: String,
    var ten: String,
    var soTien: Int,
    var moTa: String,       // mô tả: "Ngân sách ăn hàng"
    var ngayBatDau: Long,
    var ngayKetThuc: Long
) {
}