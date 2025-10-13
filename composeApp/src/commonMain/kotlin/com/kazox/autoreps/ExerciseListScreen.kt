package com.kazox.autoreps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kazox.autoreps.core.ExerciseCategory
import com.kazox.autoreps.core.ExerciseInfo
import com.kazox.autoreps.core.exerciseList
import com.kazox.autoreps.navigation.BottomNavigationBar
import com.kazox.autoreps.navigation.TopLevelBackStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    topLevelBackStack: TopLevelBackStack<Any>,
    onExerciseSelected: (ExerciseInfo) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }

    val categories = ExerciseCategory.entries

    val filteredExercises = remember(query, selectedCategory) {
        val search = query.trim().lowercase()
        exerciseList.filter { exercise ->
            val matchesName = exercise.name.lowercase().contains(search)
            val matchesCategory =
                selectedCategory == null || exercise.categories.contains(selectedCategory)
            matchesName && matchesCategory
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(topLevelBackStack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .semantics { isTraversalGroup = true }
                .padding(16.dp)
        ) {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { /* optionally close keyboard */ },
                        placeholder = { Text("Search exercises") },
                        expanded = false,
                        onExpandedChange = {}
                    )
                },
                expanded = false,
                onExpandedChange = {},
            ) {}

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                }

                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                category.name.replace('_', ' ')
                                    .lowercase()
                                    .replaceFirstChar(Char::titlecase)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredExercises.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No exercises found")
                }
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExercises) { exercise ->
                        ExerciseListItem(
                            exercise = exercise,
                            onClick = { onExerciseSelected(exercise) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: ExerciseInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exercise.categories.joinToString {
                    it.name.replace('_', ' ')
                        .lowercase()
                        .replaceFirstChar(Char::titlecase)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
