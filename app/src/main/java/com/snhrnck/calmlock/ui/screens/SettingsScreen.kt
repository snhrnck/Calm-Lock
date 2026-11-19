package com.snhrnck.calmlock.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snhrnck.calmlock.data.*
import com.snhrnck.calmlock.ui.components.SupportAuthorSection

@Composable
fun SettingsScreen(
    prefs: AppPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val t = translations[prefs.language] ?: translations["EN"]!!

    var showLangMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var isSequential by remember { mutableStateOf(prefs.isSequential) }

    val languages = listOf("RU", "EN", "DE", "ES", "PT", "ID", "HI", "FR", "IT", "ZH", "FIL", "VI")

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(t.backBtn, color = TextCream, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(t.settingsTitle, color = TextCream, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        // Language selection
        Box {
            Button(
                onClick = { showLangMenu = true },
                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = AccentSand)
                Spacer(modifier = Modifier.width(12.dp))
                Text("${t.langTitle}: ${prefs.language}", color = TextCream, fontSize = 16.sp)
            }
            DropdownMenu(
                expanded = showLangMenu,
                onDismissRequest = { showLangMenu = false },
                modifier = Modifier.background(CardSlate)
            ) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang, color = TextCream) },
                        onClick = { prefs.language = lang; showLangMenu = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hide notification
        Button(
            onClick = {
                try {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        putExtra(Settings.EXTRA_CHANNEL_ID, "calm_wallpaper_channel")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, t.openSettingsError, Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = AccentSand)
            Spacer(modifier = Modifier.width(12.dp))
            Text(t.hideNotification, color = TextCream, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Battery optimization
        val powerManager = context.getSystemService(PowerManager::class.java)
        val isIgnoringBattery = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        if (!isIgnoringBattery) {
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:${context.packageName}")
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DangerCoral),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = TextCream)
                Spacer(modifier = Modifier.width(12.dp))
                Text(t.batteryBtn, color = TextCream, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Sequential / Random toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardSlate)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sort, contentDescription = null, tint = AccentSand)
                Spacer(modifier = Modifier.width(12.dp))
                Text(if (isSequential) t.sequentialMode else t.randomMode, color = TextCream, fontSize = 16.sp)
            }
            Switch(
                checked = isSequential,
                onCheckedChange = {
                    isSequential = it
                    prefs.isSequential = it
                },
                colors = SwitchDefaults.colors(checkedThumbColor = AccentSage, checkedTrackColor = AccentSage.copy(alpha = 0.5f))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // About App
        Button(
            onClick = { showAboutDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = AccentSand)
            Spacer(modifier = Modifier.width(12.dp))
            Text(t.aboutApp, color = TextCream, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Support Author Section (New feature with raccoon)
        SupportAuthorSection(
            buttonText = t.supportAuthor,
            copyText = t.copyAddress,
            cancelText = t.cancelBtn
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = t.versionText,
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }

    // About App Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(t.aboutApp, color = TextCream) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(t.aboutDesc, color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("${t.byTimer}", color = AccentSand, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(t.timerModeDesc, color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("${t.byScreenOff}", color = AccentSand, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(t.screenOffModeDesc, color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("${t.sequentialMode}", color = AccentSand, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(t.seqModeDesc, color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("${t.randomMode}", color = AccentSand, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(t.randModeDesc, color = TextMuted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(t.privacyPolicy, color = AccentSand, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(t.privacyDesc, color = TextMuted, fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = AccentSage)
                }
            },
            containerColor = CardSlate
        )
    }
}