package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.DefaultRecipes
import com.example.data.local.FridgeDatabase
import com.example.data.local.RecipeEntity
import com.example.data.local.toEntity
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
    private val recipeDao = db.recipeDao()
    private val visionService = GeminiVisionService()
    private val recipeGeneratorService = com.example.data.remote.GeminiRecipeGeneratorService()

    val allIngredients: Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    val savedRecipes: Flow<List<Recipe>> = savedRecipeDao.getAllSavedRecipes()
    val allRecipesFromDb: Flow<List<RecipeEntity>> = recipeDao.getAllRecipes()

    suspend fun initializeIfEmpty() {
        val current = allIngredients.first()
        if (current.isEmpty()) {
            ingredientDao.insertAll(DefaultRecipes.initialStarterIngredients)
        }

        // Initialize 200+ recipes in Room Database if empty
        val recipeCount = recipeDao.getRecipeCount()
        if (recipeCount < 200) {
            val entities = DefaultRecipes.getRecipeCatalog().map { it.toEntity() }
            recipeDao.insertAllRecipes(entities)
        }
    }

    fun getRecipesByCategory(category: String): Flow<List<RecipeEntity>> {
        return recipeDao.getRecipesByCategory(category)
    }

    fun searchRecipesInDb(query: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchRecipes(query)
    }

    fun searchByIngredientInDb(ingredient: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchByIngredient(ingredient)
    }

    fun searchByNameInDb(name: String): Flow<List<RecipeEntity>> {
        return recipeDao.searchByName(name)
    }

    suspend fun getRecipeCount(): Int {
        return recipeDao.getRecipeCount()
    }

    suspend fun generateCustomRecipe(
        selectedIngredients: List<String>,
        cookingStyle: String = "BELIEBIG",
        dietary: String = "ALLES"
    ): Result<Recipe> {
        val result = recipeGeneratorService.generateCustomRecipe(selectedIngredients, cookingStyle, dietary)
        if (result.isSuccess) {
            val recipe = result.getOrNull()
            if (recipe != null) {
                // Automatically persist the chef-created recipe into local Room database
                recipeDao.insertRecipe(recipe.toEntity())
            }
        }
        return result
    }

    suspend fun analyzeImage(bitmap: Bitmap, scanContext: String = "ANY"): Result<FridgeScanResponse> {
        val result = visionService.analyzeFridgeImage(bitmap, scanContext)
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
