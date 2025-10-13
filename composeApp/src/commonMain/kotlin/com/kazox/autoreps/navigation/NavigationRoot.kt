package com.kazox.autoreps.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.kazox.autoreps.CounterScreen
import com.kazox.autoreps.StartScreen

@Composable
fun NavigationRoot() {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }

    NavDisplay(
        backStack = topLevelBackStack.backStack,
        onBack = { topLevelBackStack.removeLast() },
        entryProvider = entryProvider {
            entry<Home>{
                StartScreen(
                    topLevelBackStack,
                    onStart = {
                        topLevelBackStack.addTopLevel(Record)
                    }
                )
            }
            entry<Record>{
                CounterScreen()
            }
        },
    )
}