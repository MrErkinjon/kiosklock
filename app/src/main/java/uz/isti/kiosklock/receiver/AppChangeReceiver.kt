package uz.isti.kiosklock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import uz.isti.kioskapp.utils.PreferenceManager

class AppChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val preferenceManager = PreferenceManager(context)

        if (!preferenceManager.isKioskModeEnabled()) {
            return
        }

        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // Handle app installation/removal
                val packageName = intent.data?.schemeSpecificPart

                // Optionally notify admin about app changes
                packageName?.let {
                    // Log app change
                }
            }
        }
    }
}