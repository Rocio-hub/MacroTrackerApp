package com.ro.macrotracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ro.macrotracker.data.local.dao.IngredientDao
import com.ro.macrotracker.data.local.dao.RecipeDao
import com.ro.macrotracker.data.local.dao.RecipeIngredientDao
import com.ro.macrotracker.data.local.entity.Ingredient
import com.ro.macrotracker.data.local.entity.Recipe
import com.ro.macrotracker.data.local.entity.RecipeIngredient

@Database(
    entities = [
        Ingredient::class,
        Recipe::class,
        RecipeIngredient::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    com.ro.macrotracker.data.local.database.AppDatabase::class.java,
                    "macro-db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}