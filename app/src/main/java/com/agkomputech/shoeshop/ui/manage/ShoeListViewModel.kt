package com.agkomputech.shoeshop.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agkomputech.shoeshop.data.local.entity.Shoe
import com.agkomputech.shoeshop.data.repository.ShoeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoeListViewModel(private val repository: ShoeRepository) : ViewModel() {

    val shoes: StateFlow<List<Shoe>> = repository.observeAllShoes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _thumbnails = MutableStateFlow<Map<Long, String>>(emptyMap())
    val thumbnails: StateFlow<Map<Long, String>> = _thumbnails.asStateFlow()

    init {
        // Re-derives thumbnails every time the shoe list changes (add/delete),
        // one query rather than one per row.
        viewModelScope.launch {
            shoes.collect {
                _thumbnails.value = repository.getThumbnailPathsByShoe()
            }
        }
    }
}
