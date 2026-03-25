package com.ro.macrotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ro.macrotracker.data.local.entity.Ingredient
import com.ro.macrotracker.ui.theme.MacroTrackerTheme
import kotlinx.coroutines.launch

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

            val ingredients by ingredientDao.getAllIngredients().collectAsState(initial = emptyList())
            val recipes by recipeDao.getAllRecipes().collectAsState(initial = emptyList())

            var showAddIngredient by remember { mutableStateOf(false) }
            var showRecipeList by remember { mutableStateOf(false) }
            var showAddRecipe by remember { mutableStateOf(false) }
            var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }

            val scope = rememberCoroutineScope()

            MacroTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        Column {

                            FloatingActionButton(
                                onClick = {
                                    showRecipeList = true
                                    showAddRecipe = false
                                    showAddIngredient = false
                                    selectedIngredient = null
                                }
                            ) {
                                Text("Recipes")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FloatingActionButton(
                                onClick = {
                                    showAddRecipe = true
                                    showRecipeList = false
                                    showAddIngredient = false
                                    selectedIngredient = null
                                }
                            ) {
                                Text("+R")
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FloatingActionButton(
                                onClick = {
                                    showAddIngredient = true
                                    showRecipeList = false
                                    showAddRecipe = false
                                    selectedIngredient = null
                                }
                            ) {
                                Text("+I")
                            }
                        }
                    }
                ) { innerPadding ->

                    when {
                        // Ingredient detail
                        selectedIngredient != null -> {
                            IngredientDetailScreen(
                                ingredient = selectedIngredient!!,
                                onBack = {
                                    selectedIngredient = null
                                    showRecipeList = false
                                }
                            )
                        }

                        // Add ingredient
                        showAddIngredient -> {
                            AddIngredientScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSave = { ingredient ->
                                    scope.launch {
                                        ingredientDao.insertIngredient(ingredient)
                                        showAddIngredient = false
                                    }
                                }
                            )
                        }

                        // Add recipe
                        showAddRecipe -> {
                            AddRecipeScreen(
                                modifier = Modifier.padding(innerPadding),
                                onSave = { recipe ->
                                    scope.launch {
                                        recipeDao.insertRecipe(recipe)
                                        showAddRecipe = false
                                        showRecipeList = true
                                    }
                                }
                            )
                        }

                        // Recipe list
                        showRecipeList -> {
                            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                                items(recipes) { recipe ->
                                    Text(text = recipe.name)
                                }
                            }
                        }

                        // Ingredient list (default)
                        else -> {
                            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                                items(ingredients) { ingredient ->
                                    IngredientItem(
                                        ingredient = ingredient,
                                        onClick = { selectedIngredient = ingredient }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}