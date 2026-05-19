# ===================================================
# ProGuard Rules untuk LensaCerdas
# ===================================================

# --- Preserve line numbers for crash reports ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Retrofit & Gson ---
-keep class com.example.kotlinlensacerdasandroid.network.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

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
-keep class kotlin.Metadata { *; }