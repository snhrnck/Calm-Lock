# --- Calm Lock: Obfuscation Rules ---

# WorkManager creates Worker classes via reflection by name,
# so their names must not be obfuscated
-keep class * extends androidx.work.Worker { <init>(...); }
-keep class * extends androidx.work.CoroutineWorker { <init>(...); }
-keep class * extends androidx.work.ListenableWorker { <init>(...); }

# Data classes may be used in Bundle/Intent
-keepclassmembers class com.snhrnck.calmlock.** {
    <init>(...);
}

# Ignore warnings for optional dependencies
-dontwarn org.jetbrains.annotations.**

# --- R8 Protection for Room/WorkManager ---
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }

# --- STRICT PROTECTION OF OUR CLASSES ---
# Keep Worker (handles wallpaper changes)
-keep class com.snhrnck.calmlock.worker.** { *; }
# Keep Service (monitors screen state)
-keep class com.snhrnck.calmlock.service.** { *; }
# Keep Repository (contains wallpaper logic)
-keep class com.snhrnck.calmlock.data.WallpaperRepository { *; }