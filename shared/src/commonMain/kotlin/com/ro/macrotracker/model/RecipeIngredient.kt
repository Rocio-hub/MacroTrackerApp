package com.ro.macrotracker.model

data class RecipeIngredient(
    val id: Int = 0,
    val recipeId: Int,
    val ingredientId: Int,
    val amount: Double
)