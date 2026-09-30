package com.example

import com.example.data.local.DefaultRecipes
import com.example.data.model.Ingredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRecipeMatchingAndZeroWastePriority() {
        val available = listOf(
            Ingredient(name = "Tomaten", shelfLifeDays = 1, isUrgent = true),
            Ingredient(name = "Milch", shelfLifeDays = 1, isUrgent = true),
            Ingredient(name = "Käse (Gouda)", shelfLifeDays = 4, isUrgent = false),
            Ingredient(name = "Eier", shelfLifeDays = 2, isUrgent = false)
        )

        val matched = DefaultRecipes.matchRecipes(available)
        assertTrue(matched.isNotEmpty())
        // Top recipe should prioritize the urgent ingredients
        val first = matched.first()
        assertTrue(first.urgentIngredientsSaved.isNotEmpty())
    }

    @Test
    fun testRecipeCatalogExceeds200Recipes() {
        val allRecipes = DefaultRecipes.getRecipeCatalog()
        assertTrue("Catalog must contain more than 200 recipes", allRecipes.size >= 200)
    }

    @Test
    fun testCustomRecipeGeneratorCreatesDetailedSteps() = kotlinx.coroutines.runBlocking {
        val generator = com.example.data.remote.GeminiRecipeGeneratorService()
        val result = generator.generateCustomRecipe(
            selectedIngredients = listOf("Zucchini", "Gouda", "Eier", "Tomaten"),
            cookingStyle = "PFANNE"
        )
        assertTrue(result.isSuccess)
        val recipe = result.getOrNull()
        org.junit.Assert.assertNotNull(recipe)
        // Check that it's a full culinary process with at least 4 steps, not just warming up
        assertTrue("Recipe should have at least 4 detailed cooking steps", (recipe?.steps?.size ?: 0) >= 4)
        assertTrue("Title should be non-empty", !recipe?.title.isNullOrBlank())
        assertTrue("Ingredients used should contain selected items", recipe?.usedIngredients?.isNotEmpty() == true)
    }
}
