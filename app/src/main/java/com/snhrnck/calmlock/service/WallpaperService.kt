package com.snhrnck.calmlock.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.snhrnck.calmlock.data.AppConstants
import com.snhrnck.calmlock.data.AppPreferences
import com.snhrnck.calmlock.data.WallpaperRepository
import com.snhrnck.calmlock.data.translations
import com.snhrnck.calmlock.worker.WallpaperWorker
import kotlinx.coroutines.*

class WallpaperService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private lateinit var prefs: AppPreferences

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                if (prefs.mode == AppConstants.MODE_SCREEN_OFF) {
                    val timeSinceLastChange = System.currentTimeMillis() - prefs.lastChangeTime
                    if (timeSinceLastChange >= AppConstants.SCREEN_OFF_COOLDOWN_MS) {
                        triggerWallpaperChange()
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(this)
        NotificationHelper.createNotificationChannel(this)

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (WallpaperRepository.getSavedFiles(this).size <= 1) {
            prefs.mode = AppConstants.MODE_OFF
            stopSelf()
            return START_NOT_STICKY
        }

        val t = translations[prefs.language] ?: translations["EN"]!!
        val notificationText = if (prefs.mode == AppConstants.MODE_TIMER) t.activeTimerMsg else t.activeScreenOffMsg
        startForeground(1, NotificationHelper.buildNotification(this, notificationText))

        when (prefs.mode) {
            AppConstants.MODE_TIMER -> startTimer()
            AppConstants.MODE_SCREEN_OFF -> stopTimer()
            else -> stopSelf()
        }

        return START_STICKY
    }

    private fun startTimer() {
        timerJob?.cancel()
        val totalMinutes = (prefs.hours * 60 + prefs.minutes).coerceAtLeast(1)
        val intervalMillis = totalMinutes * 60_000L

        timerJob = serviceScope.launch {

            val timeSinceLastChange = System.currentTimeMillis() - prefs.lastChangeTime
            val timeLeft = if (timeSinceLastChange < 0) intervalMillis else (intervalMillis - timeSinceLastChange)


            if (timeLeft <= 0) {
                triggerWallpaperChange()
                prefs.lastChangeTime = System.currentTimeMillis()
            } else {
                delay(timeLeft)
                triggerWallpaperChange()
                prefs.lastChangeTime = System.currentTimeMillis()
            }


            while (isActive) {
                delay(intervalMillis)
                triggerWallpaperChange()
                prefs.lastChangeTime = System.currentTimeMillis()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun triggerWallpaperChange() {
        val workRequest = OneTimeWorkRequestBuilder<WallpaperWorker>().build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "ChangeWallpaper",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun onDestroy() {
        stopTimer()
        try { unregisterReceiver(screenOffReceiver) } catch (e: Exception) {}
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}