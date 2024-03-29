# Keep enums within the project.
-keep enum app.cash.paykit.core.models.response.GrantType { *; }
-keep enum app.cash.paykit.core.models.sdk.CashAppPayCurrency { *; }
-keep enum app.cash.paykit.core.impl.RequestType { *; }
-keep enum app.cash.paykit.core.utils.ThreadPurpose { *; }


# Rules for Kotlin Serializer - a transitive dependency of KotlinX Datetime.
# Can probably be removed after datetime is updated.

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Don't print notes about potential mistakes or omissions in the configuration for kotlinx-serialization classes
# See also https://github.com/Kotlin/kotlinx.serialization/issues/1900
-dontnote kotlinx.serialization.**

# Serialization core uses `java.lang.ClassValue` for caching inside these specified classes.
# If there is no `java.lang.ClassValue` (for example, in Android), then R8/ProGuard will print a warning.
# However, since in this case they will not be used, we can disable these warnings
-dontwarn kotlinx.serialization.internal.ClassValueReferences

# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, replace classes with those containing named companion objects.
-keepattributes InnerClasses # Needed for `getDeclaredClasses`.

-if @kotlinx.serialization.Serializable class
kotlinx.datetime.Instant$Companion, # <-- List serializable classes with named companions.
kotlinx.datetime.Instant$Companion$serializer
{
    static **$* *;
}
-keepnames class <1>$$serializer { # -keepnames suffices; class is kept when serializer() is kept.
    static <1>$$serializer INSTANCE;
}

# Keep both serializer and serializable classes to save the attribute InnerClasses
-keepclasseswithmembers, allowshrinking, allowobfuscation, allowaccessmodification class
kotlinx.datetime.Instant$Companion, # <-- List serializable classes with named companions.
kotlinx.datetime.Instant$Companion$serializer
{
    *;
}

-dontwarn kotlinx.serialization.KSerializer
-dontwarn kotlinx.serialization.Serializable
-dontwarn kotlinx.serialization.descriptors.PrimitiveKind$STRING
-dontwarn kotlinx.serialization.descriptors.PrimitiveKind
-dontwarn kotlinx.serialization.descriptors.SerialDescriptor
-dontwarn kotlinx.serialization.descriptors.SerialDescriptorsKt
-dontwarn kotlinx.serialization.internal.AbstractPolymorphicSerializer