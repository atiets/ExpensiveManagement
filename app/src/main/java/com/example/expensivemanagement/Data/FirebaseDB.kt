package com.example.expensivemanagement.Data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import com.example.expensivemanagement.Model.*

class FirebaseDB {

    private lateinit var database: DatabaseReference

    // ...
    fun initializeDatabase() {
        if (!::database.isInitialized) {
            database = FirebaseDatabase.getInstance().reference
        }
    }

    // Thêm LoaiThu vào Firebase
    fun addLoaiThu(loaiThu: LoaiThu) {
        val loaiThuRef = database.child("loaiThu")

        // Thêm đối tượng LoaiThu vào Firebase với ID tự động
        val newId = loaiThuRef.push().key       // tạo ID tự động từ Firebase
        if (newId != null) {
            try {
                loaiThu.id = newId
                loaiThuRef.child(newId).setValue(loaiThu)       // lưu đối tượng LoaiThu vào Firebase
            } catch (e: NumberFormatException) {
                println("Lỗi: Không thể chuyển đổi ID thành Int: ${e.message}")
            }
        }
    }

    // Đọc danh sách LoaiThu từ Firebase
    fun getAllLoaiThu() {
        if (!::database.isInitialized) {
            println("Database chưa được khởi tạo!")
            return
        }
        val loaiThuRef = database.child("loaiThu")

        // Đọc dữ liệu từ Firebase
        loaiThuRef.addValueEventListener(object : ValueEventListener {
            //Khi dữ liệu được thay đổi
            override fun onDataChange(snapshot: DataSnapshot) {
                val loaiThuList = mutableListOf<LoaiThu>()
                for (dataSnapshot in snapshot.children) {
                    //Chuyển dữ liệu từ Firebase thành đối tượng LoaiThu
                    val loaiThu = dataSnapshot.getValue(LoaiThu::class.java)        // Chuyển dữ liệu từ Firebase thành đối tượng LoaiThu
                    if (loaiThu != null) {
                        loaiThuList.add(loaiThu)
                    }
                }

                // hiển thị danh sách loaiThu
                println("Dữ liệu: ${loaiThuList}")
            }

            // Khi có lỗi xảy ra trong quá trình đọc dữ liệu
            override fun onCancelled(error : DatabaseError) {
                println("Error: ${error.message}")
            }
        })
    }

    // Cập nhật LoaiThu (theo ID) trong Firebase
    fun updateLoaiThu(loaiThu: LoaiThu) {
        val loaiThuRef = database.child("loaiThu")

        // sử dụng ID để tìm đối tượng LoaiThu trong Firebase và cập nhật nó
        val idString = loaiThu.id.toString()
        loaiThuRef.child(idString).setValue(loaiThu)   // cập nhật LoaiThu với ID tương ứng
    }

    // Xoá LoaiThu theo ID
    fun deleteLoaiThu(id: String) {
        val loaiThuRef = database.child("loaiThu")

        // xoá đối tượng LoaiThu theo ID
        loaiThuRef.child(id).removeValue()   // xoá đối tượng có ID tương ứng
    }

    // Thêm LoaiChi vào Firebase
    fun addLoaiChi(loaiChi: LoaiChi) {
        val loaiChiRef = database.child("loaiChi")

        // tạo ID tự động từ Firebase
        val newId = loaiChiRef.push().key
        if (newId != null) {
            loaiChi.id = newId

            // lưu đối tượng LoaiChi vào Firebase
            loaiChiRef.child(newId).setValue(loaiChi).addOnCompleteListener {
                task ->
                if (task.isSuccessful) {
                    println("Thêm LoaiChi thành công")
                }
                else {
                    println("Lỗi khi thêm LoaiChi: ${task.exception?.message}")
                }
            }
        }
    }


