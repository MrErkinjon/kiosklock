package uz.isti.kiosklock.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import uz.isti.kioskapp.utils.PreferenceManager
import uz.isti.kiosklock.data.AppInfo
import uz.isti.kiosklock.ui.theme.KiosklockTheme
import uz.isti.kiosklock.utils.KioskManager
import java.text.SimpleDateFormat
import java.util.*

class KioskActivity : ComponentActivity() {

    private lateinit var kioskManager: KioskManager
    private lateinit var preferenceManager: PreferenceManager
    private var backPressCount = 0
    private var lastBackPressTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kioskManager = KioskManager(this)
        preferenceManager = PreferenceManager(this)

        setupKioskMode()

        setContent {
            KiosklockTheme {
                KioskScreen()
            }
        }
    }

    private fun setupKioskMode() {
        // Make activity fullscreen
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        // Set navigation bar color to match background
        window.navigationBarColor = Color(0xFF0A0A0F).toArgb()
        window.statusBarColor = Color.Transparent.toArgb()
    }

    @Composable
    fun KioskScreen() {
        val context = LocalContext.current
        val bgPath = preferenceManager.getBackgroundImagePath()
        val allowedApps by remember { mutableStateOf(getAllowedApps()) }
        var showAdminDialog by remember { mutableStateOf(false) }
        var adminPassword by remember { mutableStateOf("") }
        var currentTime by remember { mutableStateOf(getCurrentTime()) }
        var currentDate by remember { mutableStateOf(getCurrentDate()) }

        // Time updater
        LaunchedEffect(Unit) {
            while (true) {
                currentTime = getCurrentTime()
                currentDate = getCurrentDate()
                delay(1000)
            }
        }

        BackHandler {
            handleBackPress { showAdminDialog = true }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A0F),
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    )
                )
        ) {

            AssetImage(context = LocalContext.current, assetName = bgPath?:"")

            Column(modifier = Modifier.fillMaxSize()) {
                // Enhanced Top Bar
                ModernTopBar(
                    kioskName = preferenceManager.getKioskName(),
                    currentTime = currentTime,
                    currentDate = currentDate,
                    onAdminClick = { showAdminDialog = true }
                )

                // Apps Grid with animations
                AnimatedAppsGrid(
                    apps = allowedApps,
                    onAppClick = { app -> launchApp(app) }
                )
            }

            // Enhanced Admin Dialog
            if (showAdminDialog) {
                ModernAdminDialog(
                    password = adminPassword,
                    onPasswordChange = { adminPassword = it },
                    onDismiss = {
                        showAdminDialog = false
                        adminPassword = ""
                    },
                    onConfirm = {
                        if (validateAdminPassword(adminPassword)) {
                            exitKioskMode()
                        } else {
                            Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                            adminPassword = ""
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun ModernTopBar(
        kioskName: String,
        currentTime: String,
        currentDate: String,
        onAdminClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f), contentColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Kiosk info
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = kioskName,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "🔒 Kiosk Mode Active",
                        color = Color(0xFF4CAF50),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Button(
                        onClick = onAdminClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3).copy(alpha = 0.2f),
                            contentColor = Color(0xFF64B5F6)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Center - Date and Time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentTime,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentDate,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

            }

        }
    }

    @Composable
    fun AnimatedAppsGrid(
        apps: List<AppInfo>,
        onAppClick: (AppInfo) -> Unit
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(apps.size) { index ->
                val app = apps[index]
                var isVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(index * 50L) // Staggered animation
                    isVisible = true
                }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    ModernAppItem(
                        app = app,
                        onClick = { onAppClick(app) }
                    )
                }
            }
        }
    }

    @Composable
    fun ModernAppItem(app: AppInfo, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
        )

        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onClick()
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App icon with glow effect
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    app.icon?.let { drawable ->
                        Image(
                            bitmap = drawable.toBitmap(120, 120).asImageBitmap(),
                            contentDescription = app.name,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // App name with better typography
                Text(
                    text = app.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }

    @Composable
    fun ModernAdminDialog(
        password: String,
        onPasswordChange: (String) -> Unit,
        onDismiss: () -> Unit,
        onConfirm: () -> Unit
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dialog icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Color(0xFF2196F3).copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title
                    Text(
                        text = "Admin Access Required",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter admin password to exit kiosk mode",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.7f)
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text(
                                text = "Confirm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AssetImage(context: Context, assetName: String) {
        val bitmap = remember(assetName) {
            if (assetName.isEmpty()) null
            else try {
                context.assets.open(assetName).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            // Dark overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

        } else {
            // Default gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0A0F),
                                Color(0xFF1A1A2E),
                                Color(0xFF16213E)
                            )
                        )
                    )
            )
        }
    }


    private fun getAllowedApps(): List<AppInfo> {
        val allowedPackages = preferenceManager.getAllowedApps()
        val packageManager = packageManager
        val apps = mutableListOf<AppInfo>()

        allowedPackages.forEach { packageName ->
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val name = packageManager.getApplicationLabel(appInfo).toString()
                val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
                val icon = packageManager.getApplicationIcon(appInfo)
                apps.add(AppInfo(packageName, name, isSystemApp, icon))
            } catch (e: PackageManager.NameNotFoundException) {
                // App not found, skip
            }
        }

        return apps.sortedBy { it.name }
    }

    private fun launchApp(app: AppInfo) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, "Cannot launch ${app.name}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error launching app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleBackPress(onShowAdminDialog: () -> Unit) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastBackPressTime < 1000) {
            backPressCount++
        } else {
            backPressCount = 1
        }

        lastBackPressTime = currentTime

        if (backPressCount >= 5) {
            onShowAdminDialog()
            backPressCount = 0
        }
    }

    private fun validateAdminPassword(password: String): Boolean {
        val storedPassword = preferenceManager.getAdminPassword()
        return password == storedPassword
    }

    private fun exitKioskMode() {
        kioskManager.stopKioskMode(this)
        if (!kioskManager.isKioskModeActive()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm").format(Date())
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        return SimpleDateFormat("EEEE, MMMM d").format(Date())
    }

    override fun onResume() {
        super.onResume()
        setupKioskMode()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupKioskMode()
        }
    }
}