package com.example.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Recipe
import com.example.ui.components.CustomRecipeCreatorSheet
import com.example.ui.components.IngredientChip
import com.example.ui.components.LanguagePickerDropdown
import com.example.ui.components.RecipeCard
import com.example.ui.components.ScannerOverlay
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.FreshGreenPrimary
import com.example.ui.theme.FreshOrangeSecondary
import com.example.ui.viewmodel.FridgeChefViewModel
import com.example.ui.viewmodel.ScanUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: FridgeChefViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToCookbook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val savedRecipes by viewModel.savedRecipes.collectAsStateWithLifecycle()
    val matchedRecipes by viewModel.matchedRecipes.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()
    val onlyFullMatch by viewModel.onlyFullMatch.collectAsStateWithLifecycle()
    val zeroWasteOnly by viewModel.zeroWasteOnly.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newIngredientName by remember { mutableStateOf("") }
    var newIngredientEmoji by remember { mutableStateOf("🥗") }
    var newIngredientUrgent by remember { mutableStateOf(false) }
    var showScanSheet by remember { mutableStateOf(false) }
    var showCustomRecipeCreator by remember { mutableStateOf(false) }
    var scanTargetContext by remember { mutableStateOf("TABLE") } // "TABLE" or "FRIDGE"
    val sheetState = rememberModalBottomSheetState()

    // Activity Result Launcher for Media / Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.startScanImage(bitmap, scanTargetContext)
            } catch (e: Exception) {
                if (scanTargetContext == "TABLE") {
                    viewModel.simulateTableDemoScan()
                } else {
                    viewModel.simulateSmartDemoScan()
                }
            }
        }
    }

    // Direct Camera picture contract
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.onPhotoCaptured(bitmap)
            viewModel.startScanImage(bitmap, scanTargetContext)
        }
    }

    // Runtime CAMERA permission launcher following Android best practices
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.onCameraPermissionResult(isGranted)
        if (isGranted) {
            takePictureLauncher.launch(null)
        }
    }

    val showPermissionRationale by viewModel.showPermissionRationale.collectAsStateWithLifecycle()
    val hasCameraPermission by viewModel.hasCameraPermission.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = strings.zeroWasteBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Quick Link to Custom Recipe Creator with Selected Ingredients
                    IconButton(
                        onClick = { showCustomRecipeCreator = true },
                        modifier = Modifier.testTag("nav_custom_creator_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = strings.navCustomCreatorTooltip,
                            tint = FreshOrangeSecondary
                        )
                    }

                    // Quick Link to 220+ Recipe Book
                    IconButton(
                        onClick = onNavigateToCookbook,
                        modifier = Modifier.testTag("nav_cookbook_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = strings.navCookbookTooltip,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.testTag("nav_favorites_btn")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = strings.navFavoritesTooltip,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            if (savedRecipes.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(FreshOrangeSecondary)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }

                    // Top-Right Language Picker Dropdown
                    LanguagePickerDropdown(
                        currentLanguage = currentLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showScanSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .testTag("fab_scan_fridge")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Kühlschrank scannen",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // Hero Banner & One-Tap Scan CTA Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("hero_scan_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                            // Hero Background Image
                            Image(
                                painter = painterResource(id = R.drawable.fridge_hero),
                                contentDescription = "Kühlschrank Inhalt",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Gradient Scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.4f),
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )

                            // Overlay Text & Scan Button
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = strings.heroTitle,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.heroSubtitle,
                                    color = Color(0xFFC8E6C9),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showScanSheet = true },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("hero_scan_btn"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(strings.heroScanBtn, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = onNavigateToCookbook,
                                        modifier = Modifier
                                            .height(44.dp)
                                            .testTag("hero_cookbook_btn"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.heroCookbookBtn, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Search Bar targeting Room Database
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onNavigateToCookbook() }
                            .testTag("home_search_bar_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Suche",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.searchBarPlaceholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Custom Recipe Creator CTA Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { showCustomRecipeCreator = true }
                            .testTag("home_custom_recipe_cta_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FreshGreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👨‍🍳", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.customCreatorCtaTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = strings.customCreatorCtaSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Inventory Review Section
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Kitchen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${strings.inventoryTitle} (${ingredients.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.textButtonColors(),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = strings.inventoryAddBtn,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.inventoryAddBtn,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (ingredients.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(strings.inventoryEmptyTitle, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = strings.inventoryEmptySubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ingredients.forEach { ingredient ->
                                    IngredientChip(
                                        ingredient = ingredient,
                                        onDelete = { viewModel.deleteIngredient(ingredient.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter & Sort Toolbar
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.matchedRecipesTitle} (${matchedRecipes.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Scrollable Filters
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = timeFilter == "ALL",
                                onClick = { viewModel.setTimeFilter("ALL") },
                                label = { Text(strings.filterAll) }
                            )

                            FilterChip(
                                selected = timeFilter == "FAST",
                                onClick = { viewModel.setTimeFilter(if (timeFilter == "FAST") "ALL" else "FAST") },
                                label = { Text(strings.filterFast) }
                            )

                            FilterChip(
                                selected = zeroWasteOnly,
                                onClick = { viewModel.toggleZeroWasteOnly() },
                                label = { Text(strings.filterZeroWaste) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            FilterChip(
                                selected = onlyFullMatch,
                                onClick = { viewModel.toggleOnlyFullMatch() },
                                label = { Text(strings.filterFullMatch) }
                            )

                            FilterChip(
                                selected = timeFilter == "NORMAL",
                                onClick = { viewModel.setTimeFilter(if (timeFilter == "NORMAL") "ALL" else "NORMAL") },
                                label = { Text("🍳 <30m") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Recipe Feed Items
                if (matchedRecipes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(strings.emptyRecipesTitle, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    strings.emptyRecipesSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                } else {
                    items(matchedRecipes, key = { it.id }) { recipe ->
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

            // Scanning AI Animation Overlay
            AnimatedVisibility(
                visible = scanState is ScanUiState.Scanning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (scanState is ScanUiState.Scanning) {
                    val s = scanState as ScanUiState.Scanning
                    ScannerOverlay(
                        progressText = s.progressText,
                        stepNumber = s.stepNumber
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet: Quick Scan Choice (Camera, Gallery, Smart Demo)
    if (showScanSheet) {
        ModalBottomSheet(
            onDismissRequest = { showScanSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = strings.scanSheetTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.scanSheetSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scan Area Selector (Table vs. Fridge)
                Text(
                    text = strings.scanSheetContextLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = scanTargetContext == "TABLE",
                        onClick = { scanTargetContext = "TABLE" },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(strings.scanSheetContextTable) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    FilterChip(
                        selected = scanTargetContext == "FRIDGE",
                        onClick = { scanTargetContext = "FRIDGE" },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(strings.scanSheetContextFridge) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Direct Camera
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showScanSheet = false
                            if (hasCameraPermission) {
                                takePictureLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .testTag("scan_option_camera"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(FreshGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Kamera öffnen",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (scanTargetContext == "TABLE") strings.scanOptionCameraTitleTable else strings.scanOptionCameraTitleFridge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (scanTargetContext == "TABLE") strings.scanOptionCameraSubTable else strings.scanOptionCameraSubFridge,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Gallery Picker
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showScanSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag("scan_option_gallery"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = strings.scanOptionGalleryTitle,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(strings.scanOptionGalleryTitle, fontWeight = FontWeight.Bold)
                            Text(strings.scanOptionGallerySub, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.scanQuickTestsLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Option 3: Table Demo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showScanSheet = false
                            viewModel.simulateTableDemoScan()
                        }
                        .testTag("scan_option_table_demo"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🍽️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(strings.scanOptionTableDemoTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(strings.scanOptionTableDemoSub, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Option 4: Fridge Demo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showScanSheet = false
                            viewModel.simulateSmartDemoScan()
                        }
                        .testTag("scan_option_fridge_demo"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧊", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(strings.scanOptionFridgeDemoTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(strings.scanOptionFridgeDemoSub, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Add Ingredient Bottom Sheet / Dialog
    if (showAddDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAddDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = strings.addDialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newIngredientName,
                    onValueChange = { newIngredientName = it },
                    label = { Text(strings.addDialogNameLabel) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_ingredient_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = newIngredientUrgent,
                        onCheckedChange = { newIngredientUrgent = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.addDialogUrgentToggle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (newIngredientName.isNotBlank()) {
                            viewModel.addIngredient(
                                name = newIngredientName.trim(),
                                emoji = newIngredientEmoji,
                                shelfLifeDays = if (newIngredientUrgent) 1 else 5,
                                isUrgent = newIngredientUrgent
                            )
                            newIngredientName = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_add_ingredient_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.addDialogConfirmBtn, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Camera Permission Rationale Dialog (Android Best Practice)
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { viewModel.setPermissionRationale(false) },
            title = {
                Text("Kamerazugriff benötigt 📸", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Um deinen Kühlschrank zu scannen und Zutaten automatisch per KI zu identifizieren, benötigt FridgeChef AI Zugriff auf die Kamera.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPermissionRationale(false)
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Berechtigung erteilen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.setPermissionRationale(false) }
                ) {
                    Text("Später")
                }
            }
        )
    }

    // Custom Recipe Creator Sheet
    if (showCustomRecipeCreator) {
        CustomRecipeCreatorSheet(
            viewModel = viewModel,
            availableIngredients = ingredients,
            onDismiss = { showCustomRecipeCreator = false },
            onRecipeCreated = {
                showCustomRecipeCreator = false
            }
        )
    }
}
