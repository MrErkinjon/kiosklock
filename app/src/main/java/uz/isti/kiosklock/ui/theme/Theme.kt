package uz.isti.kiosklock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Custom color palette for modern kiosk interface
private val KioskLightColorScheme = lightColorScheme(
    primary = Color(0xFF6B73FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9BB5FF),
    onPrimaryContainer = Color(0xFF001947),

    secondary = Color(0xFF8B5CF6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1C4E9),
    onSecondaryContainer = Color(0xFF311B92),

    tertiary = Color(0xFF03DAC6),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF80CBC4),
    onTertiaryContainer = Color(0xFF004D40),

    error = Color(0xFFFF6B6B),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B1B1F),

    surfaceVariant = Color(0xFFE2E1EC),
    onSurfaceVariant = Color(0xFF45464F),
    outline = Color(0xFF75777F),
    outlineVariant = Color(0xFFC5C6D0),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF303034),
    inverseOnSurface = Color(0xFFF2F0F4),
    inversePrimary = Color(0xFF9BB5FF)
)

private val KioskDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9BB5FF),
    onPrimary = Color(0xFF001947),
    primaryContainer = Color(0xFF0031A3),
    onPrimaryContainer = Color(0xFF9BB5FF),

    secondary = Color(0xFFB794F6),
    onSecondary = Color(0xFF311B92),
    secondaryContainer = Color(0xFF5E35B1),
    onSecondaryContainer = Color(0xFFD1C4E9),

    tertiary = Color(0xFF4DD0E1),
    onTertiary = Color(0xFF003A40),
    tertiaryContainer = Color(0xFF006064),
    onTertiaryContainer = Color(0xFF80CBC4),

    error = Color(0xFFFF8A80),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0A0A0F),
    onBackground = Color(0xFFE4E2E6),
    surface = Color(0xFF1A1A2E),
    onSurface = Color(0xFFE4E2E6),

    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    outline = Color(0xFF8F9099),
    outlineVariant = Color(0xFF45464F),

    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE4E2E6),
    inverseOnSurface = Color(0xFF1B1B1F),
    inversePrimary = Color(0xFF6B73FF)
)

// Extended color palette for special UI components
object KioskColors {
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)

    // Gradient colors
    val GradientStart = Color(0xFF667eea)
    val GradientMiddle = Color(0xFF764ba2)
    val GradientEnd = Color(0xFF9400D3)

    // Glass effect colors
    val GlassLight = Color.White.copy(alpha = 0.15f)
    val GlassDark = Color.Black.copy(alpha = 0.3f)

    // Status colors
    val ActiveGreen = Color(0xFF10b981)
    val InactiveRed = Color(0xFFef4444)
    val PendingOrange = Color(0xFFf59e0b)
}

@Composable
fun KiosklockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> KioskDarkColorScheme
        else -> KioskLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KioskTypography,
        shapes = KioskShapes,
        content = content
    )
}

// Modern typography scale
private val KioskTypography = androidx.compose.material3.Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 57.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 45.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = androidx.compose.ui.text.TextStyle(
        fontSize = 36.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 32.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 28.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 24.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 22.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontSize = 12.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontSize = 11.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Custom shapes for modern look
private val KioskShapes = androidx.compose.material3.Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

// Extension functions for gradient backgrounds
@Composable
fun getKioskGradientBrush() = androidx.compose.ui.graphics.Brush.verticalGradient(
    colors = listOf(
        KioskColors.GradientStart,
        KioskColors.GradientMiddle,
        KioskColors.GradientEnd
    )
)

@Composable
fun getGlassEffect(isDark: Boolean = isSystemInDarkTheme()) =
    if (isDark) KioskColors.GlassLight else KioskColors.GlassDark