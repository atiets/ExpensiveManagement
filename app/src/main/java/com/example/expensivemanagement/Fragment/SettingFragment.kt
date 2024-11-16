package com.example.expensivemanagement.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensivemanagement.Adapter.SettingsAdapter
import com.example.expensivemanagement.Model.SettingItem
import com.example.expensivemanagement.PasscodeActivity
import com.example.expensivemanagement.R

class SettingFragment : Fragment(R.layout.fragment_setting) {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val settings = listOf(
            SettingItem(R.drawable.ic_pin, "Đặt mã PIN"),
            SettingItem(R.drawable.ic_personalize, "Cá nhân hóa"),
            SettingItem(R.drawable.ic_backup, "Back Up bằng Google Drive")
        )

        val adapter = SettingsAdapter(settings) { position ->
            when (position) {
                0 -> {
                    // Mở PasscodeActivity khi chọn "Đặt mã PIN"
                    val intent = Intent(requireContext(), PasscodeActivity::class.java)
                    startActivity(intent)
                }

                1 -> {
                    // Xử lý sự kiện cho "Cá nhân hóa"
                }

                2 -> {
                    // Xử lý sự kiện cho "Back Up bằng Google Drive"
                }
            }
        }

        recyclerView.adapter = adapter
        return view
    }
}