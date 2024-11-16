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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.adapter.KhoanThuAdapter
import com.example.expensivemanagement.adapter.SpLoaiThuAdapter
import com.example.expensivemanagement.model.KhoanThu
import com.example.expensivemanagement.model.LoaiThu
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class KhoanThuFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnThemKhoanThu: Button
    private lateinit var database: DatabaseReference
    private lateinit var khoanThuList: MutableList<KhoanThu>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_khoan_thu, container, false)

        recyclerView = view.findViewById(R.id.listViewKhoanThu)
        btnThemKhoanThu = view.findViewById(R.id.btnThemKhoanThu)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        khoanThuList = mutableListOf()

        // Firebase Realtime Database setup
        database = FirebaseDatabase.getInstance().getReference("KhoanThu")

        // Load data from Firebase
        loadKhoanThuData()

        // Add a new KhoanThu
        btnThemKhoanThu.setOnClickListener {
            addKhoanThu()
        }

        return view
    }

    private fun loadKhoanThuData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                khoanThuList.clear()
                for (dataSnapshot in snapshot.children) {
                    val khoanThu = dataSnapshot.getValue(KhoanThu::class.java)
                    khoanThu?.let { khoanThuList.add(it) }
                }

                // Set up the adapter
                val adapter = KhoanThuAdapter(khoanThuList, { khoanThu ->
                    // Handle delete click
                    deleteKhoanThu(khoanThu)
                }, { khoanThu ->
                    // Handle edit click
                    editKhoanThu(khoanThu)
                }, { khoanThu ->
                    // Handle view click (view detail)
                    viewKhoanThuDetails(khoanThu)
                })
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "Error loading data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun viewKhoanThuDetails(khoanThu: KhoanThu) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_read_thu, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Set data to the views
        dialogView.findViewById<TextView>(R.id.txtKhoanThu).text = khoanThu.tenThu
        dialogView.findViewById<TextView>(R.id.txtLoaiThu).text = khoanThu.loaiThu
        dialogView.findViewById<TextView>(R.id.txtTienThu).text = khoanThu.soTien.toString()
        dialogView.findViewById<TextView>(R.id.txtNgayThu).text = khoanThu.thoiDiemThu

        // Handle the close button
        dialogView.findViewById<Button>(R.id.btnHuyXemKhoanThu).setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun addKhoanThu() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_add_thu, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Khai báo các View
        val edtNameThu = dialogView.findViewById<EditText>(R.id.edtNameThu)
        val edtTienThu = dialogView.findViewById<EditText>(R.id.edtTienThu)
        val edtThemNgayThu = dialogView.findViewById<EditText>(R.id.edtThemNgayThu)
        val spinnerLoaiThu = dialogView.findViewById<Spinner>(R.id.spLoaiThu)
        val btnThemKhoanThu = dialogView.findViewById<Button>(R.id.btnThemKhoanThu)
        val btnHuyThemKhoanThu = dialogView.findViewById<Button>(R.id.btnHuyThemKhoanThu)

        // Đặt ngày hiện tại làm mặc định
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtThemNgayThu.setText(dateFormat.format(calendar.time))

        // Hiển thị DatePicker khi nhấn vào EditText chọn ngày
        edtThemNgayThu.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    edtThemNgayThu.setText(dateFormat.format(selectedCalendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Lấy danh sách LoaiThu từ Firebase và cập nhật Spinner
        getLoaiThuList { loaiThuList ->
            val loaiThuAdapter = SpLoaiThuAdapter(requireContext(), loaiThuList)
            spinnerLoaiThu.adapter = loaiThuAdapter
        }

        // Xử lý sự kiện khi nhấn nút "Thêm Loại Thu"
        btnThemKhoanThu.setOnClickListener {
            val name = edtNameThu.text.toString().trim()
            val tienThu = edtTienThu.text.toString().trim()
            val ngayThu = edtThemNgayThu.text.toString().trim()
            val loaiThu = spinnerLoaiThu.selectedItem as? LoaiThu // Lấy đối tượng LoaiThu được chọn

            // Kiểm tra đầu vào
            if (name.isEmpty() || tienThu.isEmpty() || ngayThu.isEmpty() || loaiThu == null) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Kiểm tra số tiền
            val soTien = tienThu.toIntOrNull()
            if (soTien == null || soTien <= 0) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = database.push().key ?: return@setOnClickListener

            // Tạo đối tượng KhoanThu
            val newKhoanThu = KhoanThu(
                idThu = id,
                tenThu = name,
                loaiThu = loaiThu.getNameLoaiThu(), // Trả về tên LoaiThu dưới dạng String
                thoiDiemThu = ngayThu,
                soTien = soTien,
                danhGia = 0,
                deleteFlag = 0,
                idLoaiThu = loaiThu.getIdLoaiThu()
            )

            // Thêm vào Firebase
            database.child(id).setValue(newKhoanThu).addOnSuccessListener {
                Toast.makeText(requireContext(), "Thêm khoản Thu thành công!", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Thêm khoản Thu thất bại!", Toast.LENGTH_SHORT).show()
            }
        }

        // Hủy bỏ thêm khoản Thu
        btnHuyThemKhoanThu.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun deleteKhoanThu(khoanThu: KhoanThu) {
        // Firebase delete operation
        database.child(khoanThu.idThu).removeValue().addOnCompleteListener {
            Toast.makeText(activity, "Khoan Thu deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editKhoanThu(khoanThu: KhoanThu) {
        // Inflate the edit dialog layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_update_thu, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Get references to the views
        val edtNgayThu = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateNgayThu)
        val edtNameThu = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateNameThu)
        val edtTienThu = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateTienThu)
        val spinnerLoaiThu = dialogView.findViewById<Spinner>(R.id.spupdateLoaiThu)

        // Set current values from KhoanThu
        edtNgayThu.setText(khoanThu.thoiDiemThu)
        edtNameThu.setText(khoanThu.tenThu)
        edtTienThu.setText(khoanThu.soTien.toString())

        // Load LoaiThu list from Firebase and set the selected item
        getLoaiThuList { loaiThuList ->
            val loaiThuAdapter = SpLoaiThuAdapter(requireContext(), loaiThuList)
            spinnerLoaiThu.adapter = loaiThuAdapter

            // Set the spinner to the current LoaiThu
            loaiThuList.find { it.getIdLoaiThu() == khoanThu.idLoaiThu }?.let { selectedLoaiThu ->
                val spinnerPosition = loaiThuList.indexOf(selectedLoaiThu)
                spinnerLoaiThu.setSelection(spinnerPosition)
            }
        }

        // Set date picker on edit text for selecting a date
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtNgayThu.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    edtNgayThu.setText(dateFormat.format(selectedCalendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Handle save button click
        val btnSave = dialogView.findViewById<Button>(R.id.btnCapNhatKhoanThu)
        btnSave.setOnClickListener {
            val updatedName = edtNameThu.text.toString().trim()
            val updatedTien = edtTienThu.text.toString().trim()
            val updatedNgay = edtNgayThu.text.toString().trim()
            val updatedLoaiThu = spinnerLoaiThu.selectedItem as? LoaiThu

            if (updatedName.isEmpty() || updatedTien.isEmpty() || updatedNgay.isEmpty() || updatedLoaiThu == null) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedTienThu = updatedTien.toIntOrNull()
            if (updatedTienThu == null || updatedTienThu <= 0) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create the updated KhoanThu object
            val updatedKhoanThu = KhoanThu(
                idThu = khoanThu.idThu,
                tenThu = updatedName,
                loaiThu = updatedLoaiThu.getNameLoaiThu(),
                thoiDiemThu = updatedNgay,
                soTien = updatedTienThu,
                danhGia = khoanThu.danhGia, // Keep the existing rating
                deleteFlag = khoanThu.deleteFlag, // Keep the existing delete flag
                idLoaiThu = updatedLoaiThu.getIdLoaiThu()
            )

            // Update the KhoanThu in Firebase
            database.child(khoanThu.idThu).setValue(updatedKhoanThu).addOnSuccessListener {
                Toast.makeText(requireContext(), "Cập nhật khoản Thu thành công!", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Cập nhật khoản Thu thất bại!", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle cancel button click
        val btnCancel = dialogView.findViewById<Button>(R.id.btnHuyCapNhatThu)
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun getLoaiThuList(callback: (ArrayList<LoaiThu>) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("LoaiThu")
        val loaiThuList = ArrayList<LoaiThu>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loaiThuList.clear() // Đảm bảo danh sách luôn được làm mới
                for (data in snapshot.children) {
                    val loaiThu = data.getValue(LoaiThu::class.java)
                    if (loaiThu != null) {
                        loaiThuList.add(loaiThu)
                    }
                }
                callback(loaiThuList) // Trả về danh sách qua callback
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi khi tải danh sách loại Thu: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(ArrayList()) // Trả về danh sách rỗng trong trường hợp lỗi
            }
        })
    }
}