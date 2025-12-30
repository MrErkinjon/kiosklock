package uz.isti.kiosklock.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.isti.kioskapp.utils.PreferenceManager
import uz.isti.kiosklock.app.KioskActivity

class HomeWatcherService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "home_watch_service_channel"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }else{
            startForeground(NOTIFICATION_ID, createNotification())
        }

        serviceScope.launch {
            watchForHomePress()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private suspend fun watchForHomePress() {
        while (preferenceManager.isKioskModeEnabled()) {
            delay(1000)

            // Check if user pressed home button
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningTasks = activityManager.getRunningTasks(1)

            if (runningTasks.isNotEmpty()) {
                val topActivity = runningTasks[0].topActivity

                // If home launcher is active, redirect to kiosk
                if (topActivity?.packageName != packageName) {
                    val intent = Intent(this, KioskActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Home Watcher Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Home Watcher monitoring Service"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Home Watcher")
            .setContentText("Home Watcher is running")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
}
