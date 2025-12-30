package uz.isti.kiosklock.data

data class DeviceInfo(
    val deviceName: String,
    val androidVersion: String,
    val apiLevel: Int,
    val manufacturer: String,
    val model: String,
    val isRooted: Boolean,
    val hasDeviceAdmin: Boolean,
    val hasOverlayPermission: Boolean
)