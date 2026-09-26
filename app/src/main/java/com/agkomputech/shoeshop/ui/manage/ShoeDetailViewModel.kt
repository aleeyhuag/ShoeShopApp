package com.agkomputech.shoeshop.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agkomputech.shoeshop.data.local.entity.Shoe
import com.agkomputech.shoeshop.data.local.entity.ShoePhoto
import com.agkomputech.shoeshop.data.local.entity.ShoeSize
import com.agkomputech.shoeshop.data.repository.ShoeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ShoeDetailState(
    val loaded: Boolean = false,
    val shoe: Shoe? = null,
    val photos: List<ShoePhoto> = emptyList(),
    val sizes: List<ShoeSize> = emptyList(),
    val deleted: Boolean = false
)

class ShoeDetailViewModel(
    private val repository: ShoeRepository,
    private val shoeId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(ShoeDetailState())
    val state: StateFlow<ShoeDetailState> = _state

    fun reload() {
        viewModelScope.launch {
            val shoe = repository.getShoe(shoeId)
            val photos = repository.getPhotosForShoe(shoeId)
            val sizes = repository.getSizesForShoe(shoeId)
            _state.value = ShoeDetailState(loaded = true, shoe = shoe, photos = photos, sizes = sizes)
        }
    }

    fun delete() {
        viewModelScope.launch {
            repository.deleteShoe(shoeId)
            _state.value = _state.value.copy(deleted = true)
        }
    }
}
