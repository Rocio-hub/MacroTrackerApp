package com.ro.macrotracker.data.mappers

import com.ro.macrotracker.data.local.entity.Recipe as RecipeEntity
import com.ro.macrotracker.model.Recipe

fun RecipeEntity.toDomain(): Recipe {
    return Recipe(
        id = id,
        name = name,
        imageUri = imageUri
    )
}

fun Recipe.toRecipeEntity(): RecipeEntity {
    return RecipeEntity(
        id = id,
        name = name,
        imageUri = imageUri
    )
}