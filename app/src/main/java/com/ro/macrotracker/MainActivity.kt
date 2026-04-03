package com.ro.macrotracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import com.ro.macrotracker.data.mappers.toDomain
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = com.ro.macrotracker.data.local.database.AppDatabase.getDatabase(applicationContext)
        val repository = RepositoryImplementation(
            db.ingredientDao(),
            db.recipeDao(),
            db.recipeIngredientDao()
        )

        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(repository)
            )

            val currentScreen: Screen by mainViewModel.currentScreen
            val selectedRecipe by mainViewModel.selectedRecipe
            val selectedIngredient by mainViewModel.selectedIngredient

            val filteredRecipes by mainViewModel.filteredRecipes.collectAsState()

            val ingredients by mainViewModel.ingredients.collectAsState()
            val recipes by mainViewModel.recipes.collectAsState()
            val allRI by mainViewModel.allRecipeIngredients.collectAsState()

            var ingredientToDelete by remember { mutableStateOf<com.ro.macrotracker.model.Ingredient?>(null) }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            MacroTrackerTheme {
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

                                Column {
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
                                        singleLine = true
                                    )

                                    LazyColumn(modifier = Modifier.weight(1f)) {
                                        items(filteredRecipes) { recipe ->
                                            Text(
                                                text = recipe.name,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { mainViewModel.navigateTo(Screen.RECIPE_DETAIL, recipe = recipe) }
                                                    .padding(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Screen.INGREDIENTS -> {
                                val searchQuery by mainViewModel.ingredientSearchQuery.collectAsState()
                                val filteredIngredients by mainViewModel.filteredIngredients.collectAsState()

                                Column {
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
                                AddRecipeScreen(onSave = { recipeEntity ->
                                    val recipeModel = recipeEntity.toDomain()
                                    mainViewModel.saveRecipe(recipeModel)
                                    mainViewModel.navigateTo(Screen.RECIPES)
                                })
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
                                val recipeIngredientsMap = remember(allRI) { allRI.groupBy { it.recipeId } }

                                DailyPlannerScreen(
                                    recipes = recipes,
                                    ingredients = ingredients,
                                    recipeIngredientsMap = recipeIngredientsMap,
                                    onRecipeClick = { mainViewModel.navigateTo(Screen.RECIPE_DETAIL, recipe = it) }
                                )
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