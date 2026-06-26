# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# Gson
-keep class iss.nus.edu.sg.weather.data.model.** { *; }
-keepclassmembers class iss.nus.edu.sg.weather.data.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# EdDSA
-keep class net.i2p.crypto.eddsa.** { *; }

# Coil
-keep class coil3.** { *; }
-dontwarn coil3.PlatformContext

# EdDSA
-dontwarn sun.security.x509.X509Key
