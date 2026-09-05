package com.kazox.autoreps.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.kazox.ui.components.icon.KazIcons
import com.kazox.ui.components.navigationbar.NavigationBar
import com.kazox.ui.components.navigationbar.NavigationBarAction
import com.kazox.ui.components.navigationbar.NavigationBarAnimation
import com.kazox.ui.components.navigationbar.NavigationBarItem
import com.kazox.ui.components.navigationbar.NavigationBarVariant

@Composable
fun BottomNavigationBar(
    onNavigate: (AutoRepsGraph) -> Unit,
    current: NavKey,
    onAddClick: (() -> Unit)? = null,
) {
    NavigationBar(
        variant = NavigationBarVariant.Floating,
        action =
            onAddClick?.let {
                {
                    NavigationBarAction(
                        icon = KazIcons.Plus,
                        onClick = it,
                        contentDescription = "Workout aufzeichnen",
                    )
                }
            },
    ) {
        NavigationBarItem(
            selected = current is AutoRepsGraph.Home,
            onClick = { onNavigate(AutoRepsGraph.Home) },
            icon = KazIcons.Home,
            label = "Home",
            animation = NavigationBarAnimation.Tween,
        )
        NavigationBarItem(
            selected = current is AutoRepsGraph.History,
            onClick = { onNavigate(AutoRepsGraph.History) },
            icon = KazIcons.List,
            label = "Verlauf",
            animation = NavigationBarAnimation.Tween,
        )
        NavigationBarItem(
            selected = current is AutoRepsGraph.Settings,
            onClick = { onNavigate(AutoRepsGraph.Settings) },
            icon = KazIcons.Settings,
            label = "Einstellungen",
            animation = NavigationBarAnimation.Tween,
        )
    }
}
