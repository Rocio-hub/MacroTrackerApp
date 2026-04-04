package com.ro.macrotracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ro.macrotracker.data.repository.RepositoryImplementation
import com.ro.macrotracker.ui.MainViewModel
import com.ro.macrotracker.ui.MainViewModelFactory
import com.ro.macrotracker.ui.components.DeleteConfirmationDialog
import com.ro.macrotracker.ui.components.IngredientItem
import com.ro.macrotracker.ui.screens.*
import com.ro.macrotracker.ui.theme.MacroTrackerTheme
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.ro.macrotracker.data.mappers.toDomain
import com.ro.macrotracker.model.Recipe
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = com.ro.macrotracker.data.local.database.AppDatabase.getDatabase(applicationContext)
        val repository = RepositoryImplementation(
            ingredientDao = db.ingredientDao(),
            recipeDao = db.recipeDao(),
            recipeIngredientDao = db.recipeIngredientDao(),
            dailyIngredientLogDao = db.dailyIngredientLogDao()
        )

        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(repository)
            )
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("planner", 0) }
            var showWelcomeDialog by remember {
                mutableStateOf(prefs.getBoolean("isFirstRun", true))
            }
            var tempTargetCalories by remember { mutableStateOf("") }

            val currentScreen: Screen by mainViewModel.currentScreen
            val selectedRecipe by mainViewModel.selectedRecipe
            val selectedIngredient by mainViewModel.selectedIngredient

            val ingredients by mainViewModel.ingredients.collectAsState()
            val recipes by mainViewModel.recipes.collectAsState()
            val allRI by mainViewModel.allRecipeIngredients.collectAsState()

            var ingredientToDelete by remember { mutableStateOf<com.ro.macrotracker.model.Ingredient?>(null) }
            val scope = rememberCoroutineScope()

            MacroTrackerTheme {
                if (showWelcomeDialog) {
                    AlertDialog(
                        onDismissRequest = { },
                        title = { Text("Welcome to MacroTracker!") },
                        text = {
                            val dialogFocusManager = LocalFocusManager.current
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        detectTapGestures(onTap = {
                                            dialogFocusManager.clearFocus()
                                        })
                                    }
                            ) {
                                Text("Let's set your daily calorie goal to personalize your experience.")
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(Modifier.focusable())
                                OutlinedTextField(
                                    value = tempTargetCalories,
                                    onValueChange = { tempTargetCalories = it },
                                    label = { Text("Daily Calories Target") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { dialogFocusManager.clearFocus() }
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (tempTargetCalories.isNotEmpty()) {
                                        prefs.edit()
                                            .putBoolean("isFirstRun", false)
                                            .putString("targetCalories", tempTargetCalories)
                                            .apply()
                                        showWelcomeDialog = false
                                    }
                                },
                                enabled = tempTargetCalories.isNotEmpty()
                            ) {
                                Text("Get Started")
                            }
                        }
                    )
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(when(currentScreen) {
                                    Screen.RECIPES -> "Recipes"
                                    Screen.INGREDIENTS -> "Ingredients"
                                    Screen.PLANNER -> "Daily Planner"
                                    Screen.ADD_RECIPE -> "Add Recipe"
                                    Screen.ADD_INGREDIENT -> "Add Ingredient"
                                    Screen.RECIPE_DETAIL -> selectedRecipe?.name ?: "Detail"
                                    Screen.ADJUST_MEAL -> "Adjust Meal"
                                })
                            },
                            navigationIcon = {
                                if (currentScreen != Screen.RECIPES &&
                                    currentScreen != Screen.INGREDIENTS &&
                                    currentScreen != Screen.PLANNER) {
                                    TextButton(onClick = { mainViewModel.goBack() }) {
                                        Text("Back")
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == Screen.RECIPES,
                                onClick = { mainViewModel.navigateTo(Screen.RECIPES) },
                                label = { Text("Recipes") },
                                icon = { Text("🍽️") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.INGREDIENTS,
                                onClick = { mainViewModel.navigateTo(Screen.INGREDIENTS) },
                                label = { Text("Ingredients") },
                                icon = { Text("🥦") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == Screen.PLANNER,
                                onClick = { mainViewModel.navigateTo(Screen.PLANNER) },
                                label = { Text("Planner") },
                                icon = { Text("📊") }
                            )
                        }
                    },
                    floatingActionButton = {
                        when (currentScreen) {
                            Screen.RECIPES -> FloatingActionButton(onClick = { mainViewModel.navigateTo(Screen.ADD_RECIPE) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                            Screen.INGREDIENTS -> FloatingActionButton(onClick = { mainViewModel.navigateTo(Screen.ADD_INGREDIENT) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                            else -> {}
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.RECIPES -> {
                                val recipeSearchQuery by mainViewModel.recipeSearchQuery.collectAsState()
                                val filteredRecipes by mainViewModel.filteredRecipes.collectAsState()

                                val focusManager = LocalFocusManager.current
                                val keyboardController = LocalSoftwareKeyboardController.current

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures(onTap = {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                            })
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = recipeSearchQuery,
                                        onValueChange = { mainViewModel.onRecipeSearchQueryChange(it) },
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        placeholder = { Text("Search recipes...") },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                        trailingIcon = {
                                            if (recipeSearchQuery.isNotEmpty()) {
                                                IconButton(onClick = { mainViewModel.onRecipeSearchQueryChange("") }) {
                                                    Icon(Icons.Default.Clear, contentDescription = null)
                                                }
                                            }
                                        },
                                        shape = MaterialTheme.shapes.medium,
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        })
                                    )

                                    LazyColumn(modifier = Modifier.weight(1f)) {
                                        items(filteredRecipes) { recipe ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                                    .clickable { mainViewModel.navigateTo(Screen.RECIPE_DETAIL, recipe = recipe) },
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                            ) {
                                                ListItem(
                                                    headlineContent = { Text(recipe.name, fontWeight = FontWeight.Bold) },
                                                    trailingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Screen.INGREDIENTS -> {
                                val searchQuery by mainViewModel.ingredientSearchQuery.collectAsState()
                                val filteredIngredients by mainViewModel.filteredIngredients.collectAsState()

                                val focusManager = LocalFocusManager.current
                                val keyboardController = LocalSoftwareKeyboardController.current

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(Unit) {
                                            detectTapGestures(onTap = {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                            })
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { mainViewModel.onIngredientSearchQueryChange(it) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        placeholder = { Text("Search ingredients...") },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { mainViewModel.onIngredientSearchQueryChange("") }) {
                                                    Icon(Icons.Default.Clear, contentDescription = null)
                                                }
                                            }
                                        },
                                        shape = MaterialTheme.shapes.medium,
                                        singleLine = true
                                    )

                                    LazyColumn(modifier = Modifier.weight(1f)) {
                                        items(filteredIngredients) { ingredient ->
                                            IngredientItem(
                                                ingredient = ingredient,
                                                onClick = { mainViewModel.navigateTo(Screen.ADD_INGREDIENT, ingredient = ingredient) },
                                                onDelete = {
                                                    scope.launch {
                                                        val count = mainViewModel.getUsageCount(ingredient.id)
                                                        if (count > 0) {
                                                            Toast.makeText(context, "In use by $count recipes", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            ingredientToDelete = ingredient
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Screen.ADD_RECIPE -> {
                                val modelIngredients = ingredients.map { entity ->
                                    com.ro.macrotracker.model.Ingredient(
                                        id = entity.id,
                                        name = entity.name,
                                        caloriesPer100g = entity.caloriesPer100g,
                                        proteinPer100g = entity.proteinPer100g,
                                        carbsPer100g = entity.carbsPer100g,
                                        fatPer100g = entity.fatPer100g,
                                        fiberPer100g = entity.fiberPer100g,
                                        unit = entity.unit
                                    )
                                }

                                AddRecipeScreen(
                                    allIngredients = modelIngredients,
                                    onSave = { name, selectedItems ->
                                        mainViewModel.saveFullRecipe(name, selectedItems)
                                        mainViewModel.navigateTo(Screen.RECIPES)
                                    },
                                    onCancel = { mainViewModel.goBack() }
                                )
                            }
                            Screen.ADD_INGREDIENT -> {
                                AddIngredientScreen(
                                    ingredient = selectedIngredient,
                                    onSave = { ingredientModel ->
                                        mainViewModel.saveIngredient(ingredientModel)
                                        mainViewModel.navigateTo(Screen.INGREDIENTS)
                                    },
                                    onCancel = { mainViewModel.navigateTo(Screen.INGREDIENTS) }
                                )
                            }
                            Screen.RECIPE_DETAIL -> {
                                RecipeDetailScreen(
                                    recipe = selectedRecipe!!,
                                    repository = repository,
                                    onBack = { mainViewModel.navigateTo(Screen.RECIPES) }
                                )
                            }
                            Screen.PLANNER -> {
                                val dailyLogs by mainViewModel.dailyLogs.collectAsState()
                                val recipeIngredientsMap = remember(allRI) { allRI.groupBy { it.recipeId } }

                                DailyPlannerScreen(
                                    recipes = recipes,
                                    ingredients = ingredients,
                                    recipeIngredientsMap = recipeIngredientsMap,
                                    dailyLogs = dailyLogs,
                                    onRecipeClick = { recipe ->
                                        mainViewModel.navigateTo(Screen.ADJUST_MEAL, recipe = recipe)
                                    },
                                    mainViewModel = mainViewModel
                                )
                            }
                            Screen.ADJUST_MEAL -> {
                                selectedRecipe?.let { recipeModel ->
                                    val recipeIngredients by mainViewModel.allRecipeIngredients.collectAsState()
                                    val allEntities by mainViewModel.ingredients.collectAsState()

                                    val initialItems = recipeIngredients
                                        .filter { it.recipeId == recipeModel.id }
                                        .mapNotNull { ri ->
                                            val entity = allEntities.find { it.id == ri.ingredientId }
                                            if (entity != null) {
                                                val modelIng = com.ro.macrotracker.model.Ingredient(
                                                    id = entity.id,
                                                    name = entity.name,
                                                    caloriesPer100g = entity.caloriesPer100g,
                                                    proteinPer100g = entity.proteinPer100g,
                                                    carbsPer100g = entity.carbsPer100g,
                                                    fatPer100g = entity.fatPer100g,
                                                    fiberPer100g = entity.fiberPer100g,
                                                    unit = entity.unit
                                                )
                                                modelIng to ri.amount
                                            } else null
                                        }

                                    AdjustMealScreen(
                                        recipe = recipeModel,
                                        initialIngredients = initialItems,
                                        onConfirm = { adjustedItems ->
                                            mainViewModel.saveMealToPlanner(
                                                recipeId = recipeModel.id,
                                                items = adjustedItems,
                                                date = System.currentTimeMillis()
                                            )
                                            mainViewModel.navigateTo(Screen.PLANNER)
                                        },
                                        onCancel = { mainViewModel.goBack() }
                                    )
                                }
                            }
                        }
                    }

                    if (ingredientToDelete != null) {
                        DeleteConfirmationDialog(
                            ingredientName = ingredientToDelete!!.name,
                            onDismiss = { ingredientToDelete = null },
                            onConfirm = {
                                mainViewModel.deleteIngredient(ingredientToDelete!!)
                                ingredientToDelete = null
                            }
                        )
                    }
                }
            }
        }
    }
}