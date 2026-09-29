package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_recipes")
data class Recipe(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val prepTimeMinutes: Int, // z.B. 12 Min
    val difficulty: String = "Einfach", // "Einfach", "Mittel", "Chef"
    val calories: Int = 380,
    val servings: Int = 2,
    val usedIngredients: List<String> = emptyList(), // Zutaten, die im Kühlschrank vorhanden sind
    val missingIngredients: List<String> = emptyList(), // Fehlende Vorratszutaten (z.B. Salz, Olivenöl)
    val urgentIngredientsSaved: List<String> = emptyList(), // Vor dem Verfall gerettete Zutaten
    val zeroWasteScore: Int = 95, // 0 - 100%
    val matchPercentage: Int = 100, // 0 - 100%
    val steps: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val emojiHero: String = "🍳",
    val isFavorite: Boolean = false,
    val savedTimestamp: Long = System.currentTimeMillis()
) {
    val timeCategory: String
        get() = when {
            prepTimeMinutes <= 15 -> "FAST"
            prepTimeMinutes <= 30 -> "NORMAL"
            else -> "DETAILED"
        }

    val isZeroWasteHero: Boolean
        get() = urgentIngredientsSaved.isNotEmpty() || zeroWasteScore >= 90
}
