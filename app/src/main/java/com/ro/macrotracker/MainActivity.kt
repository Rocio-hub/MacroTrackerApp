package com.ro.macrotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.ui.components.IngredientItem
import com.ro.macrotracker.ui.screens.AddIngredientScreen
import com.ro.macrotracker.ui.screens.AddRecipeScreen
import com.ro.macrotracker.ui.screens.DailyPlannerScreen
import com.ro.macrotracker.ui.theme.MacroTrackerTheme
import com.ro.macrotracker.ui.screens.RecipeDetailScreen
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.data.mappers.toDomain
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.data.mappers.toIngredientEntity
import com.ro.macrotracker.data.mappers.toRecipeEntity
import com.ro.macrotracker.data.local.entity.Ingredient as IngredientEntity

enum class Screen {
    RECIPES,
    INGREDIENTS,
    PLANNER,
    ADD_RECIPE,
    ADD_INGREDIENT,
    RECIPE_DETAIL
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = androidx.room.Room.databaseBuilder(
            applicationContext,
            com.ro.macrotracker.data.local.database.Database::class.java,
            "macro-db"
        ).build()

        val ingredientDao = db.ingredientDao()
        val recipeDao = db.recipeDao()
        val recipeIngredientDao = db.recipeIngredientDao()

        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val context = LocalContext.current

            var currentScreen by remember { mutableStateOf(Screen.RECIPES) }
            var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
            var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
            var ingredientToDelete by remember { mutableStateOf<IngredientEntity?>(null) }

            val ingredients: List<IngredientEntity> by ingredientDao.getAllIngredients().collectAsState(initial = emptyList())
            val domainIngredients = ingredients.map { it.toDomain() }
            val recipes by recipeDao.getAllRecipes().collectAsState(initial = emptyList())
            val allRecipeIngredients by recipeIngredientDao.getAllRecipeIngredients().collectAsState(initial = emptyList())
            val recipeIngredientsMap = allRecipeIngredients.groupBy { it.recipeId }
            val domainRecipes = recipes.map { it.toDomain() }

            MacroTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentScreen) {
                                        Screen.RECIPES -> "Recipes"
                                        Screen.INGREDIENTS -> "Ingredients"
                                        Screen.PLANNER -> "Planner"
                                        Screen.ADD_RECIPE -> "Add Recipe"
                                        Screen.ADD_INGREDIENT -> "Add Ingredient"
                                        Screen.RECIPE_DETAIL -> selectedRecipe?.name ?: "Recipe"
                                    }
                                )
                            },
                            navigationIcon = {
                                if (currentScreen == Screen.RECIPE_DETAIL ||
                                    currentScreen == Screen.ADD_RECIPE ||
                                    currentScreen == Screen.ADD_INGREDIENT
                                ) {
                                    TextButton(onClick = {
                                        currentScreen = when (currentScreen) {
                                            Screen.RECIPE_DETAIL -> Screen.RECIPES
                                            Screen.ADD_RECIPE -> Screen.RECIPES
                                            Screen.ADD_INGREDIENT -> Screen.INGREDIENTS
                                            else -> currentScreen
                                        }
                                    }) {
                                        Text("Back")
                                    }
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        when (currentScreen) {
                            Screen.RECIPES -> {
                                FloatingActionButton(onClick = { currentScreen = Screen.ADD_RECIPE }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                            Screen.INGREDIENTS -> {
                                FloatingActionButton(onClick = {
                                    selectedIngredient = null
                                    currentScreen = Screen.ADD_INGREDIENT
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }
                            else -> {}
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == Screen.RECIPES,
                                onClick = { currentScreen = Screen.RECIPES },
                                label = { Text("Recipes") },
                                icon = { Text("🍽") }
                            )

                            NavigationBarItem(
                                selected = currentScreen == Screen.INGREDIENTS,
                                onClick = { currentScreen = Screen.INGREDIENTS },
                                label = { Text("Ingredients") },
                                icon = { Text("🥦") }
                            )

                            NavigationBarItem(
                                selected = currentScreen == Screen.PLANNER,
                                onClick = { currentScreen = Screen.PLANNER },
                                label = { Text("Planner") },
                                icon = { Text("📊") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {

                            Screen.RECIPES -> {
                                LazyColumn {
                                    items(domainRecipes) { recipe ->
                                        Text(
                                            text = recipe.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedRecipe = recipe
                                                    currentScreen = Screen.RECIPE_DETAIL
                                                }
                                                .padding(16.dp)
                                        )
                                    }
                                }
                            }

                            Screen.INGREDIENTS -> {
                                LazyColumn {
                                    items(domainIngredients) { ingredient ->
                                        IngredientItem(
                                            ingredient = ingredient.toIngredientEntity(),
                                            onClick = {
                                                selectedIngredient = ingredient
                                                currentScreen = Screen.ADD_INGREDIENT
                                            },
                                            onDelete = {
                                                scope.launch {
                                                    val usageCount = recipeIngredientDao.getUsageCountForIngredient(ingredient.id)

                                                    if (usageCount > 0) {
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            "Cannot delete: Used in $usageCount recipes",
                                                            android.widget.Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {
                                                        ingredientToDelete = ingredient.toIngredientEntity()
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // ➕ ADD RECIPE
                            Screen.ADD_RECIPE -> {
                                AddRecipeScreen(
                                    onSave = { recipe ->
                                        scope.launch {
                                            recipeDao.insertRecipe(recipe)
                                            currentScreen = Screen.RECIPES
                                        }
                                    }
                                )
                            }

                            // ➕ ADD / EDIT INGREDIENT
                            Screen.ADD_INGREDIENT -> {
                                AddIngredientScreen(
                                    ingredient = selectedIngredient?.toIngredientEntity(),
                                    onSave = { ingredient ->
                                        scope.launch {
                                            if (ingredient.id == 0)
                                                ingredientDao.insertIngredient(ingredient)
                                            else
                                                ingredientDao.updateIngredient(ingredient)

                                            selectedIngredient = null
                                            currentScreen = Screen.INGREDIENTS
                                        }
                                    },
                                    onCancel = {
                                        selectedIngredient = null
                                        currentScreen = Screen.INGREDIENTS
                                    }
                                )
                            }

                            // 📄 RECIPE DETAIL
                            Screen.RECIPE_DETAIL -> {
                                RecipeDetailScreen(
                                    recipe = selectedRecipe!!.toRecipeEntity(),
                                    ingredientDao = ingredientDao,
                                    recipeIngredientDao = recipeIngredientDao,
                                    onBack = { currentScreen = Screen.RECIPES }
                                )
                            }

                            // 📊 PLANNER
                            Screen.PLANNER -> {
                                DailyPlannerScreen(
                                    recipes = recipes,
                                    ingredients = ingredients,
                                    recipeIngredientsMap = recipeIngredientsMap,
                                    onRecipeClick = { recipe ->
                                        selectedRecipe = recipe.toDomain()
                                        currentScreen = Screen.RECIPE_DETAIL
                                    }
                                )
                            }
                        }
                    }
                    if (ingredientToDelete != null) {
                        DeleteConfirmationDialog(
                            ingredientName = ingredientToDelete!!.name,
                            onDismiss = { ingredientToDelete = null },
                            onConfirm = {
                                scope.launch {
                                    ingredientDao.deleteIngredient(ingredientToDelete!!)

                                    // 2. THE TOAST (Shows after successful deletion)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Deleted ${ingredientToDelete!!.name}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    ingredientToDelete = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun DeleteConfirmationDialog(
    ingredientName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Ingredient?") },
        text = { Text("Are you sure you want to delete \"$ingredientName\"? This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}