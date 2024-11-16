package com.example.expensivemanagement.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.expensivemanagement.adapter.PageChiAdapter
import com.example.expensivemanagement.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener

class ChiFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_chi, null)
        val tabLayout = v.findViewById<TabLayout>(R.id.main_tab)
        val viewPager = v.findViewById<ViewPager>(R.id.main_viewpager)
        val pagechiAdapter = PageChiAdapter(childFragmentManager)
        viewPager.adapter = pagechiAdapter
        tabLayout.setupWithViewPager(viewPager)
        viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        return v
    }
}