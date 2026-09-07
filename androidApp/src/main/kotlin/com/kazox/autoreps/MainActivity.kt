package com.kazox.autoreps

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // First paint, before composition runs: matches the system's own dark mode. Once the
        // app theme is known, the DisposableEffect below corrects it.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // enableEdgeToEdge above only knows the system's dark mode, but the app renders its
            // own theme (fixed Light/Dark in settings). Re-apply it whenever the effective theme
            // changes so the system-bar icons keep contrast with what is on screen.
            val isDark = rememberEffectiveDarkTheme()
            DisposableEffect(isDark) {
                val style =
                    if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
