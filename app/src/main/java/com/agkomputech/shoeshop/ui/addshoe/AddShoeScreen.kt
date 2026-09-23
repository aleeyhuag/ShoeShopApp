package com.agkomputech.shoeshop.ui.addshoe

import androidx.camera.core.ImageCapture
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.agkomputech.shoeshop.data.local.entity.PhotoAngle
import com.agkomputech.shoeshop.ui.common.CameraPreview
import com.agkomputech.shoeshop.util.captureToAppStorage
import kotlinx.coroutines.launch

private fun PhotoAngle.label() = when (this) {
    PhotoAngle.TOP -> "Top / 3-quarter view"
    PhotoAngle.SIDE -> "Side profile"
    PhotoAngle.DETAIL -> "Close-up of logo / distinguishing detail"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShoeScreen(viewModel: AddShoeViewModel, onSaved: () -> Unit, onCancel: () -> Unit) {    
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedShoeId) {
        if (state.savedShoeId != null) onSaved()
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
            Text("Add shoe", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // --- Guided photo capture ---
        val nextAngle = viewModel.nextAngleNeeded()
        Text("Reference photos (${state.photos.size}/3)", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(6.dp))

        if (nextAngle != null) {
            Text("Next: ${nextAngle.label()}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 5f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            ) {
                CameraPreview(modifier = Modifier.fillMaxSize()) { capture -> imageCapture = capture }
                Button(
                    onClick = {
                        val capture = imageCapture ?: return@Button
                        isCapturing = true
                        scope.launch {
                            try {
                                val photo = capture.captureToAppStorage(context, nextAngle.name.lowercase())
                                viewModel.addPhoto(nextAngle, photo.bitmap, photo.filePath)
                            } finally {
                                isCapturing = false
                            }
                        }
                    },
                    enabled = imageCapture != null && !isCapturing,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text(if (isCapturing) "Saving…" else "Capture ${nextAngle.name.lowercase()}")
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "All 3 reference photos captured ✓",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- Price / details form ---
        Text("Details", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = state.tagId, onValueChange = viewModel::updateTagId,
            label = { Text("Tag (e.g. #0142)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.name, onValueChange = viewModel::updateName,
            label = { Text("Name (for your own reference)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        var categoryExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
            OutlinedTextField(
                value = state.category, onValueChange = {}, readOnly = true,
                label = { Text("Category") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                CATEGORIES.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = { viewModel.updateCategory(category); categoryExpanded = false }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.costPrice, onValueChange = viewModel::updateCostPrice,
                label = { Text("Cost ₦") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.sellingPrice, onValueChange = viewModel::updateSellingPrice,
                label = { Text("Selling ₦") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.lowestPrice, onValueChange = viewModel::updateLowestPrice,
            label = { Text("Lowest you'll accept ₦") }, modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- Sizes / stock ---
        Text("Sizes in stock", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(6.dp))
        SizeEntryRow(onAdd = viewModel::addSize)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.sizes.forEach { entry ->
                AssistChip(
                    onClick = { viewModel.removeSize(entry.size) },
                    label = { Text("${entry.size} ×${entry.quantity}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.error != null) {
            Text(state.error!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Saving…" else "Save shoe")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SizeEntryRow(onAdd: (String, Int) -> Unit) {
    var size by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = size, onValueChange = { size = it },
            label = { Text("Size") }, modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = qty, onValueChange = { qty = it },
            label = { Text("Qty") }, modifier = Modifier.weight(1f)
        )
        Button(onClick = {
            val q = qty.toIntOrNull() ?: return@Button
            onAdd(size, q)
            size = ""; qty = ""
        }) { Text("Add") }
    }
}
