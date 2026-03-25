package com.ro.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ro.macrotracker.data.local.entity.RecipeIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {

    @Insert
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient)

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    fun getIngredientsForRecipe(recipeId: Int): Flow<List<RecipeIngredient>>

}