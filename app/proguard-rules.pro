# Add project specific ProGuard rules here.
# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.sahayakai.app.models.** { *; }
# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
