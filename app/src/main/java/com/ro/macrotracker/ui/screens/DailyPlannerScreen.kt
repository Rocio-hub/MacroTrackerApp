package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient
import com.ro.macrotracker.data.local.entity.Recipe
import com.ro.macrotracker.data.local.entity.RecipeIngredient
import com.ro.macrotracker.utils.format

@Composable
fun DailyPlannerScreen(
    recipes: List<Recipe>,
    ingredients: List<Ingredient>,
    recipeIngredientsMap: Map<Int, List<RecipeIngredient>>,
    onRecipeClick: (Recipe) -> Unit   // 👈 NEW
){

    val context = LocalContext.current
    var targetCalories by remember { mutableStateOf("") }
    val target = targetCalories.toDoubleOrNull() ?: 0.0

    // ✅ Load saved value
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("planner", 0)
        targetCalories = prefs.getString("targetCalories", "") ?: ""
    }

    // ✅ Save when changed
    LaunchedEffect(targetCalories) {
        val prefs = context.getSharedPreferences("planner", 0)
        prefs.edit().putString("targetCalories", targetCalories).apply()
    }

    var selectedRecipes by remember { mutableStateOf(mapOf<Recipe, Int>()) }

    val totalNutrition = selectedRecipes.entries.fold(
        com.ro.macrotracker.Nutrition(0.0, 0.0, 0.0, 0.0, 0.0)
    ) { acc, (recipe, count) ->

        val ris = recipeIngredientsMap[recipe.id] ?: emptyList()
        val nutrition = calculateNutrition(ris, ingredients)

        com.ro.macrotracker.Nutrition(
            calories = acc.calories + nutrition.calories * count,
            protein = acc.protein + nutrition.protein * count,
            carbs = acc.carbs + nutrition.carbs * count,
            fat = acc.fat + nutrition.fat * count,
            fiber = acc.fiber + nutrition.fiber * count
        )
    }

    val totalCalories = totalNutrition.calories

    Column(modifier = Modifier.padding(16.dp)) {

        // 🎯 Target input
        OutlinedTextField(
            value = targetCalories,
            onValueChange = { targetCalories = it },
            label = { Text("Daily calories target") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 📊 Current macros
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

        // 🔔 Status
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

        // 📈 Progress bar
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

        // 📋 Recipes list
        LazyColumn {
            items(recipes) { recipe ->

                val ris = recipeIngredientsMap[recipe.id] ?: emptyList()
                val nutrition = calculateNutrition(ris, ingredients)

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
                            selectedRecipes = selectedRecipes + (recipe to 1)
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
                                    val newCount = count - 1
                                    selectedRecipes =
                                        if (newCount <= 0)
                                            selectedRecipes - recipe
                                        else
                                            selectedRecipes + (recipe to newCount)
                                }) {
                                    Text("-")
                                }

                                Text(
                                    "x$count",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                TextButton(onClick = {
                                    selectedRecipes = selectedRecipes + (recipe to (count + 1))
                                }) {
                                    Text("+")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}