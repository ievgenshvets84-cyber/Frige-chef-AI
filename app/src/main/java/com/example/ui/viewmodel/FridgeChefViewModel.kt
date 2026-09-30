package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.DefaultRecipes
import com.example.data.local.RecipeEntity
import com.example.data.local.toDomainRecipe
import com.example.data.model.Ingredient
import com.example.data.model.Recipe
import com.example.data.repository.FridgeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RecipeSearchScope {
    ALL,          // Suche in Name & Zutaten
    INGREDIENTS,  // Nur nach Zutaten filtern
    NAME          // Nur nach Rezeptname filtern
}

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class Scanning(val progressText: String, val stepNumber: Int) : ScanUiState
    data class ReviewReady(val newlyFoundCount: Int, val message: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class FridgeChefViewModel(application: Application) : MainViewModel(application) {

    private val repository = FridgeRepository(application.applicationContext)
    private val languageManager = com.example.ui.i18n.LanguageManager(application.applicationContext)

    val currentLanguage: StateFlow<com.example.ui.i18n.AppLanguage> = languageManager.currentLanguage

    fun setLanguage(language: com.example.ui.i18n.AppLanguage) {
        languageManager.setLanguage(language)
    }

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

    // --- Cookbook (Rezeptbuch) State & Filtering for 220+ recipes in Room Database ---
    private val _cookbookSearch = MutableStateFlow("")
    val cookbookSearch: StateFlow<String> = _cookbookSearch.asStateFlow()

    private val _searchScope = MutableStateFlow(RecipeSearchScope.ALL)
    val searchScope: StateFlow<RecipeSearchScope> = _searchScope.asStateFlow()

    private val _isGeneratingCustomRecipe = MutableStateFlow(false)
    val isGeneratingCustomRecipe: StateFlow<Boolean> = _isGeneratingCustomRecipe.asStateFlow()

    private val _customRecipeGenerationError = MutableStateFlow<String?>(null)
    val customRecipeGenerationError: StateFlow<String?> = _customRecipeGenerationError.asStateFlow()

    private val _cookbookCategory = MutableStateFlow("ALLE")
    val cookbookCategory: StateFlow<String> = _cookbookCategory.asStateFlow()

    private val _roomRecipeCount = MutableStateFlow(0)
    val roomRecipeCount: StateFlow<Int> = _roomRecipeCount.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawRoomRecipesFlow: Flow<List<RecipeEntity>> = combine(
        _cookbookSearch,
        _searchScope
    ) { query, scope ->
        Pair(query.trim(), scope)
    }.flatMapLatest { (q, scope) ->
        if (q.isBlank()) {
            repository.allRecipesFromDb
        } else {
            when (scope) {
                RecipeSearchScope.INGREDIENTS -> repository.searchByIngredientInDb(q)
                RecipeSearchScope.NAME -> repository.searchByNameInDb(q)
                RecipeSearchScope.ALL -> repository.searchRecipesInDb(q)
            }
        }
    }

    val allCookbookRecipes: StateFlow<List<Recipe>> = combine(
        rawRoomRecipesFlow,
        _cookbookCategory,
        ingredients
    ) { roomEntities, category, currentIngredients ->
        // Use Room database entities, fallback to catalog if initial populate is in progress
        val domainRecipes = if (roomEntities.isNotEmpty()) {
            roomEntities.map { it.toDomainRecipe() }
        } else if (_cookbookSearch.value.isBlank()) {
            DefaultRecipes.getRecipeCatalog()
        } else {
            emptyList()
        }

        val ingredientNames = currentIngredients.map { it.name.lowercase().trim() }

        domainRecipes.map { recipe ->
            val matchCount = recipe.usedIngredients.count { req ->
                ingredientNames.any { it.contains(req.lowercase()) || req.lowercase().contains(it) }
            }
            val matchPercentage = if (recipe.usedIngredients.isNotEmpty()) {
                ((matchCount.toFloat() / recipe.usedIngredients.size) * 100).toInt()
            } else 100
            recipe.copy(matchPercentage = matchPercentage)
        }.filter { recipe ->
            when (category) {
                "ALLE" -> true
                "EXPRESS" -> recipe.prepTimeMinutes <= 15
                "PASTA" -> recipe.title.contains("Pasta", true) || recipe.title.contains("Spaghetti", true) || recipe.title.contains("Nudel", true) || recipe.tags.any { it.contains("Pasta", true) }
                "PFANNE" -> recipe.title.contains("Pfanne", true) || recipe.tags.any { it.contains("Pfanne", true) }
                "OFEN" -> recipe.title.contains("Ofen", true) || recipe.title.contains("Auflauf", true) || recipe.title.contains("Gratin", true) || recipe.tags.any { it.contains("Ofen", true) }
                "SUPPE" -> recipe.title.contains("Suppe", true) || recipe.title.contains("Eintopf", true) || recipe.tags.any { it.contains("Suppe", true) }
                "SNACKS" -> recipe.title.contains("Sandwich", true) || recipe.title.contains("Toast", true) || recipe.title.contains("Wrap", true) || recipe.tags.any { it.contains("Snack", true) }
                "FRUEHSTUECK" -> recipe.title.contains("Ei", true) || recipe.title.contains("Pancake", true) || recipe.title.contains("Porridge", true) || recipe.tags.any { it.contains("Frühstück", true) }
                else -> true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DefaultRecipes.getRecipeCatalog()
    )

    fun setCookbookSearch(query: String) {
        _cookbookSearch.value = query
    }

    fun setSearchScope(scope: RecipeSearchScope) {
        _searchScope.value = scope
    }

    fun setCookbookCategory(category: String) {
        _cookbookCategory.value = category
    }

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
            _roomRecipeCount.value = repository.getRecipeCount()
        }
    }

    fun generateRecipeFromCustomIngredients(
        selectedIngredients: List<String>,
        cookingStyle: String = "BELIEBIG",
        dietary: String = "ALLES",
        onRecipeReady: (Recipe) -> Unit = {}
    ) {
        if (selectedIngredients.isEmpty()) return

        viewModelScope.launch {
            _isGeneratingCustomRecipe.value = true
            _customRecipeGenerationError.value = null

            val result = repository.generateCustomRecipe(selectedIngredients, cookingStyle, dietary)
            _isGeneratingCustomRecipe.value = false

            if (result.isSuccess) {
                val recipe = result.getOrNull()
                if (recipe != null) {
                    _selectedRecipe.value = recipe
                    _roomRecipeCount.value = repository.getRecipeCount()
                    onRecipeReady(recipe)
                }
            } else {
                _customRecipeGenerationError.value = "Rezept konnte nicht kreiert werden. Bitte erneut versuchen."
            }
        }
    }

    fun startScanImage(bitmap: Bitmap, scanContext: String = "ANY") {
        viewModelScope.launch {
            val label = when (scanContext) {
                "TABLE" -> "Zutaten auf Tisch/Theke"
                "PANTRY" -> "Vorratskammer"
                else -> "Lebensmittel"
            }
            _scanState.value = ScanUiState.Scanning("Foto wird geladen ($label)...", 1)
            delay(400)
            _scanState.value = ScanUiState.Scanning("Vision-KI analysiert $label & Frischegrad...", 2)
            delay(500)
            _scanState.value = ScanUiState.Scanning("Zero-Waste Rezepte werden berechnet...", 3)

            val result = repository.analyzeImage(bitmap, scanContext)
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

    fun simulateTableDemoScan() {
        viewModelScope.launch {
            _scanState.value = ScanUiState.Scanning("Kamera erfasst Küchentisch & Zutaten...", 1)
            delay(400)
            _scanState.value = ScanUiState.Scanning("Vision-KI erkennt lose Lebensmittel & Packungen...", 2)
            delay(600)
            _scanState.value = ScanUiState.Scanning("Passende Rezepte aus über 200 Einträgen berechnen...", 3)
            delay(400)

            val tableIngredients = listOf(
                Ingredient(name = "Pasta (Penne)", category = "Teigwaren", emoji = "🍝", shelfLifeDays = 60, isUrgent = false),
                Ingredient(name = "Kirschtomaten", category = "Gemüse", emoji = "🍅", shelfLifeDays = 2, isUrgent = true),
                Ingredient(name = "Zucchini", category = "Gemüse", emoji = "🥒", shelfLifeDays = 2, isUrgent = true),
                Ingredient(name = "Knoblauch", category = "Gewürze & Vorrat", emoji = "🧄", shelfLifeDays = 20, isUrgent = false),
                Ingredient(name = "Frischer Basilikum", category = "Kräuter", emoji = "🌿", shelfLifeDays = 1, isUrgent = true),
                Ingredient(name = "Käse (Gouda)", category = "Milchprodukte", emoji = "🧀", shelfLifeDays = 4, isUrgent = false),
                Ingredient(name = "Eier", category = "Milch & Eier", emoji = "🥚", shelfLifeDays = 3, isUrgent = false),
                Ingredient(name = "Olivenöl", category = "Vorrat", emoji = "🫒", shelfLifeDays = 90, isUrgent = false)
            )
            repository.clearIngredients()
            for (item in tableIngredients) {
                repository.addIngredient(item.name, item.category, item.emoji, item.shelfLifeDays, item.isUrgent)
            }
            _scanState.value = ScanUiState.ReviewReady(tableIngredients.size, "🍽️ 8 Zutaten auf dem Tisch erkannt! Basilikum, Tomaten & Zucchini bereit zum Kochen.")
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
