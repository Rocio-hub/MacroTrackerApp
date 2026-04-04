package com.ro.macrotracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ro.macrotracker.data.local.entity.DailyIngredientLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyIngredientLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DailyIngredientLog>)

    @Query("SELECT * FROM daily_ingredient_log WHERE date = :date")
    fun getLogsByDate(date: Long): Flow<List<DailyIngredientLog>>

    @Delete
    suspend fun deleteLog(log: DailyIngredientLog)

    @Query("DELETE FROM daily_ingredient_log WHERE recipeId = :recipeId AND date = :date")
    suspend fun deleteLogsForRecipe(recipeId: Int, date: Long)
}