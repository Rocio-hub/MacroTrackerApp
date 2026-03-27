package com.ro.macrotracker.ui.theme

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
    var selectedRecipes by remember { mutableStateOf(mapOf<Recipe, Int>()) }

    val totalCalories = selectedRecipes.entries.sumOf { (recipe, count) ->
        val ris = recipeIngredientsMap[recipe.id] ?: emptyList()
        calculateNutrition(ris, ingredients).calories * count
    }

    val target = targetCalories.toDoubleOrNull() ?: 0.0

    Column(modifier = Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = targetCalories,
            onValueChange = { targetCalories = it },
            label = { Text("Daily calories target") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Calories: ${totalCalories.toInt()} / ${target.toInt()}",
            style = MaterialTheme.typography.titleMedium
        )

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
                val calories = calculateNutrition(ris, ingredients).calories

                val count = selectedRecipes[recipe] ?: 0
                val isSelected = count > 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    onClick = {
                        if (!isSelected) {
                            selectedRecipes = selectedRecipes + (recipe to 1)
                        }
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // 🧾 Recipe info
                        Column {

                            Text(
                                text = recipe.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "$calories kcal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 👇 Controls only if selected
                        if (isSelected) {

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Button(
                                    onClick = {
                                        selectedRecipes = selectedRecipes + (recipe to (count + 1))
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("+")
                                }

                                Text("x$count")

                                Button(
                                    onClick = {
                                        val newCount = count - 1
                                        selectedRecipes =
                                            if (newCount <= 0)
                                                selectedRecipes - recipe
                                            else
                                                selectedRecipes + (recipe to newCount)
                                    }
                                ) {
                                    Text("-")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}