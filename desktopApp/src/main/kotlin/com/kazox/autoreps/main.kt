package com.kazox.autoreps

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kazox.autoreps.app.di.initKoin
import com.kazox.autoreps.app.di.seedDemoDataIfEmpty

fun main() = application {
    initKoin()
    seedDemoDataIfEmpty()
    val state = rememberWindowState(
        width = 393.dp,
        height = 852.dp,
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "AutoReps",
        state = state
    ) {
        App()
    }
}