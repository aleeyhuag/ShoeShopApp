package com.agkomputech.shoeshop.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agkomputech.shoeshop.data.repository.ShoeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockValueViewModel(private val repository: ShoeRepository) : ViewModel() {

    private val _report = MutableStateFlow<ShoeRepository.StockValueReport?>(null)
    val report: StateFlow<ShoeRepository.StockValueReport?> = _report

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _report.value = repository.computeStockValueReport()
        }
    }
}
