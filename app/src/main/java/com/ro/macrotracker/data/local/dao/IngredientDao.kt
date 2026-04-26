package com.ro.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ro.macrotracker.data.local.entity.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Query("UPDATE ingredients SET isDeleted = 1 WHERE id = :ingredientId")
    suspend fun softDeleteIngredient(ingredientId: Int)
}