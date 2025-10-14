package com.kazox.autoreps.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@Composable
fun NumberWheelPicker(
    initial: Int = 50,
    from: Int = 5,
    to: Int = 100,
    step: Int = 5,
    itemHeight: Dp = 50.dp,
    onValueChanged: (Int) -> Unit
) {
    val items = remember(from, to, step) {
        (from..to step step).map { it }
    }

    val startIndex = remember(initial, from, step) {
        val index = (initial - from) / step
        index.coerceIn(0, items.size - 1)
    }

    var selectedIndex by remember(initial) {
        mutableIntStateOf(startIndex)
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = startIndex
    )

    val snapperLayoutInfo = rememberSnapFlingBehavior(listState)

    LaunchedEffect(initial) {
        val newIndex = (initial - from) / step
        val validIndex = newIndex.coerceIn(0, items.size - 1)
        selectedIndex = validIndex
        listState.animateScrollToItem(validIndex)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val index = if (listState.firstVisibleItemScrollOffset == 0) {
                listState.firstVisibleItemIndex
            } else {
                listState.firstVisibleItemIndex + 1
            }
            val value = items.getOrNull(index)
            if (value != null && index != selectedIndex) {
                selectedIndex = index
                onValueChanged(value)
            }
        }
    }

    Box(
        modifier = Modifier.height(3 * itemHeight),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            flingBehavior = snapperLayoutInfo,
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString(),
                        textAlign = TextAlign.Center,
                        style = if (isSelected) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
    }
}