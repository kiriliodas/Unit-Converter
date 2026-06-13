# Default ProGuard/R8 rules. The app uses no reflection, serialization, or
# native interop, so the optimized defaults are sufficient.
# Keep Compose tooling-friendly line numbers for any crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
