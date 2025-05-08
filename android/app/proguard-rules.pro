# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes SourceFile,LineNumberTable
-keepattributes Annotation, InnerClasses
-keep class kotlin.** { *; }
-keep class org.jetbrains.** { *; }
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.** { *; }
-keep class org.koin.** { *; }
-keep class * { public <init>(...); }
-keep class * extends androidx.room.RoomDatabase
-keep class com.aarokoinsaari.inwheel.** { *; }
-keep @androidx.room.Entity class *
-keep,includedescriptorclasses class io.github.jan.supabase.**$$serializer { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepclassmembers class kotlin.Metadata { *; }
-keepclassmembers class com.google.firebase.** { *; }
-dontnote kotlinx.serialization.SerializationKt
-dontwarn androidx.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn io.ktor.util.debug.**
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
