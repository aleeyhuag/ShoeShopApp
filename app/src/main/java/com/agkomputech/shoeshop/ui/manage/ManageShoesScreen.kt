package com.agkomputech.shoeshop.ui.manage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agkomputech.shoeshop.data.local.entity.Shoe

@Composable
fun ManageShoesScreen(
    viewModel: ManageShoesViewModel,
    onEditShoe: (Long) -> Unit,
    onBack: () -> Unit
) {
    val shoes by viewModel.shoes.collectAsState()
    var pendingDelete by remember { mutableStateOf<Shoe?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Manage shoes", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (shoes.isEmpty()) {
            Text("No shoes saved yet — add one from the scan screen.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(shoes, key = { it.id }) { shoe ->
                    ShoeRow(
                        shoe = shoe,
                        onEdit = { onEditShoe(shoe.id) },
                        onDelete = { pendingDelete = shoe }
                    )
                }
            }
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${toDelete.name}?") },
            text = { Text("This removes its price, photos and stock. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteShoe(toDelete.id)
                    pendingDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ShoeRow(shoe: Shoe, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(shoe.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Tag ${shoe.tagId} · ${shoe.category} · ₦${shoe.sellingPrice}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onEdit) { Text("Edit") }
            TextButton(onClick = onDelete) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        }
    }
}
