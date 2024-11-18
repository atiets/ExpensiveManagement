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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.R
import com.example.expensivemanagement.adapter.KhoanThuAdapter
import com.example.expensivemanagement.adapter.SpLoaiChiAdapter
import com.example.expensivemanagement.adapter.SpLoaiThuAdapter
import com.example.expensivemanagement.model.KhoanThu
import com.example.expensivemanagement.model.LoaiThu
import com.google.firebase.auth.FirebaseAuth
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch data only for the current user
        val userRef = FirebaseDatabase.getInstance().getReference("KhoanThu").child(uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                khoanThuList.clear()
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val khoanThu = dataSnapshot.getValue(KhoanThu::class.java)
                        khoanThu?.let { khoanThuList.add(it) }
                    }
                    Log.d("KhoanThuFragment", "Data loaded, items count: ${khoanThuList.size}")
                } else {
                    Log.d("KhoanThuFragment", "No data found for the user")
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
                    viewKhoanThuDetails(khoanThu.idThu)
                })
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Ensure operations only occur if the fragment is attached
                if (isAdded) {
                    try {
                        val context = requireContext()
                        Toast.makeText(context, "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
                        Log.e("KhoanThuFragment", "Error loading data: ${error.message}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Log.e("KhoanThuFragment", "Fragment not attached to activity.")
                }
            }
        })
    }

    private fun viewKhoanThuDetails(khoanThuId: String) {
        // Log để kiểm tra ID khoản thu
        Log.d("KhoanThuDetails", "KhoanThuId: $khoanThuId")

        // Get the current user ID
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            Log.d("KhoanThuDetails", "User ID: $uid")

            val userRef = FirebaseDatabase.getInstance().getReference("KhoanThu").child(uid)

            // Log to confirm the reference to the right path in Firebase
            Log.d("KhoanThuDetails", "Reference Path: KhoanThu/$uid/$khoanThuId")

            // Retrieve the specific KhoanThu details using its ID
            userRef.child(khoanThuId).get().addOnSuccessListener { dataSnapshot ->
                // Log the raw snapshot data to check if it's null or missing
                Log.d("KhoanThuDetails", "Data snapshot: ${dataSnapshot.value}")

                // Check if dataSnapshot exists and contains valid data
                if (dataSnapshot.exists()) {
                    val khoanThu = dataSnapshot.getValue(KhoanThu::class.java)
                    if (khoanThu != null) {
                        Log.d("KhoanThuDetails", "KhoanThu details: $khoanThu")

                        // Inflate the details dialog layout
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
                    } else {
                        Log.d("KhoanThuDetails", "KhoanThu data is null or malformed")
                        Toast.makeText(requireContext(), "Không tìm thấy thông tin khoản thu!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d("KhoanThuDetails", "KhoanThu data not found at path KhoanThu/$uid/$khoanThuId")
                    Toast.makeText(requireContext(), "Không tìm thấy thông tin khoản thu!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.e("KhoanThuDetails", "Error retrieving KhoanThu details", exception)
                Toast.makeText(requireContext(), "Lỗi khi tải chi tiết khoản thu!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("KhoanThuDetails", "User not logged in")
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addKhoanThu() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_add_thu, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Declare the views
        val edtNameThu = dialogView.findViewById<EditText>(R.id.edtNameThu)
        val edtTienThu = dialogView.findViewById<EditText>(R.id.edtTienThu)
        val edtThemNgayThu = dialogView.findViewById<EditText>(R.id.edtThemNgayThu)
        val spinnerLoaiThu = dialogView.findViewById<Spinner>(R.id.spLoaiThu)
        val btnThemKhoanThu = dialogView.findViewById<Button>(R.id.btnThemKhoanThu)
        val btnHuyThemKhoanThu = dialogView.findViewById<Button>(R.id.btnHuyThemKhoanThu)

        // Set the current date as the default
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        edtThemNgayThu.setText(dateFormat.format(calendar.time))

        // Show the DatePicker when clicking on the date EditText
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

        // Get the list of LoaiThu from Firebase and update the Spinner
        getLoaiThuList { loaiThuList ->
            val loaiThuAdapter = SpLoaiThuAdapter(requireContext(), loaiThuList)
            spinnerLoaiThu.adapter = loaiThuAdapter
        }

        // Handle click event for "Add KhoanThu" button
        btnThemKhoanThu.setOnClickListener {
            val name = edtNameThu.text.toString().trim()
            val tienThu = edtTienThu.text.toString().trim()
            val ngayThu = edtThemNgayThu.text.toString().trim()
            val loaiThu = spinnerLoaiThu.selectedItem as? LoaiThu // Get selected LoaiThu object

            // Validate inputs
            if (name.isEmpty() || tienThu.isEmpty() || ngayThu.isEmpty() || loaiThu == null) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate amount
            val soTien = tienThu.toIntOrNull()
            if (soTien == null || soTien <= 0) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get UID from FirebaseAuth
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                return@setOnClickListener
            }

            // Query "user" table to get userId
            val userRef = FirebaseDatabase.getInstance().getReference("user").child(uid)
            userRef.get().addOnSuccessListener { snapshot ->
                val userId = snapshot.child("userId").getValue(String::class.java)
                if (userId.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "Không tìm thấy userId!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                    return@addOnSuccessListener
                }

                val id = database.push().key ?: return@addOnSuccessListener

                // Create the KhoanThu object
                val newKhoanThu = KhoanThu(
                    idThu = id,
                    tenThu = name,
                    loaiThu = loaiThu.getNameLoaiThu(),
                    thoiDiemThu = ngayThu,
                    soTien = soTien,
                    danhGia = 0,
                    deleteFlag = 0,
                    idLoaiThu = loaiThu.getIdLoaiThu()
                )

                // Add to Firebase
                database.child(userId).child(id).setValue(newKhoanThu).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Thêm khoản Thu thành công!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()

                    // Reload data if necessary
                    loadKhoanThuData()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Thêm khoản Thu thất bại!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            }
        }

        // Cancel adding KhoanThu
        btnHuyThemKhoanThu.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun deleteKhoanThu(khoanThu: KhoanThu) {
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

            // Thực hiện xóa KhoanThu theo userId
            val khoanThuRef = FirebaseDatabase.getInstance().getReference("KhoanThu").child(userId)
            khoanThuRef.child(khoanThu.idThu).removeValue().addOnSuccessListener {
                Toast.makeText(requireContext(), "Xóa khoản thu thành công!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Xóa khoản thu thất bại!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Lỗi khi truy vấn userId: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editKhoanThu(khoanThu: KhoanThu) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.diag_update_thu, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        // Tham chiếu tới các view
        val edtNgayThu = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateNgayThu)
        val edtNameThu = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateNameThu)
        val edtTienThu = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtupdateTienThu)
        val spinnerLoaiThu = dialogView.findViewById<Spinner>(R.id.spupdateLoaiThu)

        // Đặt giá trị hiện tại của KhoanThu
        edtNgayThu.setText(khoanThu.thoiDiemThu)
        edtNameThu.setText(khoanThu.tenThu)
        edtTienThu.setText(khoanThu.soTien.toString())

        // Tải danh sách LoạiThu và chọn loại thu hiện tại
        getLoaiThuList { loaiThuList ->
            val loaiThuAdapter = SpLoaiThuAdapter(requireContext(), loaiThuList)
            spinnerLoaiThu.adapter = loaiThuAdapter

            // Đặt spinner vào loại thu hiện tại
            loaiThuList.find { it.getIdLoaiThu() == khoanThu.idLoaiThu }?.let { selectedLoaiThu ->
                val spinnerPosition = loaiThuList.indexOf(selectedLoaiThu)
                spinnerLoaiThu.setSelection(spinnerPosition)
            }
        }

        // Hiển thị DatePicker khi nhấn vào edtNgayThu
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

        // Xử lý khi nhấn nút lưu
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

            val updatedKhoanThu = KhoanThu(
                idThu = khoanThu.idThu,
                tenThu = updatedName,
                loaiThu = updatedLoaiThu.getNameLoaiThu(),
                thoiDiemThu = updatedNgay,
                soTien = updatedTienThu,
                danhGia = khoanThu.danhGia,
                deleteFlag = khoanThu.deleteFlag,
                idLoaiThu = updatedLoaiThu.getIdLoaiThu()
            )

            // Xác định uid người dùng
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val userRef = FirebaseDatabase.getInstance().getReference("KhoanThu").child(uid)
                userRef.child(khoanThu.idThu).setValue(updatedKhoanThu).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cập nhật khoản thu thành công!", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Cập nhật khoản thu thất bại!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý khi nhấn nút hủy
        val btnCancel = dialogView.findViewById<Button>(R.id.btnHuyCapNhatThu)
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun getLoaiThuList(callback: (ArrayList<LoaiThu>) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            // Trường hợp không đăng nhập
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập!", Toast.LENGTH_SHORT).show()
            callback(ArrayList())
            return
        }

        // Truy vấn dữ liệu loại thu theo uid
        val database = FirebaseDatabase.getInstance().getReference("LoaiThu").child(uid)
        val loaiThuList = ArrayList<LoaiThu>()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loaiThuList.clear() // Đảm bảo danh sách được làm mới

                for (data in snapshot.children) {
                    val loaiThu = data.getValue(LoaiThu::class.java)
                    if (loaiThu != null) {
                        loaiThuList.add(loaiThu)
                    }
                }

                // Gọi callback để trả danh sách loại thu
                callback(loaiThuList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý khi có lỗi từ Firebase
                Toast.makeText(requireContext(), "Lỗi khi tải danh sách loại Thu: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(ArrayList()) // Trả về danh sách rỗng trong trường hợp lỗi
            }
        })
    }
}