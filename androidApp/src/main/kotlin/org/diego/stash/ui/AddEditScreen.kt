package org.diego.stash.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.diego.stash.domain.model.*
import org.diego.stash.presentation.AddEditViewModel
import org.koin.androidx.compose.koinViewModel
import kotlinx.datetime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(navController: NavController, itemId: Long, viewModel: AddEditViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    var showScanner by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        }
    }

    if (showScanner) {
        ModalBottomSheet(onDismissRequest = { showScanner = false }) {
            Box(modifier = Modifier.fillMaxHeight(0.6f)) {
                BarcodeScannerView(onBarcodeScanned = { code ->
                    viewModel.scanBarcode(code)
                    showScanner = false
                })
            }
        }
    }

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.expiryDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
                        viewModel.onExpiryDateChanged(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (itemId == 0L) "Add Food" else "Edit Food") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            Button(
                onClick = { 
                    when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                        PackageManager.PERMISSION_GRANTED -> showScanner = true
                        else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }, 
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Barcode")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextField(
                value = state.name, 
                onValueChange = { viewModel.onNameChanged(it) }, 
                label = { Text("Name") }, 
                modifier = Modifier.fillMaxWidth(),
                isError = state.nameError != null,
                supportingText = {
                    state.nameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            
            if (state.isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
            }
            
            state.lookupMessage?.let { message ->
                Text(
                    message,
                    color = if (state.lookupIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (state.barcode.isNotBlank()) {
                Text(
                    "Barcode: ${state.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                TextField(value = state.category.displayName, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    Category.entries.forEach { category ->
                        DropdownMenuItem(text = { Text(category.displayName) }, onClick = { viewModel.onCategoryChanged(category); categoryExpanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(value = state.quantity.toString(), onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.onQuantityChanged(v) } }, label = { Text("Qty") }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                var unitExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }, modifier = Modifier.weight(1f)) {
                    TextField(value = state.unit.displayName, onValueChange = {}, readOnly = true, label = { Text("Unit") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        FoodUnit.entries.forEach { unit ->
                            DropdownMenuItem(text = { Text(unit.displayName) }, onClick = { viewModel.onUnitChanged(unit); unitExpanded = false })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Expiry Date", style = MaterialTheme.typography.labelMedium)
                    Text(state.expiryDate.toString(), style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Button(onClick = { 
                viewModel.saveItem()
            }, modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving) {
                Text(if (state.isSaving) "Saving..." else "Save Item")
            }
        }
    }
}
