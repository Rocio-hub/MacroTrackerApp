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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.ro.macrotracker.ui.components.AddIngredientToRecipeDialog
import com.ro.macrotracker.ui.components.MacroBadgeSmall
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
        onResult = { uri -> selectedImageUri = uri?.toString() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    OutlinedTextField(
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        label = { Text("Recipe Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryColumn("Total", "${nutrition.kcal.format()} kcal", Color.Unspecified)
                        SummaryColumn("F", "${nutrition.f.format()} g", Color(0xFFFFB300))
                        SummaryColumn("C", "${nutrition.c.format()} g", Color(0xFF42A5F5))
                        SummaryColumn("Fi", "${nutrition.fi.format()} g", Color(0xFF50C878))
                        SummaryColumn("P", "${nutrition.p.format()} g", Color(0xFFEF5350))
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ingredients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(selectedItems, key = { it.first.id }) { item ->
                val (ing, amount) = item
                val factor = amount / 100.0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!ing.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = ing.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("🥦", style = MaterialTheme.typography.headlineSmall)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ing.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${(ing.caloriesPer100 * factor).format()} kcal total",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }

                                OutlinedTextField(
                                    value = if (amount == 0.0) "" else amount.toString(),
                                    onValueChange = { newValue ->
                                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                                        val index = selectedItems.indexOf(item)
                                        if (index != -1) {
                                            selectedItems[index] = ing to (filtered.toDoubleOrNull() ?: 0.0)
                                        }
                                    },
                                    modifier = Modifier.width(85.dp),
                                    label = { Text(ing.unit, style = MaterialTheme.typography.labelSmall) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall
                                )

                                IconButton(
                                    onClick = { selectedItems.remove(item) },
                                    modifier = Modifier.size(32.dp).padding(start = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MacroBadgeSmall("Fat", ing.fatPer100 * factor, Color(0xFFFFB300))
                                MacroBadgeSmall("Carbs", ing.carbsPer100 * factor, Color(0xFF42A5F5))
                                MacroBadgeSmall("Fiber", ing.fiberPer100 * factor, Color(0xFF50C878))
                                MacroBadgeSmall("Protein", ing.proteinPer100 * factor, Color(0xFFEF5350))
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = { onSave(recipeName, selectedImageUri, selectedItems.toList()) },
                modifier = Modifier.weight(1f),
                enabled = recipeName.isNotBlank() && selectedItems.isNotEmpty()
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

@Composable
fun SummaryColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
    }
}