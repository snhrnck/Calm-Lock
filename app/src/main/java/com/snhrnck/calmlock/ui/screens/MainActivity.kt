package com.snhrnck.calmlock

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.snhrnck.calmlock.data.AccentSage
import com.snhrnck.calmlock.data.AppConstants
import com.snhrnck.calmlock.data.AppPreferences
import com.snhrnck.calmlock.data.BgSlate
import com.snhrnck.calmlock.data.CardSlate
import com.snhrnck.calmlock.data.TextCream
import com.snhrnck.calmlock.data.TextMuted
import com.snhrnck.calmlock.data.translations
import com.snhrnck.calmlock.service.NotificationHelper
import com.snhrnck.calmlock.service.WallpaperService
import com.snhrnck.calmlock.ui.screens.GalleryScreen
import com.snhrnck.calmlock.ui.screens.HomeScreen
import com.snhrnck.calmlock.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val prefs = remember { AppPreferences(applicationContext) }

            LaunchedEffect(Unit) {
                if (prefs.mode != AppConstants.MODE_OFF) {
                    val intent = Intent(applicationContext, WallpaperService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BgSlate) {
                    AppNavigation(prefs)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(prefs: AppPreferences) {
    var currentScreen by remember { mutableStateOf("HOME") }
    val context = LocalContext.current
    val t = translations[prefs.language] ?: translations["EN"]!!


    var showBatteryDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val powerManager = context.getSystemService(PowerManager::class.java)
        val isIgnoringBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        if (!isIgnoringBattery && prefs.showBatteryPrompt) {
            showBatteryDialog = true
        }
    }

    if (showBatteryDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryDialog = false; prefs.showBatteryPrompt = false },
            title = { Text(t.batteryPromptTitle, color = TextCream, fontWeight = FontWeight.Bold) },
            text = { Text(t.batteryPromptMsg, color = TextMuted, fontSize = 15.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showBatteryDialog = false
                    prefs.showBatteryPrompt = false
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:${context.packageName}")
                    context.startActivity(intent)
                }) {
                    Text(t.allowBtn, color = AccentSage, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBatteryDialog = false
                    prefs.showBatteryPrompt = false
                }) {
                    Text(t.laterBtn, color = TextMuted)
                }
            },
            containerColor = CardSlate
        )
    }

    when (currentScreen) {
        "HOME" -> HomeScreen(
            prefs = prefs,
            onNavigateToGallery = { currentScreen = "GALLERY" },
            onNavigateToSettings = { currentScreen = "SETTINGS" }
        )
        "GALLERY" -> GalleryScreen(
            prefs = prefs,
            onBack = { currentScreen = "HOME" }
        )
        "SETTINGS" -> SettingsScreen(
            prefs = prefs,
            onBack = { currentScreen = "HOME" }
        )
    }
}