package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Recipe

/**
 * Room database entity storing recipes with name, ingredients, instructions, category, and metadata.
 */
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val category: String,
    val description: String = "",
    val prepTimeMinutes: Int = 15,
    val difficulty: String = "Einfach",
    val calories: Int = 350,
    val servings: Int = 2,
    val emojiHero: String = "🍳",
    val tags: List<String> = emptyList(),
    val zeroWasteScore: Int = 90
)

fun Recipe.toEntity(): RecipeEntity {
    val derivedCategory = when {
        title.contains("Pasta", true) || title.contains("Spaghetti", true) || title.contains("Nudel", true) -> "Pasta & Nudeln"
        title.contains("Pfanne", true) || tags.any { it.contains("Pfanne", true) } -> "Pfannengerichte"
        title.contains("Ofen", true) || title.contains("Auflauf", true) || title.contains("Gratin", true) -> "Ofen & Auflauf"
        title.contains("Suppe", true) || title.contains("Eintopf", true) -> "Suppen & Eintöpfe"
        title.contains("Salat", true) || title.contains("Bowl", true) -> "Salate & Bowls"
        title.contains("Toast", true) || title.contains("Sandwich", true) || title.contains("Wrap", true) -> "Snacks & Wraps"
        title.contains("Ei", true) || title.contains("Pancake", true) || title.contains("Porridge", true) -> "Frühstück & Eier"
        title.contains("Reis", true) || title.contains("Risotto", true) -> "Reis & Grain"
        else -> tags.firstOrNull() ?: "Alltagsküche"
    }

    val allIngredientsList = (usedIngredients + missingIngredients).distinct()

    return RecipeEntity(
        id = id,
        name = title,
        ingredients = allIngredientsList.ifEmpty { listOf("Grundzutaten nach Wahl") },
        instructions = steps.ifEmpty { listOf("Zutaten vorbereiten", "Garen und anrichten", "Servieren und genießen") },
        category = derivedCategory,
        description = description,
        prepTimeMinutes = prepTimeMinutes,
        difficulty = difficulty,
        calories = calories,
        servings = servings,
        emojiHero = emojiHero,
        tags = tags,
        zeroWasteScore = zeroWasteScore
    )
}

fun RecipeEntity.toDomainRecipe(): Recipe {
    return Recipe(
        id = id,
        title = name,
        description = description,
        prepTimeMinutes = prepTimeMinutes,
        difficulty = difficulty,
        calories = calories,
        servings = servings,
        usedIngredients = ingredients,
        missingIngredients = emptyList(),
        steps = instructions,
        tags = tags.ifEmpty { listOf(category) },
        emojiHero = emojiHero,
        zeroWasteScore = zeroWasteScore
    )
}
