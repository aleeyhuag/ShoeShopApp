package com.agkomputech.shoeshop.ui.scan

import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.agkomputech.shoeshop.data.local.entity.Shoe
import com.agkomputech.shoeshop.data.local.entity.ShoeSize
import com.agkomputech.shoeshop.ml.ShoeMatch
import com.agkomputech.shoeshop.ui.common.CameraPreview
import com.agkomputech.shoeshop.util.captureToAppStorage
import kotlinx.coroutines.launch

@Composable
fun ScanScreen(viewModel: ScanViewModel, onAddNewShoe: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Shoe Shop", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onAddNewShoe) { Text("+ Add shoe") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // --- Camera viewfinder ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            CameraPreview(modifier = Modifier.fillMaxSize()) { capture ->
                imageCapture = capture
            }

            Button(
                onClick = {
                    val capture = imageCapture ?: return@Button
                    isCapturing = true
                    scope.launch {
                        try {
                            val photo = capture.captureToAppStorage(context, "scan")
                            viewModel.onFrameCaptured(photo.bitmap)
                        } finally {
                            isCapturing = false
                        }
                    }
                },
                enabled = imageCapture != null && !isCapturing,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                Text(if (isCapturing) "Matching…" else "Scan shoe")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is ScanUiState.Scanning -> {
                Text("Point the camera at a shoe and tap \"Scan shoe\".")
            }
            is ScanUiState.NoMatches -> {
                Column {
                    Text("No close match found.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddNewShoe) { Text("Add this as a new shoe") }
                }
            }
            is ScanUiState.MatchesFound -> {
                Text("Closest matches", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 220.dp)
                ) {
                    items(state.matches) { match ->
                        MatchRow(
                            match = match,
                            isSelected = match.shoeId == state.selectedShoe.id,
                            onClick = { viewModel.selectMatch(match) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                ShoeDetailCard(shoe = state.selectedShoe, sizes = state.selectedSizes)
            }
        }
    }
}

@Composable
private fun MatchRow(match: ShoeMatch, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Shoe #${match.shoeId}", style = MaterialTheme.typography.bodyMedium)
            Text("${match.confidencePercent}% match", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ShoeDetailCard(shoe: Shoe, sizes: List<ShoeSize>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(shoe.name, style = MaterialTheme.typography.titleMedium)
            Text("Tag ${shoe.tagId}", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text("₦${shoe.sellingPrice}", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Cost ₦${shoe.costPrice}  ·  Lowest ₦${shoe.lowestPrice}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Sizes in stock:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                sizes.filter { it.quantity > 0 }.forEach { size ->
                    AssistChip(onClick = {}, label = { Text(size.size) })
                }
            }
        }
    }
}
