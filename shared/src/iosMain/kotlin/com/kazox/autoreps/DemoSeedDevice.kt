package com.kazox.autoreps

import platform.Foundation.NSProcessInfo

actual fun isStoreCaptureDevice(): Boolean =
    NSProcessInfo.processInfo.environment.containsKey("SIMULATOR_DEVICE_NAME")
