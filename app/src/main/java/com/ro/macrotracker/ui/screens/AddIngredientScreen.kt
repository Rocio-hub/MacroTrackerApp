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

    var selectedImageUri by remember { mutableStateOf(ingredient?.imageUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri?.toString()
        }
    )

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                        contentDescription = "Ingredient Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { unit = "g" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (unit == "g") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (unit == "g") MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("g")
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

        Text(
            text = "Nutritional values per 100 $unit",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = protein,
            onValueChange = { protein = it },
            label = { Text("Protein") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFEF5350).copy(alpha = 0.2f),
                unfocusedContainerColor = Color(0xFFEF5350).copy(alpha = 0.1f),
                focusedBorderColor = Color(0xFFEF5350),
                focusedLabelColor = Color(0xFFEF5350),
                cursorColor = Color(0xFFEF5350)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = carbs,
            onValueChange = { carbs = it },
            label = { Text("Carbs") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF42A5F5).copy(alpha = 0.2f),
                unfocusedContainerColor = Color(0xFF42A5F5).copy(alpha = 0.1f),
                focusedBorderColor = Color(0xFF42A5F5),
                focusedLabelColor = Color(0xFF42A5F5),
                cursorColor = Color(0xFF42A5F5)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fat,
            onValueChange = { fat = it },
            label = { Text("Fat") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFFB300).copy(alpha = 0.2f),
                unfocusedContainerColor = Color(0xFFFFB300).copy(alpha = 0.1f),
                focusedBorderColor = Color(0xFFFFB300),
                focusedLabelColor = Color(0xFFFFB300),
                cursorColor = Color(0xFFFFB300)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = fiber,
            onValueChange = { fiber = it },
            label = { Text("Fiber") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF50C878).copy(alpha = 0.2f),
                unfocusedContainerColor = Color(0xFF50C878).copy(alpha = 0.1f),
                focusedBorderColor = Color(0xFF50C878),
                focusedLabelColor = Color(0xFF50C878),
                cursorColor = Color(0xFF50C878)
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
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
                        unit = unit,
                        imageUri = selectedImageUri
                    )
                    onSave(newIngredient)
                },
                modifier = Modifier.weight(1f),
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}