package com.example.expensivemanagement

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.expensivemanagement.fragment.SettingFragment
import com.example.expensivemanagement.fragment.ChartFragment
import com.example.expensivemanagement.fragment.ChiFragment
import com.example.expensivemanagement.fragment.DateFragment
import com.example.expensivemanagement.fragment.InforFragment
import com.example.expensivemanagement.fragment.RemindFragment
import com.example.expensivemanagement.fragment.ThuFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.main)
        navigationView = findViewById(R.id.nav_view)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) { // Kiểm tra nếu activity được tạo lần đầu
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChiFragment())
                .commit()
            navigationView.setCheckedItem(R.id.nav_chi)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.nav_thu) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ThuFragment()).commit()
        } else if (itemId == R.id.nav_chi) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChiFragment()).commit()
        } else if (itemId == R.id.nav_date) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DateFragment()).commit()
        } else if (itemId == R.id.nav_chart) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChartFragment()).commit()
        } else if (itemId == R.id.nav_remind) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RemindFragment()).commit()
        } else if (itemId == R.id.nav_setting) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingFragment()).commit()
        } else if (itemId == R.id.nav_about) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InforFragment()).commit()
        } else if (itemId == R.id.nav_logout) {
            thongBaoLogOut()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun thongBaoLogOut() {
        val builder = AlertDialog.Builder(this@MainActivity)
        builder.setMessage("Bạn có muốn đăng xuất?")
        builder.setCancelable(true)
        builder.setPositiveButton("Yes") { dialog, id ->
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this@MainActivity, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
            // Chuyển đến màn hình đăng nhập
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()  // Kết thúc MainActivity để không quay lại
            dialog.cancel()
        }
        builder.setNegativeButton("No") { dialog, id ->
            dialog.cancel()  // Đóng hộp thoại mà không làm gì
        }
        val alertDialog = builder.create()
        alertDialog.show()  // Hiển thị hộp thoại
    }
}
