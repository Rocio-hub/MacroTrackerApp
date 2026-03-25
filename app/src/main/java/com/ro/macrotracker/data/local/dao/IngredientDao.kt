package com.ro.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ro.macrotracker.data.local.entity.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {

    @Insert
    suspend fun insertIngredient(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): Flow<List<Ingredient>>
}