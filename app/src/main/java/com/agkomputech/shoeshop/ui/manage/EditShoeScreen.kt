package com.agkomputech.shoeshop.ui.manage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agkomputech.shoeshop.ui.addshoe.CATEGORIES

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShoeScreen(viewModel: EditShoeViewModel, onSaved: () -> Unit, onCancel: () -> Unit) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
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
            Text("Edit shoe", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (!state.loaded) {
            Text("Loading…")
            return@Column
        }

        Text(
            "Reference photos aren't edited here — re-add the shoe if its look changed.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(16.dp))

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
        Text("Sizes in stock", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(6.dp))
        SizeEditRow(onAdd = viewModel::addSize)
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
            Text(if (state.isSaving) "Saving…" else "Save changes")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SizeEditRow(onAdd: (String, Int) -> Unit) {
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
