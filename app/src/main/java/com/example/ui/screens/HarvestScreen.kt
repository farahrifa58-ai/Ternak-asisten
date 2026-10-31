package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Harvest
import com.example.data.entity.Livestock
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestScreen(
    harvestList: List<Harvest>,
    livestockList: List<Livestock>,
    onAddHarvest: (livestockId: Long, livestockName: String, date: Long, product: String, qty: Double, unit: String, estPrice: Long, notes: String) -> Unit,
    onDeleteHarvest: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteConfirmTarget by remember { mutableStateOf<Harvest?>(null) }

    val totalHarvestQty = harvestList.sumOf { it.quantity }
    val totalRemainingStock = harvestList.sumOf { it.remainingQuantity }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header stats
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Stok Panen Siap Jual",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${Formatters.formatNumber(totalRemainingStock)} unit",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Egg,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (harvestList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Egg,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada catatan panen",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan tombol + Catat Panen untuk mencatat hasil panen telur, daging, susu, ikan, dll.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(harvestList, key = { it.id }) { harvest ->
                        HarvestCard(
                            harvest = harvest,
                            onDeleteClick = { deleteConfirmTarget = harvest }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_harvest"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Catat Panen")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Catat Panen", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AddHarvestDialog(
            livestockList = livestockList,
            onDismiss = { showAddDialog = false },
            onConfirm = { lId, lName, prod, qty, unit, estPrice, notes ->
                onAddHarvest(lId, lName, System.currentTimeMillis(), prod, qty, unit, estPrice, notes)
                showAddDialog = false
            }
        )
    }

    deleteConfirmTarget?.let { harvest ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text("Hapus Catatan Panen?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Hapus catatan hasil panen \"${harvest.productName}\" (${Formatters.formatNumber(harvest.quantity)} ${harvest.unit})?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHarvest(harvest.id)
                        deleteConfirmTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteConfirmTarget = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun HarvestCard(
    harvest: Harvest,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Egg,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = harvest.productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Asal: ${harvest.livestockName} • ${Formatters.formatDate(harvest.date)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Panen: ${Formatters.formatNumber(harvest.quantity)} ${harvest.unit}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (harvest.remainingQuantity > 0) Color(0xFFDCFCE7) else Color(0xFFE2E8F0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Sisa: ${Formatters.formatNumber(harvest.remainingQuantity)} ${harvest.unit}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (harvest.remainingQuantity > 0) ProfitGreen else Color(0xFF64748B)
                        )
                    }
                }
                if (harvest.estimatedPricePerUnit > 0) {
                    Text(
                        text = "Estimasi Nilai: ${Formatters.formatRupiah((harvest.quantity * harvest.estimatedPricePerUnit).toLong())}",
                        fontSize = 11.sp,
                        color = Color(0xFF0F766E)
                    )
                }
                if (harvest.notes.isNotBlank()) {
                    Text(
                        text = "Ket: ${harvest.notes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Panen",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHarvestDialog(
    livestockList: List<Livestock>,
    onDismiss: () -> Unit,
    onConfirm: (livestockId: Long, livestockName: String, product: String, qty: Double, unit: String, estPrice: Long, notes: String) -> Unit
) {
    var selectedLivestock by remember { mutableStateOf(livestockList.firstOrNull()) }
    var expandedLivestock by remember { mutableStateOf(false) }

    val commonProducts = listOf("Telur", "Ayam Siap Jual", "Daging", "Susu", "Ikan", "Lainnya")
    var selectedProduct by remember { mutableStateOf(commonProducts[0]) }
    var customProduct by remember { mutableStateOf("") }
    var expandedProduct by remember { mutableStateOf(false) }

    val commonUnits = listOf("Butir", "Kg", "Ekor", "Liter", "Rak")
    var selectedUnit by remember { mutableStateOf(commonUnits[0]) }

    var qtyText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Hasil Panen", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Pilih Ternak
                if (livestockList.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedLivestock,
                        onExpandedChange = { expandedLivestock = !expandedLivestock }
                    ) {
                        OutlinedTextField(
                            value = selectedLivestock?.name ?: "Pilih Ternak",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Asal Ternak") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLivestock) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLivestock,
                            onDismissRequest = { expandedLivestock = false }
                        ) {
                            livestockList.forEach { l ->
                                DropdownMenuItem(
                                    text = { Text("${l.name} (${l.currentPopulation} ekor)") },
                                    onClick = {
                                        selectedLivestock = l
                                        expandedLivestock = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Pilih Produk
                ExposedDropdownMenuBox(
                    expanded = expandedProduct,
                    onExpandedChange = { expandedProduct = !expandedProduct }
                ) {
                    OutlinedTextField(
                        value = selectedProduct,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nama Produk Panen") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProduct) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProduct,
                        onDismissRequest = { expandedProduct = false }
                    ) {
                        commonProducts.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    selectedProduct = p
                                    expandedProduct = false
                                }
                            )
                        }
                    }
                }

                if (selectedProduct == "Lainnya") {
                    OutlinedTextField(
                        value = customProduct,
                        onValueChange = { customProduct = it },
                        label = { Text("Ketik Nama Hasil Panen") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Jumlah & Satuan
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Jumlah Panen") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = { selectedUnit = it },
                        label = { Text("Satuan") },
                        modifier = Modifier.width(90.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Perkiraan Satuan (Rp, opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Kualitas Panen") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Text(errorText ?: "", color = ExpenseRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalProd = if (selectedProduct == "Lainnya" && customProduct.isNotBlank()) {
                        customProduct.trim()
                    } else selectedProduct

                    val qty = qtyText.toDoubleOrNull() ?: 0.0
                    if (qty <= 0) {
                        errorText = "Jumlah panen harus lebih dari 0"
                        return@Button
                    }
                    val estPrice = priceText.toLongOrNull() ?: 0L
                    val lId = selectedLivestock?.id ?: 0L
                    val lName = selectedLivestock?.name ?: "Peternakan Umum"

                    onConfirm(lId, lName, finalProd, qty, selectedUnit.trim(), estPrice, notes.trim())
                }
            ) {
                Text("Simpan Panen")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
