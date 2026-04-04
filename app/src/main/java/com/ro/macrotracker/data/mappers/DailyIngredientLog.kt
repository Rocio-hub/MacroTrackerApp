package com.ro.macrotracker.data.mappers

fun com.ro.macrotracker.model.DailyIngredientLog.toEntity(): com.ro.macrotracker.data.local.entity.DailyIngredientLog {
    return com.ro.macrotracker.data.local.entity.DailyIngredientLog(
        id = this.id,
        date = this.date,
        ingredientId = this.ingredientId,
        recipeId = this.recipeId,
        amount = this.amount
    )
}

fun com.ro.macrotracker.data.local.entity.DailyIngredientLog.toDomain(): com.ro.macrotracker.model.DailyIngredientLog {
    return com.ro.macrotracker.model.DailyIngredientLog(
        id = this.id,
        date = this.date,
        ingredientId = this.ingredientId,
        recipeId = this.recipeId,
        amount = this.amount
    )
}