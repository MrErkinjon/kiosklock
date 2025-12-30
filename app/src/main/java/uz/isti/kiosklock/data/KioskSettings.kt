package uz.isti.kiosklock.data

data class KioskSettings(
    val adminPassword: String = "1234",
    val kioskName: String = "KioskLock",
    val autoStartEnabled: Boolean = false,
    val allowedApps: Set<String> = emptySet(),
    val isKioskModeEnabled: Boolean = false
)
