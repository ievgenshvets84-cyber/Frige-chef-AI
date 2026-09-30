package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.DefaultRecipes
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.i18n.LocalAppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.Translations
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecipeBookScreen
import com.example.ui.screens.RecipeDetailScreen
import com.example.ui.screens.SocialStoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FridgeChefViewModel
import com.example.widget.DailyRecipeWidgetProvider
import com.example.widget.QuickScanWidgetProvider

enum class ScreenState {
    HOME,
    RECIPE_BOOK,
    FAVORITES
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startScanImmediately = intent?.getBooleanExtra(QuickScanWidgetProvider.EXTRA_START_SCAN, false) ?: false
        val initialRecipeId = intent?.getStringExtra(DailyRecipeWidgetProvider.EXTRA_RECIPE_ID)

        setContent {
            MyApplicationTheme {
                FridgeChefApp(
                    startScanImmediately = startScanImmediately,
                    initialRecipeId = initialRecipeId
                )
            }
        }
    }
}

@Composable
fun FridgeChefApp(
    startScanImmediately: Boolean = false,
    initialRecipeId: String? = null,
    viewModel: FridgeChefViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(ScreenState.HOME) }
    val selectedRecipe by viewModel.selectedRecipe.collectAsStateWithLifecycle()
    val storyRecipe by viewModel.storyRecipe.collectAsStateWithLifecycle()
    val savedRecipes by viewModel.savedRecipes.collectAsStateWithLifecycle()

    LaunchedEffect(startScanImmediately) {
        if (startScanImmediately) {
            viewModel.simulateSmartDemoScan()
        }
    }

    LaunchedEffect(initialRecipeId) {
        if (!initialRecipeId.isNullOrEmpty()) {
            val recipe = DefaultRecipes.getRecipeCatalog().find { it.id == initialRecipeId }
            if (recipe != null) {
                viewModel.selectRecipe(recipe)
            }
        }
    }

    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val strings = Translations.forLanguage(currentLanguage)

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalAppStrings provides strings
    ) {
        when {
            // Full screen 9:16 Social Story modal
            storyRecipe != null -> {
                SocialStoryScreen(
                    recipe = storyRecipe!!,
                    onBackClick = { viewModel.closeStoryCreator() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Recipe Detail View
            selectedRecipe != null -> {
                val isFav = savedRecipes.any { it.id == selectedRecipe!!.id }
                RecipeDetailScreen(
                    recipe = selectedRecipe!!,
                    isFavorite = isFav,
                    onBackClick = { viewModel.selectRecipe(null) },
                    onFavoriteToggle = { viewModel.toggleFavorite(selectedRecipe!!) },
                    onShareStoryClick = {
                        val r = selectedRecipe!!
                        viewModel.openStoryCreator(r)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            currentScreen == ScreenState.RECIPE_BOOK -> {
                RecipeBookScreen(
                    viewModel = viewModel,
                    onBackClick = { currentScreen = ScreenState.HOME },
                    modifier = Modifier.fillMaxSize()
                )
            }
            currentScreen == ScreenState.FAVORITES -> {
                FavoritesScreen(
                    viewModel = viewModel,
                    onBackClick = { currentScreen = ScreenState.HOME },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToFavorites = { currentScreen = ScreenState.FAVORITES },
                    onNavigateToCookbook = { currentScreen = ScreenState.RECIPE_BOOK },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
