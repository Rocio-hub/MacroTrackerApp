package com.ro.macrotracker.data.mappers

import com.ro.macrotracker.data.local.entity.Ingredient as IngredientEntity
import com.ro.macrotracker.model.Ingredient

fun IngredientEntity.toDomain(): Ingredient {
    return Ingredient(
        id = id,
        name = name,
        caloriesPer100 = caloriesPer100,
        fatPer100 = fatPer100,
        carbsPer100 = carbsPer100,
        fiberPer100 = fiberPer100,
        proteinPer100 = proteinPer100,
        unit = unit,
        imageUri = imageUri
    )
}

fun Ingredient.toIngredientEntity(): IngredientEntity {
    return IngredientEntity(
        id = id,
        name = name,
        caloriesPer100 = caloriesPer100,
        fatPer100 = fatPer100,
        carbsPer100 = carbsPer100,
        fiberPer100 = fiberPer100,
        proteinPer100 = proteinPer100,
        unit = unit,
        imageUri = imageUri
    )
}