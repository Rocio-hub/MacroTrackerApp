package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.RecipeIngredient
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.ui.components.AddIngredientToRecipeDialog
import com.ro.macrotracker.utils.format

@Composable
fun AddRecipeScreen(
    allIngredients: List<Ingredient>,
    onSave: (String, List<Pair<Ingredient, Double>>) -> Unit,
    onCancel: () -> Unit
) {
    var recipeName by remember { mutableStateOf("") }
    // Usamos una lista mutable de Compose para que detecte cambios internos
    val selectedItems = remember { mutableStateListOf<Pair<Ingredient, Double>>() }
    var showDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Cálculo de macros: importante usar .toList() para que recompose al cambiar la lista
    val nutrition = remember(selectedItems.toList()) {
        val cals = selectedItems.sumOf { (ing, amount) -> (ing.caloriesPer100 * amount) / 100 }
        val prot = selectedItems.sumOf { (ing, amount) -> (ing.proteinPer100 * amount) / 100 }
        Pair(cals, prot)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        OutlinedTextField(
            value = recipeName,
            onValueChange = { recipeName = it },
            label = { Text("Recipe Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Panel de Macros
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                "Total: ${nutrition.first.format()} kcal | Protein: ${nutrition.second.format()}g",
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(selectedItems, key = { it.first.id }) { item ->
                val (ing, amount) = item

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ing.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)

                        // Input de gramos
                        OutlinedTextField(
                            value = if (amount == 0.0) "" else amount.toString(),
                            onValueChange = { newValue ->
                                // Filtramos para que solo acepte números y un punto
                                val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                                val newAmount = filteredValue.toDoubleOrNull() ?: 0.0

                                // ACTUALIZACIÓN CRÍTICA: Reemplazamos el objeto en la lista
                                val index = selectedItems.indexOf(item)
                                if (index != -1) {
                                    selectedItems[index] = ing to newAmount
                                }
                            },
                            modifier = Modifier.width(100.dp),
                            label = { Text(" ${ing.unit}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        IconButton(onClick = { selectedItems.remove(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Botonera inferior
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = { onSave(recipeName, selectedItems.toList()) },
                modifier = Modifier.weight(1f),
                enabled = recipeName.isNotBlank() && selectedItems.isNotEmpty()
            ) { Text("Save Recipe") }
        }
    }

    if (showDialog) {
        AddIngredientToRecipeDialog(
            ingredients = allIngredients,
            onIngredientsSelected = { newIngredients ->
                newIngredients.forEach { ingredient ->
                    if (!selectedItems.any { it.first.id == ingredient.id }) {
                        selectedItems.add(ingredient to 100.0)
                    }
                }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}