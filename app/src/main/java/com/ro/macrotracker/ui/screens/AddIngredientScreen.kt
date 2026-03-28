package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient

@Composable
fun AddIngredientScreen(
    modifier: Modifier = Modifier,
    ingredient: Ingredient? = null,
    onSave: (Ingredient) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var calories by remember { mutableStateOf(ingredient?.caloriesPer100g?.toString() ?: "") }
    var protein by remember { mutableStateOf(ingredient?.proteinPer100g?.toString() ?: "") }
    var carbs by remember { mutableStateOf(ingredient?.carbsPer100g?.toString() ?: "") }
    var fat by remember { mutableStateOf(ingredient?.fatPer100g?.toString() ?: "") }
    var fiber by remember { mutableStateOf(ingredient?.fiberPer100g?.toString() ?: "") }
    var unit by remember { mutableStateOf(ingredient?.unit ?: "g") }

    Column(modifier = modifier.padding(16.dp)) {

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Unit")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            Button(
                onClick = { unit = "g" },
                colors = ButtonDefaults.run {
                    buttonColors(
                                containerColor = if (unit == "g")
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                }
            ) {
                Text("g")
            }

            Button(
                onClick = { unit = "ml" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (unit == "ml")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("ml")
            }
        }

        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories per 100g/ml") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = protein,
            onValueChange = { protein = it },
            label = { Text("Protein per 100g") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = carbs,
            onValueChange = { carbs = it },
            label = { Text("Carbs per 100g") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fat,
            onValueChange = { fat = it },
            label = { Text("Fat per 100g") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fiber,
            onValueChange = { fiber = it },
            label = { Text("Fiber per 100g/ml") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(onClick = onCancel) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val newIngredient = Ingredient(
                        id = ingredient?.id ?: 0,
                        name = name,
                        caloriesPer100g = calories.toDoubleOrNull() ?: 0.0,
                        proteinPer100g = protein.toDoubleOrNull() ?: 0.0,
                        carbsPer100g = carbs.toDoubleOrNull() ?: 0.0,
                        fatPer100g = fat.toDoubleOrNull() ?: 0.0,
                        fiberPer100g = fiber.toDoubleOrNull() ?: 0.0,
                        unit = unit
                    )
                    onSave(newIngredient)
                }
            ) {
                Text("Save")
            }
        }
    }
}