package com.ro.macrotracker.ui.screens

import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ro.macrotracker.Screen
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.ui.components.AddIngredientToRecipeDialog
import com.ro.macrotracker.ui.components.MacroBadgeSmall
import com.ro.macrotracker.utils.format
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddRecipeScreen(
    allIngredients: List<Ingredient>,
    mainViewModel: com.ro.macrotracker.ui.MainViewModel,
    onSave: (String, String?, List<Pair<Ingredient, Double>>) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val recipeName = mainViewModel.temporaryRecipeName

    val selectedItems = mainViewModel.temporarySelectedIngredients

    val selectedImageUri = mainViewModel.temporaryRecipeImage

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val amountsTextMap = remember { mutableStateMapOf<Int, String>() }

    fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val file = java.io.File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                mainViewModel.temporaryRecipeImage = uri.toString()
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempImageUri != null) {
                mainViewModel.temporaryRecipeImage = tempImageUri.toString()
            }
        }
    )

    val nutrition = remember {
        derivedStateOf {
            val calories = selectedItems.sumOf { (ing, amount) -> (ing.caloriesPer100 * amount) / 100 }
            val protein = selectedItems.sumOf { (ing, amount) -> (ing.proteinPer100 * amount) / 100 }
            val carbs = selectedItems.sumOf { (ing, amount) -> (ing.carbsPer100 * amount) / 100 }
            val fat = selectedItems.sumOf { (ing, amount) -> (ing.fatPer100 * amount) / 100 }
            val fiber = selectedItems.sumOf { (ing, amount) -> (ing.fiberPer100 * amount) / 100 }
            object { val kcal = calories; val p = protein; val c = carbs; val f = fat; val fi = fiber }
        }
    }.value

    Column(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)).clickable { showImageSourceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    OutlinedTextField(
                        value = recipeName,
                        onValueChange = { mainViewModel.temporaryRecipeName = it },
                        label = { Text("Recipe Name") },
                        modifier = Modifier.weight(1f), singleLine = true)
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryColumn("Total", "${nutrition.kcal.format()} kcal", Color.Unspecified)
                        SummaryColumn("P", "${nutrition.p.format()} g", Color(0xFFEF5350))
                        SummaryColumn("C", "${nutrition.c.format()} g", Color(0xFF42A5F5))
                        SummaryColumn("F", "${nutrition.f.format()} g", Color(0xFFFFB300))
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ingredients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, "Add", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(selectedItems, key = { it.first.id }) { item ->
                IngredientSelectedRow(
                    item = item,
                    amountsTextMap = amountsTextMap,
                    onRemove = {
                        mainViewModel.temporarySelectedIngredients.remove(item)
                    },
                    onAmountChange = { ing, valDouble ->
                        val index = mainViewModel.temporarySelectedIngredients.indexOfFirst { it.first.id == ing.id }
                        if (index != -1) {
                            mainViewModel.temporarySelectedIngredients[index] = ing to valDouble
                        }
                    }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(onClick = { onSave(recipeName, selectedImageUri, selectedItems.toList()) }, modifier = Modifier.weight(1f), enabled = recipeName.isNotBlank() && selectedItems.isNotEmpty()) { Text("Save") }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Select Ingredients")
                    TextButton(onClick = {
                        mainViewModel.navigateTo(Screen.ADD_INGREDIENT, returnTo = Screen.ADD_RECIPE)
                        showDialog = false
                    }) {
                        Icon(Icons.Default.Add, null)
                        Text("New")
                    }
                }
            },
            text = {
                AddIngredientToRecipeDialog(
                    ingredients = allIngredients,
                    onIngredientsSelected = { newIngredients ->
                        newIngredients.forEach { ingredient ->
                            if (!selectedItems.any { it.first.id == ingredient.id }) {
                                mainViewModel.temporarySelectedIngredients.add(ingredient to 100.0)
                            }
                        }
                        showDialog = false
                    },
                    onDismiss = { showDialog = false },
                    onNewIngredientClick = {
                        mainViewModel.navigateTo(
                            screen = Screen.ADD_INGREDIENT,
                            returnTo = Screen.ADD_RECIPE
                        )
                        showDialog = false
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Close") }
            }
        )
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Select Photo Source") },
            confirmButton = {
                TextButton(onClick = {
                    val uri = createImageUri()
                    tempImageUri = uri
                    cameraLauncher.launch(uri)
                    showImageSourceDialog = false
                }) { Text("Camera") }
            },
            dismissButton = {
                TextButton(onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    showImageSourceDialog = false
                }) { Text("Gallery") }
            }
        )
    }
}

@Composable
fun IngredientSelectedRow(
    item: Pair<Ingredient, Double>,
    amountsTextMap: MutableMap<Int, String>,
    onRemove: () -> Unit,
    onAmountChange: (Ingredient, Double) -> Unit
) {
    val (ing, amount) = item
    val factor = amount / 100.0
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                if (!ing.imageUri.isNullOrBlank()) AsyncImage(model = ing.imageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Text("🥦")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ing.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("${(ing.caloriesPer100 * factor).format()} kcal", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(
                value = amountsTextMap[ing.id] ?: "",
                onValueChange = {
                    amountsTextMap[ing.id] = it
                    onAmountChange(ing, it.toDoubleOrNull() ?: 0.0)
                },
                modifier = Modifier.width(80.dp),
                label = { Text(ing.unit, style = MaterialTheme.typography.labelSmall) },
                singleLine = true
            )
            IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.6f)) }
        }
    }
}

@Composable
fun SummaryColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold)
    }
}