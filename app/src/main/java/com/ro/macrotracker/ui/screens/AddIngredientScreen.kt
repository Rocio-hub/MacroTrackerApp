package com.ro.macrotracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.utils.format

@Composable
fun AddIngredientScreen(
    ingredient: Ingredient?,
    onSave: (Ingredient) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var unit by remember { mutableStateOf(ingredient?.unit ?: "g") }

    var calories by remember { mutableStateOf(ingredient?.caloriesPer100?.format() ?: "") }
    var protein by remember { mutableStateOf(ingredient?.proteinPer100?.format() ?: "") }
    var carbs by remember { mutableStateOf(ingredient?.carbsPer100?.format() ?: "") }
    var fat by remember { mutableStateOf(ingredient?.fatPer100?.format() ?: "") }
    var fiber by remember { mutableStateOf(ingredient?.fiberPer100?.format() ?: "") }

    val focusManager = LocalFocusManager.current

    var selectedImageUri by remember { mutableStateOf(ingredient?.imageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri?.toString()
        }
    )

    @Composable
    fun NutrientField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        focusedColor: Color,
        imeAction: ImeAction = ImeAction.Next
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.toDoubleOrNull() != null || newValue.endsWith(".")) {
                    onValueChange(newValue)
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = imeAction
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = focusedColor.copy(alpha = 0.1f),
                unfocusedContainerColor = focusedColor.copy(alpha = 0.05f),
                focusedBorderColor = focusedColor,
                focusedLabelColor = focusedColor,
                cursorColor = focusedColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            listOf("g", "ml").forEach { option ->
                Button(
                    onClick = { unit = option },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (unit == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (unit == option) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) { Text(option) }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Text(
            text = "Nutritional values per 100 $unit",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        NutrientField(calories, { calories = it }, "Calories", MaterialTheme.colorScheme.primary)
        NutrientField(protein, { protein = it }, "Protein", Color(0xFFEF5350))
        NutrientField(carbs, { carbs = it }, "Carbs", Color(0xFF42A5F5))
        NutrientField(fat, { fat = it }, "Fat", Color(0xFFFFB300))
        NutrientField(fiber, { fiber = it }, "Fiber", Color(0xFF50C878), imeAction = ImeAction.Done)

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onSave(Ingredient(
                        id = ingredient?.id ?: 0,
                        name = name,
                        caloriesPer100 = calories.toDoubleOrNull() ?: 0.0,
                        proteinPer100 = protein.toDoubleOrNull() ?: 0.0,
                        carbsPer100 = carbs.toDoubleOrNull() ?: 0.0,
                        fatPer100 = fat.toDoubleOrNull() ?: 0.0,
                        fiberPer100 = fiber.toDoubleOrNull() ?: 0.0,
                        unit = unit,
                        imageUri = selectedImageUri
                    ))
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank() && calories.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}