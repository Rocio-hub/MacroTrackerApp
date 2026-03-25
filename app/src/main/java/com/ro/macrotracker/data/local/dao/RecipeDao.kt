package com.ro.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ro.macrotracker.data.local.entity.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Insert
    suspend fun insertRecipe(recipe: Recipe): Long

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<Recipe>>

}