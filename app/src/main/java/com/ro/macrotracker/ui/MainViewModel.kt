package com.ro.macrotracker.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ro.macrotracker.Screen
import com.ro.macrotracker.domain.repository.Repository
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel (private val repository: Repository) : ViewModel() {

    private val _currentScreen = mutableStateOf(Screen.RECIPES)
    val currentScreen: State<Screen> = _currentScreen

    private val _selectedRecipe = mutableStateOf<Recipe?>(null)
    val selectedRecipe: State<Recipe?> = _selectedRecipe

    private val _selectedIngredient = mutableStateOf<Ingredient?>(null)
    val selectedIngredient: State<Ingredient?> = _selectedIngredient


    val ingredients: StateFlow<List<Ingredient>> = repository.getAllIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun navigateTo(screen: Screen, recipe: Recipe? = null, ingredient: Ingredient? = null) {
        _selectedRecipe.value = recipe
        _selectedIngredient.value = ingredient
        _currentScreen.value = screen
    }

    fun goBack() {
        _currentScreen.value = when (_currentScreen.value) {
            Screen.RECIPE_DETAIL -> Screen.RECIPES
            Screen.ADD_RECIPE -> Screen.RECIPES
            Screen.ADD_INGREDIENT -> Screen.INGREDIENTS
            else -> _currentScreen.value
        }
    }

    fun saveIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            if (ingredient.id == 0) repository.insertIngredient(ingredient)
            else repository.updateIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.deleteIngredient(ingredient)
        }
    }

    fun saveRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.insertRecipe(recipe)
        }
    }

    val allRecipeIngredients: StateFlow<List<com.ro.macrotracker.model.RecipeIngredient>> =
        repository.getAllRecipeIngredients()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    suspend fun getUsageCount(ingredientId: Int): Int {
        return repository.getUsageCountForIngredient(ingredientId)
    }
}