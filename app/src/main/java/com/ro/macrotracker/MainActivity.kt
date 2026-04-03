package com.ro.macrotracker

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ro.macrotracker.data.local.entity.Ingredient as IngredientEntity
import com.ro.macrotracker.data.mappers.toDomain
import com.ro.macrotracker.data.mappers.toIngredientEntity
import com.ro.macrotracker.data.mappers.toRecipeEntity
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.ui.MainViewModel
import com.ro.macrotracker.ui.components.DeleteConfirmationDialog
import com.ro.macrotracker.ui.components.IngredientItem
import com.ro.macrotracker.ui.screens.*
import com.ro.macrotracker.ui.theme.MacroTrackerTheme
import kotlinx.coroutines.launch

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
            val mainViewModel: MainViewModel = viewModel()

            val currentScreen: Screen by mainViewModel.currentScreen
            val selectedRecipe: Recipe? by mainViewModel.selectedRecipe
            val selectedIngredient: Ingredient? by mainViewModel.selectedIngredient

            var ingredientToDelete by remember { mutableStateOf<IngredientEntity?>(null) }

            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            val ingredients by ingredientDao.getAllIngredients().collectAsState(initial = emptyList())
            val recipes by recipeDao.getAllRecipes().collectAsState(initial = emptyList())
            val allRecipeIngredients by recipeIngredientDao.getAllRecipeIngredients().collectAsState(initial = emptyList())
            val recipeIngredientsMap = allRecipeIngredients.groupBy { it.recipeId }

            MacroTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(when(currentScreen) {
                                    Screen.RECIPES -> "Recipes"
                                    Screen.INGREDIENTS -> "Ingredients"
                                    Screen.PLANNER -> "Planner"
                                    Screen.ADD_RECIPE -> "Add Recipe"
                                    Screen.ADD_INGREDIENT -> "Add Ingredient"
                                    Screen.RECIPE_DETAIL -> selectedRecipe?.name ?: "Detail"
                                })
                            },
                            navigationIcon = {
                                if (currentScreen == Screen.RECIPE_DETAIL ||
                                    currentScreen == Screen.ADD_RECIPE ||
                                    currentScreen == Screen.ADD_INGREDIENT) {
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
                        if (currentScreen == Screen.RECIPES) {
                            FloatingActionButton(onClick = { mainViewModel.navigateTo(Screen.ADD_RECIPE) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        } else if (currentScreen == Screen.INGREDIENTS) {
                            FloatingActionButton(onClick = { mainViewModel.navigateTo(Screen.ADD_INGREDIENT) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.RECIPES -> {
                                LazyColumn {
                                    items(recipes) { recipe ->
                                        val domainRecipe = recipe.toDomain()
                                        Text(
                                            text = domainRecipe.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { mainViewModel.navigateTo(Screen.RECIPE_DETAIL, recipe = domainRecipe) }
                                                .padding(16.dp)
                                        )
                                    }
                                }
                            }
                            Screen.INGREDIENTS -> {
                                LazyColumn {
                                    items(ingredients) { ingredient ->
                                        IngredientItem(
                                            ingredient = ingredient,
                                            onClick = { mainViewModel.navigateTo(Screen.ADD_INGREDIENT, ingredient = ingredient.toDomain()) },
                                            onDelete = {
                                                scope.launch {
                                                    val count = recipeIngredientDao.getUsageCountForIngredient(ingredient.id)
                                                    if (count > 0) {
                                                        Toast.makeText(context, "Used in $count recipes", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        ingredientToDelete = ingredient
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            Screen.ADD_RECIPE -> {
                                AddRecipeScreen(onSave = {
                                    scope.launch {
                                        recipeDao.insertRecipe(it)
                                        mainViewModel.navigateTo(Screen.RECIPES)
                                    }
                                })
                            }
                            Screen.ADD_INGREDIENT -> {
                                AddIngredientScreen(
                                    ingredient = selectedIngredient?.toIngredientEntity(),
                                    onSave = {
                                        scope.launch {
                                            if (it.id == 0) ingredientDao.insertIngredient(it)
                                            else ingredientDao.updateIngredient(it)
                                            mainViewModel.navigateTo(Screen.INGREDIENTS)
                                        }
                                    },
                                    onCancel = { mainViewModel.navigateTo(Screen.INGREDIENTS) }
                                )
                            }
                            Screen.RECIPE_DETAIL -> {
                                RecipeDetailScreen(
                                    recipe = selectedRecipe!!.toRecipeEntity(),
                                    ingredientDao = ingredientDao,
                                    recipeIngredientDao = recipeIngredientDao,
                                    onBack = { mainViewModel.navigateTo(Screen.RECIPES) }
                                )
                            }
                            Screen.PLANNER -> {
                                DailyPlannerScreen(
                                    recipes = recipes,
                                    ingredients = ingredients,
                                    recipeIngredientsMap = recipeIngredientsMap,
                                    onRecipeClick = { mainViewModel.navigateTo(Screen.RECIPE_DETAIL, recipe = it.toDomain()) }
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