package com.ro.macrotracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.dao.IngredientDao
import com.ro.macrotracker.data.local.dao.RecipeIngredientDao
import com.ro.macrotracker.data.local.entity.Ingredient
import com.ro.macrotracker.data.local.entity.Recipe
import com.ro.macrotracker.data.local.entity.RecipeIngredient
import kotlinx.coroutines.launch

fun calculateNutrition(
    recipeIngredients: List<RecipeIngredient>,
    ingredients: List<Ingredient>
): Nutrition {

    var calories = 0.0
    var protein = 0.0
    var carbs = 0.0
    var fat = 0.0

    recipeIngredients.forEach { ri ->
        val ingredient = ingredients.find { it.id == ri.ingredientId } ?: return@forEach

        val factor = ri.quantityInGrams / 100.0

        calories += ingredient.caloriesPer100g * factor
        protein += ingredient.proteinPer100g * factor
        carbs += ingredient.carbsPer100g * factor
        fat += ingredient.fatPer100g * factor
    }

    return Nutrition(calories, protein, carbs, fat)
}

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    ingredientDao: IngredientDao,
    recipeIngredientDao: RecipeIngredientDao,
    onBack: () -> Unit
) {

    val ingredients by ingredientDao.getAllIngredients().collectAsState(initial = emptyList())
    val recipeIngredients by recipeIngredientDao
        .getIngredientsForRecipe(recipe.id)
        .collectAsState(initial = emptyList())

    val nutrition = calculateNutrition(recipeIngredients, ingredients)

    var showAdd by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = recipe.name)

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Nutrition summary
        Text(text = "Calories: ${nutrition.calories}")
        Text(text = "Protein: ${nutrition.protein}")
        Text(text = "Carbs: ${nutrition.carbs}")
        Text(text = "Fat: ${nutrition.fat}")

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { showAdd = true }) {
            Text("Add Ingredient")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(recipeIngredients) { ri ->
                val ingredient = ingredients.find { it.id == ri.ingredientId }

                Text(
                    text = "${ingredient?.name ?: "Unknown"} - ${ri.quantityInGrams}g"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text("Back")
        }

        if (showAdd) {
            AddIngredientToRecipeDialog(
                ingredients = ingredients,
                onAdd = { ingredientId, grams ->
                    scope.launch {
                        recipeIngredientDao.insertRecipeIngredient(
                            RecipeIngredient(
                                recipeId = recipe.id,
                                ingredientId = ingredientId,
                                quantityInGrams = grams
                            )
                        )
                        showAdd = false
                    }
                },
                onDismiss = { showAdd = false }
            )
        }
    }
}