package com.kazox.autoreps.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.kazox.autoreps.resources.Res
import com.kazox.autoreps.resources.nav_history
import com.kazox.autoreps.resources.nav_home
import com.kazox.autoreps.resources.nav_record_workout
import com.kazox.autoreps.resources.nav_settings
import com.kazox.ui.components.icon.KazIcons
import com.kazox.ui.components.navigationbar.NavigationBar
import com.kazox.ui.components.navigationbar.NavigationBarAction
import com.kazox.ui.components.navigationbar.NavigationBarAnimation
import com.kazox.ui.components.navigationbar.NavigationBarItem
import com.kazox.ui.components.navigationbar.NavigationBarVariant
import org.jetbrains.compose.resources.stringResource

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
                        contentDescription = stringResource(Res.string.nav_record_workout),
                    )
                }
            },
    ) {
        NavigationBarItem(
            selected = current is AutoRepsGraph.Home,
            onClick = { onNavigate(AutoRepsGraph.Home) },
            icon = KazIcons.Home,
            label = stringResource(Res.string.nav_home),
            animation = NavigationBarAnimation.Tween,
        )
        NavigationBarItem(
            selected = current is AutoRepsGraph.History,
            onClick = { onNavigate(AutoRepsGraph.History) },
            icon = KazIcons.List,
            label = stringResource(Res.string.nav_history),
            animation = NavigationBarAnimation.Tween,
        )
        NavigationBarItem(
            selected = current is AutoRepsGraph.Settings,
            onClick = { onNavigate(AutoRepsGraph.Settings) },
            icon = KazIcons.Settings,
            label = stringResource(Res.string.nav_settings),
            animation = NavigationBarAnimation.Tween,
        )
    }
}
