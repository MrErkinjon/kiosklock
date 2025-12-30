package uz.isti.kiosklock.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import uz.isti.kiosklock.data.AppInfo

// App utilities
class AppUtils(private val context: Context) {

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledApps(): List<AppInfo> {

        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        // Launcher intent filter
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

        for (resolveInfo in resolveInfoList) {
            val packageInfo = resolveInfo.activityInfo.applicationInfo
            val name = packageManager.getApplicationLabel(packageInfo).toString()
            val isSystemApp = (packageInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            val icon = packageManager.getApplicationIcon(packageInfo)

            apps.add(AppInfo(packageInfo.packageName, name,isSystemApp, icon))
        }

        return apps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }






    fun isSystemApp(packageName: String): Boolean {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    fun getAppVersion(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}