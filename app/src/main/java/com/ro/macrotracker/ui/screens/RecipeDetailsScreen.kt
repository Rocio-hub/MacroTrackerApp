package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.ui.components.AddIngredientToRecipeDialog
import com.ro.macrotracker.data.local.dao.IngredientDao
import com.ro.macrotracker.data.local.dao.RecipeIngredientDao
import com.ro.macrotracker.data.local.entity.Recipe
import com.ro.macrotracker.data.local.entity.RecipeIngredient
import com.ro.macrotracker.utils.format
import kotlinx.coroutines.launch
import com.ro.macrotracker.domain.calculateNutrition
import com.ro.macrotracker.data.mappers.toDomain

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

    val nutritionInput = recipeIngredients.mapNotNull { ri ->
        val ingredient = ingredients.find { it.id == ri.ingredientId }

        ingredient?.toDomain()?.let {
            it to ri.quantityInGrams
        }
    }

    val nutrition = calculateNutrition(nutritionInput)

    var showAdd by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<RecipeIngredient?>(null) }
    var newGrams by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = recipe.name)

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ Nutrition summary
        Text(text = "Calories: ${nutrition.calories.format()}")
        Text(text = "Protein: ${nutrition.protein.format()}")
        Text(text = "Carbs: ${nutrition.carbs.format()}")
        Text(text = "Fat: ${nutrition.fat.format()}")
        Text(text = "Fiber: ${nutrition.fiber.format()}")

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { showAdd = true }) {
            Text("Add Ingredient")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(recipeIngredients) { ri ->

                val ingredient = ingredients.find { it.id == ri.ingredientId }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {

                    Text("${ingredient?.name ?: "Unknown"}")

                    Text("${ri.quantityInGrams.format()}${ingredient?.unit ?: ""}")

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        Button(onClick = {
                            editingItem = ri
                            newGrams = ri.quantityInGrams.toString()
                        }) {
                            Text("Edit")
                        }

                        Button(onClick = {
                            scope.launch {
                                recipeIngredientDao.deleteRecipeIngredient(ri.id)
                            }
                        }) {
                            Text("Delete")
                        }
                    }

                    // ✏️ EDIT MODE
                    if (editingItem?.id == ri.id) {

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newGrams,
                            onValueChange = { newGrams = it },
                            label = { Text("Grams / ml") }
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                            Button(onClick = {
                                val updated = newGrams.toDoubleOrNull()
                                    ?: ri.quantityInGrams

                                scope.launch {
                                    recipeIngredientDao.updateRecipeIngredient(
                                        ri.copy(quantityInGrams = updated)
                                    )
                                    editingItem = null
                                }
                            }) {
                                Text("Save")
                            }

                            Button(onClick = {
                                editingItem = null
                            }) {
                                Text("Cancel")
                            }
                        }
                    }
                }
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