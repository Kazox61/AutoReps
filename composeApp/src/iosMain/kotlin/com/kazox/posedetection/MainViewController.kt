package com.kazox.posedetection

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ComposeUIViewController

val LocalNativeViewFactory = staticCompositionLocalOf<NativeViewFactory> {
    error("No NativeViewFactory provided")
}

fun MainViewController(
    nativeViewFactory: NativeViewFactory
) = ComposeUIViewController {
    CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory) {
        App()
    }
}