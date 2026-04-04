package com.ro.macrotracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.model.RecipeIngredient
import com.ro.macrotracker.domain.calculateRecipeNutrition
import com.ro.macrotracker.ui.viewmodel.DailyPlannerViewModel
import com.ro.macrotracker.utils.format

@Composable
fun DailyPlannerScreen(
    recipes: List<Recipe>,
    ingredients: List<Ingredient>,
    recipeIngredientsMap: Map<Int, List<RecipeIngredient>>,
    dailyLogs: List<com.ro.macrotracker.model.DailyIngredientLog>,
    onRecipeClick: (Recipe) -> Unit,
    mainViewModel: com.ro.macrotracker.ui.MainViewModel
) {
    val context = LocalContext.current
    val viewModel: DailyPlannerViewModel = viewModel()

    val totalNutrition = remember(dailyLogs, ingredients) {
        var cals = 0.0
        var prot = 0.0
        var carbs = 0.0
        var fat = 0.0
        var fiber = 0.0

        dailyLogs.forEach { log ->
            val ing = ingredients.find { it.id == log.ingredientId }
            ing?.let {
                val ratio = log.amount / 100.0
                cals += it.caloriesPer100g * ratio
                prot += it.proteinPer100g * ratio
                carbs += it.carbsPer100g * ratio
                fat += it.fatPer100g * ratio
                fiber += it.fiberPer100g * ratio
            }
        }
        com.ro.macrotracker.domain.Nutrition(calories = cals, protein = prot, carbs=carbs, fat=fat, fiber=fiber)
    }

    var targetCalories by remember { mutableStateOf("") }
    val target = targetCalories.toDoubleOrNull() ?: 0.0
    val searchQuery by viewModel.plannerSearchQuery.collectAsState()

    val filteredRecipes = remember(searchQuery, recipes) {
        if (searchQuery.isBlank()) recipes
        else recipes.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("planner", 0)
        targetCalories = prefs.getString("targetCalories", "") ?: ""
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        if (showDeleteDialog && recipeToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    recipeToDelete = null
                },
                title = { Text("Confirm Deletion") },
                text = {
                    Text(
                        text = buildAnnotatedString {
                            append("Are you sure you want to remove ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("${recipeToDelete?.name}")
                            }
                            append(" from your planner?")
                        }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            recipeToDelete?.let { recipe ->
                                mainViewModel.deleteMeal(recipe.id)
                            }
                            showDeleteDialog = false
                            recipeToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        recipeToDelete = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        OutlinedTextField(
            value = targetCalories,
            onValueChange = {
                targetCalories = it
                context.getSharedPreferences("planner", 0).edit().putString("targetCalories", it).apply()
            },
            label = { Text("Daily Calories Target") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            })
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Daily Progress", style = MaterialTheme.typography.labelMedium)
                        Text("${totalNutrition.calories.format()} / ${target.format()} kcal",
                            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    }
                }
                LinearProgressIndicator(
                    progress = { if (target > 0) (totalNutrition.calories / target).toFloat().coerceAtMost(1f) else 0f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 8.dp),
                    strokeCap = StrokeCap.Round
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MacroSummaryItem("Protein", totalNutrition.protein, Color(0xFFEF5350))
                    MacroSummaryItem("Carbs", totalNutrition.carbs, Color(0xFF42A5F5))
                    MacroSummaryItem("Fat", totalNutrition.fat, Color(0xFFFFB300))
                }
            }
        }

        Text("Add to your day", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            placeholder = { Text("Search recipe...") },
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            val eatenRecipes = dailyLogs.groupBy { it.recipeId }
            if (eatenRecipes.isNotEmpty()) {
                item { Text("Consumed Today", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary) }
                items(eatenRecipes.keys.toList()) { recipeId ->
                    val recipe = recipes.find { it.id == recipeId }
                    val logsForThisRecipe = eatenRecipes[recipeId] ?: emptyList()
                    val mealCals = logsForThisRecipe.sumOf { log ->
                        val ing = ingredients.find { it.id == log.ingredientId }
                        ((ing?.caloriesPer100g ?: 0.0) * log.amount) / 100.0
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    ) {
                        ListItem(
                            headlineContent = { Text(recipe?.name ?: "Unknown", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("${mealCals.format()} kcal") },
                            trailingContent = {
                                IconButton(onClick = {
                                    val selectedRecipe = recipes.find { it.id == recipeId }

                                    recipeToDelete = selectedRecipe
                                    showDeleteDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.clickable { recipe?.let { onRecipeClick(it) } }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { Text("All Recipes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary) }

            items(filteredRecipes) { recipe ->
                val ris = recipeIngredientsMap[recipe.id] ?: emptyList()
                val nutrition = calculateRecipeNutrition(ris, ingredients)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onRecipeClick(recipe) }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MacroBadge("P", nutrition.protein, Color(0xFFFFEBEE))
                                MacroBadge("C", nutrition.carbs, Color(0xFFE3F2FD))
                                MacroBadge("F", nutrition.fat, Color(0xFFFFF8E1))
                            }
                        }
                        Text("${nutrition.calories.format()} kcal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MacroSummaryItem(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(16.dp, 4.dp).background(color, MaterialTheme.shapes.extraSmall))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text("${value.format()}g", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MacroBadge(label: String, value: Double, color: Color) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${value.format()}g",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}