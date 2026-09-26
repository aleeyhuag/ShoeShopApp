package com.agkomputech.shoeshop.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun ShoeDetailScreen(
    viewModel: ShoeDetailViewModel,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var confirmingDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.reload() }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Shoe details", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onBack) { Text("Back") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (!state.loaded) {
            Text("Loading…")
            return@Column
        }
        val shoe = state.shoe
        if (shoe == null) {
            Text("This shoe was deleted or could not be found.")
            return@Column
        }

        if (state.photos.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.photos) { photo ->
                    AsyncImage(
                        model = File(photo.imagePath),
                        contentDescription = photo.angle.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(shoe.name, style = MaterialTheme.typography.titleMedium)
        Text("Tag ${shoe.tagId} · ${shoe.category}", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("₦${shoe.sellingPrice}", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Cost ₦${shoe.costPrice}  ·  Lowest ₦${shoe.lowestPrice}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Sizes in stock", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (state.sizes.isEmpty()) {
                Text("None recorded", style = MaterialTheme.typography.bodySmall)
            } else {
                state.sizes.forEach { size ->
                    AssistChip(onClick = {}, label = { Text("${size.size} ×${size.quantity}") })
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onEdit(shoe.id) }, modifier = Modifier.weight(1f)) {
                Text("Edit")
            }
            OutlinedButton(
                onClick = { confirmingDelete = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Delete this shoe?") },
            text = { Text("This removes its price, photos and stock. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmingDelete = false
                    viewModel.delete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Cancel") }
            }
        )
    }
}
