# Consumer ProGuard rules for pum-library
# These rules are applied to apps that depend on this library

# ==========================================
# NOTIFICATIONS — FCM Service + Helper
# ==========================================
-keep class com.akustom15.pum.notifications.** { *; }

# ==========================================
# SECURITY — Anti-piracy system
# ==========================================
-keep class com.akustom15.pum.security.** { *; }

# ==========================================
# DATA — Preferences and models
# ==========================================
-keep class com.akustom15.pum.data.** { *; }
-keep class com.akustom15.pum.config.** { *; }

# ==========================================
# FIREBASE — Keep all Firebase-related classes
# ==========================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ==========================================
# AIDL — License verification
# ==========================================
-keep class com.android.vending.licensing.** { *; }
