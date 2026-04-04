package com.ro.macrotracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.*
import com.ro.macrotracker.utils.*

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
                cals += it.caloriesPer100 * ratio
                prot += it.proteinPer100 * ratio
                carbs += it.carbsPer100 * ratio
                fat += it.fatPer100 * ratio
                fiber += it.fiberPer100 * ratio
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

    var sessionIdToDelete by remember { mutableStateOf<Long?>(null) }

    val selectedDate by mainViewModel.selectedDate.collectAsState()

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("planner", 0)
        targetCalories = prefs.getString("targetCalories", "") ?: ""
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DateSelector(
        selectedDate: Long,
        onDateChange: (Long) -> Unit
    ) {
        var showCalendar by remember { mutableStateOf(false) }

        val sdf = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        val dateText = remember(selectedDate) {
            val calendar = Calendar.getInstance()
            val today = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_YEAR, +1)
            val tomorrow = calendar.timeInMillis

            when {
                isSameDay(selectedDate, today) -> "Today"
                isSameDay(selectedDate, yesterday) -> "Yesterday"
                isSameDay(selectedDate, tomorrow) -> "Tomorrow"
                else -> sdf.format(Date(selectedDate))
            }
        }

        if (showCalendar) {
            CalendarDialog(
                initialSelectedDate = selectedDate,
                onDateSelected = { onDateChange(it) },
                onDismiss = { showCalendar = false }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateChange(selectedDate - 24 * 60 * 60 * 1000) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
            }

            Button(
                onClick = { showCalendar = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { onDateChange(selectedDate + 24 * 60 * 60 * 1000) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }

    fun isSameDay(date1: Long, date2: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(date1)) == fmt.format(Date(date2))
    }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

        DateSelector(
            selectedDate = selectedDate,
            onDateChange = { newDate ->
                mainViewModel.onDateChange(newDate)
            }
        )

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
                            sessionIdToDelete?.let { sessionId ->
                                mainViewModel.deleteMealBySession(sessionId)
                            }
                            showDeleteDialog = false
                            recipeToDelete = null
                            sessionIdToDelete = null
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)) {
                Column {
                    Text("Daily Progress", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${totalNutrition.calories.format()} / ${target.format()} kcal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { if (target > 0) (totalNutrition.calories / target).toFloat().coerceAtMost(1f) else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(vertical = 12.dp),
                    strokeCap = StrokeCap.Round,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroSummaryItem("Protein", totalNutrition.protein.coerceAtLeast(0.0), Color(0xFFEF5350))
                    MacroSummaryItem("Carbs", totalNutrition.carbs.coerceAtLeast(0.0), Color(0xFF42A5F5))
                    MacroSummaryItem("Fat", totalNutrition.fat.coerceAtLeast(0.0), Color(0xFFFFB300))
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
            val eatenMeals = dailyLogs.groupBy { it.mealSessionId }

            if (eatenMeals.isNotEmpty()) {
                item {
                        val title = if (isSameDay(selectedDate, System.currentTimeMillis())) "Consumed Today" else "Consumed this day"
                        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                }

                items(eatenMeals.keys.toList().sortedDescending()) { sessionId ->
                    val logsInThisMeal = eatenMeals[sessionId] ?: emptyList()
                    val firstLog = logsInThisMeal.firstOrNull()
                    val recipe = recipes.find { it.id == firstLog?.recipeId }

                    val mealNutrition = logsInThisMeal.fold(com.ro.macrotracker.domain.Nutrition()) { acc, log ->
                        val ing = ingredients.find { it.id == log.ingredientId }
                        val ratio = log.amount / 100.0
                        if (ing != null) {
                            com.ro.macrotracker.domain.Nutrition(
                                calories = acc.calories + (ing.caloriesPer100 * ratio),
                                protein = acc.protein + (ing.proteinPer100 * ratio),
                                carbs = acc.carbs + (ing.carbsPer100 * ratio),
                                fat = acc.fat + (ing.fatPer100 * ratio),
                                fiber = acc.fiber + (ing.fiberPer100 * ratio)
                            )
                        } else acc
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(recipe?.name ?: "Unknown", fontWeight = FontWeight.Bold) },
                            supportingContent = {
                                Text("${mealNutrition.calories.format()} kcal", style = MaterialTheme.typography.bodyMedium)
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    recipeToDelete = recipe
                                    sessionIdToDelete = sessionId
                                    showDeleteDialog = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroSummaryItem("Protein", totalNutrition.protein.coerceAtLeast(0.0), Color(0xFFEF5350))
                            MacroSummaryItem("Carbs", totalNutrition.carbs.coerceAtLeast(0.0), Color(0xFF42A5F5))
                            MacroSummaryItem("Fat", totalNutrition.fat.coerceAtLeast(0.0), Color(0xFFFFB300))
                        }
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 60.dp) // 👈 Da una base sólida al componente
    ) {
        Surface(
            color = color.copy(alpha = 0.2f),
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }

        Text(
            text = "${value.format()} g",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    initialSelectedDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDate
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(it)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}