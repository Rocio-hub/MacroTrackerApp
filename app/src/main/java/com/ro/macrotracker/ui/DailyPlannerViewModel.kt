package com.ro.macrotracker.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.ro.macrotracker.data.local.entity.Recipe
import com.ro.macrotracker.domain.Nutrition
import com.ro.macrotracker.domain.calculateRecipeNutrition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DailyPlannerViewModel : ViewModel() {

    var targetCalories by mutableStateOf("")

    private val _selectedRecipes = MutableStateFlow<Map<Recipe, Int>>(emptyMap())
    val selectedRecipes: StateFlow<Map<Recipe, Int>> = _selectedRecipes

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

    fun addRecipe(recipe: Recipe) {
        val current = _selectedRecipes.value[recipe] ?: 0
        _selectedRecipes.value = _selectedRecipes.value + (recipe to (current + 1))
    }

    fun removeRecipe(recipe: Recipe) {
        val current = _selectedRecipes.value[recipe] ?: 0
        val newCount = current - 1

        _selectedRecipes.value =
            if (newCount <= 0)
                _selectedRecipes.value - recipe
            else
                _selectedRecipes.value + (recipe to newCount)
    }
}