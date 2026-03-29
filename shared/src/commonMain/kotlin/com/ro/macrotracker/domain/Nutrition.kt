package com.ro.macrotracker.domain

import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.RecipeIngredient

data class Nutrition(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0
)

fun calculateNutrition(
    ingredients: List<Pair<Ingredient, Double>>
): Nutrition {

    return ingredients.fold(Nutrition()) { acc, (ingredient, grams) ->

        val factor = grams / 100.0

        acc.copy(
            calories = acc.calories + ingredient.caloriesPer100g * factor,
            protein = acc.protein + ingredient.proteinPer100g * factor,
            carbs = acc.carbs + ingredient.carbsPer100g * factor,
            fat = acc.fat + ingredient.fatPer100g * factor,
            fiber = acc.fiber + ingredient.fiberPer100g * factor
        )
    }
}

fun calculateRecipeNutrition(
    recipeIngredients: List<RecipeIngredient>,
    ingredients: List<Ingredient>
): Nutrition {

    val input = recipeIngredients.mapNotNull { ri ->
        val ingredient = ingredients.find { it.id == ri.ingredientId }
        ingredient?.let { it to ri.quantityInGrams }
    }

    return calculateNutrition(input)
}