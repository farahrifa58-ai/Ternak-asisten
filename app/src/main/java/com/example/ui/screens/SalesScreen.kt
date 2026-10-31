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
import androidx.compose.material.icons.filled.PointOfSale
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
import com.example.data.entity.Sale
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.util.Formatters

@Composable
fun SalesScreen(
    salesList: List<Sale>,
    availableHarvests: List<Harvest>,
    onAddSale: (harvestId: Long?, date: Long, product: String, qty: Double, unit: String, unitPrice: Long, buyerNotes: String) -> Unit,
    onDeleteSale: (Sale) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteConfirmTarget by remember { mutableStateOf<Sale?>(null) }

    val totalRevenue = salesList.sumOf { it.totalPrice }

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
                            text = "Total Penjualan (${salesList.size} Transaksi)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = Formatters.formatRupiah(totalRevenue),
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
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (salesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada transaksi penjualan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tekan tombol + Catat Penjualan untuk mencatat hasil ternak yang terjual ke pembeli.",
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
                    items(salesList, key = { it.id }) { sale ->
                        SaleCard(
                            sale = sale,
                            onDeleteClick = { deleteConfirmTarget = sale }
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
                .testTag("fab_add_sale"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Catat Penjualan")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Catat Penjualan", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AddSaleDialog(
            availableHarvests = availableHarvests,
            onDismiss = { showAddDialog = false },
            onConfirm = { hId, prod, qty, unit, unitPrice, buyerNotes ->
                onAddSale(hId, System.currentTimeMillis(), prod, qty, unit, unitPrice, buyerNotes)
                showAddDialog = false
            }
        )
    }

    deleteConfirmTarget?.let { sale ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text("Hapus Penjualan?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Hapus catatan penjualan \"${sale.productName}\" (${Formatters.formatRupiah(sale.totalPrice)})? Stok panen dan pencatatan pemasukan akan disinkronkan kembali.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSale(sale)
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
private fun SaleCard(
    sale: Sale,
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
                    .background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PointOfSale,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sale.productName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${Formatters.formatNumber(sale.quantity)} ${sale.unit} × ${Formatters.formatRupiah(sale.unitPrice)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tanggal: ${Formatters.formatDate(sale.date)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (sale.buyerNotes.isNotBlank()) {
                    Text(
                        text = "Pembeli: ${sale.buyerNotes}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Formatters.formatRupiah(sale.totalPrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfitGreen
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Penjualan",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSaleDialog(
    availableHarvests: List<Harvest>,
    onDismiss: () -> Unit,
    onConfirm: (harvestId: Long?, product: String, qty: Double, unit: String, unitPrice: Long, buyerNotes: String) -> Unit
) {
    var fromHarvestStock by remember { mutableStateOf(availableHarvests.isNotEmpty()) }
    var selectedHarvest by remember { mutableStateOf(availableHarvests.firstOrNull()) }
    var expandedHarvest by remember { mutableStateOf(false) }

    var manualProduct by remember { mutableStateOf("") }
    var manualUnit by remember { mutableStateOf("Kg") }

    var qtyText by remember { mutableStateOf("") }
    var unitPriceText by remember { mutableStateOf("") }
    var buyerNotes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Auto-fill price if harvest has estimated price
    val currentProduct = if (fromHarvestStock && selectedHarvest != null) {
        selectedHarvest!!.productName
    } else manualProduct

    val currentUnit = if (fromHarvestStock && selectedHarvest != null) {
        selectedHarvest!!.unit
    } else manualUnit

    val qty = qtyText.toDoubleOrNull() ?: 0.0
    val price = unitPriceText.toLongOrNull() ?: 0L
    val calculatedTotal = (qty * price).toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Penjualan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Source toggle: Ambil dari Stok Panen vs Input Bebas
                if (availableHarvests.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                fromHarvestStock = true
                                if (selectedHarvest != null && selectedHarvest!!.estimatedPricePerUnit > 0) {
                                    unitPriceText = selectedHarvest!!.estimatedPricePerUnit.toString()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fromHarvestStock) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9),
                                contentColor = if (fromHarvestStock) Color.White else Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dari Stok Panen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { fromHarvestStock = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!fromHarvestStock) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9),
                                contentColor = if (!fromHarvestStock) Color.White else Color(0xFF475569)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Produk Bebas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (fromHarvestStock && availableHarvests.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedHarvest,
                        onExpandedChange = { expandedHarvest = !expandedHarvest }
                    ) {
                        OutlinedTextField(
                            value = selectedHarvest?.let { "${it.productName} (Sisa: ${Formatters.formatNumber(it.remainingQuantity)} ${it.unit})" } ?: "Pilih Stok Panen",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Stok Panen") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHarvest) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedHarvest,
                            onDismissRequest = { expandedHarvest = false }
                        ) {
                            availableHarvests.forEach { h ->
                                DropdownMenuItem(
                                    text = { Text("${h.productName} - ${h.livestockName} (${Formatters.formatNumber(h.remainingQuantity)} ${h.unit})") },
                                    onClick = {
                                        selectedHarvest = h
                                        if (h.estimatedPricePerUnit > 0 && unitPriceText.isBlank()) {
                                            unitPriceText = h.estimatedPricePerUnit.toString()
                                        }
                                        expandedHarvest = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = manualProduct,
                        onValueChange = { manualProduct = it },
                        label = { Text("Nama Produk / Hewan yang Dijual") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = manualUnit,
                        onValueChange = { manualUnit = it },
                        label = { Text("Satuan (Kg / Ekor / Butir)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Jumlah Terjual ($currentUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Satuan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Total Otomatis Highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF0FDF4))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Otomatis:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        Text(
                            text = Formatters.formatRupiah(calculatedTotal),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ProfitGreen
                        )
                    }
                }

                OutlinedTextField(
                    value = buyerNotes,
                    onValueChange = { buyerNotes = it },
                    label = { Text("Catatan Pembeli (Nama toko/orang)") },
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
                    if (currentProduct.isBlank()) {
                        errorText = "Nama produk tidak boleh kosong"
                        return@Button
                    }
                    if (qty <= 0) {
                        errorText = "Jumlah terjual harus lebih besar dari 0"
                        return@Button
                    }
                    if (fromHarvestStock && selectedHarvest != null && qty > selectedHarvest!!.remainingQuantity) {
                        errorText = "Jumlah (${qty}) melebihi sisa stok panen (${selectedHarvest!!.remainingQuantity})"
                        return@Button
                    }
                    if (price <= 0) {
                        errorText = "Harga satuan harus diisi"
                        return@Button
                    }

                    val hId = if (fromHarvestStock) selectedHarvest?.id else null
                    onConfirm(hId, currentProduct.trim(), qty, currentUnit.trim(), price, buyerNotes.trim())
                }
            ) {
                Text("Simpan & Masuk Kas")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
