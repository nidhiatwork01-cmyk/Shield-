# Add project specific ProGuard rules here.
# Keep Retrofit and Gson model classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.scamshield.app.api.** { *; }
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
