# Add project specific ProGuard rules here.
# Preserve Kotlin metadata & annotations required by Moshi reflection, Room, and Coroutines
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-keep class kotlin.Metadata { *; }
-dontwarn kotlinx.coroutines.**

# Moshi KotlinJsonAdapterFactory reflection rules for Aura models, Room entities, & backup payloads
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class com.squareup.moshi.** { *; }
-keep class com.example.aura.data.model.** { *; }
-keepclassmembers class com.example.aura.data.model.** { *; }
-keep class com.example.aura.data.db.** { *; }
-keepclassmembers class com.example.aura.data.db.** { *; }
-keep class com.example.aura.data.repository.AuraBackupPayload { *; }
-keepclassmembers class com.example.aura.data.repository.AuraBackupPayload { *; }
-keep class com.example.aura.engine.RemoteExerciseEntry { *; }
-keepclassmembers class com.example.aura.engine.RemoteExerciseEntry { *; }

# Room & WorkManager workers / receivers
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.example.aura.notifications.** { *; }

# OkHttp / Okio / Retrofit / Coil
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
