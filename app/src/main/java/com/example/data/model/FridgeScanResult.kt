package com.example.data.model

data class DetectedItem(
    val name: String,
    val emoji: String,
    val category: String,
    val shelfLifeDays: Int,
    val isUrgent: Boolean,
    val confidence: Float = 0.95f
)

data class FridgeScanResponse(
    val detectedItems: List<DetectedItem>,
    val zeroWasteAdvice: String,
    val generatedRecipes: List<Recipe>
)
