package org.diego.stash.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.diego.stash.domain.model.FoodItem
import org.diego.stash.presentation.DashboardViewModel
import org.koin.androidx.compose.koinViewModel
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Stash Dashboard") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add_edit/0") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            ExpirySummary(state.expiredCount, state.expiring3DaysCount, state.expiring7DaysCount)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Urgent Items", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(state.urgentItems) { item ->
                    UrgentItemRow(item)
                }
            }
        }
    }
}

@Composable
fun ExpirySummary(expired: Long, in3Days: Long, in7Days: Long) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem("Expired", expired.toString(), Color.Red)
                SummaryItem("Next 3 days", in3Days.toString(), Color(0xFFFFA500))
                SummaryItem("Next 7 days", in7Days.toString(), Color.Yellow)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, count: String, color: Color) {
    Column {
        Text(count, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun UrgentItemRow(item: FoodItem) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val color = when {
        item.expiryDate < now -> Color.Red
        item.expiryDate <= now.plus(7, DateTimeUnit.DAY) -> Color.Yellow
        else -> Color.Green
    }
    
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                Text(item.expiryDate.toString(), style = MaterialTheme.typography.bodySmall)
            }
            Surface(modifier = Modifier.size(24.dp), color = color, shape = MaterialTheme.shapes.small) {}
        }
    }
}
