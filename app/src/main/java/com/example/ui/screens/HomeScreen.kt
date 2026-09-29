package com.example.ui.screens

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
import androidx.compose.material.icons.filled.PhotoLibrary
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
import com.example.ui.components.IngredientChip
import com.example.ui.components.RecipeCard
import com.example.ui.components.ScannerOverlay
import com.example.ui.theme.FreshGreenPrimary
import com.example.ui.theme.FreshOrangeSecondary
import com.example.ui.viewmodel.FridgeChefViewModel
import com.example.ui.viewmodel.ScanUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: FridgeChefViewModel,
    onNavigateToFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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
                viewModel.startScanImage(bitmap)
            } catch (e: Exception) {
                viewModel.simulateSmartDemoScan()
            }
        }
    }

    // Direct Camera picture contract
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.startScanImage(bitmap)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FridgeChef AI",
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
                                text = "ZERO WASTE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.testTag("nav_favorites_btn")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Gespeicherte Rezepte",
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
                                    text = "Was kochen wir heute? 🍳",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Foto machen • KI erkennt Zutaten • Sofort Rezepte",
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
                                        Text("Kühlschrank scannen", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.simulateSmartDemoScan() },
                                        modifier = Modifier
                                            .height(44.dp)
                                            .testTag("hero_quick_demo_btn"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("KI Demo", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
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
                                    text = "Erkannte Zutaten (${ingredients.size})",
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
                                    contentDescription = "Zutat hinzufügen",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Zutat ergänzen",
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
                                    Text("Dein Kühlschrank ist noch leer 🧊", fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Scanne ein Foto oder füge Zutaten manuell hinzu.",
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
                                text = "Rezept-Vorschläge (${matchedRecipes.size})",
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
                                label = { Text("Alle Zeiten") }
                            )

                            FilterChip(
                                selected = timeFilter == "FAST",
                                onClick = { viewModel.setTimeFilter(if (timeFilter == "FAST") "ALL" else "FAST") },
                                label = { Text("⚡ Express <15m") }
                            )

                            FilterChip(
                                selected = zeroWasteOnly,
                                onClick = { viewModel.toggleZeroWasteOnly() },
                                label = { Text("🌱 Zero Waste Hero") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            FilterChip(
                                selected = onlyFullMatch,
                                onClick = { viewModel.toggleOnlyFullMatch() },
                                label = { Text("💯 100% Match") }
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
                                Text("Keine Rezepte für diesen Filter gefunden 🔍", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Ergänze Zutaten oder deaktiviere Filter, um mehr Rezepte zu sehen.",
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
                    text = "Kühlschrank scannen 📸",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vision-KI erkennt Barcodes, Frische & Zutaten blitzschnell.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Option 1: Direct Camera
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showScanSheet = false
                            takePictureLauncher.launch(null)
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
                            Text("Foto mit Kamera aufnehmen", fontWeight = FontWeight.Bold)
                            Text("Öffnet direkt die Kamera für Kühlschrank-Foto", style = MaterialTheme.typography.bodySmall)
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
                                contentDescription = "Galerie",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Foto aus Galerie wählen", fontWeight = FontWeight.Bold)
                            Text("Bereits gespeichertes Bild hochladen", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Smart AI Demo Test (Zero Friction)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showScanSheet = false
                            viewModel.simulateSmartDemoScan()
                        }
                        .testTag("scan_option_demo"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Schnell-Scan",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Express KI-Scan (Beispiel-Kühlschrank)", fontWeight = FontWeight.Bold)
                            Text("Sofort testen mit realistischen Frische-Zutaten", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
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
                    text = "Zutat ergänzen ✏️",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newIngredientName,
                    onValueChange = { newIngredientName = it },
                    label = { Text("Zutatenname (z.B. Zucchini, Sahne)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_ingredient_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Haltbarkeit / Dringlichkeit:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = newIngredientUrgent,
                        onClick = { newIngredientUrgent = true },
                        label = { Text("🔴 Bald verbrauchen") }
                    )
                    FilterChip(
                        selected = !newIngredientUrgent,
                        onClick = { newIngredientUrgent = false },
                        label = { Text("🟢 Noch frisch") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Schnell-Vorschläge:", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("🥚 Eier", "🧀 Gouda", "🥛 Sahne", "🥩 Schinken", "🍞 Toast", "🥔 Kartoffeln", "🥕 Karotten").forEach { item ->
                        val parts = item.split(" ")
                        val emoji = parts.firstOrNull() ?: "🥗"
                        val name = parts.drop(1).joinToString(" ")
                        Button(
                            onClick = {
                                newIngredientName = name
                                newIngredientEmoji = emoji
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(item, fontSize = 12.sp)
                        }
                    }
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
                    Text("Zutat hinzufügen", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
