package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for recipes in the Room database.
 */
@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY name ASC")
    fun getRecipesByCategory(category: String): Flow<List<RecipeEntity>>

    /**
     * Searches the local Room database for recipes by name, ingredient, category or description.
     */
    @Query("""
        SELECT * FROM recipes 
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR ingredients LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    /**
     * Search specifically for recipes containing a particular ingredient in Room.
     */
    @Query("SELECT * FROM recipes WHERE ingredients LIKE '%' || :ingredient || '%' ORDER BY name ASC")
    fun searchByIngredient(ingredient: String): Flow<List<RecipeEntity>>

    /**
     * Search specifically by recipe name in Room.
     */
    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :name || '%' ORDER BY name ASC")
    fun searchByName(name: String): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeById(id: String): RecipeEntity?

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRecipes(recipes: List<RecipeEntity>)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: String)

    @Query("DELETE FROM recipes")
    suspend fun clearAllRecipes()
}
