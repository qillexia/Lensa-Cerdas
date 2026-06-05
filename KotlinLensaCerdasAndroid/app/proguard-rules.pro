# ===================================================
# ProGuard Rules untuk LensaCerdas
# ===================================================

# --- Preserve line numbers for crash reports ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Retrofit & Gson ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Preserve semua class di network package
-keep class com.example.kotlinlensacerdasandroid.network.** { *; }
-keepclassmembers class com.example.kotlinlensacerdasandroid.network.** { *; }

# Preserve specific model classes dengan semua members (properties, constructors, methods)
-keep class com.example.kotlinlensacerdasandroid.network.LoginRequest { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.LoginResponse { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.UserData { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.SummarizeRequest { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.SummarizeResponse { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.SummaryData { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.HistoryResponse { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.HistoryItem { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.UpdateRequest { 
    <init>(...);
    *;
}
-keep class com.example.kotlinlensacerdasandroid.network.BaseResponse { 
    <init>(...);
    *;
}

# Preserve Gson dan TypeAdapter
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * extends com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keepattributes Exceptions
-dontwarn okhttp3.**
-dontwarn okio.**

# --- OkHttp3 ---
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- iTextG (PDF Reader & Writer) ---
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# --- Google ML Kit (OCR) ---
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**

# --- Google Credential Manager (Sign-In) ---
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn com.google.android.libraries.identity.**
-keep class androidx.credentials.** { *; }

# --- Jetpack Compose (safe defaults) ---
-dontwarn androidx.compose.**

# --- Coil (Image Loading) ---
-dontwarn coil.**

# --- Lottie ---
-dontwarn com.airbnb.android.**
-keep class com.airbnb.android.** { *; }

# --- Keep Kotlin Metadata ---
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# --- Kotlin data class & synthetic methods ---
-keepclassmembers class kotlin.** {
    *** _clinit_(...);
    *;
}
-keep class kotlin.** { *; }

# Keep Kotlin functions and properties
-keepclasseswithmembernames class kotlin.** {
    native <methods>;
}