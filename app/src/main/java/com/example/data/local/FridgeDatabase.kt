package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Ingredient
import com.example.data.model.Recipe

@Database(
    entities = [Ingredient::class, Recipe::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FridgeDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun savedRecipeDao(): SavedRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: FridgeDatabase? = null

        fun getDatabase(context: Context): FridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FridgeDatabase::class.java,
                    "fridge_chef_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
