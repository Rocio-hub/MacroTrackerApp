package com.ro.macrotracker.model

data class Ingredient(
    val id: Int = 0,
    val name: String,
    val caloriesPer100: Double,
    val proteinPer100: Double,
    val carbsPer100: Double,
    val fatPer100: Double,
    val fiberPer100: Double,
    val unit: String
)