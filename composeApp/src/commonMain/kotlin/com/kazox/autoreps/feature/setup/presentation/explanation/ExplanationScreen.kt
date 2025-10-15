package com.kazox.autoreps.feature.setup.presentation.explanation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.explanation_texts
import autoreps.composeapp.generated.resources.next
import autoreps.composeapp.generated.resources.step_template
import com.kazox.autoreps.core.presentation.components.PrimaryButton
import com.kazox.autoreps.core.presentation.components.PushupAnimation
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExplanationScreen(
    onNext: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PushupAnimation(
                    modifier = Modifier.width(400.dp).padding(horizontal = 16.dp)
                )

                stringArrayResource(Res.array.explanation_texts).forEachIndexed { index, text ->
                    Column(Modifier
                        .fillMaxWidth()
                    ) {
                        Text(
                            stringResource(Res.string.step_template, (index + 1)),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }

                PrimaryButton(
                    onClick = onNext
                ) {
                    Text(
                        stringResource(Res.string.next),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}