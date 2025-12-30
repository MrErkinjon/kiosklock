package uz.isti.kiosklock.service


import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import uz.isti.kioskapp.utils.PreferenceManager
import uz.isti.kiosklock.app.KioskActivity
import uz.isti.kiosklock.utils.KioskManager

class KioskService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var kioskManager: KioskManager
    private lateinit var preferenceManager: PreferenceManager

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "kiosk_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        kioskManager = KioskManager(this)
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
            monitorKioskMode()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private suspend fun monitorKioskMode() {
        while (preferenceManager.isKioskModeEnabled()) {
            delay(5000) // Check every 5 seconds

            // Ensure kiosk activity is running
            if (!isKioskActivityRunning()) {
                launchKioskActivity()
            }

            // Monitor for unauthorized apps
            checkRunningApps()
        }
    }

    private fun isKioskActivityRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(1)

        return if (runningTasks.isNotEmpty()) {
            val topActivity = runningTasks[0].topActivity
            topActivity?.className == KioskActivity::class.java.name
        } else {
            false
        }
    }

    private fun launchKioskActivity() {
        val intent = Intent(this, KioskActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun checkRunningApps() {
        val allowedApps = preferenceManager.getAllowedApps()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        try {
            val runningApps = activityManager.runningAppProcesses
            runningApps?.forEach { processInfo ->
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    val packageName = processInfo.processName

                    if (!allowedApps.contains(packageName) && packageName != this.packageName) {
                        // Kill unauthorized app
                        activityManager.killBackgroundProcesses(packageName)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle security exceptions
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kiosk Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Kiosk mode monitoring service"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("KioskLock Active")
            .setContentText("Kiosk mode is running")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
}