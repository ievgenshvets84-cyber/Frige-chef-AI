package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.LanguagePickerDropdown
import com.example.ui.components.RecipeCard
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.FreshGreenPrimary
import com.example.ui.viewmodel.FridgeChefViewModel
import com.example.ui.viewmodel.RecipeSearchScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBookScreen(
    viewModel: FridgeChefViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val strings = LocalAppStrings.current
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val recipes by viewModel.allCookbookRecipes.collectAsStateWithLifecycle()
    val savedRecipes by viewModel.savedRecipes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.cookbookSearch.collectAsStateWithLifecycle()
    val searchScope by viewModel.searchScope.collectAsStateWithLifecycle()
    val currentCategory by viewModel.cookbookCategory.collectAsStateWithLifecycle()
    val roomTotalCount by viewModel.roomRecipeCount.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.cookbookTitle, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Room DB (${if (roomTotalCount > 0) roomTotalCount else "220+"})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("recipebook_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.detailBackBtn)
                    }
                },
                actions = {
                    LanguagePickerDropdown(
                        currentLanguage = currentLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input targeting Room DB
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setCookbookSearch(it) },
                placeholder = {
                    Text(
                        when (searchScope) {
                            RecipeSearchScope.INGREDIENTS -> strings.cookbookSearchPlaceholderIngredients
                            RecipeSearchScope.NAME -> strings.cookbookSearchPlaceholderName
                            RecipeSearchScope.ALL -> strings.cookbookSearchPlaceholderAll
                        },
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suche",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setCookbookSearch("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = strings.cancel)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("cookbook_search_input")
            )

            // Search Scope Selector (Alles vs. Nur Zutaten vs. Nur Rezeptname)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = searchScope == RecipeSearchScope.ALL,
                    onClick = { viewModel.setSearchScope(RecipeSearchScope.ALL) },
                    label = { Text(strings.cookbookScopeAll, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                FilterChip(
                    selected = searchScope == RecipeSearchScope.INGREDIENTS,
                    onClick = { viewModel.setSearchScope(RecipeSearchScope.INGREDIENTS) },
                    label = { Text(strings.cookbookScopeIngredients, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
                FilterChip(
                    selected = searchScope == RecipeSearchScope.NAME,
                    onClick = { viewModel.setSearchScope(RecipeSearchScope.NAME) },
                    label = { Text(strings.cookbookScopeName, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }

            // Quick Ingredient Search Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = strings.cookbookIngredientTipLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                val quickIngredients = listOf("Tomaten", "Zucchini", "Käse", "Eier", "Pasta", "Kartoffeln", "Knoblauch", "Basilikum")
                quickIngredients.forEach { ingredient ->
                    ElevatedSuggestionChip(
                        onClick = {
                            viewModel.setSearchScope(RecipeSearchScope.INGREDIENTS)
                            viewModel.setCookbookSearch(ingredient)
                        },
                        label = { Text(ingredient, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            // Category Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    "ALLE" to strings.cookbookCatAll,
                    "EXPRESS" to strings.cookbookCatExpress,
                    "PASTA" to strings.cookbookCatPasta,
                    "PFANNE" to strings.cookbookCatPan,
                    "OFEN" to strings.cookbookCatOven,
                    "SUPPE" to strings.cookbookCatSoup,
                    "SNACKS" to strings.cookbookCatSnack,
                    "FRUEHSTUECK" to strings.cookbookCatBreakfast
                )

                categories.forEach { (catKey, catLabel) ->
                    FilterChip(
                        selected = currentCategory == catKey,
                        onClick = { viewModel.setCookbookCategory(catKey) },
                        label = { Text(catLabel, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            // Results count banner with Room DB confirmation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Room DB",
                        tint = FreshGreenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            String.format(strings.cookbookDbHitsPattern, recipes.size, searchQuery)
                        } else {
                            String.format(strings.cookbookDbStatusPattern, recipes.size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Recipe List
            if (recipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(strings.cookbookEmptyTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = strings.cookbookEmptySubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.setCookbookSearch("")
                                viewModel.setCookbookCategory("ALLE")
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(strings.cookbookResetBtn)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(recipes, key = { it.id }) { recipe ->
                        val isFav = savedRecipes.any { it.id == recipe.id }
                        RecipeCard(
                            recipe = recipe,
                            isFavorite = isFav,
                            onCookClick = { viewModel.selectRecipe(recipe) },
                            onShareStoryClick = { viewModel.openStoryCreator(recipe) },
                            onFavoriteToggle = { viewModel.toggleFavorite(recipe) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