    // Đọc danh sách LoaiChi từ Firebase
    fun getAllLoaiChi() {
        if (!::database.isInitialized) {
            println("Database chưa được khởi tạo!")
            return
        }
        val loaiChiRef = database.child("loaiChi")

        // Đọc dữ liệu từ Firebase
        loaiChiRef.addValueEventListener(object : ValueEventListener {

            //Khi dữ liệu được thay đổi
            override fun onDataChange(snapshot: DataSnapshot) {
                val loaiChiList = mutableListOf<LoaiChi>()

                for (dataSnapshot in snapshot.children) {
                    //Chuyển dữ liệu từ Firebase thành đối tượng LoaiThu
                    val loaiChi = dataSnapshot.getValue(LoaiChi::class.java)        // Chuyển dữ liệu từ Firebase thành đối tượng LoaiThu
                    if (loaiChi != null) {
                        loaiChiList.add(loaiChi)
                    } else {
                        // Log cảnh báo nếu có dữ liệu không hợp lệ
                        println("Cảnh báo: dữ liệu không hợp lệ tại ${dataSnapshot.key}")
                    }
                }
                // Hiển thị danh sách LoaiChi
                println("Dữ liệu: ${loaiChiList.joinToString(separator = "\n")}")
            }

            // Khi có lỗi xảy ra trong quá trình đọc dữ liệu
            override fun onCancelled(error : DatabaseError) {
                println("Error: ${error.message}")
            }
        })
    }

    // Cập nhật LoaiChi trong Firebase
    fun updateLoaiChi(loaiChi: LoaiChi) {
        val loaiChiRef = database.child("loaiChi")

        // sử dụng ID để tìm đối tượng LoaiThu trong Firebase và cập nhật nó
        loaiChi.id?.let {
            loaiChiRef.child(it).setValue(loaiChi)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Cập nhật LoaiChi thành công")
                    } else {
                        println("Lỗi khi cập nhật LoaiChi: ${task.exception?.message}")
                    }
                }
            val idString = it.toString()
        }
    }

    // Xoá LoaiChi theo ID
    fun deleteLoaiChi(id: String) {
        val loaiChiRef = database.child("loaiChi")

        // xoá đối tượng LoaiChitheo ID
        loaiChiRef.child(id).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Xoá LoaiChi thành công")
                } else {
                    println("Lỗi khi xoá LoaiChi: ${task.exception?.message}")
                }
            }
    }

    // Thêm TaiChinh vào Firebase
    fun addTaiChinh(taiChinh: TaiChinh) {
        val taiChinhRef = database.child("taiChinh")

        // Tạo ID tự động từ Firebase
        val newId = taiChinhRef.push().key
        if (newId != null) {
            taiChinh.id = newId
            taiChinhRef.child(newId).setValue(taiChinh)    // Lưu đối tượng TaiChinh vào Firebase
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        println("Successful")
                    else
                        println("Fail")
                }
        }
    }

    // Đọc danh sách TaiChinh ngân sách từ Firebase
    fun readTaiChinh() {
        val taiChinhRef = database.child("taiChinh")

        // Đọc dữ liệu từ firebase
        taiChinhRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val taiChinhList = mutableListOf<TaiChinh>()
                for (dataSnapshot in snapshot.children) {
                    // Chuyển dữ liệu từ Firebase thành đối tượng TaiChinh
                    val taiChinh = dataSnapshot.getValue(TaiChinh::class.java)
                    if (taiChinh != null)
                        taiChinhList.add(taiChinh)
                }
                // hiển thị danh sách tài chính
                println("Dữ liệu ngân sách: $taiChinhList")
            }

            override fun onCancelled(error: DatabaseError) {
                println("Lỗi: ${error.message}")
            }

        })
    }

    // Cập nhật TaiChinh trong Firebase
    fun updateTaiChinh(taiChinh: TaiChinh) {
        val taiChinhRef = database.child("taiChinh")

        // Sử dụng ID để tìm đối tượng TaiChinh trong Firebase và cập nhật nó
        taiChinhRef.child(taiChinh.id).setValue(taiChinh)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Successful")
                } else {
                    println("Failed to update")
                }
            }
    }

    // Xoá TaiChinh theo ID
    fun deleteTaiChinh(id: String) {
        val taiChinhRef = database.child("taiChinh")

        // Xoá đối tượng TaiChinh theo ID
        taiChinhRef.child(id).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Xoá ngân sách thành công")
                } else {
                    println("Xoá ngân sách thất bại")
                }
            }
    }
}