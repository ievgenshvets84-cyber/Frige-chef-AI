package com.example.data.local

import com.example.data.model.Ingredient
import com.example.data.model.Recipe

object DefaultRecipes {

    val initialStarterIngredients = listOf(
        Ingredient(
            name = "Eier",
            category = "Milch & Eier",
            emoji = "🥚",
            quantity = "4 Stück",
            shelfLifeDays = 2,
            isUrgent = false
        ),
        Ingredient(
            name = "Käse (Gouda)",
            category = "Milchprodukte",
            emoji = "🧀",
            quantity = "150g",
            shelfLifeDays = 3,
            isUrgent = false
        ),
        Ingredient(
            name = "Tomaten",
            category = "Gemüse",
            emoji = "🍅",
            quantity = "3 Stück",
            shelfLifeDays = 1,
            isUrgent = true // Today!
        ),
        Ingredient(
            name = "Milch",
            category = "Milchprodukte",
            emoji = "🥛",
            quantity = "500ml",
            shelfLifeDays = 1,
            isUrgent = true // Open milk, today!
        ),
        Ingredient(
            name = "Paprika",
            category = "Gemüse",
            emoji = "🫑",
            quantity = "1 Stück",
            shelfLifeDays = 4,
            isUrgent = false
        ),
        Ingredient(
            name = "Zwiebeln",
            category = "Gemüse",
            emoji = "🧅",
            quantity = "2 Stück",
            shelfLifeDays = 10,
            isUrgent = false
        )
    )

    // Complete catalog with over 220 simple, diverse recipes
    private val fullCatalog: List<Recipe> by lazy {
        RecipeCatalog.getRecipesBatch1() +
            RecipeCatalog.getRecipesBatch2() +
            RecipeCatalog.getRecipesBatch3() +
            RecipeCatalogPart2.getRecipesBatch4() +
            RecipeCatalogPart2.getRecipesBatch5()
    }

    fun getRecipeCatalog(): List<Recipe> = fullCatalog

    fun matchRecipes(availableIngredients: List<Ingredient>): List<Recipe> {
        val ingredientNames = availableIngredients.map { it.name.lowercase().trim() }
        val urgentNames = availableIngredients
            .filter { it.isUrgent || it.shelfLifeDays <= 1 }
            .map { it.name.lowercase().trim() }

        return getRecipeCatalog().map { recipe ->
            val usedInRecipe = recipe.usedIngredients.filter { req ->
                ingredientNames.any { it.contains(req.lowercase()) || req.lowercase().contains(it) }
            }
            val missingInRecipe = recipe.usedIngredients.filterNot { req ->
                ingredientNames.any { it.contains(req.lowercase()) || req.lowercase().contains(it) }
            }
            val urgentSaved = recipe.usedIngredients.filter { req ->
                urgentNames.any { it.contains(req.lowercase()) || req.lowercase().contains(it) }
            }

            val totalReq = recipe.usedIngredients.size
            val matchPct = if (totalReq > 0) ((usedInRecipe.size.toFloat() / totalReq) * 100).toInt() else 100
            val zeroWasteBonus = if (urgentSaved.isNotEmpty()) 20 else 0
            val calculatedScore = (matchPct * 0.8f + zeroWasteBonus).coerceIn(40f, 100f).toInt()

            recipe.copy(
                usedIngredients = if (usedInRecipe.isNotEmpty()) usedInRecipe else recipe.usedIngredients,
                missingIngredients = missingInRecipe,
                urgentIngredientsSaved = urgentSaved,
                matchPercentage = matchPct.coerceAtMost(100),
                zeroWasteScore = calculatedScore
            )
        }.sortedWith(
            // Sort by: Zero Waste urgency first, then match %, then prep time
            compareByDescending<Recipe> { it.urgentIngredientsSaved.size }
                .thenByDescending { it.matchPercentage }
                .thenBy { it.prepTimeMinutes }
        )
    }
}
