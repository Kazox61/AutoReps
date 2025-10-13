package com.kazox.autoreps

import androidx.compose.runtime.Composable
import com.kazox.autoreps.navigation.NavigationRoot
import com.kazox.autoreps.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        NavigationRoot()
    }
}