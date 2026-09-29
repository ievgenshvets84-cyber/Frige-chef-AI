package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DefaultRecipes
import com.example.data.model.Ingredient
import com.example.data.model.Recipe
import com.example.data.repository.FridgeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Scanning(val progressText: String, val stepNumber: Int) : ScanUiState
    data class ReviewReady(val newlyFoundCount: Int, val message: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class FridgeChefViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FridgeRepository(application.applicationContext)

    val ingredients: StateFlow<List<Ingredient>> = repository.allIngredients
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedRecipes: StateFlow<List<Recipe>> = repository.savedRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _timeFilter = MutableStateFlow("ALL") // "ALL", "FAST", "NORMAL", "DETAILED"
    val timeFilter: StateFlow<String> = _timeFilter.asStateFlow()

    private val _onlyFullMatch = MutableStateFlow(false)
    val onlyFullMatch: StateFlow<Boolean> = _onlyFullMatch.asStateFlow()

    private val _zeroWasteOnly = MutableStateFlow(false)
    val zeroWasteOnly: StateFlow<Boolean> = _zeroWasteOnly.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    private val _storyRecipe = MutableStateFlow<Recipe?>(null)
    val storyRecipe: StateFlow<Recipe?> = _storyRecipe.asStateFlow()

    val matchedRecipes: StateFlow<List<Recipe>> = combine(
        ingredients,
        _timeFilter,
        _onlyFullMatch,
        _zeroWasteOnly
    ) { currentIngredients, filterTime, fullMatch, zeroWasteOnly ->
        val matched = DefaultRecipes.matchRecipes(currentIngredients)
        matched.filter { recipe ->
            val matchesTime = when (filterTime) {
                "FAST" -> recipe.prepTimeMinutes <= 15
                "NORMAL" -> recipe.prepTimeMinutes in 16..30
                "DETAILED" -> recipe.prepTimeMinutes > 30
                else -> true
            }
            val matchesFull = if (fullMatch) recipe.matchPercentage >= 95 else true
            val matchesZeroWaste = if (zeroWasteOnly) recipe.urgentIngredientsSaved.isNotEmpty() else true
            matchesTime && matchesFull && matchesZeroWaste
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeIfEmpty()
        }
    }

    fun startScanImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Scanning("Kühlschrank-Foto wird geladen...", 1)
            delay(400)
            _scanState.value = ScanUiState.Scanning("KI analysiert Lebensmittel & Frischegrad...", 2)
            delay(500)
            _scanState.value = ScanUiState.Scanning("Zero-Waste Rezepte werden berechnet...", 3)

            val result = repository.analyzeImage(bitmap)
            if (result.isSuccess) {
                val data = result.getOrNull()
                val count = data?.detectedItems?.size ?: 0
                val advice = data?.zeroWasteAdvice ?: "Zutaten erfolgreich erkannt!"
                _scanState.value = ScanUiState.ReviewReady(count, advice)
            } else {
                _scanState.value = ScanUiState.Error("Konnte Bild nicht analysieren. Bitte erneut versuchen.")
            }
        }
    }

    fun simulateSmartDemoScan() {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Scanning("Kamera fokussiert auf Kühlschrank...", 1)
            delay(400)
            _scanState.value = ScanUiState.Scanning("Vision-KI erkennt Barcodes & Frische-Zustand...", 2)
            delay(600)
            _scanState.value = ScanUiState.Scanning("Zero-Waste Matching aktiviert...", 3)
            delay(400)

            val demoIngredients = listOf(
                Ingredient(name = "Eier", category = "Milch & Eier", emoji = "🥚", shelfLifeDays = 2, isUrgent = false),
                Ingredient(name = "Käse (Gouda)", category = "Milchprodukte", emoji = "🧀", shelfLifeDays = 3, isUrgent = false),
                Ingredient(name = "Tomaten", category = "Gemüse", emoji = "🍅", shelfLifeDays = 1, isUrgent = true),
                Ingredient(name = "Milch", category = "Milchprodukte", emoji = "🥛", shelfLifeDays = 1, isUrgent = true),
                Ingredient(name = "Paprika", category = "Gemüse", emoji = "🫑", shelfLifeDays = 4, isUrgent = false),
                Ingredient(name = "Frühlingszwiebeln", category = "Gemüse", emoji = "🧅", shelfLifeDays = 1, isUrgent = true),
                Ingredient(name = "Butter", category = "Milchprodukte", emoji = "🧈", shelfLifeDays = 10, isUrgent = false)
            )
            repository.clearIngredients()
            for (item in demoIngredients) {
                repository.addIngredient(item.name, item.category, item.emoji, item.shelfLifeDays, item.isUrgent)
            }
            _scanState.value = ScanUiState.ReviewReady(demoIngredients.size, "7 frische Zutaten erkannt! Tomaten & Milch bald verbrauchen 🌱")
        }
    }

    fun resetScanState() {
        _scanState.value = ScanUiState.Idle
    }

    fun deleteIngredient(id: Long) {
        viewModelScope.launch {
            repository.removeIngredient(id)
        }
    }

    fun addIngredient(name: String, category: String = "Allgemein", emoji: String = "🥗", shelfLifeDays: Int = 3, isUrgent: Boolean = false) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addIngredient(name.trim(), category, emoji, shelfLifeDays, isUrgent)
        }
    }

    fun setTimeFilter(filter: String) {
        _timeFilter.value = filter
    }

    fun toggleOnlyFullMatch() {
        _onlyFullMatch.value = !_onlyFullMatch.value
    }

    fun toggleZeroWasteOnly() {
        _zeroWasteOnly.value = !_zeroWasteOnly.value
    }

    fun selectRecipe(recipe: Recipe?) {
        _selectedRecipe.value = recipe
    }

    fun openStoryCreator(recipe: Recipe) {
        _storyRecipe.value = recipe
    }

    fun closeStoryCreator() {
        _storyRecipe.value = null
    }

    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            repository.toggleFavorite(recipe)
        }
    }
}
