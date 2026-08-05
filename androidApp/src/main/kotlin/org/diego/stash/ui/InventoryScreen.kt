package org.diego.stash.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.diego.stash.domain.model.FoodItem
import org.diego.stash.presentation.InventoryViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(navController: NavController, viewModel: InventoryViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Food") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            TextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search food...") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(state.filteredItems) { item ->
                    FoodItemRow(item, 
                        onConsume = { viewModel.consumeItem(item.id) },
                        onReduce = { viewModel.reduceQuantity(item.id, item.quantity) },
                        onDelete = { viewModel.discardItem(item.id, "Manually removed") }
                    )
                }
            }
        }
    }
}

@Composable
fun FoodItemRow(item: FoodItem, onConsume: () -> Unit, onReduce: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                Text("${item.quantity} ${item.unit.displayName} - ${item.storageLocation.displayName}", style = MaterialTheme.typography.bodySmall)
                Text("Expires: ${item.expiryDate}", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onReduce) { Icon(Icons.Default.Remove, contentDescription = "Reduce") }
                Button(onClick = onConsume) { Text("Use") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
            }
        }
    }
}
