# --- Room ---
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Hilt / Dagger ---
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends androidx.hilt.work.WorkerAssistedFactory
-keep interface * extends androidx.hilt.work.WorkerAssistedFactory

# --- iText (PDF Generation) ---
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-dontwarn org.bouncycastle.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# --- Billing ---
-keep class com.android.billingclient.api.** { *; }

# --- General Android ---
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
