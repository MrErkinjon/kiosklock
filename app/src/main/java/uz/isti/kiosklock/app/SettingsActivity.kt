package uz.isti.kiosklock.app

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import uz.isti.kioskapp.utils.PreferenceManager
import uz.isti.kiosklock.data.AppInfo
import uz.isti.kiosklock.utils.AppUtils

class SettingsActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appUtils: AppUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(this)
        appUtils = AppUtils(this)

        setContent {
            ModernKioskLockTheme {
                SettingsScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen() {
        val context = LocalContext.current
        var adminPassword by remember { mutableStateOf(preferenceManager.getAdminPassword()) }
        var kioskName by remember { mutableStateOf(preferenceManager.getKioskName()) }
        var autoStart by remember { mutableStateOf(preferenceManager.isAutoStartEnabled()) }
        var allowedApps by remember { mutableStateOf(preferenceManager.getAllowedApps()) }
        var installedApps by remember { mutableStateOf(appUtils.getInstalledApps()) }
        var showPasswordDialog by remember { mutableStateOf(false) }
        var showAppSelector by remember { mutableStateOf(false) }

        var showAssetsDialog by remember { mutableStateOf(false) }
        var selectedImage by remember { mutableStateOf<String?>(preferenceManager.getBackgroundImagePath()) }
        val assetImages = remember {
            context.assets.list("")?.filter { it.endsWith(".png") || it.endsWith(".jpg") } ?: emptyList()
        }


        LaunchedEffect(Unit) {
            installedApps = appUtils.getInstalledApps()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1e3c72),
                            Color(0xFF2a5298),
                            Color(0xFF3b82f6)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Top Bar
                ModernTopBar(onBackClick = { finish() })

                // Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // General Settings
                    item {
                        AnimatedSettingsSection(
                            title = "General Settings",
                            icon = Icons.Default.Settings,
                            delay = 100
                        ) {
                            ModernSettingsCard {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Text(
                                        text = "Kiosk Display Name",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = kioskName,
                                        onValueChange = {
                                            kioskName = it
                                            preferenceManager.setKioskName(it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                                            focusedBorderColor = Color(0xFF3b82f6),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            cursorColor = Color(0xFF3b82f6)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        placeholder = { Text("Enter kiosk name", color = Color.White.copy(alpha = 0.5f)) }
                                    )
                                }
                            }
                        }
                    }

                    // Appearance Settings
                    item {
                        AnimatedSettingsSection(
                            title = "Appearance",
                            icon = Icons.Default.Palette,
                            delay = 200
                        ) {
                            ModernSettingsCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Background Image",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Customize your kiosk background",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }

                                    Button(
                                        onClick = { showAssetsDialog = true  },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF3b82f6)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        AssetImage(context = LocalContext.current, assetName = selectedImage?:"")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Choose", fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // Security Settings
                    item {
                        AnimatedSettingsSection(
                            title = "Security",
                            icon = Icons.Default.Security,
                            delay = 300
                        ) {
                            ModernSettingsCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Admin Password",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Required to exit kiosk mode",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }

                                    Button(
                                        onClick = { showPasswordDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFf59e0b)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Change", fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // Startup Settings
                    item {
                        AnimatedSettingsSection(
                            title = "Startup",
                            icon = Icons.Default.PlayArrow,
                            delay = 400
                        ) {
                            ModernSettingsCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Auto Start on Boot",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Automatically start kiosk mode after device restart",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }

                                    Switch(
                                        checked = autoStart,
                                        onCheckedChange = {
                                            autoStart = it
                                            preferenceManager.setAutoStartEnabled(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF10b981),
                                            uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                                            uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // App Management
                    item {
                        AnimatedSettingsSection(
                            title = "App Management",
                            icon = Icons.Default.Apps,
                            delay = 500
                        ) {
                            ModernSettingsCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Allowed Applications",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${allowedApps.size} apps selected for kiosk access",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }

                                    Button(
                                        onClick = { showAppSelector = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8b5cf6)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ManageAccounts,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Manage", fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // Statistics
                    item {
                        AnimatedSettingsSection(
                            title = "Statistics",
                            icon = Icons.Default.Analytics,
                            delay = 600
                        ) {
                            ModernSettingsCard {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    StatRow(
                                        label = "Total Installed Apps",
                                        value = installedApps.size.toString(),
                                        icon = Icons.Default.GetApp,
                                        color = Color(0xFF06b6d4)
                                    )
                                    Divider(
                                        color = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                    StatRow(
                                        label = "Allowed Apps",
                                        value = allowedApps.size.toString(),
                                        icon = Icons.Default.CheckCircle,
                                        color = Color(0xFF10b981)
                                    )
                                    Divider(
                                        color = Color.White.copy(alpha = 0.1f),
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                    StatRow(
                                        label = "System Apps",
                                        value = installedApps.count { appUtils.isSystemApp(it.packageName) }.toString(),
                                        icon = Icons.Default.Android,
                                        color = Color(0xFFf59e0b)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showPasswordDialog) {
            ModernPasswordDialog(
                currentPassword = adminPassword,
                onDismiss = { showPasswordDialog = false },
                onConfirm = { newPassword ->
                    adminPassword = newPassword
                    preferenceManager.setAdminPassword(newPassword)
                    showPasswordDialog = false
                    Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showAppSelector) {
            ModernAppSelectorDialog(
                installedApps = installedApps,
                allowedApps = allowedApps,
                onDismiss = { showAppSelector = false },
                onConfirm = { selectedApps ->
                    allowedApps = selectedApps
                    preferenceManager.setAllowedApps(selectedApps)
                    showAppSelector = false
                    Toast.makeText(context, "App selection updated", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showAssetsDialog) {
            AlertDialog(
                onDismissRequest = { showAssetsDialog = false },
                title = { Text("Choose Background") },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(assetImages) { imageName ->
                            val bitmap = remember(imageName) {
                                try {
                                    context.assets.open(imageName.toString()).use { inputStream ->
                                        BitmapFactory.decodeStream(inputStream)
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            bitmap?.asImageBitmap()?.let { img ->
                                Image(
                                    bitmap = img,
                                    contentDescription = imageName.toString(),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(80.dp)
                                        .clickable {
                                            selectedImage = imageName.toString()
                                            preferenceManager.setBackgroundImagePath(selectedImage?:"")
                                            showAssetsDialog = false
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                },
                confirmButton = {

                }
            )
        }

    }

    @Composable
    fun ModernTopBar(onBackClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Kiosk Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Configure your kiosk environment",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    @Composable
    fun AnimatedSettingsSection(
        title: String,
        icon: ImageVector,
        delay: Long,
        content: @Composable () -> Unit
    ) {
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(delay)
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight }, // pastdan chiqadi
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                content()
            }
        }
    }

    @Composable
    fun ModernSettingsCard(content: @Composable () -> Unit) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
        ) {
            content()
        }
    }

    @Composable
    fun StatRow(label: String, value: String, icon: ImageVector, color: Color) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 18.sp
            )
        }
    }

    @Composable
    fun ModernPasswordDialog(
        currentPassword: String,
        onDismiss: () -> Unit,
        onConfirm: (String) -> Unit
    ) {
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E)
                ),
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
                                Color(0xFFf59e0b).copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = Color(0xFFf59e0b),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Change Admin Password",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create a secure password for admin access",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Password fields
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFf59e0b),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color(0xFFf59e0b)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFf59e0b),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color(0xFFf59e0b)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        isError = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
                    )

                    if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Passwords do not match",
                            color = Color(0xFFff4444),
                            fontSize = 14.sp
                        )
                    }

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
                            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                if (newPassword == confirmPassword && newPassword.isNotEmpty()) {
                                    onConfirm(newPassword)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFf59e0b)
                            ),
                            enabled = newPassword == confirmPassword && newPassword.isNotEmpty()
                        ) {
                            Text("Update", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ModernAppSelectorDialog(
        installedApps: List<AppInfo>,
        allowedApps: Set<String>,
        onDismiss: () -> Unit,
        onConfirm: (Set<String>) -> Unit
    ) {
        var selectedApps by remember { mutableStateOf(allowedApps) }
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("System Apps", "User Apps")

        val systemApps = installedApps.filter { it.isSystemApp }
        val userApps = installedApps.filter { !it.isSystemApp }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E2E)
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Color(0xFF8b5cf6).copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = Color(0xFF8b5cf6),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Select Apps",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Choose which apps will be available in kiosk mode",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White.copy(alpha = 0.1f),
                        contentColor = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // App List
                    val appsToShow = if (selectedTab == 0) systemApps else userApps
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(appsToShow) { app ->
                            ModernAppSelectionItem(
                                app = app,
                                isSelected = selectedApps.contains(app.packageName),
                                onSelectionChanged = { isSelected ->
                                    selectedApps = if (isSelected) {
                                        selectedApps + app.packageName
                                    } else {
                                        selectedApps - app.packageName
                                    }
                                }
                            )
                        }
                    }

                    // Bottom buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
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
                            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = { onConfirm(selectedApps) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8b5cf6)
                            )
                        ) {
                            Text("Save Selection", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ModernAppSelectionItem(
        app: AppInfo,
        isSelected: Boolean,
        onSelectionChanged: (Boolean) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected)
                    Color(0xFF8b5cf6).copy(alpha = 0.2f)
                else
                    Color.White.copy(alpha = 0.05f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onSelectionChanged,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF8b5cf6),
                        uncheckedColor = Color.White.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.width(16.dp))

                app.icon?.let { drawable ->
                    Image(
                        bitmap = drawable.toBitmap(96, 96).asImageBitmap(),
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                if (app.isSystemApp) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFf59e0b).copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "System",
                            fontSize = 10.sp,
                            color = Color(0xFFf59e0b),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Medium
                        )
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
        if (bitmap!=null){
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                contentScale = ContentScale.Crop
            )
        }else{
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}