package com.ro.macrotracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.RecipeIngredient
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.ui.components.AddIngredientToRecipeDialog
import com.ro.macrotracker.utils.format

@Composable
fun AddRecipeScreen(
    allIngredients: List<Ingredient>,
    onSave: (String, String?, List<Pair<Ingredient, Double>>) -> Unit,
    onCancel: () -> Unit
) {
    var recipeName by remember { mutableStateOf("") }
    val selectedItems = remember { mutableStateListOf<Pair<Ingredient, Double>>() }
    var showDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    val nutrition = remember(selectedItems.toList()) {
        val calories = selectedItems.sumOf { (ing, amount) -> (ing.caloriesPer100 * amount) / 100 }
        val protein = selectedItems.sumOf { (ing, amount) -> (ing.proteinPer100 * amount) / 100 }
        val carbs = selectedItems.sumOf { (ing, amount) -> (ing.carbsPer100 * amount) / 100 }
        val fat = selectedItems.sumOf { (ing, amount) -> (ing.fatPer100 * amount) / 100 }
        val fiber = selectedItems.sumOf { (ing, amount) -> (ing.fiberPer100 * amount) / 100 }

        object {
            val kcal = calories
            val p = protein
            val c = carbs
            val f = fat
            val fi = fiber
        }
    }

    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            selectedImageUri = uri?.toString()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
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
                        contentDescription = "Recipe Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OutlinedTextField(
                value = recipeName,
                onValueChange = { recipeName = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelSmall)
                    Text("${nutrition.kcal.format()} kcal", fontWeight = FontWeight.ExtraBold)
                }

                VerticalDivider(modifier = Modifier.height(24.dp), thickness = 1.dp)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("P", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
                    Text("${nutrition.p.format()} g", style = MaterialTheme.typography.bodyMedium)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("C", color = Color(0xFF42A5F5), fontWeight = FontWeight.Bold)
                    Text("${nutrition.c.format()} g", style = MaterialTheme.typography.bodyMedium)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("F", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                    Text("${nutrition.f.format()} g", style = MaterialTheme.typography.bodyMedium)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Fi", color = Color(0xFF50C878), fontWeight = FontWeight.Bold)
                    Text("${nutrition.fi.format()} g", style = MaterialTheme.typography.bodyMedium)
                }
            }
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

                        OutlinedTextField(
                            value = if (amount == 0.0) "" else amount.toString(),
                            onValueChange = { newValue ->
                                // Filtramos para que solo acepte números y un punto
                                val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                                val newAmount = filteredValue.toDoubleOrNull() ?: 0.0

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { onSave(recipeName, selectedImageUri, selectedItems.toList()) },
                modifier = Modifier.weight(1f),
                enabled = recipeName.isNotBlank() && selectedItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save")
            }
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