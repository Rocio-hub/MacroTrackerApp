package com.ro.macrotracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.model.RecipeIngredient
import com.ro.macrotracker.domain.calculateRecipeNutrition
import com.ro.macrotracker.ui.viewmodel.DailyPlannerViewModel
import com.ro.macrotracker.utils.format

@Composable
fun DailyPlannerScreen(
    recipes: List<Recipe>,
    ingredients: List<Ingredient>,
    recipeIngredientsMap: Map<Int, List<RecipeIngredient>>,
    onRecipeClick: (Recipe) -> Unit
) {

    val context = LocalContext.current
    val viewModel: DailyPlannerViewModel = viewModel()
    val selectedRecipes by viewModel.selectedRecipes.collectAsState()

    var targetCalories by remember { mutableStateOf("") }
    val target = targetCalories.toDoubleOrNull() ?: 0.0

    val searchQuery by viewModel.plannerSearchQuery.collectAsState()
    val filteredRecipes = remember(searchQuery, recipes) {
        viewModel.getFilteredRecipes(recipes)
    }

    val totalNutrition = remember(selectedRecipes, recipeIngredientsMap, ingredients) {
        viewModel.calculateTotalNutrition(recipeIngredientsMap, ingredients)
    }

    val totalCalories = totalNutrition.calories

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("planner", 0)
        targetCalories = prefs.getString("targetCalories", "") ?: ""
    }

    LaunchedEffect(targetCalories) {
        val prefs = context.getSharedPreferences("planner", 0)
        prefs.edit().putString("targetCalories", targetCalories).apply()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = targetCalories,
            onValueChange = { targetCalories = it },
            label = { Text("Daily Calories Target") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
           Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Daily Progress", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text("${totalCalories.format()} / ${target.format()} kcal",
                            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    }

                    Surface(
                        color = when {
                            target == 0.0 -> MaterialTheme.colorScheme.surfaceVariant
                            totalCalories > target -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = when {
                                target == 0.0 -> "Set Target"
                                totalCalories > target -> "Over Limit"
                                else -> "Left: ${(target - totalCalories).format()}"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { if (target > 0) (totalCalories / target).toFloat().coerceAtMost(1f) else 0f },

                   modifier = Modifier.fillMaxWidth().height(8.dp),
                    strokeCap = StrokeCap.Round,
                    color = if (totalCalories > target) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MacroSummaryItem("Protein", totalNutrition.protein, Color(0xFFEF5350))
                    MacroSummaryItem("Carbs", totalNutrition.carbs, Color(0xFF42A5F5))
                    MacroSummaryItem("Fat", totalNutrition.fat, Color(0xFFFFB300))
                    MacroSummaryItem("Fiber", totalNutrition.fiber, Color(0xFF66BB6A))
                }
            }
        }

        Text("Add to your day", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            placeholder = { Text("Search recipe to add...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredRecipes) { recipe ->
                val ris = recipeIngredientsMap[recipe.id] ?: emptyList()
                val nutrition = calculateRecipeNutrition(ris, ingredients)
                val count = selectedRecipes[recipe] ?: 0
                val isSelected = count > 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    onClick = { if (!isSelected) viewModel.addRecipe(recipe) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(recipe.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onRecipeClick(recipe) }) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${nutrition.calories.format()} kcal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            VerticalDivider(modifier = Modifier.height(16.dp), thickness = 1.dp)
                            MacroBadge("P", nutrition.protein, Color(0xFFFFEBEE))
                            MacroBadge("C", nutrition.carbs, Color(0xFFE3F2FD))
                            MacroBadge("F", nutrition.fat, Color(0xFFFFF8E1))
                        }

                        if (isSelected) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.removeRecipe(recipe) }) {
                                    Icon(Icons.Default.Remove, contentDescription = null)
                                }

                                AnimatedContent(
                                    targetState = count,
                                    transitionSpec = {
                                        if (targetState > initialState) {
                                            (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                                        } else {
                                            (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                                        }.using(SizeTransform(clip = false))
                                    },
                                    label = "countAnim"
                                ) { targetCount ->
                                    Text(targetCount.toString(), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 20.dp))
                                }

                                IconButton(onClick = { viewModel.addRecipe(recipe) }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroSummaryItem(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(16.dp, 4.dp).background(color, MaterialTheme.shapes.extraSmall))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("${value.format()}g", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MacroBadge(label: String, value: Double, color: Color) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${value.format()}g",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}