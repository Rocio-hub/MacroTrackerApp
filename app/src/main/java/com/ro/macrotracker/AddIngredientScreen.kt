package com.ro.macrotracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient

@Composable
fun AddIngredientScreen(
    modifier: Modifier = Modifier,
    onSave: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories per 100g") },
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

        Button(
            onClick = {
                val ingredient = Ingredient(
                    name = name,
                    caloriesPer100g = calories.toDoubleOrNull() ?: 0.0,
                    proteinPer100g = protein.toDoubleOrNull() ?: 0.0,
                    carbsPer100g = carbs.toDoubleOrNull() ?: 0.0,
                    fatPer100g = fat.toDoubleOrNull() ?: 0.0
                )
                onSave(ingredient)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save")
        }
    }
}