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
}
