package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.model.Ingredient

@Composable
fun AddIngredientScreen(
    ingredient: Ingredient? = null,
    onSave: (Ingredient) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var calories by remember { mutableStateOf(ingredient?.caloriesPer100?.toString() ?: "") }
    var protein by remember { mutableStateOf(ingredient?.proteinPer100?.toString() ?: "") }
    var carbs by remember { mutableStateOf(ingredient?.carbsPer100?.toString() ?: "") }
    var fat by remember { mutableStateOf(ingredient?.fatPer100?.toString() ?: "") }
    var fiber by remember { mutableStateOf(ingredient?.fiberPer100?.toString() ?: "") }
    var unit by remember { mutableStateOf(ingredient?.unit ?: "g") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
            .verticalScroll(rememberScrollState())
    ) {

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Unit")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            Button(
                onClick = { unit = " g" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (unit == "g") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (unit == "g") MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(" g")
            }

            Button(
                onClick = { unit = "ml" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (unit == "ml") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (unit == "ml") MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("ml")
            }
        }

        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories per 100 $unit") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = protein,
            onValueChange = { protein = it },
            label = { Text("Protein per 100 $unit") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = carbs,
            onValueChange = { carbs = it },
            label = { Text("Carbs per 100 $unit") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fat,
            onValueChange = { fat = it },
            label = { Text("Fat per 100 $unit") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fiber,
            onValueChange = { fiber = it },
            label = { Text("Fiber per 100 $unit") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
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
                        caloriesPer100 = calories.toDoubleOrNull() ?: 0.0,
                        proteinPer100 = protein.toDoubleOrNull() ?: 0.0,
                        carbsPer100 = carbs.toDoubleOrNull() ?: 0.0,
                        fatPer100 = fat.toDoubleOrNull() ?: 0.0,
                        fiberPer100 = fiber.toDoubleOrNull() ?: 0.0,
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