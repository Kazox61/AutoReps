package com.kazox.autoreps.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun BottomNavigationBar(
    topLevelBackStack: TopLevelBackStack<Any>
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        TOP_LEVEL_ROUTES.forEach { item ->
            val selected = topLevelBackStack.topLevelKey == item
            NavigationBarItem(
                selected = selected,
                onClick = {
                    topLevelBackStack.addTopLevel(item)
                },
                icon = {
                    Icon(
                        imageVector = vectorResource(item.icon),
                        contentDescription = stringResource(item.description),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(stringResource(item.title))
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.background,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    )
            )
        }
    }
}