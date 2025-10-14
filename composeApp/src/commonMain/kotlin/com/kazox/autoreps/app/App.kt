package com.kazox.autoreps.app

import androidx.compose.runtime.Composable
import com.kazox.autoreps.app.navigation.NavigationRoot
import com.kazox.autoreps.core.presentation.theme.AppTheme
import com.kazox.autoreps.feature.home.presentation.HomeViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    homeViewModel: HomeViewModel = koinViewModel()
) {
    AppTheme {
        NavigationRoot()
    }
}