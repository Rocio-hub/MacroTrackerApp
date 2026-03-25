package com.ro.macrotracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient
import com.ro.macrotracker.data.local.entity.Recipe
import com.ro.macrotracker.data.local.entity.RecipeIngredient

@Composable
fun DailyPlannerScreen(
    recipes: List<Recipe>,
    ingredients: List<Ingredient>,
    recipeIngredientsMap: Map<Int, List<RecipeIngredient>>
) {

    var targetCalories by remember { mutableStateOf("") }
    var selectedRecipes by remember { mutableStateOf(setOf<Recipe>()) }

    val totalCalories = selectedRecipes.sumOf { recipe ->
        val ris = recipeIngredientsMap[recipe.id] ?: emptyList()
        calculateNutrition(ris, ingredients).calories
    }

    val target = targetCalories.toDoubleOrNull() ?: 0.0
    val isOver = totalCalories > target && target > 0
    val isNear = totalCalories <= target && target > 0

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = targetCalories,
            onValueChange = { targetCalories = it },
            label = { Text("Daily calories target") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Current calories: $totalCalories")

        Text(
            text = when {
                target == 0.0 -> "Enter a target"
                totalCalories > target -> "🔴 Over calories"
                totalCalories == target -> "✅ Perfect"
                else -> "🟢 Under target"
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
                val calories = calculateNutrition(ris, ingredients).calories

                val isSelected = selectedRecipes.contains(recipe)

                // simulate adding this recipe
                val newTotal = if (isSelected) {
                    totalCalories
                } else {
                    totalCalories + calories
                }

                val willBeOver = newTotal > target && target > 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected && totalCalories > target -> MaterialTheme.colorScheme.errorContainer
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            !isSelected && willBeOver -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    onClick = {
                        selectedRecipes =
                            if (isSelected) selectedRecipes - recipe
                            else selectedRecipes + recipe
                    }
                ) {
                    Text(
                        text = "${recipe.name} - $calories kcal",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}