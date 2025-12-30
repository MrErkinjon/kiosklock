package uz.isti.kiosklock


import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import uz.isti.kioskapp.utils.SecureManager
import uz.isti.kiosklock.receiver.AppChangeReceiver

class KioskApplication : Application() {

    private lateinit var appChangeReceiver: AppChangeReceiver
    private lateinit var securityManager: SecureManager

    override fun onCreate() {
        super.onCreate()

        securityManager = SecureManager(this)

        // Check device security
        if (securityManager.isDeviceRooted()) {
            // Handle rooted device
        }

        // Register app change receiver
        appChangeReceiver = AppChangeReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(appChangeReceiver, filter)
    }

    override fun onTerminate() {
        super.onTerminate()
        try {
            unregisterReceiver(appChangeReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
    }
}