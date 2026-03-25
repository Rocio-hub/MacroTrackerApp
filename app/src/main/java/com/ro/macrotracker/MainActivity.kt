package com.ro.macrotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient
import com.ro.macrotracker.data.local.entity.Recipe
import com.ro.macrotracker.ui.theme.MacroTrackerTheme
import kotlinx.coroutines.launch

enum class Screen {
    INGREDIENT_LIST,
    ADD_INGREDIENT,
    RECIPE_LIST,
    ADD_RECIPE,
    RECIPE_DETAIL,
    DAILY_PLANNER
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

        setContent {

            var currentScreen by remember { mutableStateOf(Screen.INGREDIENT_LIST) }
            var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
            var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }

            val ingredients by ingredientDao.getAllIngredients().collectAsState(initial = emptyList())
            val recipes by recipeDao.getAllRecipes().collectAsState(initial = emptyList())

            val scope = rememberCoroutineScope()

            val recipeIngredientDao = db.recipeIngredientDao()
            val allRecipeIngredients by recipeIngredientDao
                .getAllRecipeIngredients()
                .collectAsState(initial = emptyList())

            val recipeIngredientsMap = allRecipeIngredients.groupBy { it.recipeId }

            MacroTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column {

                            Row {

                                Button(onClick = {
                                    currentScreen = Screen.INGREDIENT_LIST
                                }) {
                                    Text("Ingredients")
                                }

                                Button(onClick = {
                                    currentScreen = Screen.RECIPE_LIST
                                }) {
                                    Text("Recipes")
                                }

                                Button(onClick = {
                                    currentScreen = Screen.DAILY_PLANNER
                                }) {
                                    Text("Planner")
                                }
                            }

                            Row {

                                Button(onClick = {
                                    currentScreen = Screen.ADD_INGREDIENT
                                }) {
                                    Text("+ Ingredient")
                                }

                                Button(onClick = {
                                    currentScreen = Screen.ADD_RECIPE
                                }) {
                                    Text("+ Recipe")
                                }
                            }
                        }
                    }
                ) { innerPadding ->

                    when (currentScreen) {

                        Screen.INGREDIENT_LIST -> {
                            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                                items(ingredients) { ingredient ->
                                    IngredientItem(
                                        ingredient = ingredient,
                                        onClick = { selectedIngredient = ingredient }
                                    )
                                }
                            }
                        }

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
                                        currentScreen = Screen.INGREDIENT_LIST
                                    }
                                }
                            )
                        }

                        Screen.RECIPE_LIST -> {
                            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                                items(recipes) { recipe ->
                                    Text(
                                        text = recipe.name,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .clickable {
                                                selectedRecipe = recipe
                                                currentScreen = Screen.RECIPE_DETAIL
                                            }
                                    )
                                }
                            }
                        }

                        Screen.ADD_RECIPE -> {
                            AddRecipeScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSave = { recipe ->
                                    scope.launch {
                                        recipeDao.insertRecipe(recipe)
                                        currentScreen = Screen.RECIPE_LIST
                                    }
                                }
                            )
                        }

                        Screen.RECIPE_DETAIL -> {
                            RecipeDetailScreen(
                                recipe = selectedRecipe!!,
                                ingredientDao = ingredientDao,
                                recipeIngredientDao = db.recipeIngredientDao(),
                                onBack = {
                                    currentScreen = Screen.RECIPE_LIST
                                }
                            )
                        }

                        Screen.DAILY_PLANNER -> {
                            DailyPlannerScreen(
                                recipes = recipes,
                                ingredients = ingredients,
                                recipeIngredientsMap = recipeIngredientsMap
                            )
                        }
                    }
                }
            }
        }
    }
}