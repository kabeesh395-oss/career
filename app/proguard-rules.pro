# R8 / ProGuard Configuration for CareerPilot AI Native Android App

# Keep Room Database Entities and DAOs
-keep class com.example.careerpilot.data.model.** { *; }
-keep class com.example.careerpilot.data.local.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}
-dontwarn androidx.room.paging.**

# Keep Jetpack Compose Models & ViewModel States
-keep class com.example.careerpilot.ui.viewmodel.** { *; }
-keepclassmembers class * implements androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep Kotlin Serialization & Retrofit / OkHttp Models
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Firebase Auth & Firestore models
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Preserve Line Numbers for Crashlytics Stack Traces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
