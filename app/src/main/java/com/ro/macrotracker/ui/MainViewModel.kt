package com.ro.macrotracker.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ro.macrotracker.Screen
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe

class MainViewModel : ViewModel() {

    private val _currentScreen = mutableStateOf(Screen.RECIPES)
    val currentScreen: State<Screen> = _currentScreen

    private val _selectedRecipe = mutableStateOf<Recipe?>(null)
    val selectedRecipe: State<Recipe?> = _selectedRecipe

    private val _selectedIngredient = mutableStateOf<Ingredient?>(null)
    val selectedIngredient: State<Ingredient?> = _selectedIngredient

    fun navigateTo(screen: Screen, recipe: Recipe? = null, ingredient: Ingredient? = null) {
        _selectedRecipe.value = recipe
        _selectedIngredient.value = ingredient
        _currentScreen.value = screen
    }

    fun goBack() {
        val nextScreen = when (_currentScreen.value) {
            Screen.RECIPE_DETAIL -> Screen.RECIPES
            Screen.ADD_RECIPE -> Screen.RECIPES
            Screen.ADD_INGREDIENT -> Screen.INGREDIENTS
            else -> _currentScreen.value
        }
        _currentScreen.value = nextScreen
    }
}