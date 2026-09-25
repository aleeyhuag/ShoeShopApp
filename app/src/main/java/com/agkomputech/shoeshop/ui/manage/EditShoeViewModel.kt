package com.agkomputech.shoeshop.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agkomputech.shoeshop.data.repository.ShoeRepository
import com.agkomputech.shoeshop.ui.addshoe.CATEGORIES
import com.agkomputech.shoeshop.ui.addshoe.SizeEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditShoeState(
    val loaded: Boolean = false,
    val name: String = "",
    val category: String = CATEGORIES.first(),
    val costPrice: String = "",
    val sellingPrice: String = "",
    val lowestPrice: String = "",
    val sizes: List<SizeEntry> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class EditShoeViewModel(
    private val repository: ShoeRepository,
    private val shoeId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(EditShoeState())
    val state: StateFlow<EditShoeState> = _state

    init {
        viewModelScope.launch {
            val shoe = repository.getShoe(shoeId)
            val sizes = repository.getSizesForShoe(shoeId)
            if (shoe != null) {
                _state.value = EditShoeState(
                    loaded = true,
                    name = shoe.name,
                    category = shoe.category,
                    costPrice = shoe.costPrice.toString(),
                    sellingPrice = shoe.sellingPrice.toString(),
                    lowestPrice = shoe.lowestPrice.toString(),
                    sizes = sizes.map { SizeEntry(it.size, it.quantity) }
                )
            } else {
                _state.value = _state.value.copy(loaded = true, error = "Shoe not found")
            }
        }
    }

    fun updateName(v: String) { _state.value = _state.value.copy(name = v) }
    fun updateCategory(v: String) { _state.value = _state.value.copy(category = v) }
    fun updateCostPrice(v: String) { _state.value = _state.value.copy(costPrice = v) }
    fun updateSellingPrice(v: String) { _state.value = _state.value.copy(sellingPrice = v) }
    fun updateLowestPrice(v: String) { _state.value = _state.value.copy(lowestPrice = v) }

    fun addSize(size: String, quantity: Int) {
        if (size.isBlank() || quantity <= 0) return
        val current = _state.value
        val withoutSameSize = current.sizes.filterNot { it.size == size }
        _state.value = current.copy(sizes = withoutSameSize + SizeEntry(size, quantity))
    }

    fun removeSize(size: String) {
        _state.value = _state.value.copy(sizes = _state.value.sizes.filterNot { it.size == size })
    }

    fun save() {
        val s = _state.value
        val cost = s.costPrice.toIntOrNull()
        val selling = s.sellingPrice.toIntOrNull()
        val lowest = s.lowestPrice.toIntOrNull()
        if (s.name.isBlank() || cost == null || selling == null || lowest == null || s.sizes.isEmpty()) {
            _state.value = s.copy(error = "Fill in all fields and at least one size before saving.")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                repository.updateShoe(
                    shoeId = shoeId,
                    name = s.name,
                    category = s.category,
                    costPrice = cost,
                    sellingPrice = selling,
                    lowestPrice = lowest,
                    sizes = s.sizes.map { it.size to it.quantity }
                )
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message ?: "Could not save changes")
            }
        }
    }
}
