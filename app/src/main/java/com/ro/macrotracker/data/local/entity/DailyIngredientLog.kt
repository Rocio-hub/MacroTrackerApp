package com.ro.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_ingredient_log")
data class DailyIngredientLog (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val ingredientId: Int,
    val amount: Double,
    val recipeId: Int,
    val mealSessionId: Long
)