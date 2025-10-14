package com.kazox.autoreps.feature.workout.presentation.addEditWorkout.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.arrow_back
import autoreps.composeapp.generated.resources.save
import autoreps.composeapp.generated.resources.save_workout
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(stringResource(Res.string.save_workout))
        },
        navigationIcon = {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.arrow_back),
                    contentDescription = "Go Back",
                )
            }
        },
        actions = {
            TextButton(
                onClick = onSave,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    stringResource(Res.string.save).uppercase()
                )
            }
        }
    )
}