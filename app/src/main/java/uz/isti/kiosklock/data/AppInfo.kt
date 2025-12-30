package uz.isti.kiosklock.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val isSystemApp: Boolean,
    val icon: Drawable?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppInfo

        if (packageName != other.packageName) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}