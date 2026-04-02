package com.ro.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ro.macrotracker.data.local.entity.RecipeIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {

    @Insert
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient)

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    fun getIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient>>

    @Query("SELECT * FROM recipe_ingredients")
    fun getAllRecipeIngredients(): Flow<List<RecipeIngredient>>

    @Query("DELETE FROM recipe_ingredients WHERE id = :id")
    suspend fun deleteRecipeIngredient(id: Int)

    @Query("SELECT COUNT(*) FROM recipe_ingredients WHERE ingredientId = :ingredientId")
    suspend fun getUsageCountForIngredient(ingredientId: Int): Int

    @Update
    suspend fun updateRecipeIngredient(recipeIngredient: RecipeIngredient)
}