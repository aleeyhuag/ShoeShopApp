package com.agkomputech.shoeshop.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agkomputech.shoeshop.data.local.entity.Shoe
import com.agkomputech.shoeshop.data.repository.ShoeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ManageShoesViewModel(private val repository: ShoeRepository) : ViewModel() {

    val shoes: StateFlow<List<Shoe>> = repository.observeAllShoes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteShoe(shoeId: Long) {
        viewModelScope.launch { repository.deleteShoe(shoeId) }
    }
}
