package com.example.expensivemanagement.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expensivemanagement.R
import com.example.expensivemanagement.adapter.NganSachListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class NganSachFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var nganSachListAdapter: NganSachListAdapter
    private lateinit var database: DatabaseReference
    private var nganSachList: MutableList<NganSach> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ngansach, container, false)

        listView = view.findViewById(R.id.listViewNganSach)

        database = FirebaseDatabase.getInstance().getReference("NganSach")


        nganSachListAdapter = NganSachListAdapter(requireContext(), nganSachList, { nganSach ->
            deleteNganSach(nganSach)
        }, { nganSach ->
            showEditDialog(nganSach)
        })
        listView.adapter = nganSachListAdapter

        loadNganSachData()

        val btnThemNganSach = view.findViewById<Button>(R.id.btnThemNganSach)
        btnThemNganSach.setOnClickListener {
            showEditDialog()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val nganSach = nganSachList[position]
            showEditDialog(nganSach)
        }

        return view
    }

    private fun loadNganSachData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                nganSachList.clear()

                for (dataSnapshot in snapshot.children) {
                    val nganSach = dataSnapshot.getValue(NganSach::class.java)
                    nganSach?.let { nganSachList.add(it) }
                }

                // Tính tổng chi theo tháng/năm
                for (nganSach in nganSachList) {
                    val thangNam = nganSach.thangNam // Giả sử thangNam có định dạng "MM/YYYY"
                    calculateTotalChiForMonth(uid, thangNam) { totalChi ->
                        nganSach.totalChi = totalChi // Cập nhật tổng chi vào ngân sách

                        // Kiểm tra xem có vượt ngân sách không
                        Log.d("NganSachFragment", "Total Chi: $totalChi, So Tien: ${nganSach.soTien}")
                        if (totalChi > nganSach.soTien) {
                            nganSach.tenNganSach += " (Vượt ngân sách)" // Thêm thông báo nếu vượt ngân sách
                        } else {
                            nganSach.tenNganSach = nganSach.tenNganSach.replace(" (Vượt ngân sách)", "") // Xóa thông báo nếu không vượt
                        }

                        nganSachListAdapter.notifyDataSetChanged() // Cập nhật giao diện
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi khi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateTotalChiForMonth(uid: String, thangNam: String, callback: (Double) -> Unit) {
        var total = 0.0
        val khoanChiRef = FirebaseDatabase.getInstance().getReference("KhoanChi").child(uid)

        // Lấy dữ liệu chi từ Firebase
        khoanChiRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val khoanChi = dataSnapshot.getValue(KhoanChi::class.java)
                    if (khoanChi != null) {
                        // Trích xuất tháng và năm từ thoiDiemChi
                        val dateParts = khoanChi.thoiDiemChi.split("/")
                        if (dateParts.size == 3) {
                            val day = dateParts[0]
                            val month = dateParts[1]
                            val year = dateParts[2]

                            // So sánh tháng/năm
                            if ("$month/$year" == thangNam) {
                                total += khoanChi.soTien
                                Log.d("NganSachFragment", "Added ${khoanChi.soTien} to total for $thangNam")
                            }
                        }
                    }
                }
                // Gọi callback với tổng chi
                callback(total)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
                callback(0.0) // Trả về 0 nếu có lỗi
            }
        })
    }

    private fun showEditDialog(nganSach: NganSach? = null) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ngansach, null)
        val edtTenNganSach = dialogView.findViewById<EditText>(R.id.edtTenNganSach)
        val txtThangNam = dialogView.findViewById<TextView>(R.id.txtThangNam)
        val btnSelectDate = dialogView.findViewById<Button>(R.id.btnSelectDate)
        val edtSoTien = dialogView.findViewById<EditText>(R.id.edtSoTien)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        if (nganSach != null) {
            edtTenNganSach.setText(nganSach.tenNganSach)
            txtThangNam.text = nganSach.thangNam
            edtSoTien.setText(nganSach.soTien.toString())
        }

        btnSelectDate.setOnClickListener {
            // Hiển thị DatePickerDialog
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, _ ->
                // Cập nhật TextView với tháng/năm đã chọn
                txtThangNam.text = "${selectedMonth + 1}/$selectedYear" // Tháng bắt đầu từ 0
            }, year, month, 1)

            datePickerDialog.show()
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(if (nganSach == null) "Thêm ngân sách" else "Sửa ngân sách")
            .create()

        btnSave.setOnClickListener {
            val tenNganSach = edtTenNganSach.text.toString().trim()
            val thangNam = txtThangNam.text.toString().trim()
            val soTien = edtSoTien.text.toString().trim()

            if (tenNganSach.isEmpty() || thangNam.isEmpty() || soTien.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nganSach == null) {
                val id = database.push().key ?: return@setOnClickListener
                val newNganSach = NganSach(id, tenNganSach, thangNam, soTien.toDouble())
                database.child(uid).child(id).setValue(newNganSach).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Thêm ngân sách thành công!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Thêm ngân sách thất bại!", Toast.LENGTH_SHORT).show()
                }
            } else {
                nganSach.tenNganSach = tenNganSach
                nganSach.thangNam = thangNam
                nganSach.soTien = soTien.toDouble()
                database.child(uid).child(nganSach.id).setValue(nganSach).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cập nhật ngân sách thành công!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Cập nhật ngân sách thất bại!", Toast.LENGTH_SHORT).show()
                }
            }

            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun deleteNganSach(nganSach: NganSach) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        database.child(uid).child(nganSach.id).removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "Xóa ngân sách thành công!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Xóa ngân sách thất bại!", Toast.LENGTH_SHORT).show()
        }
    }
}
