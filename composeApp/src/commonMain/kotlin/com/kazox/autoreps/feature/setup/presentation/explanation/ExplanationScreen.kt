package com.kazox.autoreps.feature.setup.presentation.explanation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.kazox.autoreps.core.presentation.components.PushupAnimation
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExplanationScreen(
    onNext: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PushupAnimation(
            modifier = Modifier.width(400.dp).padding(horizontal = 16.dp)
        )

        stringArrayResource(Res.array.explanation_texts).forEachIndexed { index, text ->
            Column(Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                stringResource(Res.string.next),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}