package com.ro.macrotracker.model

import com.ro.macrotracker.domain.Nutrition

data class RecipeIngredient(
    val id: Int = 0,
    val recipeId: Int,
    val ingredientId: Int,
    val amount: Double
)