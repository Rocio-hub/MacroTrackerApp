package com.ro.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ro.macrotracker.data.local.entity.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Query("SELECT * FROM recipes WHERE isDeleted = 0")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("UPDATE recipes SET isDeleted = 1 WHERE id = :recipeId")
    suspend fun softDeleteRecipe(recipeId: Int)

}