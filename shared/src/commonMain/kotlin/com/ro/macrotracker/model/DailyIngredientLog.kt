package com.ro.macrotracker.model

data class DailyIngredientLog(
    val id: Int = 0,
    val date: Long,
    val ingredientId: Int,
    val recipeId: Int,
    val amount: Double,
    val mealSessionId: Long
)