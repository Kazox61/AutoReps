package com.kazox.autoreps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazox.autoreps.components.PrimaryButton
import com.kazox.autoreps.navigation.BottomNavigationBar
import com.kazox.autoreps.navigation.TopLevelBackStack

@Composable
fun StartScreen(
    topLevelBackStack: TopLevelBackStack<Any>,
    onStart: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(topLevelBackStack) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PushupAnimation(
                modifier = Modifier
                    .padding(32.dp)
                    .width(500.dp)
            )
            Spacer(modifier = Modifier.height(64.dp))
            PrimaryButton(
                onClick = onStart
            ) {
                Text(text = "Start")
            }
        }
    }
}