package com.agkomputech.shoeshop.ui.addshoe

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agkomputech.shoeshop.data.local.entity.PhotoAngle
import com.agkomputech.shoeshop.data.repository.ShoeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CapturedAnglePhoto(val angle: PhotoAngle, val bitmap: Bitmap, val filePath: String)

data class SizeEntry(val size: String, val quantity: Int)

data class AddShoeFormState(
    val tagId: String = "",
    val name: String = "",
    val category: String = "Sneakers",
    val costPrice: String = "",
    val sellingPrice: String = "",
    val lowestPrice: String = "",
    val photos: List<CapturedAnglePhoto> = emptyList(),
    val sizes: List<SizeEntry> = emptyList(),
    val isSaving: Boolean = false,
    val savedShoeId: Long? = null,
    val error: String? = null
)

val CATEGORIES = listOf("Sneakers", "Slides", "Flip-flops", "Crocs", "Formal", "Boots")
val REQUIRED_ANGLES = listOf(PhotoAngle.TOP, PhotoAngle.SIDE, PhotoAngle.DETAIL)

class AddShoeViewModel(private val repository: ShoeRepository) : ViewModel() {

    private val _state = MutableStateFlow(AddShoeFormState())
    val state: StateFlow<AddShoeFormState> = _state

    fun updateTagId(v: String) { _state.value = _state.value.copy(tagId = v) }
    fun updateName(v: String) { _state.value = _state.value.copy(name = v) }
    fun updateCategory(v: String) { _state.value = _state.value.copy(category = v) }
    fun updateCostPrice(v: String) { _state.value = _state.value.copy(costPrice = v) }
    fun updateSellingPrice(v: String) { _state.value = _state.value.copy(sellingPrice = v) }
    fun updateLowestPrice(v: String) { _state.value = _state.value.copy(lowestPrice = v) }

    /** Called once per angle as the user captures TOP, then SIDE, then DETAIL. */
    fun addPhoto(angle: PhotoAngle, bitmap: Bitmap, filePath: String) {
        val current = _state.value
        val withoutSameAngle = current.photos.filterNot { it.angle == angle }
        _state.value = current.copy(photos = withoutSameAngle + CapturedAnglePhoto(angle, bitmap, filePath))
    }

    fun addSize(size: String, quantity: Int) {
        if (size.isBlank() || quantity <= 0) return
        val current = _state.value
        val withoutSameSize = current.sizes.filterNot { it.size == size }
        _state.value = current.copy(sizes = withoutSameSize + SizeEntry(size, quantity))
    }

    fun removeSize(size: String) {
        _state.value = _state.value.copy(sizes = _state.value.sizes.filterNot { it.size == size })
    }

    /** Next angle still needed, or null if TOP/SIDE/DETAIL are all captured. */
    fun nextAngleNeeded(): PhotoAngle? {
        val captured = _state.value.photos.map { it.angle }.toSet()
        return REQUIRED_ANGLES.firstOrNull { it !in captured }
    }

    fun canSave(): Boolean {
        val s = _state.value
        return s.tagId.isNotBlank() &&
            s.name.isNotBlank() &&
            s.costPrice.toIntOrNull() != null &&
            s.sellingPrice.toIntOrNull() != null &&
            s.lowestPrice.toIntOrNull() != null &&
            s.photos.size == REQUIRED_ANGLES.size &&
            s.sizes.isNotEmpty()
    }

    fun save() {
        val s = _state.value
        if (!canSave()) {
            _state.value = s.copy(error = "Fill in all fields, all 3 photos, and at least one size before saving.")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val shoeId = repository.addShoe(
                    tagId = s.tagId,
                    name = s.name,
                    category = s.category,
                    costPrice = s.costPrice.toInt(),
                    sellingPrice = s.sellingPrice.toInt(),
                    lowestPrice = s.lowestPrice.toInt(),
                    photos = s.photos.map { it.angle to it.bitmap },
                    imagePaths = s.photos.map { it.filePath },
                    sizes = s.sizes.map { it.size to it.quantity }
                )
                _state.value = _state.value.copy(isSaving = false, savedShoeId = shoeId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message ?: "Could not save shoe")
            }
        }
    }
}
