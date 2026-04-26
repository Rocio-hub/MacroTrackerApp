package com.ro.macrotracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val caloriesPer100: Double,
    val fatPer100: Double,
    val carbsPer100: Double,
    val fiberPer100: Double,
    val proteinPer100: Double,
    val unit: String,
    val imageUri: String? = null,
    val isDeleted: Boolean = false
)