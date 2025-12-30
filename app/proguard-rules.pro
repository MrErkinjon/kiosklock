# Add project specific ProGuard rules here.
-keep class com.kiosklock.app.** { *; }
-keepclassmembers class com.kiosklock.app.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep device admin receiver
-keep class uz.isti.kiosklock.receiver.KioskDeviceAdminReceiver { *; }

# Keep services
-keep class com.kiosklock.app.service.** { *; }

# Keep data classes
-keep class com.kiosklock.app.data.** { *; }