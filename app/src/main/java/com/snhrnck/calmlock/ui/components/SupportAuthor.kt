package com.snhrnck.calmlock.ui.components

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.snhrnck.calmlock.R
import com.snhrnck.calmlock.data.*

@Composable
fun SupportAuthorSection(
    buttonText: String,
    copyText: String,
    cancelText: String
) {
    var showSupportDialog by remember { mutableStateOf(false) }
    var showThanksImage by remember { mutableStateOf(false) }

    val walletPolygon = "0x0E9ECfe7eE9E0e2FEf6462A39e9B7a3e271BE7E9"
    val walletSolana = "67dMBxg5yXZEudrK2G1u5QYJJgQ7sp8jHDcWqZjQbsMH"
    val walletTron = "TUEZPrkvfcGHmaj6Uomjo1t5n35r1y3dy1"

    // Main button
    Button(
        onClick = { showSupportDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = AccentSage),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = TextCream)
        Spacer(modifier = Modifier.width(12.dp))
        Text(buttonText, color = TextCream, fontSize = 16.sp)
    }

    // Dialog with wallets
    if (showSupportDialog) {
        val clipboardManager = LocalClipboardManager.current

        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text(buttonText, color = TextCream, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NetworkRow("Polygon / Base / BNB", walletPolygon, copyText) {
                        clipboardManager.setText(AnnotatedString(walletPolygon))
                        showSupportDialog = false
                        showThanksImage = true
                    }
                    NetworkRow("Solana", walletSolana, copyText) {
                        clipboardManager.setText(AnnotatedString(walletSolana))
                        showSupportDialog = false
                        showThanksImage = true
                    }
                    NetworkRow("Tron", walletTron, copyText) {
                        clipboardManager.setText(AnnotatedString(walletTron))
                        showSupportDialog = false
                        showThanksImage = true
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupportDialog = false }) {
                    Text(cancelText, color = TextMuted)
                }
            },
            containerColor = CardSlate
        )
    }

    // Easter egg with raccoon
    if (showThanksImage) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        // Automatically close on pause/stop
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                    showThanksImage = false
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        // Music playback
        DisposableEffect(Unit) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.lofi_thanks)
            mediaPlayer?.setVolume(1f, 1f)
            mediaPlayer?.start()
            onDispose {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) player.stop()
                        player.release()
                    } catch (e: Exception) { }
                }
            }
        }

        // Pulse animation (72 BPM)
        val bpm = 72f
        val beatDurationMillis = (60000f / bpm).toInt()
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = beatDurationMillis
                    1f at 0
                    1.08f at (beatDurationMillis / 2)
                },
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        // Full screen raccoon display
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showThanksImage = false },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.raccoon_thanks),
                contentDescription = "Thank you",
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun NetworkRow(
    name: String,
    address: String,
    copyText: String,
    onCopy: () -> Unit
) {
    Surface(
        color = BgSlate,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextCream, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    address.take(6) + "..." + address.takeLast(4),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onCopy) {
                Text(copyText, color = AccentSand, textAlign = TextAlign.Center)
            }
        }
    }
}