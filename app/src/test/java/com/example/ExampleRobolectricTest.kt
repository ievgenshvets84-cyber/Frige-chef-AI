package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FridgeChef AI", appName)
  }

  @Test
  fun `verify MainViewModel camera permission and boilerplate configuration`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.viewmodel.MainViewModel(app)

    // Test permission denial sets rationale and error state
    viewModel.onCameraPermissionResult(false)
    assertEquals(false, viewModel.hasCameraPermission.value)
    assertEquals(true, viewModel.showPermissionRationale.value)
    assertTrue(viewModel.cameraUiState.value is com.example.ui.viewmodel.CameraUiState.Error)

    // Test permission granted sets initializing state
    viewModel.onCameraPermissionResult(true)
    assertEquals(true, viewModel.hasCameraPermission.value)
    assertEquals(false, viewModel.showPermissionRationale.value)
    assertTrue(viewModel.cameraUiState.value is com.example.ui.viewmodel.CameraUiState.Initializing)

    // Test CameraX lens toggle
    val initialLens = viewModel.lensFacing.value
    viewModel.toggleLensFacing()
    assertEquals(androidx.camera.core.CameraSelector.LENS_FACING_FRONT, viewModel.lensFacing.value)

    // Test CameraX flash cycling
    viewModel.cycleFlashMode()
    assertEquals(androidx.camera.core.ImageCapture.FLASH_MODE_ON, viewModel.flashMode.value)
  }

  @Test
  fun `verify RecipeEntity and RecipeDao in Room database`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.local.FridgeDatabase::class.java
    ).allowMainThreadQueries().build()

    val recipeDao = db.recipeDao()

    val sampleRecipe = com.example.data.local.RecipeEntity(
      id = "test_rec_1",
      name = "Zucchini-Käse-Pfanne",
      category = "Pfannengerichte",
      ingredients = listOf("Zucchini", "Gouda", "Zwiebeln", "Olivenöl"),
      instructions = listOf("Zucchini würfeln", "In der Pfanne braten", "Käse schmelzen lassen"),
      description = "Schnelle Pfanne mit 4 Zutaten",
      prepTimeMinutes = 12
    )

    recipeDao.insertRecipe(sampleRecipe)

    val count = recipeDao.getRecipeCount()
    assertEquals(1, count)

    val fetched = recipeDao.getRecipeById("test_rec_1")
    org.junit.Assert.assertNotNull(fetched)
    assertEquals("Zucchini-Käse-Pfanne", fetched?.name)
    assertEquals("Pfannengerichte", fetched?.category)
    assertEquals(4, fetched?.ingredients?.size)
    assertEquals("Zucchini würfeln", fetched?.instructions?.first())

    // Test querying by ingredient
    val ingredientResults = kotlinx.coroutines.flow.first(recipeDao.searchByIngredient("Zucchini"))
    assertEquals(1, ingredientResults.size)
    assertEquals("Zucchini-Käse-Pfanne", ingredientResults[0].name)

    // Test querying by recipe name
    val nameResults = kotlinx.coroutines.flow.first(recipeDao.searchByName("Pfanne"))
    assertEquals(1, nameResults.size)

    // Test general search query
    val generalResults = kotlinx.coroutines.flow.first(recipeDao.searchRecipes("Gouda"))
    assertEquals(1, generalResults.size)

    db.close()
  }
}
