package com.ro.macrotracker.data.repository

import com.ro.macrotracker.data.local.dao.IngredientDao
import com.ro.macrotracker.data.local.dao.RecipeDao
import com.ro.macrotracker.data.local.dao.RecipeIngredientDao
import com.ro.macrotracker.data.mappers.toDomain
import com.ro.macrotracker.data.mappers.toIngredientEntity
import com.ro.macrotracker.data.mappers.toRecipeEntity
import com.ro.macrotracker.domain.repository.Repository
import com.ro.macrotracker.model.Ingredient
import com.ro.macrotracker.model.Recipe
import com.ro.macrotracker.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.ro.macrotracker.data.mappers.toEntity

class RepositoryImplementation(
    private val ingredientDao: IngredientDao,
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao
) : Repository {

    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getAllIngredients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient.toIngredientEntity())
    }

    override suspend fun updateIngredient(ingredient: Ingredient) {
        ingredientDao.updateIngredient(ingredient.toIngredientEntity())
    }

    override suspend fun deleteIngredient(ingredient: Ingredient) {
        ingredientDao.deleteIngredient(ingredient.toIngredientEntity())
    }

    override fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        return recipeDao.insertRecipe(recipe.toRecipeEntity())
    }

    override fun getIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient>> {
        return recipeIngredientDao.getIngredientsForRecipe(recipeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getUsageCountForIngredient(ingredientId: Int): Int {
        return recipeIngredientDao.getUsageCountForIngredient(ingredientId)
    }

    override fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>> {
        return recipeIngredientDao.getAllRecipeIngredients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient) {
        recipeIngredientDao.insertRecipeIngredient(recipeIngredient.toEntity())
    }

    override suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient) {
        recipeIngredientDao.updateRecipeIngredient(recipeIngredient.toEntity())
    }

    override suspend fun deleteRecipeIngredient(id: Int) {
        recipeIngredientDao.deleteRecipeIngredient(id)
    }
}