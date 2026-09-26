package com.agkomputech.shoeshop.ui.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agkomputech.shoeshop.data.repository.ShoeRepository

@Composable
fun StockValueScreen(viewModel: StockValueViewModel, onBack: () -> Unit) {
    val report by viewModel.report.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stock value", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Cost price × quantity, summed across every shoe and every size — this is what your manual monthly count worked out to, calculated automatically.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        val currentReport = report
        if (currentReport == null) {
            Text("Calculating…")
            return@Column
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total stock value (at cost)", style = MaterialTheme.typography.labelMedium)
                Text("₦${currentReport.totalValue}", style = MaterialTheme.typography.headlineMedium)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("By shoe", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))

        if (currentReport.rows.isEmpty()) {
            Text("No shoes saved yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(currentReport.rows, key = { it.shoeId }) { row ->
                    StockRow(row)
                }
            }
        }
    }
}

@Composable
private fun StockRow(row: ShoeRepository.StockValueRow) {
    Surface(shape = RoundedCornerShape(12.dp), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "₦${row.costPrice} × ${row.totalQuantity}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("₦${row.value}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
