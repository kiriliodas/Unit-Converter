# Keep line numbers for readable crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- kotlinx.serialization ----------------------------------------------------
# Keep generated serializers and the annotations they rely on so R8/minify in
# the release build doesn't strip them (which would crash history/settings I/O).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep the serializer() lookup for all @Serializable classes in this app.
-keepclassmembers class com.blood.unitconverter.** {
    *** Companion;
}
-keepclasseswithmembers class com.blood.unitconverter.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.blood.unitconverter.**$$serializer { *; }

# kotlinx-serialization core (safe general rules).
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
    *** INSTANCE;
}
-keep class kotlinx.serialization.** { *; }
