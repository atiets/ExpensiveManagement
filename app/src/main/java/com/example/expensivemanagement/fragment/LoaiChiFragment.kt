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
import com.example.expensivemanagement.adapter.LoaiChiAdapter
import com.example.expensivemanagement.model.LoaiChi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LoaiChiFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var loaiChiList: MutableList<LoaiChi>
    private lateinit var adapter: LoaiChiAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loai_chi, container, false)

        database = FirebaseDatabase.getInstance().getReference("LoaiChi")
        loaiChiList = mutableListOf()

        val rvLoaiChi = view.findViewById<RecyclerView>(R.id.recyclerViewLoaiChi)
        rvLoaiChi.layoutManager = LinearLayoutManager(requireContext())

        adapter = LoaiChiAdapter(loaiChiList, onEdit = { loaiChi ->
            editLoaiChi( loaiChi)
        }, onDelete = { id ->
            deleteLoaiChi(id)
        })
        rvLoaiChi.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.btnThemLoaiChi).setOnClickListener {
            addLoaiChi()
        }

        loadLoaiChi()

        return view
    }

    private fun loadLoaiChi() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy dữ liệu chỉ cho người dùng hiện tại
        database.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loaiChiList.clear()
                if (snapshot.exists()) {
                    for (loaiChiSnapshot in snapshot.children) {
                        val loaiChi = loaiChiSnapshot.getValue(LoaiChi::class.java)
                        Log.d("LoaiChiFragment", "Loaded LoaiChi: $loaiChi")
                        loaiChi?.let { loaiChiList.add(it) }
                    }
                    Log.d("LoaiChiFragment", "Data loaded, items count: ${loaiChiList.size}")
                } else {
                    Log.d("LoaiChiFragment", "No data found")
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
                        Log.e("LoaiChiFragment", "Error loading data: ${error.message}")
                    } catch (e: Exception) {
                        // In lỗi nếu có bất kỳ ngoại lệ nào xảy ra trong quá trình xử lý
                        e.printStackTrace()
                    }
                } else {
                    // Fragment không còn đính kèm, không thực hiện thao tác
                    Log.e("LoaiChiFragment", "Fragment not attached to activity.")
                }
            }
        })
    }


    private fun addLoaiChi() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_add_loachi, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        val edtNameLoaiChi = dialogView.findViewById<EditText>(R.id.edtNameLoaiChi)
        val btnThemLoaiChi = dialogView.findViewById<Button>(R.id.btnThemLoaiChi)
        val btnHuyThemLoaiChi = dialogView.findViewById<Button>(R.id.btnHuyThemLoaiChi)

        btnThemLoaiChi.setOnClickListener {
            val name = edtNameLoaiChi.text.toString().trim()
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

                    // Tạo ID cho LoaiChi
                    val id = database.push().key ?: return@addOnSuccessListener

                    val newLoaiChi = LoaiChi(id, name, userId)
                    Log.d("AddLoaiChi", "Saving new LoaiChi: $newLoaiChi")

                    // Lưu LoaiChi vào cơ sở dữ liệu
                    database.child(userId).child(id).setValue(newLoaiChi).addOnSuccessListener {
                        Log.d("AddLoaiChi", "LoaiChi added successfully with ID: $id")
                        Toast.makeText(requireContext(), "Thêm loại chi thành công!", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()

                        // Gọi lại phương thức tải lại dữ liệu sau khi thêm
                        loadLoaiChi()  // Phương thức này cần được định nghĩa để tải lại dữ liệu từ Firebase
                    }.addOnFailureListener {  exception ->
                        Log.e("AddLoaiChi", "Failed to add LoaiChi: ${exception.message}")
                        Toast.makeText(requireContext(), "Thêm loại chi thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }
            } else {
                edtNameLoaiChi.error = "Tên loại chi không được để trống!"
            }
        }

        btnHuyThemLoaiChi.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun editLoaiChi(loaiChi: LoaiChi) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_update_loaichi, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        val edtUpdateNameLoaiChi = dialogView.findViewById<EditText>(R.id.edtUpdateNameLoaiChi)
        val btnUpdateLoaiChi = dialogView.findViewById<Button>(R.id.btnUpdateLoaiChi)
        val btnHuyUpdateLoaiChi = dialogView.findViewById<Button>(R.id.btnHuyUpdateLoaiChi)

        // Set current name to EditText
        edtUpdateNameLoaiChi.setText(loaiChi.name)

        btnUpdateLoaiChi.setOnClickListener {
            val updatedName = edtUpdateNameLoaiChi.text.toString().trim()
            if (updatedName.isNotEmpty()) {
                val updatedLoaiChi = loaiChi.copy(name = updatedName)

                // Ensure we access by userId
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val userRef = FirebaseDatabase.getInstance().getReference("LoaiChi").child(uid)
                    userRef.child(loaiChi.id).setValue(updatedLoaiChi).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Cập nhật loại chi thành công!", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Cập nhật loại chi thất bại!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                edtUpdateNameLoaiChi.error = "Tên loại chi không được để trống!"
            }
        }

        btnHuyUpdateLoaiChi.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun deleteLoaiChi(id: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            // Ensure we are accessing the right user node
            val userRef = FirebaseDatabase.getInstance().getReference("LoaiChi").child(uid)
            userRef.child(id).removeValue().addOnSuccessListener {
                Toast.makeText(requireContext(), "Xóa loại chi thành công!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Xóa loại chi thất bại!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}