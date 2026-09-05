package com.kazox.autoreps.core.presentation

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * A window flag rather than a wake lock: it needs no permission, and the system drops it by
 * itself whenever the window stops being visible — so backgrounding the app cannot leave the
 * display pinned on.
 */
@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    val window = LocalContext.current.findActivity()?.window

    DisposableEffect(window, enabled) {
        if (window == null || !enabled) return@DisposableEffect onDispose { }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

/**
 * The Activity behind a composable's context, or null when there is none.
 *
 * `LocalContext.current` is not always the Activity — inside an AndroidView or a dialog it is a
 * ContextWrapper around it — so the chain has to be unwrapped rather than cast.
 */
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
