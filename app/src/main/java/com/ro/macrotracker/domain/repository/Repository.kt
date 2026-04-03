package com.ro.macrotracker.domain.repository

import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow

interface Repository {

    fun getAllIngredients(): Flow<List<Ingredient>>
    suspend fun insertIngredient(ingredient: Ingredient)
    suspend fun updateIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient)

    fun getAllRecipes(): Flow<List<Recipe>>
    suspend fun insertRecipe(recipe: Recipe): Long

    fun getIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient>>
    suspend fun getUsageCountForIngredient(ingredientId: Int): Int

    fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>>
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient)
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient)
    suspend fun deleteRecipeIngredient(id: Int)
}