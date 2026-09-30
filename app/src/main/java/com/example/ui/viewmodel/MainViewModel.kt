package com.example.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State representing CameraX lifecycle and capture phases.
 */
sealed interface CameraUiState {
    data object Idle : CameraUiState
    data object Initializing : CameraUiState
    data class Ready(val isFlashAvailable: Boolean = true) : CameraUiState
    data object Capturing : CameraUiState
    data class Captured(val bitmap: Bitmap) : CameraUiState
    data class Error(val message: String) : CameraUiState
}

/**
 * MainViewModel providing CameraX integration boilerplate, camera permission management,
 * and hardware configuration states following modern Android best practices.
 */
open class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- Camera Permission State ---
    private val _hasCameraPermission = MutableStateFlow(checkInitialCameraPermission())
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    private val _showPermissionRationale = MutableStateFlow(false)
    val showPermissionRationale: StateFlow<Boolean> = _showPermissionRationale.asStateFlow()

    // --- CameraX Configuration & State Boilerplate ---
    private val _cameraUiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_AUTO)
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    private val _isTorchEnabled = MutableStateFlow(false)
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    private val _captureMode = MutableStateFlow(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
    val captureMode: StateFlow<Int> = _captureMode.asStateFlow()

    private val _capturedPhoto = MutableStateFlow<Bitmap?>(null)
    val capturedPhoto: StateFlow<Bitmap?> = _capturedPhoto.asStateFlow()

    /**
     * Checks whether CAMERA permission is already granted.
     */
    private fun checkInitialCameraPermission(): Boolean {
        val context = getApplication<Application>().applicationContext
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Verifies camera permission against Context at runtime.
     */
    fun updateCameraPermission(context: Context): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        _hasCameraPermission.value = isGranted
        return isGranted
    }

    /**
     * Called when the permission launcher returns a result.
     */
    fun onCameraPermissionResult(isGranted: Boolean) {
        _hasCameraPermission.value = isGranted
        if (isGranted) {
            _showPermissionRationale.value = false
            _cameraUiState.value = CameraUiState.Initializing
        } else {
            _showPermissionRationale.value = true
            _cameraUiState.value = CameraUiState.Error("Kamerazugriff verweigert. Bitte Berechtigung in den Einstellungen erteilen.")
        }
    }

    fun setPermissionRationale(show: Boolean) {
        _showPermissionRationale.value = show
    }

    /**
     * Toggles between back and front camera lens.
     */
    fun toggleLensFacing() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    /**
     * Cycles through Flash modes: AUTO -> ON -> OFF -> AUTO.
     */
    fun cycleFlashMode() {
        _flashMode.value = when (_flashMode.value) {
            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
            else -> ImageCapture.FLASH_MODE_AUTO
        }
    }

    fun setTorchEnabled(enabled: Boolean) {
        _isTorchEnabled.value = enabled
    }

    /**
     * CameraX lifecycle callbacks.
     */
    fun onCameraReady(isFlashAvailable: Boolean = true) {
        _cameraUiState.value = CameraUiState.Ready(isFlashAvailable)
    }

    fun onCapturingStarted() {
        _cameraUiState.value = CameraUiState.Capturing
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        _capturedPhoto.value = bitmap
        _cameraUiState.value = CameraUiState.Captured(bitmap)
    }

    fun onCameraError(errorMessage: String) {
        _cameraUiState.value = CameraUiState.Error(errorMessage)
    }

    fun resetCamera() {
        _capturedPhoto.value = null
        _cameraUiState.value = if (_hasCameraPermission.value) CameraUiState.Initializing else CameraUiState.Idle
    }
}
