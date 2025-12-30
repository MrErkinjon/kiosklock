package uz.isti.kiosklock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import uz.isti.kiosklock.service.KioskService
import uz.isti.kioskapp.utils.PreferenceManager
import uz.isti.kiosklock.app.KioskActivity

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            val preferenceManager = PreferenceManager(context)

            if (preferenceManager.isAutoStartEnabled() && preferenceManager.isKioskModeEnabled()) {
                // Start kiosk service
                val serviceIntent = Intent(context, KioskService::class.java)
                context.startService(serviceIntent)

                // Launch kiosk activity
                val activityIntent = Intent(context, KioskActivity::class.java)
                activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(activityIntent)
            }
        }
    }
}