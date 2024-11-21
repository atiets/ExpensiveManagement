package com.example.expensivemanagement.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.expensivemanagement.MainActivity
import com.example.expensivemanagement.R

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "ReminderChannel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Lấy thông tin từ Intent
        val title = intent.getStringExtra("title") ?: "Lời nhắc"
        val message = intent.getStringExtra("message") ?: "Nội dung lời nhắc"

        // Tạo kênh thông báo
        createNotificationChannel(context)

        // Tạo Intent để mở ứng dụng khi nhấp vào thông báo
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tạo thông báo
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications) // Biểu tượng thông báo
            .setContentTitle(title)                   // Tiêu đề
            .setContentText(message)                  // Nội dung
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Độ ưu tiên cao
            .setContentIntent(pendingIntent)          // Gắn PendingIntent
            .setAutoCancel(true)                      // Đóng thông báo sau khi nhấp
            .build()

        // Hiển thị thông báo
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    // Tạo kênh thông báo (chỉ cần thực hiện một lần)
    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Reminder Notifications"
            val description = "Channel for Reminder Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                this.description = description
            }

            // Đăng ký kênh với hệ thống
            val notificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}