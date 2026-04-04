package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.ui.components.AddIngredientToRecipeDialog
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.model.RecipeIngredient
import com.ro.macrotracker.domain.repository.Repository
import com.ro.macrotracker.utils.format
import kotlinx.coroutines.launch
import com.ro.macrotracker.domain.calculateNutrition

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    repository: Repository,
    onBack: () -> Unit
) {
    val ingredients by repository.getAllIngredients().collectAsState(initial = emptyList())
    val recipeIngredients by repository.getIngredientsForRecipe(recipe.id).collectAsState(initial = emptyList())

    val nutritionInput = recipeIngredients.mapNotNull { ri ->
        val ingredient = ingredients.find { it.id == ri.ingredientId }
        ingredient?.let { it to ri.amount }
    }
    val nutrition = calculateNutrition(nutritionInput)

    var showAdd by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = recipe.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Nutrition", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${nutrition.calories.format()} kcal")
                    Text("P: ${nutrition.protein.format()} g")
                    Text("C: ${nutrition.carbs.format()} g")
                    Text("F: ${nutrition.fat.format()} g")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { showAdd = true }) {
                Icon(Icons.Default.Add, null)
                Text("Add")
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(recipeIngredients) { ri ->
                val ingredient = ingredients.find { it.id == ri.ingredientId }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(ingredient?.name ?: "Unknown", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedTextField(
                            value = ri.amount.toString(),
                            onValueChange = { newValue ->
                                val updatedGrams = newValue.toDoubleOrNull() ?: 0.0
                                scope.launch {
                                    repository.updateRecipeIngredient(ri.copy(amount = updatedGrams))
                                }
                            },
                            modifier = Modifier.width(90.dp),
                            suffix = {
                                ingredient?.unit?.let { unit ->
                                    Text(" $unit")
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        IconButton(onClick = {
                            scope.launch { repository.deleteRecipeIngredient(ri.id) }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Back to Recipes")
        }
    }

    if (showAdd) {
        AddIngredientToRecipeDialog(
            ingredients = ingredients,
            onIngredientsSelected = { selectedIngredients ->
                scope.launch {
                    val newItems = selectedIngredients.map { ing ->
                        RecipeIngredient(
                            id = 0,
                            recipeId = recipe.id,
                            ingredientId = ing.id,
                            amount = 100.0
                        )
                    }
                    repository.insertRecipeIngredients(newItems)
                    showAdd = false
                }
            },
            onDismiss = { showAdd = false }
        )
    }
}