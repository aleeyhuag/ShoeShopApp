package com.agkomputech.shoeshop.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.agkomputech.shoeshop.data.local.entity.Shoe
import java.io.File

@Composable
fun ShoeListScreen(
    viewModel: ShoeListViewModel,
    onOpenShoe: (Long) -> Unit,
    onBack: () -> Unit
) {
    val shoes by viewModel.shoes.collectAsState()
    val thumbnails by viewModel.thumbnails.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("All shoes", style = MaterialTheme.typography.titleLarge)
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
                        thumbnailPath = thumbnails[shoe.id],
                        onClick = { onOpenShoe(shoe.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoeRow(shoe: Shoe, thumbnailPath: String?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (thumbnailPath != null) {
                    AsyncImage(
                        model = File(thumbnailPath),
                        contentDescription = shoe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(shoe.name, style = MaterialTheme.typography.bodyLarge)
                Text("₦${shoe.sellingPrice}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
