package com.example.expensivemanagement.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.expensivemanagement.fragment.KhoanThuFragment
import com.example.expensivemanagement.fragment.LoaiThuFragment

class PageThuAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> KhoanThuFragment()
        1 -> LoaiThuFragment()
        else -> throw IllegalArgumentException("Vị trí không hợp lệ $position")
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence = when (position) {
        0 -> "Khoản Thu"
        1 -> "Loại Thu"
        else -> ""
    }
}
