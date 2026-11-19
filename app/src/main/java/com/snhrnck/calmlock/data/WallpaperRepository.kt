package com.snhrnck.calmlock.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

object WallpaperRepository {

    fun getPhotosDir(context: Context): File {
        return File(context.filesDir, AppConstants.WALLPAPER_DIR).apply { mkdirs() }
    }

    fun getSavedFiles(context: Context): List<File> {
        return getPhotosDir(context).listFiles()
            ?.filter { it.isFile && !it.name.startsWith("thumb_") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun getThumbnailFile(context: Context, originalFile: File): File {
        return File(getPhotosDir(context), "thumb_${originalFile.name}")
    }

    suspend fun saveOptimizedWallpaper(context: Context, uri: Uri): Int = withContext(Dispatchers.IO) {
        try {

            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext -1
            val digest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            inputStream.close()

            val hashString = digest.digest().joinToString("") { "%02x".format(it) }
            val fileName = "img_$hashString.jpg"

            val outFile = File(getPhotosDir(context), fileName)
            if (outFile.exists()) {
                return@withContext 0
            }

            val metrics = context.resources.displayMetrics
            val targetW = metrics.widthPixels.coerceAtLeast(1080)
            val targetH = metrics.heightPixels.coerceAtLeast(2400)

            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOptions)
            }

            var sampleSize = 1
            while ((boundsOptions.outWidth / (sampleSize * 2)) >= targetW &&
                (boundsOptions.outHeight / (sampleSize * 2)) >= targetH) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            var rawBitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return@withContext -1

            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val exif = ExifInterface(input)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    val rotationDegrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                    if (rotationDegrees != 0f) {
                        val matrix = Matrix().apply { postRotate(rotationDegrees) }
                        val rotatedBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                        if (rotatedBitmap != rawBitmap) {
                            rawBitmap.recycle()
                            rawBitmap = rotatedBitmap
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val scale = maxOf(targetW.toFloat() / rawBitmap.width, targetH.toFloat() / rawBitmap.height)
            val scaledW = (rawBitmap.width * scale).toInt()
            val scaledH = (rawBitmap.height * scale).toInt()

            val scaledBitmap = Bitmap.createScaledBitmap(rawBitmap, scaledW, scaledH, true)
            val xOff = ((scaledW - targetW) / 2).coerceAtLeast(0)
            val yOff = ((scaledH - targetH) / 2).coerceAtLeast(0)

            val finalBitmap = Bitmap.createBitmap(
                scaledBitmap,
                xOff, yOff,
                targetW.coerceAtMost(scaledW - xOff),
                targetH.coerceAtMost(scaledH - yOff)
            )

            FileOutputStream(outFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }

            val thumbFile = File(getPhotosDir(context), "thumb_$fileName")
            val thumbBitmap = Bitmap.createScaledBitmap(finalBitmap, finalBitmap.width / 4, finalBitmap.height / 4, true)
            FileOutputStream(thumbFile).use { out ->
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
            }
            thumbBitmap.recycle()

            if (scaledBitmap != rawBitmap) scaledBitmap.recycle()
            if (finalBitmap != scaledBitmap) finalBitmap.recycle()
            if (!rawBitmap.isRecycled) rawBitmap.recycle()

            return@withContext 1
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext -1
        }
    }

    suspend fun applyRandomWallpaper(context: Context): Boolean = withContext(Dispatchers.IO) {
        val files = getSavedFiles(context)
        if (files.size <= 1) return@withContext false

        val prefs = AppPreferences(context)
        val lastFile = prefs.lastFile

        val chosenFile = if (prefs.isSequential) {

            val sortedFiles = files.sortedBy { it.lastModified() }
            if (lastFile.isEmpty() || sortedFiles.none { it.name == lastFile }) {
                sortedFiles.first()
            } else {
                val currentIndex = sortedFiles.indexOfFirst { it.name == lastFile }
                if (currentIndex != -1 && currentIndex < sortedFiles.size - 1) {
                    sortedFiles[currentIndex + 1]
                } else {
                    sortedFiles.first()
                }
            }
        } else {
            val available = files.filter { it.name != lastFile }
            available.random()
        }

        prefs.lastFile = chosenFile.name
        prefs.lastChangeTime = System.currentTimeMillis()

        val wallpaperManager = WallpaperManager.getInstance(context)
        try {
            FileInputStream(chosenFile).use { inputStream ->
                wallpaperManager.setStream(inputStream, null, true, WallpaperManager.FLAG_LOCK)
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    fun deleteAllWallpapers(context: Context) {
        val files = getPhotosDir(context).listFiles() ?: return
        for (file in files) {
            file.delete()
        }
    }
}