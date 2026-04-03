package com.ro.macrotracker.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ro.macrotracker.domain.Nutrition
import com.ro.macrotracker.domain.calculateRecipeNutrition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.RecipeIngredient
import kotlinx.coroutines.flow.asStateFlow

class DailyPlannerViewModel : ViewModel() {

    private val _selectedRecipes = MutableStateFlow<Map<com.ro.macrotracker.model.Recipe, Int>>(emptyMap())
    val selectedRecipes: StateFlow<Map<com.ro.macrotracker.model.Recipe, Int>> = _selectedRecipes

    private val _plannerSearchQuery = MutableStateFlow("")
    val plannerSearchQuery = _plannerSearchQuery.asStateFlow()

    fun calculateTotalNutrition(
        recipeIngredientsMap: Map<Int, List<com.ro.macrotracker.model.RecipeIngredient>>,
        ingredients: List<com.ro.macrotracker.model.Ingredient>
    ): Nutrition {

        return _selectedRecipes.value.entries.fold(Nutrition()) { acc, (recipe, count) ->

            val ris = recipeIngredientsMap[recipe.id] ?: emptyList()

            val nutrition = calculateRecipeNutrition(ris, ingredients)

            acc.copy(
                calories = acc.calories + nutrition.calories * count,
                protein = acc.protein + nutrition.protein * count,
                carbs = acc.carbs + nutrition.carbs * count,
                fat = acc.fat + nutrition.fat * count,
                fiber = acc.fiber + nutrition.fiber * count
            )
        }
    }

    fun addRecipe(recipe: com.ro.macrotracker.model.Recipe) { // ✅ Aquí debe ser el MODEL
        val currentCount = _selectedRecipes.value[recipe] ?: 0
        _selectedRecipes.value = _selectedRecipes.value + (recipe to (currentCount + 1))
    }

    fun removeRecipe(recipe: com.ro.macrotracker.model.Recipe) {
        val current = _selectedRecipes.value[recipe] ?: 0
        val newCount = current - 1

        _selectedRecipes.value =
            if (newCount <= 0)
                _selectedRecipes.value - recipe
            else
                _selectedRecipes.value + (recipe to newCount)
    }

    fun onSearchQueryChange(newQuery: String) {
        _plannerSearchQuery.value = newQuery
    }

    fun getFilteredRecipes(allRecipes: List<Recipe>): List<Recipe> {
        val query = _plannerSearchQuery.value
        return if (query.isBlank()) {
            allRecipes
        } else {
            allRecipes.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
}