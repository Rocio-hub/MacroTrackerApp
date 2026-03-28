package com.ro.macrotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient
import com.ro.macrotracker.data.local.entity.Recipe
import com.ro.macrotracker.ui.components.IngredientItem
import com.ro.macrotracker.ui.screens.AddIngredientScreen
import com.ro.macrotracker.ui.screens.AddRecipeScreen
import com.ro.macrotracker.ui.screens.DailyPlannerScreen
import com.ro.macrotracker.ui.theme.MacroTrackerTheme
import com.ro.macrotracker.ui.screens.RecipeDetailScreen
import kotlinx.coroutines.launch

enum class Screen {
    RECIPES,
    INGREDIENTS,
    PLANNER,
    ADD_RECIPE,
    ADD_INGREDIENT,
    RECIPE_DETAIL
}

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

            var currentScreen by remember { mutableStateOf(Screen.RECIPES) }
            var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
            var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }

            val ingredients by ingredientDao.getAllIngredients().collectAsState(initial = emptyList())
            val recipes by recipeDao.getAllRecipes().collectAsState(initial = emptyList())
            val allRecipeIngredients by recipeIngredientDao
                .getAllRecipeIngredients()
                .collectAsState(initial = emptyList())

            val recipeIngredientsMap = allRecipeIngredients.groupBy { it.recipeId }

            val scope = rememberCoroutineScope()

            MacroTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    // ✅ FAB (only for add actions)
                    floatingActionButton = {
                        when (currentScreen) {

                            Screen.RECIPES -> {
                                FloatingActionButton(
                                    onClick = { currentScreen = Screen.ADD_RECIPE }
                                ) {
                                    Text("+")
                                }
                            }

                            Screen.INGREDIENTS -> {
                                FloatingActionButton(
                                    onClick = {
                                        selectedIngredient = null
                                        currentScreen = Screen.ADD_INGREDIENT
                                    }
                                ) {
                                    Text("+")
                                }
                            }

                            else -> {}
                        }
                    },

                    // ✅ Bottom navigation
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

                    when (currentScreen) {

                        // 🍽 RECIPES
                        Screen.RECIPES -> {
                            LazyColumn {
                                items(recipes) { recipe ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        Text(
                                            text = recipe.name,
                                            modifier = Modifier.clickable {
                                                selectedRecipe = recipe
                                                currentScreen = Screen.RECIPE_DETAIL
                                            }
                                        )

                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    recipeDao.deleteRecipe(recipe)
                                                }
                                            }
                                        ) {
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                        }

                        // 🥦 INGREDIENTS
                        Screen.INGREDIENTS -> {
                            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                                items(ingredients) { ingredient ->
                                    IngredientItem(
                                        ingredient = ingredient,
                                        onClick = {
                                            selectedIngredient = ingredient
                                            currentScreen = Screen.ADD_INGREDIENT
                                        },
                                        onDelete = {
                                            scope.launch {
                                                ingredientDao.deleteIngredient(ingredient)

                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // ➕ ADD RECIPE
                        Screen.ADD_RECIPE -> {
                            AddRecipeScreen(
                                modifier = Modifier.padding(innerPadding),
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
                                modifier = Modifier.padding(innerPadding),
                                ingredient = selectedIngredient,
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
                                recipe = selectedRecipe!!,
                                ingredientDao = ingredientDao,
                                recipeIngredientDao = recipeIngredientDao,
                                onBack = {
                                    currentScreen = Screen.RECIPES
                                }
                            )
                        }

                        // 📊 PLANNER
                        Screen.PLANNER -> {
                            DailyPlannerScreen(
                                recipes = recipes,
                                ingredients = ingredients,
                                recipeIngredientsMap = recipeIngredientsMap,
                                onRecipeClick = { recipe ->
                                    selectedRecipe = recipe
                                    currentScreen = Screen.RECIPE_DETAIL
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}