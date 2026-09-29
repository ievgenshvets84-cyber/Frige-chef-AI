package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.DefaultRecipes
import com.example.data.local.FridgeDatabase
import com.example.data.model.FridgeScanResponse
import com.example.data.model.Ingredient
import com.example.data.model.Recipe
import com.example.data.remote.GeminiVisionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FridgeRepository(context: Context) {

    private val db = FridgeDatabase.getDatabase(context)
    private val ingredientDao = db.ingredientDao()
    private val savedRecipeDao = db.savedRecipeDao()
    private val visionService = GeminiVisionService()

    val allIngredients: Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    val savedRecipes: Flow<List<Recipe>> = savedRecipeDao.getAllSavedRecipes()

    suspend fun initializeIfEmpty() {
        val current = allIngredients.first()
        if (current.isEmpty()) {
            ingredientDao.insertAll(DefaultRecipes.initialStarterIngredients)
        }
    }

    suspend fun analyzeImage(bitmap: Bitmap): Result<FridgeScanResponse> {
        val result = visionService.analyzeFridgeImage(bitmap)
        if (result.isSuccess) {
            val response = result.getOrNull()
            if (response != null && response.detectedItems.isNotEmpty()) {
                val newIngredients = response.detectedItems.map { item ->
                    Ingredient(
                        name = item.name,
                        category = item.category,
                        emoji = item.emoji,
                        quantity = "1x",
                        shelfLifeDays = item.shelfLifeDays,
                        isUrgent = item.isUrgent
                    )
                }
                ingredientDao.clearAllIngredients()
                ingredientDao.insertAll(newIngredients)
            }
        }
        return result
    }

    suspend fun addIngredient(name: String, category: String = "Allgemein", emoji: String = "🥗", shelfLifeDays: Int = 3, isUrgent: Boolean = false) {
        val ingredient = Ingredient(
            name = name,
            category = category,
            emoji = emoji,
            shelfLifeDays = shelfLifeDays,
            isUrgent = isUrgent
        )
        ingredientDao.insertIngredient(ingredient)
    }

    suspend fun removeIngredient(id: Long) {
        ingredientDao.deleteIngredientById(id)
    }

    suspend fun clearIngredients() {
        ingredientDao.clearAllIngredients()
    }

    suspend fun toggleFavorite(recipe: Recipe) {
        val exists = savedRecipeDao.isRecipeSaved(recipe.id)
        if (exists) {
            savedRecipeDao.deleteRecipeById(recipe.id)
        } else {
            savedRecipeDao.saveRecipe(recipe.copy(isFavorite = true, savedTimestamp = System.currentTimeMillis()))
        }
    }

    suspend fun isFavorite(recipeId: String): Boolean {
        return savedRecipeDao.isRecipeSaved(recipeId)
    }
}
