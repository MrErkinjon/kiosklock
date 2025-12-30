package uz.isti.kiosklock.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import uz.isti.kiosklock.receiver.KioskDeviceAdminReceiver
import uz.isti.kiosklock.service.KioskService
import uz.isti.kiosklock.utils.KioskManager


class MainActivity : ComponentActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private lateinit var kioskManager: KioskManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, KioskDeviceAdminReceiver::class.java)
        kioskManager = KioskManager(this)

        setContent {
            ModernKioskLockTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current
        var isKioskActive by remember { mutableStateOf(kioskManager.isKioskModeActive()) }
        val hasDeviceAdminPermission by remember { mutableStateOf(devicePolicyManager.isAdminActive(adminComponent)) }
        var hasOverlayPermission by remember {
            mutableStateOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
            )
        }

        val overlayPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true
            }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667eea),
                            Color(0xFF764ba2),
                            Color(0xFF6B73FF),
                            Color(0xFF9400D3)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Header Section
                AnimatedHeader()

                Spacer(modifier = Modifier.height(16.dp))

                // Status Card
                AnimatedStatusCard(isKioskActive = isKioskActive)

                Spacer(modifier = Modifier.height(16.dp))

                // Permissions Section
                PermissionsSection(
                    hasDeviceAdminPermission = hasDeviceAdminPermission,
                    hasOverlayPermission = hasOverlayPermission,
                    onRequestDeviceAdmin = { requestDeviceAdminPermission() },
                    onRequestOverlay = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        overlayPermissionLauncher.launch(intent)
                    },
                    onOpenSettings = {
                        startActivity(Intent(context, SettingsActivity::class.java))
                    },
                    onOpenHomeSettings = {
                        kioskManager.openHomeMode(this@MainActivity)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Button
                AnimatedActionButton(
                    isKioskActive = isKioskActive,
                    canStart = hasDeviceAdminPermission && hasOverlayPermission,
                    onClick = {
                        if (isKioskActive) {
                            stopKioskMode()
                            isKioskActive = false
                        } else {
                            startKioskMode()
                            isKioskActive = true
                        }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    @Composable
    fun AnimatedHeader() {
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(300)
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 800,
                    easing = EaseOutBack
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 800,
                    easing = EaseOutBack
                )
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            Color.Transparent,
                            CircleShape
                        )
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "KioskLock",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Advanced Android Kiosk Solution",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    fun AnimatedStatusCard(isKioskActive: Boolean) {
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(600)
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Kiosk Status",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (isKioskActive) "Active" else "Inactive",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isKioskActive) Color(0xFF4CAF50) else Color(0xFFFF6B6B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                if (isKioskActive)
                                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                                else
                                    Color(0xFFFF6B6B).copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isKioskActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (isKioskActive) Color(0xFF4CAF50) else Color(0xFFFF6B6B),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PermissionsSection(
        hasDeviceAdminPermission: Boolean,
        hasOverlayPermission: Boolean,
        onRequestDeviceAdmin: () -> Unit,
        onRequestOverlay: () -> Unit,
        onOpenSettings: () -> Unit,
        onOpenHomeSettings: () -> Unit
    ) {
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(900)
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Setup & Configuration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ModernPermissionCard(
                    title = "Device Administrator",
                    description = "Required for kiosk mode control",
                    icon = Icons.Default.AdminPanelSettings,
                    isGranted = hasDeviceAdminPermission,
                    onAction = onRequestDeviceAdmin
                )

                ModernPermissionCard(
                    title = "Display Over Apps",
                    description = "Required for overlay protection",
                    icon = Icons.Default.Layers,
                    isGranted = hasOverlayPermission,
                    onAction = onRequestOverlay
                )

                ModernPermissionCard(
                    title = "Kiosk Settings",
                    description = "Configure apps and security",
                    icon = Icons.Default.Settings,
                    isGranted = null,
                    onAction = onOpenSettings
                )

                ModernPermissionCard(
                    title = "Home Settings",
                    description = "Set as default launcher",
                    icon = Icons.Default.Home,
                    isGranted = null,
                    onAction = onOpenHomeSettings
                )
            }
        }
    }

    @Composable
    fun ModernPermissionCard(
        title: String,
        description: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        isGranted: Boolean?,
        onAction: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                when (isGranted) {
                    true -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Granted",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    false -> {
                        Button(
                            onClick = onAction,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6B6B).copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Grant", fontWeight = FontWeight.Medium)
                        }
                    }

                    null -> {
                        IconButton(
                            onClick = onAction,
                            modifier = Modifier
                                .background(
                                    Color.Transparent,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Open",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AnimatedActionButton(
        isKioskActive: Boolean,
        canStart: Boolean,
        onClick: () -> Unit
    ) {
        var visible by remember { mutableStateOf(false) }
        var isPressed by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(1200)
            visible = true
        }

        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
        )

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            if (canStart) {
                Button(
                    onClick = {
                        isPressed = true
                        onClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isKioskActive)
                            Color(0xFFFF6B6B)
                        else
                            Color(0xFF4CAF50),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 12.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isKioskActive) Icons.Filled.Clear else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isKioskActive) "Stop Kiosk Mode" else "Start Kiosk Mode",
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Complete setup to enable kiosk mode",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    private fun requestDeviceAdminPermission() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "KioskLock requires device administrator permission to enable kiosk mode."
        )
        startActivity(intent)
    }

    private fun startKioskMode() {
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            kioskManager.startKioskMode(this)
            startService(Intent(this, KioskService::class.java))
            Toast.makeText(this, "Kiosk mode activated", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Device admin permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopKioskMode() {
        kioskManager.stopKioskMode(this)
        stopService(Intent(this, KioskService::class.java))
        Toast.makeText(this, "Kiosk mode deactivated", Toast.LENGTH_SHORT).show()
    }

}

@Composable
fun ModernKioskLockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6B73FF),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF0A0A0F),
            surface = Color(0xFF1E1E2E)
        ),
        content = content
    )
}