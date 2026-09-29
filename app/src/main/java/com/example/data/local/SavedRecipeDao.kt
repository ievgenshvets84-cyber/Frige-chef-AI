package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecipeDao {
    @Query("SELECT * FROM saved_recipes ORDER BY savedTimestamp DESC")
    fun getAllSavedRecipes(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecipe(recipe: Recipe)

    @Query("DELETE FROM saved_recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_recipes WHERE id = :id)")
    suspend fun isRecipeSaved(id: String): Boolean
}
