package com.kazox.autoreps

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    // Before the first frame: the home screen should load fully populated, not watch the seed land.
    seedDemoDataForStoreCaptures()
    return ComposeUIViewController { App() }
}