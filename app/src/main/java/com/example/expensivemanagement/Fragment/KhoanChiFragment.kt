package com.example.expensivemanagement.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.adapter.KhoanChiAdapter
import com.example.expensivemanagement.adapter.SpLoaiChiAdapter
import com.example.expensivemanagement.model.KhoanChi
import com.example.expensivemanagement.model.LoaiChi
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class KhoanChiFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnThemKhoanChi: Button
    private lateinit var database: DatabaseReference
    private lateinit var khoanChiList: MutableList<KhoanChi>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_khoan_chi, container, false)

        recyclerView = view.findViewById(R.id.listViewKhoanChi)
        btnThemKhoanChi = view.findViewById(R.id.btnThemKhoanChi)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        khoanChiList = mutableListOf()

        // Firebase Realtime Database setup
        database = FirebaseDatabase.getInstance().getReference("KhoanChi")

        // Load data from Firebase
        loadKhoanChiData()

        // Add a new KhoanChi
        btnThemKhoanChi.setOnClickListener {
            addKhoanChi()
        }

        return view
    }

    private fun loadKhoanChiData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                khoanChiList.clear()
                for (dataSnapshot in snapshot.children) {
                    val khoanChi = dataSnapshot.getValue(KhoanChi::class.java)
                    khoanChi?.let { khoanChiList.add(it) }
                }

                // Set up the adapter
                val adapter = KhoanChiAdapter(khoanChiList, { khoanChi ->
                    // Handle delete click
                    deleteKhoanChi(khoanChi)
                }, { khoanChi ->
                    // Handle edit click
                    editKhoanChi(khoanChi)
                }, { khoanChi ->
                    // Handle view click (view detail)
                    viewKhoanChiDetails(khoanChi)
                })
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun viewKhoanChiDetails(khoanChi: KhoanChi) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_read_chi, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Set data to the views
        dialogView.findViewById<TextView>(R.id.txtKhoanChi).text = khoanChi.name
        dialogView.findViewById<TextView>(R.id.txtLoaiChi).text = khoanChi.loaiChi
        dialogView.findViewById<TextView>(R.id.txtTienChi).text = khoanChi.soTien.toString()
        dialogView.findViewById<TextView>(R.id.txtNgayChi).text = khoanChi.thoiDiemChi

        // Handle the close button
        dialogView.findViewById<Button>(R.id.btnThoatChiTiet).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun addKhoanChi() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_add_chi, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Khai báo các View
        val edtNameChi = dialogView.findViewById<EditText>(R.id.edtNameChi)
        val edtTienChi = dialogView.findViewById<EditText>(R.id.edtTienChi)
        val edtThemNgayChi = dialogView.findViewById<EditText>(R.id.edtThemNgayChi)
        val spinnerLoaiChi = dialogView.findViewById<Spinner>(R.id.spLoaiChi)
        val btnThemLoaiChi = dialogView.findViewById<Button>(R.id.btnThemLoaiChi)
        val btnHuyThemLoaiChi = dialogView.findViewById<Button>(R.id.btnHuyThemLoaiChi)

        // Đặt ngày hiện tại làm mặc định
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtThemNgayChi.setText(dateFormat.format(calendar.time))

        // Hiển thị DatePicker khi nhấn vào EditText chọn ngày
        edtThemNgayChi.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    edtThemNgayChi.setText(dateFormat.format(selectedCalendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Lấy danh sách LoaiChi từ Firebase và cập nhật Spinner
        getLoaiChiList { loaiChiList ->
            val loaiChiAdapter = SpLoaiChiAdapter(requireContext(), loaiChiList)
            spinnerLoaiChi.adapter = loaiChiAdapter
        }

        // Xử lý sự kiện khi nhấn nút "Thêm Loại Chi"
        btnThemLoaiChi.setOnClickListener {
            val name = edtNameChi.text.toString().trim()
            val tienChi = edtTienChi.text.toString().trim()
            val ngayChi = edtThemNgayChi.text.toString().trim()
            val loaiChi = spinnerLoaiChi.selectedItem as? LoaiChi // Lấy đối tượng LoaiChi được chọn

            // Kiểm tra đầu vào
            if (name.isEmpty() || tienChi.isEmpty() || ngayChi.isEmpty() || loaiChi == null) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra số tiền
            val soTien = tienChi.toIntOrNull()
            if (soTien == null || soTien <= 0) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = database.push().key ?: return@setOnClickListener

            // Tạo đối tượng KhoanChi
            val newKhoanChi = KhoanChi(
                id = id,
                name = name,
                loaiChi = loaiChi.getNameLoaiChi(), // Trả về tên LoaiChi dưới dạng String
                thoiDiemChi = ngayChi,
                soTien = soTien,
                danhGia = 0,
                deleteFlag = 0,
                idLoaiChi = loaiChi.getIdLoaiChi()
            )

            // Thêm vào Firebase
            database.child(id).setValue(newKhoanChi).addOnSuccessListener {
                Toast.makeText(requireContext(), "Thêm khoản chi thành công!", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Thêm khoản chi thất bại!", Toast.LENGTH_SHORT).show()
            }
        }

        // Hủy bỏ thêm khoản chi
        btnHuyThemLoaiChi.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun deleteKhoanChi(khoanChi: KhoanChi) {
        // Firebase delete operation
        database.child(khoanChi.id).removeValue().addOnCompleteListener {
            Toast.makeText(activity, "Khoan Chi deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editKhoanChi(khoanChi: KhoanChi) {
        // Inflate the edit dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_update_chi, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Get references to the views
        val edtNgayChi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateNgayChi)
        val edtNameChi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateNameChi)
        val edtTienChi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateTienChi)
        val spinnerLoaiChi = dialogView.findViewById<Spinner>(R.id.spupdateLoaiChi)

        // Set current values from KhoanChi
        edtNgayChi.setText(khoanChi.thoiDiemChi)
        edtNameChi.setText(khoanChi.name)
        edtTienChi.setText(khoanChi.soTien.toString())

        // Load LoaiChi list from Firebase and set the selected item
        getLoaiChiList { loaiChiList ->
            val loaiChiAdapter = SpLoaiChiAdapter(requireContext(), loaiChiList)
            spinnerLoaiChi.adapter = loaiChiAdapter

            // Set the spinner to the current LoaiChi
            loaiChiList.find { it.getIdLoaiChi() == khoanChi.idLoaiChi }?.let { selectedLoaiChi ->
                val spinnerPosition = loaiChiList.indexOf(selectedLoaiChi)
                spinnerLoaiChi.setSelection(spinnerPosition)
            }
        }

        // Set date picker on edit text for selecting a date
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtNgayChi.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    edtNgayChi.setText(dateFormat.format(selectedCalendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Handle save button click
        val btnSave = dialogView.findViewById<Button>(R.id.btnCapNhatKhoanChi)
        btnSave.setOnClickListener {
            val updatedName = edtNameChi.text.toString().trim()
            val updatedTien = edtTienChi.text.toString().trim()
            val updatedNgay = edtNgayChi.text.toString().trim()
            val updatedLoaiChi = spinnerLoaiChi.selectedItem as? LoaiChi

            if (updatedName.isEmpty() || updatedTien.isEmpty() || updatedNgay.isEmpty() || updatedLoaiChi == null) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedTienChi = updatedTien.toIntOrNull()
            if (updatedTienChi == null || updatedTienChi <= 0) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create the updated KhoanChi object
            val updatedKhoanChi = KhoanChi(
                id = khoanChi.id,
                name = updatedName,
                loaiChi = updatedLoaiChi.getNameLoaiChi(),
                thoiDiemChi = updatedNgay,
                soTien = updatedTienChi,
                danhGia = khoanChi.danhGia, // Keep the existing rating
                deleteFlag = khoanChi.deleteFlag, // Keep the existing delete flag
                idLoaiChi = updatedLoaiChi.getIdLoaiChi()
            )

            // Update the KhoanChi in Firebase
            database.child(khoanChi.id).setValue(updatedKhoanChi).addOnSuccessListener {
                Toast.makeText(requireContext(), "Cập nhật khoản chi thành công!", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Cập nhật khoản chi thất bại!", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle cancel button click
        val btnCancel = dialogView.findViewById<Button>(R.id.btnHuyCapNhatChi)
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun getLoaiChiList(callback: (ArrayList<LoaiChi>) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("LoaiChi")
        val loaiChiList = ArrayList<LoaiChi>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loaiChiList.clear() // Đảm bảo danh sách luôn được làm mới
                for (data in snapshot.children) {
                    val loaiChi = data.getValue(LoaiChi::class.java)
                    if (loaiChi != null) {
                        loaiChiList.add(loaiChi)
                    }
                }
                callback(loaiChiList) // Trả về danh sách qua callback
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi khi tải danh sách loại chi: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(ArrayList()) // Trả về danh sách rỗng trong trường hợp lỗi
            }
        })
    }
}