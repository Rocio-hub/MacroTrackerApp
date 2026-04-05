package com.ro.macrotracker.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ro.macrotracker.Screen
import com.ro.macrotracker.model.RecipeIngredient
import com.ro.macrotracker.domain.repository.Repository
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.utils.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel (private val repository: Repository) : ViewModel() {

    private val _currentScreen = mutableStateOf(Screen.RECIPES)
    val currentScreen: State<Screen> = _currentScreen

    private val _selectedRecipe = mutableStateOf<Recipe?>(null)
    val selectedRecipe: State<Recipe?> = _selectedRecipe

    private val _selectedIngredient = mutableStateOf<Ingredient?>(null)
    val selectedIngredient: State<Ingredient?> = _selectedIngredient

    private val _ingredientSearchQuery = MutableStateFlow("")
    val ingredientSearchQuery = _ingredientSearchQuery.asStateFlow()

    private val _recipeSearchQuery = MutableStateFlow("")
    val recipeSearchQuery = _recipeSearchQuery.asStateFlow()

    val ingredients: StateFlow<List<Ingredient>> = repository.getAllIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredIngredients: StateFlow<List<Ingredient>> = repository.getAllIngredients()
        .combine(_ingredientSearchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate = _selectedDate.asStateFlow()

    val dailyLogs = _selectedDate.flatMapLatest { date ->
        repository.getLogsForDate(getStartOfDay(date), getEndOfDay(date))
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun onIngredientSearchQueryChange(newQuery: String) {
        _ingredientSearchQuery.value = newQuery
    }

    fun onRecipeSearchQueryChange(newQuery: String) {
        _recipeSearchQuery.value = newQuery
    }

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
            Screen.ADJUST_MEAL -> Screen.PLANNER
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

    fun saveFullRecipe(name: String, imageUri: String?, items: List<Pair<Ingredient, Double>>) {
        viewModelScope.launch {
            val recipe = Recipe(name = name, imageUri = imageUri)
            val recipeId = repository.insertRecipe(recipe).toInt()

            val modelsToSave = items.map { (modelIng, amount) ->
                RecipeIngredient(
                    id = 0,
                    recipeId = recipeId,
                    ingredientId = modelIng.id,
                    amount = amount
                )
            }

            repository.insertRecipeIngredients(modelsToSave)
        }
    }

    val allRecipeIngredients: StateFlow<List<RecipeIngredient>> =
        repository.getAllRecipeIngredients()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRecipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .combine(_recipeSearchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getUsageCount(ingredientId: Int): Int {
        return repository.getUsageCountForIngredient(ingredientId)
    }


    fun saveMealToPlanner(recipeId: Int, items: List<Pair<Ingredient, Double>>, date: Long) {
        viewModelScope.launch {
            val normalizedDate = getStartOfDay(date)
            val sessionId = System.currentTimeMillis()
            val logs = items.map { (ingredient, amount) ->
                com.ro.macrotracker.model.DailyIngredientLog(
                    date = normalizedDate,
                    ingredientId = ingredient.id,
                    recipeId = recipeId,
                    amount = amount,
                    mealSessionId = sessionId
                )
            }
            repository.insertDailyLogs(logs)
        }
    }

    fun deleteMealBySession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteMealFromPlanner(sessionId)
        }
    }

    fun onDateChange(newTimestamp: Long) {
        _selectedDate.value = newTimestamp
    }
}