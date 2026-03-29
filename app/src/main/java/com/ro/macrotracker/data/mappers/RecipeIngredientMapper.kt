package com.ro.macrotracker.data.mappers

import com.ro.macrotracker.data.local.entity.RecipeIngredient as RecipeIngredientEntity
import com.ro.macrotracker.model.RecipeIngredient

fun RecipeIngredientEntity.toDomain(): RecipeIngredient {
    return RecipeIngredient(
        id = id,
        recipeId = recipeId,
        ingredientId = ingredientId,
        quantityInGrams = quantityInGrams
    )
}

fun RecipeIngredient.toEntity(): RecipeIngredientEntity {
    return RecipeIngredientEntity(
        id = id,
        recipeId = recipeId,
        ingredientId = ingredientId,
        quantityInGrams = quantityInGrams
    )
}