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
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

class MainViewModel (private val repository: Repository, private val prefs: SharedPreferences) : ViewModel() {

    private val _currentScreen = mutableStateOf(Screen.PLANNER)
    val currentScreen: State<Screen> = _currentScreen

    private val _selectedRecipe = mutableStateOf<Recipe?>(null)
    val selectedRecipe: State<Recipe?> = _selectedRecipe

    private val _selectedIngredient = mutableStateOf<Ingredient?>(null)
    val selectedIngredient: State<Ingredient?> = _selectedIngredient

    private val _ingredientSearchQuery = MutableStateFlow("")
    val ingredientSearchQuery = _ingredientSearchQuery.asStateFlow()

    private val _recipeSearchQuery = MutableStateFlow("")
    val recipeSearchQuery = _recipeSearchQuery.asStateFlow()

    private val _globalTarget = MutableStateFlow("")
    val globalTarget: StateFlow<String> = _globalTarget.asStateFlow()

    private val _proteinTarget = MutableStateFlow("")
    val proteinTarget: StateFlow<String> = _proteinTarget.asStateFlow()

    private var returnToScreen: Screen? = null

    val ingredients: StateFlow<List<Ingredient>> = repository.getAllIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .map { list -> list.filter { !it.isDeleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredIngredients: StateFlow<List<Ingredient>> = repository.getAllIngredients()
        .combine(_ingredientSearchQuery) { list, query ->
            if (query.isBlank()) list
            else list.filter { it.name.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRecipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .combine(_recipeSearchQuery) { list, query ->
            val activeRecipes = list.filter { !it.isDeleted }
            if (query.isBlank()) activeRecipes
            else activeRecipes.filter { it.name.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate = _selectedDate.asStateFlow()

    val dailyLogs = _selectedDate.flatMapLatest { date ->
        repository.getLogsForDate(getStartOfDay(date), getEndOfDay(date))
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeRecipes: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .map { list -> list.filter { !it.isDeleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipesHistory: StateFlow<List<Recipe>> = repository.getAllRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeIngredients: StateFlow<List<Ingredient>> = repository.getAllIngredients()
        .map { list -> list.filter { !it.isDeleted } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIngredientsHistory: StateFlow<List<Ingredient>> = repository.getAllIngredients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var temporaryRecipeName by mutableStateOf("")
    val temporarySelectedIngredients = mutableStateListOf<Pair<Ingredient, Double>>()
    var temporaryRecipeImage by mutableStateOf<String?>(null)

    init {
        loadUserPreferences()
    }


    private fun loadUserPreferences() {
        _globalTarget.value = prefs.getString(Constants.KEY_GLOBAL_TARGET, "") ?: ""
        _proteinTarget.value = prefs.getString(Constants.KEY_PROTEIN_TARGET, "") ?: ""
    }

    fun clearTemporaryRecipe() {
        temporaryRecipeName = ""
        temporarySelectedIngredients.clear()
        temporaryRecipeImage = null
    }

    fun updateProteinTarget(newTarget: String) {
        _proteinTarget.value = newTarget
        prefs.edit().putString(Constants.KEY_PROTEIN_TARGET, newTarget).apply()
    }

    fun updateGlobalTarget(newTarget: String) {
        _globalTarget.value = newTarget
        prefs.edit().putString(Constants.KEY_GLOBAL_TARGET, newTarget).apply()
    }

    fun softDeleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            repository.softDeleteRecipe(recipe.id)
        }
    }
    fun softDeleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.softDeleteIngredient(ingredient.id)
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

    fun onIngredientSearchQueryChange(newQuery: String) {
        _ingredientSearchQuery.value = newQuery
    }

    fun onRecipeSearchQueryChange(newQuery: String) {
        _recipeSearchQuery.value = newQuery
    }

    fun navigateTo(screen: Screen, recipe: Recipe? = null, ingredient: Ingredient? = null, returnTo: Screen? = null) {
        _selectedRecipe.value = recipe
        _selectedIngredient.value = ingredient
        _currentScreen.value = screen

        if (returnTo != null) {
            returnToScreen = returnTo
        }
    }

    fun goBack() {
        if (returnToScreen != null) {
            val destination = returnToScreen!!
            _currentScreen.value = destination
        } else {
            _currentScreen.value = when (_currentScreen.value) {
                Screen.RECIPE_DETAIL -> Screen.RECIPES
                Screen.ADD_RECIPE -> Screen.RECIPES
                Screen.ADD_INGREDIENT -> Screen.INGREDIENTS
                Screen.ADJUST_MEAL -> Screen.PLANNER
                else -> Screen.PLANNER
            }
        }
    }

    fun saveIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            val newIdLong = repository.insertIngredient(ingredient)
            val ingredientWithId = ingredient.copy(id = newIdLong.toInt())

            if (returnToScreen == Screen.ADD_RECIPE) {
                temporarySelectedIngredients.add(ingredientWithId to 100.0)
                println("DEBUG_VM: ¡Añadido con éxito!")

                returnToScreen = null
            }
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.softDeleteIngredient(ingredient.id)
        }
    }

    val allRecipeIngredients: StateFlow<List<RecipeIngredient>> =
        repository.getAllRecipeIngredients()
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