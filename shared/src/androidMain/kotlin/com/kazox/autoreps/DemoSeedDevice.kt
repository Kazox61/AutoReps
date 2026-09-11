package com.kazox.autoreps

import android.os.Build

actual fun isStoreCaptureDevice(): Boolean =
    Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.contains("emulator") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("sdk_gphone") ||
        Build.PRODUCT.startsWith("sdk")
