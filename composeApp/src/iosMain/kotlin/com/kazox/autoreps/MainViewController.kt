package com.kazox.autoreps

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ComposeUIViewController
import com.kazox.autoreps.app.App
import com.kazox.autoreps.core.NativeViewFactory
import com.kazox.autoreps.di.initKoin

val LocalNativeViewFactory = staticCompositionLocalOf<NativeViewFactory> {
    error("No NativeViewFactory provided")
}

fun MainViewController(
    nativeViewFactory: NativeViewFactory,
) = ComposeUIViewController(
    configure = { initKoin() }
) {
    CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory) {
        App()
    }
}