package com.example.expensivemanagement.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuth
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch data only for the current user
        val userRef = FirebaseDatabase.getInstance().getReference("KhoanChi").child(uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                khoanChiList.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val khoanChi = dataSnapshot.getValue(KhoanChi::class.java)
                        khoanChi?.let { khoanChiList.add(it) }
                    }
                    Log.d("KhoanChiFragment", "Data loaded, items count: ${khoanChiList.size}")
                } else {
                    Log.d("KhoanChiFragment", "No data found for the user")
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
                    viewKhoanChiDetails(khoanChi.id)
                })
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Ensure operations only occur if the fragment is attached
                if (isAdded) {
                    try {
                        val context = requireContext()
                        Toast.makeText(context, "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
                        Log.e("KhoanChiFragment", "Error loading data: ${error.message}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Log.e("KhoanChiFragment", "Fragment not attached to activity.")
                }
            }
        })
    }

    private fun viewKhoanChiDetails(khoanChiId: String) {
        // Log để kiểm tra ID khoản chi
        Log.d("KhoanChiDetails", "KhoanChiId: $khoanChiId")

        // Get the current user ID
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            Log.d("KhoanChiDetails", "User ID: $uid")

            val userRef = FirebaseDatabase.getInstance().getReference("KhoanChi").child(uid)

            // Log to confirm the reference to the right path in Firebase
            Log.d("KhoanChiDetails", "Reference Path: KhoanChi/$uid/$khoanChiId")

            // Retrieve the specific KhoanChi details using its ID
            userRef.child(khoanChiId).get().addOnSuccessListener { dataSnapshot ->
                // Log the raw snapshot data to check if it's null or missing
                Log.d("KhoanChiDetails", "Data snapshot: ${dataSnapshot.value}")

                // Check if dataSnapshot exists and contains valid data
                if (dataSnapshot.exists()) {
                    val khoanChi = dataSnapshot.getValue(KhoanChi::class.java)
                    if (khoanChi != null) {
                        Log.d("KhoanChiDetails", "KhoanChi details: $khoanChi")

                        // Inflate the details dialog layout
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
                    } else {
                        Log.d("KhoanChiDetails", "KhoanChi data is null or malformed")
                        Toast.makeText(requireContext(), "Không tìm thấy thông tin khoản chi!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("KhoanChiDetails", "KhoanChi data not found at path KhoanChi/$uid/$khoanChiId")
                    Toast.makeText(requireContext(), "Không tìm thấy thông tin khoản chi!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("KhoanChiDetails", "Error retrieving KhoanChi details", exception)
                Toast.makeText(requireContext(), "Lỗi khi tải chi tiết khoản chi!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("KhoanChiDetails", "User not logged in")
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
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
            val loaiChi = spinnerLoaiChi.selectedItem as? LoaiChi

            if (name.isEmpty() || tienChi.isEmpty() || ngayChi.isEmpty() || loaiChi == null) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val soTien = tienChi.toIntOrNull()
            if (soTien == null || soTien <= 0) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
                val userId = snapshot.child("userId").getValue(Int::class.java)?.toString()
                if (userId.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Không tìm thấy userId!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                    return@addOnSuccessListener
                }

                val id = database.push().key ?: return@addOnSuccessListener

                // Tạo đối tượng KhoanChi
                val newKhoanChi = KhoanChi(
                    id = id,
                    name = name,
                    loaiChi = loaiChi.getNameLoaiChi(),
                    thoiDiemChi = ngayChi,
                    soTien = soTien,
                    danhGia = 0,
                    deleteFlag = 0,
                    idLoaiChi = loaiChi.getIdLoaiChi()
                )

                // Thêm vào Firebase
                database.child(userId).child(id).setValue(newKhoanChi).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Thêm khoản chi thành công!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()

                    // Gọi lại phương thức tải dữ liệu nếu cần
                    loadKhoanChiData()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Thêm khoản chi thất bại!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }
        }

        // Hủy bỏ thêm khoản chi
        btnHuyThemLoaiChi.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun deleteKhoanChi(khoanChi: KhoanChi) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Truy vấn để lấy userId từ bảng "user"
        val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)
        userRef.get().addOnSuccessListener { snapshot ->
            val userId = snapshot.child("userId").getValue(String::class.java)
            if (userId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Không tìm thấy userId!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Thực hiện xóa KhoanChi theo userId
            val khoanChiRef = FirebaseDatabase.getInstance().getReference("KhoanChi").child(userId)
            khoanChiRef.child(khoanChi.id).removeValue().addOnSuccessListener {
                Toast.makeText(requireContext(), "Xóa khoản chi thành công!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Xóa khoản chi thất bại!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editKhoanChi(khoanChi: KhoanChi) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_update_chi, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Tham chiếu tới các view
        val edtNgayChi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateNgayChi)
        val edtNameChi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateNameChi)
        val edtTienChi = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateTienChi)
        val spinnerLoaiChi = dialogView.findViewById<Spinner>(R.id.spupdateLoaiChi)

        // Đặt giá trị hiện tại của KhoanChi
        edtNgayChi.setText(khoanChi.thoiDiemChi)
        edtNameChi.setText(khoanChi.name)
        edtTienChi.setText(khoanChi.soTien.toString())

        // Tải danh sách LoạiChi và chọn loại chi hiện tại
        getLoaiChiList { loaiChiList ->
            val loaiChiAdapter = SpLoaiChiAdapter(requireContext(), loaiChiList)
            spinnerLoaiChi.adapter = loaiChiAdapter

            // Đặt spinner vào loại chi hiện tại
            loaiChiList.find { it.getIdLoaiChi() == khoanChi.idLoaiChi }?.let { selectedLoaiChi ->
                val spinnerPosition = loaiChiList.indexOf(selectedLoaiChi)
                spinnerLoaiChi.setSelection(spinnerPosition)
            }
        }

        // Hiển thị DatePicker khi nhấn vào edtNgayChi
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

        // Xử lý khi nhấn nút lưu
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

            val updatedKhoanChi = KhoanChi(
                id = khoanChi.id,
                name = updatedName,
                loaiChi = updatedLoaiChi.getNameLoaiChi(),
                thoiDiemChi = updatedNgay,
                soTien = updatedTienChi,
                danhGia = khoanChi.danhGia,
                deleteFlag = khoanChi.deleteFlag,
                idLoaiChi = updatedLoaiChi.getIdLoaiChi()
            )

            // Xác định uid người dùng
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val userRef = FirebaseDatabase.getInstance().getReference("KhoanChi").child(uid)
                userRef.child(khoanChi.id).setValue(updatedKhoanChi).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cập nhật khoản chi thành công!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Cập nhật khoản chi thất bại!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý khi nhấn nút hủy
        val btnCancel = dialogView.findViewById<Button>(R.id.btnHuyCapNhatChi)
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun getLoaiChiList(callback: (ArrayList<LoaiChi>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            // Trường hợp không đăng nhập
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            callback(ArrayList())
            return
        }

        // Truy vấn dữ liệu loại chi theo uid
        val database = FirebaseDatabase.getInstance().getReference("LoaiChi").child(uid)
        val loaiChiList = ArrayList<LoaiChi>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loaiChiList.clear() // Đảm bảo danh sách được làm mới

                for (data in snapshot.children) {
                    val loaiChi = data.getValue(LoaiChi::class.java)
                    if (loaiChi != null) {
                        loaiChiList.add(loaiChi)
                    }
                }

                // Gọi callback để trả danh sách loại chi
                callback(loaiChiList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý khi có lỗi từ Firebase
                Toast.makeText(requireContext(), "Lỗi khi tải danh sách loại chi: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(ArrayList()) // Trả về danh sách rỗng trong trường hợp lỗi
            }
        })
    }
}