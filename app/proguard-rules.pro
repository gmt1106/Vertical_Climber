# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep game classes
-keep class com.yourpackage.mountaingoat.** { *; }

# Keep vector math
-keep class com.yourpackage.mountaingoat.utils.Vector2 { *; }
