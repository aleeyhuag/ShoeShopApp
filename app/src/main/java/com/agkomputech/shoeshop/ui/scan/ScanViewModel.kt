package com.agkomputech.shoeshop.ui.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agkomputech.shoeshop.data.local.entity.Shoe
import com.agkomputech.shoeshop.data.local.entity.ShoeSize
import com.agkomputech.shoeshop.data.repository.ShoeRepository
import com.agkomputech.shoeshop.ml.ShoeMatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data object Scanning : ScanUiState
    data class MatchesFound(
        val matches: List<ShoeMatch>,
        val selectedShoe: Shoe,
        val selectedSizes: List<ShoeSize>
    ) : ScanUiState
    data object NoMatches : ScanUiState
}

class ScanViewModel(private val repository: ShoeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Scanning)
    val uiState: StateFlow<ScanUiState> = _uiState

    private var lastMatches: List<ShoeMatch> = emptyList()

    /** Call this with a captured/cropped camera frame — see CameraX ImageCapture in ScanScreen. */
    fun onFrameCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            val matches = repository.findMatchesForFrame(bitmap, topN = 3)
            lastMatches = matches
            if (matches.isEmpty()) {
                _uiState.value = ScanUiState.NoMatches
            } else {
                selectMatch(matches.first())
            }
        }
    }

    /** Called when the user taps a different match card, same as the mockup's tap-to-swap behavior. */
    fun selectMatch(match: ShoeMatch) {
        viewModelScope.launch {
            val shoe = repository.getShoe(match.shoeId) ?: return@launch
            val sizes = repository.getSizesForShoe(match.shoeId)
            _uiState.value = ScanUiState.MatchesFound(
                matches = lastMatches,
                selectedShoe = shoe,
                selectedSizes = sizes
            )
        }
    }

    fun resetToScanning() {
        _uiState.value = ScanUiState.Scanning
    }
}
