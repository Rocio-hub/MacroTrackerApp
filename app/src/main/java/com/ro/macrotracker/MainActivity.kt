package com.ro.macrotracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.ro.macrotracker.ui.components.RecipeItem
import kotlinx.coroutines.launch

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
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("planner", 0) }

            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(repository, prefs)
            )

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
            val selectedDate by mainViewModel.selectedDate.collectAsState()

            var ingredientToDelete by remember { mutableStateOf<com.ro.macrotracker.model.Ingredient?>(null) }
            val scope = rememberCoroutineScope()

            MacroTrackerTheme {
                if (showWelcomeDialog) {
                    WelcomeGoalDialog(
                        tempValue = tempTargetCalories,
                        onValueChange = { tempTargetCalories = it },
                        onConfirm = {
                            if (tempTargetCalories.isNotEmpty()) {
                                mainViewModel.updateGlobalTarget(tempTargetCalories)

                                prefs.edit().putBoolean("isFirstRun", false).apply()
                                showWelcomeDialog = false
                            }
                        }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MainTopAppBar(
                            currentScreen = currentScreen,
                            selectedRecipeName = selectedRecipe?.name,
                            onBack = { mainViewModel.goBack() }
                        )
                    },
                    bottomBar = {
                        MainNavigationBar(
                            currentScreen = currentScreen,
                            onNavigate = { mainViewModel.navigateTo(it) }
                        )
                    },
                    floatingActionButton = {
                        MainFloatingActionButton(
                            currentScreen = currentScreen,
                            onAddRecipe = { mainViewModel.navigateTo(Screen.ADD_RECIPE) },
                            onAddIngredient = { mainViewModel.navigateTo(Screen.ADD_INGREDIENT) }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.RECIPES -> {
                                RecipesListContent(
                                    mainViewModel = mainViewModel,
                                    recipes = recipes,
                                    allRI = allRI,
                                    ingredients = ingredients
                                )
                            }
                            Screen.INGREDIENTS -> {
                                IngredientsListContent(
                                    mainViewModel = mainViewModel,
                                    onDeleteRequest = { ingredient ->
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
                            Screen.ADD_RECIPE -> {
                                AddRecipeScreen(
                                    allIngredients = ingredients,
                                    onSave = { name, imageUri, selectedIngredientsList ->
                                        mainViewModel.saveFullRecipe(name, imageUri, selectedIngredientsList)
                                        mainViewModel.navigateTo(Screen.RECIPES)
                                    },
                                    onCancel = { mainViewModel.navigateTo(Screen.RECIPES) }
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
                                    AdjustMealScreenContent(recipeModel, mainViewModel, selectedDate)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(currentScreen: Screen, selectedRecipeName: String?, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(when(currentScreen) {
                Screen.RECIPES -> "Recipes"
                Screen.INGREDIENTS -> "Ingredients"
                Screen.PLANNER -> "Daily Planner"
                Screen.ADD_RECIPE -> "Add Recipe"
                Screen.ADD_INGREDIENT -> "Add Ingredient"
                Screen.RECIPE_DETAIL -> selectedRecipeName ?: "Detail"
                Screen.ADJUST_MEAL -> "Adjust Meal"
            })
        },
        navigationIcon = {
            if (currentScreen !in listOf(Screen.RECIPES, Screen.INGREDIENTS, Screen.PLANNER)) {
                TextButton(onClick = onBack) { Text("Back") }
            }
        }
    )
}

@Composable
fun MainNavigationBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = currentScreen == Screen.RECIPES,
            onClick = { onNavigate(Screen.RECIPES) },
            label = { Text("Recipes") },
            icon = { Text("🍽️") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.INGREDIENTS,
            onClick = { onNavigate(Screen.INGREDIENTS) },
            label = { Text("Ingredients") },
            icon = { Text("🥦") }
        )
        NavigationBarItem(
            selected = currentScreen == Screen.PLANNER,
            onClick = { onNavigate(Screen.PLANNER) },
            label = { Text("Planner") },
            icon = { Text("📊") }
        )
    }
}

@Composable
fun MainFloatingActionButton(currentScreen: Screen, onAddRecipe: () -> Unit, onAddIngredient: () -> Unit) {
    when (currentScreen) {
        Screen.RECIPES -> FloatingActionButton(onClick = onAddRecipe) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
        Screen.INGREDIENTS -> FloatingActionButton(onClick = onAddIngredient) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
        else -> {}
    }
}

@Composable
fun RecipesListContent(
    mainViewModel: MainViewModel,
    recipes: List<com.ro.macrotracker.model.Recipe>,
    allRI: List<com.ro.macrotracker.model.RecipeIngredient>,
    ingredients: List<com.ro.macrotracker.model.Ingredient>
) {
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
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (recipeSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { mainViewModel.onRecipeSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, null)
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
                // Cálculo de nutrición delegado al dominio
                val recipeIngredients = allRI.filter { it.recipeId == recipe.id }
                val nutrition = com.ro.macrotracker.domain.calculateRecipeNutrition(
                    recipeIngredients,
                    ingredients
                )

                RecipeItem(
                    recipe = recipe,
                    nutrition = nutrition,
                    onClick = { mainViewModel.navigateTo(Screen.RECIPE_DETAIL, recipe = recipe) }
                )
            }
        }
    }
}

@Composable
fun IngredientsListContent(
    mainViewModel: MainViewModel,
    onDeleteRequest: (com.ro.macrotracker.model.Ingredient) -> Unit
) {
    val searchQuery by mainViewModel.ingredientSearchQuery.collectAsState()
    val filteredIngredients by mainViewModel.filteredIngredients.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures(onTap = { focusManager.clearFocus(); keyboardController?.hide() })
    }) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { mainViewModel.onIngredientSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search ingredients...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredIngredients) { ingredient ->
                IngredientItem(
                    ingredient = ingredient,
                    onClick = { mainViewModel.navigateTo(Screen.ADD_INGREDIENT, ingredient = ingredient) },
                    onDelete = { onDeleteRequest(ingredient) }
                )
            }
        }
    }
}

@Composable
fun AdjustMealScreenContent(recipe: com.ro.macrotracker.model.Recipe, mainViewModel: MainViewModel, selectedDate: Long) {
    val recipeIngredients by mainViewModel.allRecipeIngredients.collectAsState()
    val allEntities by mainViewModel.ingredients.collectAsState()

    val initialItems = recipeIngredients
        .filter { it.recipeId == recipe.id }
        .mapNotNull { ri ->
            val entity = allEntities.find { it.id == ri.ingredientId }
            entity?.let { it to ri.amount }
        }

    AdjustMealScreen(
        recipe = recipe,
        initialIngredients = initialItems,
        onConfirm = { adjustedItems ->
            mainViewModel.saveMealToPlanner(recipe.id, adjustedItems, selectedDate)
            mainViewModel.navigateTo(Screen.PLANNER)
        },
        onCancel = { mainViewModel.goBack() }
    )
}

@Composable
fun WelcomeGoalDialog(tempValue: String, onValueChange: (String) -> Unit, onConfirm: () -> Unit) {
    val dialogFocusManager = LocalFocusManager.current
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Welcome to MacroTracker!") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().pointerInput(Unit) { detectTapGestures(onTap = { dialogFocusManager.clearFocus() }) }) {
                Text("Let's set your daily calorie goal.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = tempValue,
                    onValueChange = onValueChange,
                    label = { Text("Daily Calories Target") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = tempValue.isNotEmpty()) {
                Text("Get Started")
            }
        }
    )
}