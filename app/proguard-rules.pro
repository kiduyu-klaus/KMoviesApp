# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Jsoup classes
-keep class org.jsoup.** { *; }
-keeppackagenames org.jsoup.nodes

# Keep ExoPlayer classes
-keep class androidx.media3.** { *; }

# Keep Glide classes
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Keep model classes
-keep class com.klaus.kmoviesapp.models.** { *; }
