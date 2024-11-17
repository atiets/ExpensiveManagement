package com.example.expensivemanagement.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.adapter.LoaiThuAdapter // Thay LoaiChiAdapter thành LoaiThuAdapter
import com.example.expensivemanagement.model.LoaiThu // Thay LoaiChi thành LoaiThu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoaiThuFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var loaiThuList: MutableList<LoaiThu>
    private lateinit var adapter: LoaiThuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loai_thu, container, false)

        database = FirebaseDatabase.getInstance().getReference("LoaiThu")
        loaiThuList = mutableListOf()

        val rvLoaiThu = view.findViewById<RecyclerView>(R.id.recyclerViewLoaiThu)
        rvLoaiThu.layoutManager = LinearLayoutManager(requireContext())

        adapter = LoaiThuAdapter(loaiThuList, onEdit = { loaiThu ->
            editLoaiThu(loaiThu)
        }, onDelete = { id ->
            deleteLoaiThu(id)
        })
        rvLoaiThu.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.btnThemLoaiThu).setOnClickListener {
            addLoaiThu()
        }

        loadLoaiThu()

        return view
    }

    private fun loadLoaiThu() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy dữ liệu chỉ cho người dùng hiện tại
        database.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loaiThuList.clear()
                if (snapshot.exists()) {
                    for (loaiThuSnapshot in snapshot.children) {
                        val loaiThu = loaiThuSnapshot.getValue(LoaiThu::class.java)
                        Log.d("LoaiThuFragment", "Loaded LoaiThu: $loaiThu")
                        loaiThu?.let { loaiThuList.add(it) }
                    }
                    Log.d("LoaiThuFragment", "Data loaded, items count: ${loaiThuList.size}")
                } else {
                    Log.d("LoaiThuFragment", "No data found")
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Kiểm tra xem Fragment có còn đính kèm vào Activity không
                if (isAdded) {
                    try {
                        // Chỉ thực hiện các thao tác nếu Fragment còn đính kèm
                        val context = requireContext()
                        // Thực hiện các thao tác khác với context, ví dụ: hiển thị thông báo lỗi
                        Toast.makeText(context, "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
                        Log.e("LoaiThuFragment", "Error loading data: ${error.message}")
                    } catch (e: Exception) {
                        // In lỗi nếu có bất kỳ ngoại lệ nào xảy ra trong quá trình xử lý
                        e.printStackTrace()
                    }
                } else {
                    // Fragment không còn đính kèm, không thực hiện thao tác
                    Log.e("LoaiThuFragment", "Fragment not attached to activity.")
                }
            }
        })
    }

    private fun addLoaiThu() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_add_loaithu, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        val edtNameLoaiThu = dialogView.findViewById<EditText>(R.id.edtNameLoaiThu)
        val btnThemLoaiThu = dialogView.findViewById<Button>(R.id.btnThemLoaiThu)
        val btnHuyThemLoaiThu = dialogView.findViewById<Button>(R.id.btnHuyThemLoaiThu)

        btnThemLoaiThu.setOnClickListener {
            val name = edtNameLoaiThu.text.toString().trim()
            if (name.isNotEmpty()) {
                // Lấy UID từ FirebaseAuth
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid == null) {
                    Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                    return@setOnClickListener
                }

                // Truy vấn bảng "User" để lấy userId
                val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)
                userRef.get().addOnSuccessListener { snapshot ->
                    val userId = snapshot.child("userId").getValue(String::class.java)
                    if (userId.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), "Không tìm thấy userId!", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                        return@addOnSuccessListener
                    }

                    // Tạo ID cho LoaiThu
                    val id = database.push().key ?: return@addOnSuccessListener

                    val newLoaiThu = LoaiThu(id, name, userId)
                    Log.d("AddLoaiThu", "Saving new LoaiThu: $newLoaiThu")

                    // Lưu LoaiThu vào cơ sở dữ liệu
                    database.child(userId).child(id).setValue(newLoaiThu).addOnSuccessListener {
                        Log.d("AddLoaiThu", "LoaiThu added successfully with ID: $id")
                        Toast.makeText(requireContext(), "Thêm loại thu thành công!", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()

                        // Gọi lại phương thức tải lại dữ liệu sau khi thêm
                        loadLoaiThu()  // Phương thức này cần được định nghĩa để tải lại dữ liệu từ Firebase
                    }.addOnFailureListener { exception ->
                        Log.e("AddLoaiThu", "Failed to add LoaiThu: ${exception.message}")
                        Toast.makeText(requireContext(), "Thêm loại thu thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
            } else {
                edtNameLoaiThu.error = "Tên loại thu không được để trống!"
            }
        }

        btnHuyThemLoaiThu.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun editLoaiThu(loaiThu: LoaiThu) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_update_loaithu, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        val edtUpdateNameLoaiThu = dialogView.findViewById<EditText>(R.id.edtUpdateNameLoaiThu)
        val btnUpdateLoaiThu = dialogView.findViewById<Button>(R.id.btnUpdateLoaiThu)
        val btnHuyUpdateLoaiThu = dialogView.findViewById<Button>(R.id.btnHuyUpdateLoaiThu)

        // Set the current name to the EditText
        edtUpdateNameLoaiThu.setText(loaiThu.name)

        btnUpdateLoaiThu.setOnClickListener {
            val updatedName = edtUpdateNameLoaiThu.text.toString().trim()
            if (updatedName.isNotEmpty()) {
                val updatedLoaiThu = loaiThu.copy(name = updatedName)

                // Ensure we access by userId
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val userRef = FirebaseDatabase.getInstance().getReference("LoaiThu").child(uid)
                    userRef.child(loaiThu.id).setValue(updatedLoaiThu).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Cập nhật loại thu thành công!", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Cập nhật loại thu thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                edtUpdateNameLoaiThu.error = "Tên loại thu không được để trống!"
            }
        }

        btnHuyUpdateLoaiThu.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun deleteLoaiThu(id: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            // Ensure we are accessing the right user node
            val userRef = FirebaseDatabase.getInstance().getReference("LoaiThu").child(uid)
            userRef.child(id).removeValue().addOnSuccessListener {
                Toast.makeText(requireContext(), "Xóa loại thu thành công!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Xóa loại thu thất bại!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}