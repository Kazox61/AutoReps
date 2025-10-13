package com.kazox.autoreps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.kazox.autoreps.navigation.NavigationRoot
import com.kazox.autoreps.theme.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    mainViewModel: MainViewModel = koinViewModel()
) {
    AppTheme {
        NavigationRoot()
    }



    LaunchedEffect(Unit) {
        delay(2000)
        mainViewModel.addWorkout()
    }
}