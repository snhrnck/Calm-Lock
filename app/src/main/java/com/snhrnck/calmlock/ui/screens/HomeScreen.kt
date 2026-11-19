package com.snhrnck.calmlock.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snhrnck.calmlock.data.*
import com.snhrnck.calmlock.service.WallpaperService
import com.snhrnck.calmlock.ui.components.ModeSelectionButton
import com.snhrnck.calmlock.ui.components.OutlinedButtonCustom
import com.snhrnck.calmlock.ui.components.PrimaryButton
import com.snhrnck.calmlock.ui.components.WheelNumberPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    prefs: AppPreferences,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val t = translations[prefs.language] ?: translations["EN"]!!

    var savedFilesCount by remember { mutableStateOf(WallpaperRepository.getSavedFiles(context).size) }
    var activeMode by remember { mutableStateOf(prefs.mode) }
    var selectedHours by remember { mutableStateOf(prefs.hours) }
    var selectedMinutes by remember { mutableStateOf(prefs.minutes) }
    var showTimerPicker by remember { mutableStateOf(activeMode == AppConstants.MODE_TIMER) }

    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        savedFilesCount = WallpaperRepository.getSavedFiles(context).size
    }

    LaunchedEffect(savedFilesCount) {
        if (savedFilesCount == 0 && activeMode != AppConstants.MODE_OFF) {
            activeMode = AppConstants.MODE_OFF
            showTimerPicker = false
            prefs.mode = AppConstants.MODE_OFF
            context.stopService(Intent(context, WallpaperService::class.java))
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            isSaving = true
            scope.launch {
                var addedCount = 0
                var duplicateCount = 0
                withContext(Dispatchers.IO) {
                    uris.forEach { uri ->
                        val result = WallpaperRepository.saveOptimizedWallpaper(context, uri)
                        if (result == 1) addedCount++
                        if (result == 0) duplicateCount++
                    }
                }
                savedFilesCount = WallpaperRepository.getSavedFiles(context).size
                isSaving = false

                if (addedCount > 0) {
                    Toast.makeText(context, "${t.photoAdded}: $addedCount", Toast.LENGTH_SHORT).show()
                } else if (duplicateCount > 0) {
                    Toast.makeText(context, t.photoExists, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, t.photoAddError, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun startService() {
        val intent = Intent(context, WallpaperService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = t.appTitle, color = TextCream, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardSlate)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = AccentSand)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PrimaryButton(
                text = if (isSaving) "..." else t.addPhotos,
                icon = if (isSaving) null else Icons.Default.Add,
                modifier = Modifier.weight(1f).height(115.dp),
                enabled = !isSaving,
                onClick = {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )

            Button(
                onClick = {
                    // Force update the counter before navigating
                    savedFilesCount = WallpaperRepository.getSavedFiles(context).size
                    onNavigateToGallery()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).height(115.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$savedFilesCount", fontSize = 26.sp, color = AccentSand, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = t.myCollection, color = TextCream, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    val success = WallpaperRepository.applyRandomWallpaper(context)
                    if (!success) Toast.makeText(context, t.noPhotosForChange, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = savedFilesCount > 1,
            colors = ButtonDefaults.buttonColors(containerColor = CardSlate, disabledContainerColor = CardSlate.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Default.Autorenew, contentDescription = null, tint = AccentSand, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = t.changeNowBtn, color = TextCream, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = t.modeSectionTitle, color = TextMuted, fontSize = 18.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ModeSelectionButton(
                title = t.byTimer,
                subtitle = "",
                isSelected = (activeMode == AppConstants.MODE_TIMER),
                enabled = savedFilesCount > 1,
                modifier = Modifier.weight(1f),
                onClick = { showTimerPicker = true }
            )

            ModeSelectionButton(
                title = t.byScreenOff,
                subtitle = "",
                isSelected = (activeMode == AppConstants.MODE_SCREEN_OFF),
                enabled = savedFilesCount > 1,
                modifier = Modifier.weight(1f),
                onClick = {
                    activeMode = AppConstants.MODE_SCREEN_OFF
                    showTimerPicker = false
                    prefs.mode = AppConstants.MODE_SCREEN_OFF
                    startService()
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (showTimerPicker) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardSlate)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelNumberPicker(
                        label = t.hoursLabel,
                        range = 0..23,
                        initialValue = selectedHours,
                        onValueChange = { selectedHours = it }
                    )
                    Text(":", color = AccentSand, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    WheelNumberPicker(
                        label = t.minutesLabel,
                        range = 1..59,
                        initialValue = if (selectedMinutes == 0) 15 else selectedMinutes,
                        onValueChange = { selectedMinutes = it }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                PrimaryButton(
                    text = t.applyTimer,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = savedFilesCount > 1,
                    onClick = {
                        activeMode = AppConstants.MODE_TIMER
                        prefs.mode = AppConstants.MODE_TIMER
                        prefs.hours = selectedHours
                        prefs.minutes = selectedMinutes
                        prefs.lastChangeTime = System.currentTimeMillis() // Reset timer on start
                        startService()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CardSlate)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val statusMessage = when (activeMode) {
                AppConstants.MODE_TIMER -> "${t.activeTimerMsg}: ${selectedHours}h ${selectedMinutes}m"
                AppConstants.MODE_SCREEN_OFF -> t.activeScreenOffMsg
                else -> t.modeOffMsg
            }
            Text(
                text = statusMessage,
                color = if (activeMode == AppConstants.MODE_OFF) TextMuted else AccentSand,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            if (activeMode != AppConstants.MODE_OFF) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButtonCustom(
                    text = t.stopBtn,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        activeMode = AppConstants.MODE_OFF
                        showTimerPicker = false
                        prefs.mode = AppConstants.MODE_OFF
                        context.stopService(Intent(context, WallpaperService::class.java))
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}