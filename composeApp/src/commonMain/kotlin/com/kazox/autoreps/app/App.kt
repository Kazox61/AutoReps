package com.kazox.autoreps.app

import androidx.compose.runtime.Composable
import com.kazox.autoreps.app.navigation.NavigationRoot
import com.kazox.autoreps.core.presentation.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        NavigationRoot()
    }
}