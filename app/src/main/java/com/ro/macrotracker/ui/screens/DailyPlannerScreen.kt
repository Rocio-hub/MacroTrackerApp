package com.ro.macrotracker.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.utils.format
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.RecipeIngredient
import com.ro.macrotracker.domain.calculateRecipeNutrition
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ro.macrotracker.ui.viewmodel.DailyPlannerViewModel

@Composable
fun DailyPlannerScreen(
    recipes: List<com.ro.macrotracker.model.Recipe>,
    ingredients: List<com.ro.macrotracker.model.Ingredient>,
    recipeIngredientsMap: Map<Int, List<com.ro.macrotracker.model.RecipeIngredient>>,
    onRecipeClick: (com.ro.macrotracker.model.Recipe) -> Unit
){
    val context = LocalContext.current
    var targetCalories by remember { mutableStateOf("") }
    val target = targetCalories.toDoubleOrNull() ?: 0.0

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("planner", 0)
        targetCalories = prefs.getString("targetCalories", "") ?: ""
    }

    LaunchedEffect(targetCalories) {
        val prefs = context.getSharedPreferences("planner", 0)
        prefs.edit().putString("targetCalories", targetCalories).apply()
    }

    val viewModel: DailyPlannerViewModel = viewModel()
    val selectedRecipes by viewModel.selectedRecipes.collectAsState()

    val totalNutrition = remember(selectedRecipes) {
        viewModel.calculateTotalNutrition(
            recipeIngredientsMap,
            ingredients
        )
    }

    val totalCalories = totalNutrition.calories

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = targetCalories,
            onValueChange = { targetCalories = it },
            label = { Text("Daily calories target") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column (modifier = Modifier.padding(bottom = 12.dp)) {

            Text(
                "Daily target",
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                "${totalCalories.format()} / ${target.format()} kcal",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Fat: ${totalNutrition.fat.format()} g")
            Text("Carbs: ${totalNutrition.carbs.format()} g")
            Text("Fiber: ${totalNutrition.fiber.format()} g")
            Text("Protein: ${totalNutrition.protein.format()} g")
        }

        Text(
            text = when {
                target == 0.0 -> "Enter a target"
                totalCalories > target -> "🔴 Over calories"
                totalCalories == target -> "✅ Perfect"
                else -> "🟢 Under target"
            },
            color = when {
                target == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
                totalCalories > target -> MaterialTheme.colorScheme.error
                totalCalories == target -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.primary
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = {
                if (target > 0)
                    (totalCalories / target).toFloat().coerceAtMost(1f)
                else 0f
            },
            color = if (totalCalories > target && target > 0)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(recipes) { recipe ->

                val ris = recipeIngredientsMap[recipe.id] ?: emptyList()
                val nutrition = calculateRecipeNutrition(
                    ris, // Ya es List<com.ro.macrotracker.model.RecipeIngredient>
                    ingredients
                )
                val count = selectedRecipes[recipe] ?: 0
                val isSelected = count > 0

                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    onClick = {
                        if (!isSelected) {
                            viewModel.addRecipe(recipe)
                        }
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // 🧾 Recipe info
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        TextButton(
                            onClick = {
                                onRecipeClick(recipe)
                            }
                        ) {
                            Text("Details")
                        }

                        Text(
                            text = "${nutrition.calories.format()} kcal | " +
                                    "${nutrition.fat.format()}F | " +
                                    "${nutrition.carbs.format()}C | " +
                                    "${nutrition.fiber.format()}Fi | " +
                                    "${nutrition.protein.format()}P",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 👇 Controls only if selected
                        if (isSelected) {

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                TextButton(onClick = {
                                   viewModel.removeRecipe(recipe)
                                }) {
                                    Icon(Icons.Default.Remove, contentDescription = null)
                                }

                                AnimatedContent(
                                    targetState = count,
                                    transitionSpec = {
                                        if (targetState > initialState) {
                                            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                                        } else {
                                            slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                                        }.using(SizeTransform(clip = false))
                                    }
                                ) { targetCount ->
                                    Text(
                                        text = targetCount.toString(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                TextButton(onClick = {
                                    viewModel.addRecipe(recipe)
                                }) {
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