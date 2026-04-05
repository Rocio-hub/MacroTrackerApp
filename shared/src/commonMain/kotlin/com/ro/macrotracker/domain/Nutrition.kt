package com.ro.macrotracker.domain

import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.RecipeIngredient

data class Nutrition(
    val calories: Double = 0.0,
    val fat: Double = 0.0,
    val carbs: Double = 0.0,
    val fiber: Double = 0.0,
    val protein: Double = 0.0
)

fun calculateNutrition(
    ingredients: List<Pair<Ingredient, Double>>
): Nutrition {

    return ingredients.fold(Nutrition()) { acc, (ingredient, grams) ->

        val factor = grams / 100.0

        acc.copy(
            calories = acc.calories + ingredient.caloriesPer100 * factor,
            fat = acc.fat + ingredient.fatPer100 * factor,
            carbs = acc.carbs + ingredient.carbsPer100 * factor,
            fiber = acc.fiber + ingredient.fiberPer100 * factor,
            protein = acc.protein + ingredient.proteinPer100 * factor
            )
    }
}

fun calculateRecipeNutrition(
    recipeIngredients: List<RecipeIngredient>,
    ingredients: List<Ingredient>
): Nutrition {

    val input = recipeIngredients.mapNotNull { ri ->
        val ingredient = ingredients.find { it.id == ri.ingredientId }
        ingredient?.let { it to ri.amount }
    }

    return calculateNutrition(input)
}