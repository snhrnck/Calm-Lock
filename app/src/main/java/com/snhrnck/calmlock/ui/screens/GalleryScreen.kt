package com.snhrnck.calmlock.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snhrnck.calmlock.data.*
import com.snhrnck.calmlock.service.WallpaperService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun GalleryScreen(
    prefs: AppPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val t = translations[prefs.language] ?: translations["EN"]!!
    var savedFiles by remember { mutableStateOf(WallpaperRepository.getSavedFiles(context)) }

    var fileToDelete by remember { mutableStateOf<File?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f).height(56.dp)
            ) {
                Text(t.backBtn, color = TextCream, fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (savedFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(t.emptyGallery, color = TextMuted, fontSize = 18.sp, textAlign = TextAlign.Center)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(savedFiles, key = { it.name }) { file ->
                    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, file) {
                        value = withContext(Dispatchers.IO) {
                            val thumbFile = WallpaperRepository.getThumbnailFile(context, file)
                            val targetFile = if (thumbFile.exists()) thumbFile else file

                            val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                            BitmapFactory.decodeFile(targetFile.absolutePath, opts)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardSlate)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)).background(BgSlate))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { fileToDelete = file },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerCoral),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(t.deleteBtn, color = TextCream, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showClearAllDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DangerCoral),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = TextCream)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t.clearCollectionBtn, color = TextCream, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }


    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text(text = t.deleteBtn, color = TextCream) },
            text = { Text(text = t.confirmDeleteMsg, color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    val thumb = WallpaperRepository.getThumbnailFile(context, fileToDelete!!)
                    thumb.delete()
                    fileToDelete?.delete()
                    savedFiles = WallpaperRepository.getSavedFiles(context)
                    fileToDelete = null


                    if (savedFiles.isEmpty()) {
                        prefs.mode = AppConstants.MODE_OFF
                        context.stopService(Intent(context, WallpaperService::class.java))
                    }
                }) {
                    Text(t.deleteBtn, color = DangerCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text(t.cancelBtn, color = TextMuted)
                }
            },
            containerColor = CardSlate
        )
    }


    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text(t.clearCollectionTitle, color = TextCream) },
            text = { Text(t.clearCollectionMsg, color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    WallpaperRepository.deleteAllWallpapers(context)
                    savedFiles = emptyList()
                    showClearAllDialog = false


                    prefs.mode = AppConstants.MODE_OFF
                    context.stopService(Intent(context, WallpaperService::class.java))
                }) {
                    Text(t.deleteBtn, color = DangerCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text(t.cancelBtn, color = TextMuted)
                }
            },
            containerColor = CardSlate
        )
    }
}