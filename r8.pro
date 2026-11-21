# Instantiated by Android using the manifest, not a Java call site.
-keep class local.dufs.manage.MainActivity { public <init>(); }
# This class resolves publicsuffixes.gz using a relative resource path.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Compile-only annotations and optional TLS provider implementations. The
# app does not bundle or select these providers; platform TLS remains enabled.
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
