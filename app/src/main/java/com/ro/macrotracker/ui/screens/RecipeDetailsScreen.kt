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
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // CABECERA: Nombre + Borrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Recipe",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // DIÁLOGO DE BORRADO
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete Recipe?") },
                    text = { Text("The recipe will be removed from your list, but your past logs in the planner will be kept.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    // 👈 USAMOS EL ID AQUÍ para que coincida con tu @Query
                                    repository.softDeleteRecipe(recipe.id)
                                    onBack()
                                }
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // CARD DE NUTRICIÓN TOTAL
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Total Nutrition",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${nutrition.calories.format()} kcal")
                        Text("P: ${nutrition.protein.format()}g")
                        Text("C: ${nutrition.carbs.format()}g")
                        Text("F: ${nutrition.fat.format()}g")
                    }
                }
            }

            // SECCIÓN INGREDIENTES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = { showAdd = true },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add")
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                items(recipeIngredients) { ri ->
                    val ingredient = ingredients.find { it.id == ri.ingredientId }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                ingredient?.name ?: "Unknown",
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = if (ri.amount == 0.0) "" else ri.amount.format(),
                                onValueChange = { newValue ->
                                    val updatedGrams = newValue.toDoubleOrNull() ?: 0.0
                                    scope.launch {
                                        repository.updateRecipeIngredient(ri.copy(amount = updatedGrams))
                                    }
                                },
                                modifier = Modifier.width(95.dp),
                                label = { Text(ingredient?.unit ?: "g") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodySmall
                            )

                            IconButton(onClick = {
                                scope.launch { repository.deleteRecipeIngredient(ri.id) }
                            }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // BOTÓN DE SALIDA (SAVE CHANGES)
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save Changes & Exit", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAdd) {
        AddIngredientToRecipeDialog(
            ingredients = ingredients,
            onIngredientsSelected = { selectedIngredients ->
                scope.launch {
                    val newItems = selectedIngredients.map { ing ->
                        RecipeIngredient(id = 0, recipeId = recipe.id, ingredientId = ing.id, amount = 100.0)
                    }
                    repository.insertRecipeIngredients(newItems)
                    showAdd = false
                }
            },
            onDismiss = { showAdd = false }
        )
    }
}