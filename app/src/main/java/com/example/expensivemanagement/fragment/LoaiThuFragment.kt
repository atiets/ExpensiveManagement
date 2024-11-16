package com.example.expensivemanagement.fragment

import android.os.Bundle
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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loaiThuList.clear()
                for (child in snapshot.children) {
                    val loaiThu = child.getValue(LoaiThu::class.java)
                    loaiThu?.let { loaiThuList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
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
                val id = database.push().key ?: return@setOnClickListener
                val newLoaiThu = LoaiThu(id, name)
                database.child(id).setValue(newLoaiThu).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Thêm loại thu thành công!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Thêm loại thu thất bại!", Toast.LENGTH_SHORT).show()
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

        edtUpdateNameLoaiThu.setText(loaiThu.name)

        btnUpdateLoaiThu.setOnClickListener {
            val updatedName = edtUpdateNameLoaiThu.text.toString().trim()
            if (updatedName.isNotEmpty()) {
                val updatedLoaiThu = loaiThu.copy(name = updatedName)
                database.child(loaiThu.id).setValue(updatedLoaiThu).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cập nhật loại thu thành công!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Cập nhật loại thu thất bại!", Toast.LENGTH_SHORT).show()
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
        database.child(id).removeValue()
    }
}