package uz.isti.kiosklock.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import uz.isti.kiosklock.receiver.KioskDeviceAdminReceiver
import uz.isti.kioskapp.utils.PreferenceManager

class KioskManager(private val context: Context) {

    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, KioskDeviceAdminReceiver::class.java)
    private val preferenceManager = PreferenceManager(context)

    private fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    private fun isDeviceOwner(): Boolean {
        return devicePolicyManager.isDeviceOwnerApp(context.packageName)
    }

    fun isKioskModeActive(): Boolean {
        return preferenceManager.isKioskModeEnabled()
    }
    fun openHomeMode(activity: Activity){
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        activity.startActivity(intent)
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName?.equals(context.packageName, ignoreCase = true) == true
    }


    /**
     * Kiosk mode ni yoqish (faqat Device Owner bo‘lsa ishlaydi)
     */
    fun startKioskMode(activity: Activity) {
        if (!isDeviceAdminActive()) {
            throw SecurityException("Device admin permission not granted")
        }

        if (!isDefaultLauncher()) {
            throw SecurityException("App is not set as default launcher")
        }


        preferenceManager.setKioskModeEnabled(true)

        if (isDeviceOwner() && devicePolicyManager.isLockTaskPermitted(context.packageName)) {
            activity.startLockTask()
        }
    }

    /**
     * Kiosk mode ni to‘xtatish
     */
    fun stopKioskMode(activity: Activity) {
        if (isKioskModeActive()) {
            preferenceManager.setKioskModeEnabled(false)
            activity.stopLockTask()
        }
    }

    /**
     * Qaysi ilovalarga kiosk rejimda ruxsat berish
     */
    fun enableKioskApps(packageNames: List<String>) {
        if (isDeviceOwner()) {
            devicePolicyManager.setLockTaskPackages(
                adminComponent,
                packageNames.toTypedArray()
            )
        }
    }
}
