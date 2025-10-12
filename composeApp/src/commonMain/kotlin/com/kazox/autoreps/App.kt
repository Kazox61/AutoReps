package com.kazox.autoreps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kazox.autoreps.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        var started by remember { mutableStateOf(false) }

        if (started) {
            CounterScreen()
        }
        else {
            StartScreen(
                onStart = { started = true }
            )
        }
    }
}