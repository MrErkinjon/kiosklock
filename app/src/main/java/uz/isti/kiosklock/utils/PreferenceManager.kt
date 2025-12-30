package uz.isti.kioskapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build


class PreferenceManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("kiosk_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_KIOSK_ENABLED = "kiosk_enabled"
        private const val KEY_ADMIN_PASSWORD = "admin_password"
        private const val KEY_ALLOWED_APPS = "allowed_apps"
        private const val KEY_KIOSK_NAME = "kiosk_name"
        private const val KEY_AUTO_START = "auto_start"
        private const val DEFAULT_PASSWORD = "1234"
        private const val KIOSK_BACKGROUND = "kiosk_background"
    }

    fun isKioskModeEnabled(): Boolean {
        return preferences.getBoolean(KEY_KIOSK_ENABLED, false)
    }

    fun setKioskModeEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_KIOSK_ENABLED, enabled).apply()
    }

    fun getAdminPassword(): String {
        return preferences.getString(KEY_ADMIN_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    fun setAdminPassword(password: String) {
        preferences.edit().putString(KEY_ADMIN_PASSWORD, password).apply()
    }

    fun getAllowedApps(): Set<String> {
        return preferences.getStringSet(KEY_ALLOWED_APPS, getDefaultAppsForDevice()) ?: getDefaultAppsForDevice()
    }

    fun setAllowedApps(apps: Set<String>) {
        preferences.edit().putStringSet(KEY_ALLOWED_APPS, apps).apply()
    }

    fun getKioskName(): String {
        return preferences.getString(KEY_KIOSK_NAME, "KioskLock") ?: "KioskLock"
    }

    fun setKioskName(name: String) {
        preferences.edit().putString(KEY_KIOSK_NAME, name).apply()
    }

    fun isAutoStartEnabled(): Boolean {
        return preferences.getBoolean(KEY_AUTO_START, false)
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_START, enabled).apply()
    }

    fun setBackgroundImagePath(path: String) {
        preferences.edit().putString(KIOSK_BACKGROUND, path).apply()
    }

    fun getBackgroundImagePath(): String? {
        return preferences.getString(KIOSK_BACKGROUND, null)
    }

    private fun getDefaultAppsForDevice(): Set<String> {
        val brand = Build.BRAND.lowercase() // misol: "samsung", "xiaomi", "huawei"
        val universal = setOf(
            "com.android.settings",
            "com.android.contacts",
            "com.android.dialer",
            "com.android.browser",
            "com.android.gallery3d",
            "com.android.camera",
            "com.google.android.apps.chrome",
            "com.google.android.apps.maps",
            "com.android.documentsui"
        )

        val samsung = setOf(
            "com.sec.android.app.camera",
            "com.sec.android.gallery3d",
            "com.samsung.android.contacts",
            "com.samsung.android.dialer",
            "com.samsung.android.messaging",
            "com.sec.android.app.sbrowser",
            "com.sec.android.app.myfiles"
        )

        val xiaomi = setOf(
            "com.miui.camera",
            "com.miui.gallery",
            "com.miui.contacts",
            "com.miui.dialer",
            "com.miui.mms",
            "com.mi.android.globalFileexplorer"
        )

        val huawei = setOf(
            "com.huawei.camera",
            "com.huawei.photos",
            "com.huawei.contacts",
            "com.huawei.mms",
            "com.huawei.hidisk"
        )

        val oppo = setOf(
            "com.oppo.camera",
            "com.coloros.gallery3d",
            "com.coloros.contacts",
            "com.coloros.filemanager"
        )

        val vivo = setOf(
            "com.vivo.camera",
            "com.vivo.gallery",
            "com.vivo.contacts",
            "com.vivo.dialer",
            "com.vivo.filemanager"
        )

        val googlePixel = setOf(
            "com.google.android.GoogleCamera",
            "com.google.android.contacts",
            "com.google.android.dialer",
            "com.google.android.apps.photos",
            "com.google.android.apps.messaging",
            "com.google.android.apps.maps",
            "com.google.android.apps.chrome"
        )

        return when {
            brand.contains("samsung") -> universal + samsung
            brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco") -> universal + xiaomi
            brand.contains("huawei") || brand.contains("honor") -> universal + huawei
            brand.contains("oppo") || brand.contains("realme") -> universal + oppo
            brand.contains("vivo") -> universal + vivo
            brand.contains("google") || brand.contains("pixel") -> universal + googlePixel
            else -> universal // Noma'lum brend, universal paketlar yetarli
        }
    }

}