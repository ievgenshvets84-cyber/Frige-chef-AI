package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "Allgemein", // z.B. "Milchprodukte", "Gemüse", "Obst", "Fleisch", "Vorrat"
    val emoji: String = "🥗",
    val quantity: String = "1x",
    val shelfLifeDays: Int = 3, // Tage bis Verfall
    val isUrgent: Boolean = false, // True wenn bald ablaufend
    val addedTimestamp: Long = System.currentTimeMillis()
) {
    val freshnessLabel: String
        get() = when {
            shelfLifeDays <= 1 || isUrgent -> "Heute verbrauchen! 🔴"
            shelfLifeDays <= 3 -> "Bald verbrauchen 🟡"
            else -> "Noch frisch 🟢"
        }
}
