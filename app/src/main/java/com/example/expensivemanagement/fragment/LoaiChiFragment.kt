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
import com.example.expensivemanagement.adapter.LoaiChiAdapter
import com.example.expensivemanagement.model.LoaiChi
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loaiChiList.clear()
                for (child in snapshot.children) {
                    val loaiChi = child.getValue(LoaiChi::class.java)
                    loaiChi?.let { loaiChiList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
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
                val id = database.push().key ?: return@setOnClickListener
                val newLoaiChi = LoaiChi(id, name)
                database.child(id).setValue(newLoaiChi).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Thêm loại chi thành công!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Thêm loại chi thất bại!", Toast.LENGTH_SHORT).show()
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
                database.child(loaiChi.id).setValue(updatedLoaiChi).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cập nhật loại chi thành công!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Cập nhật loại chi thất bại!", Toast.LENGTH_SHORT).show()
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
        database.child(id).removeValue()
    }
}