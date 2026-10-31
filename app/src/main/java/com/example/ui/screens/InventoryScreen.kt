package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.InventoryItem
import com.example.data.entity.InventoryLog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.util.AppStrings
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    items: List<InventoryItem>,
    logs: List<InventoryLog>,
    lang: String = "IN",
    onAddItem: (name: String, category: String, unit: String, initialStock: Double, minAlert: Double, pricePerUnit: Long, supplierName: String, supplierPhone: String, notes: String) -> Unit,
    onUpdateItem: (InventoryItem) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onRecordPurchase: (itemId: Long, date: Long, qty: Double, unitPrice: Long, source: String, notes: String) -> Unit,
    onRecordUsage: (itemId: Long, date: Long, qty: Double, purpose: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var editTargetItem by remember { mutableStateOf<InventoryItem?>(null) }
    var purchaseTargetItem by remember { mutableStateOf<InventoryItem?>(null) }
    var usageTargetItem by remember { mutableStateOf<InventoryItem?>(null) }
    var deleteConfirmTarget by remember { mutableStateOf<InventoryItem?>(null) }
    var supplierEditTarget by remember { mutableStateOf<InventoryItem?>(null) }

    val categories = if (AppStrings.isEnglish(lang)) {
        listOf("Semua", "Feed", "Medicine", "Vitamin", "Equipment")
    } else {
        listOf("Semua", "Pakan", "Obat", "Vitamin", "Perlengkapan")
    }

    val lowStockCount = items.count { it.currentStock <= it.minimumStockAlert }
    val filteredItems = if (selectedCategoryFilter == "Semua") items else {
        items.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("${AppStrings.warehouseStock(lang)} (${items.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text(AppStrings.stockHistory(lang), fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTabIndex == 0) {
                // Low stock alert warning if any
                if (lowStockCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.lowStockWarning(lowStockCount, lang),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }

                // Category Filter Bar
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = cat == selectedCategoryFilter,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (AppStrings.isEnglish(lang)) "No inventory items" else "Gudang stok kosong",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (AppStrings.isEnglish(lang)) "Add feed, medicine, vitamins, and farm supplies." else "Tambahkan persediaan pakan, obat, vitamin, atau perlengkapan kandang.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            InventoryCard(
                                item = item,
                                lang = lang,
                                onPurchaseClick = { purchaseTargetItem = item },
                                onUsageClick = { usageTargetItem = item },
                                onWhatsAppClick = {
                                    if (item.supplierPhone.isNotBlank()) {
                                        openWhatsApp(context, item.supplierPhone, AppStrings.generateWhatsAppMessage(
                                            supplierName = item.supplierName,
                                            itemName = item.name,
                                            currentStock = Formatters.formatNumber(item.currentStock),
                                            unit = item.unit,
                                            lang = lang
                                        ))
                                    } else {
                                        supplierEditTarget = item
                                    }
                                },
                                onEditSupplierClick = { supplierEditTarget = item },
                                onDeleteClick = { deleteConfirmTarget = item }
                            )
                        }
                    }
                }
            } else {
                // Tab 1: Inventory Logs
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (AppStrings.isEnglish(lang)) "No stock movement logs yet" else "Belum ada riwayat mutasi stok gudang",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            InventoryLogCard(log = log)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddItemDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_inventory"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Barang")
                Spacer(modifier = Modifier.width(6.dp))
                Text(AppStrings.addProduct(lang), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddItemDialog) {
        AddInventoryItemDialog(
            categories = listOf("Pakan", "Obat", "Vitamin", "Perlengkapan"),
            lang = lang,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, cat, unit, initialStock, minAlert, price, supName, supPhone, notes ->
                onAddItem(name, cat, unit, initialStock, minAlert, price, supName, supPhone, notes)
                showAddItemDialog = false
            }
        )
    }

    purchaseTargetItem?.let { item ->
        RecordPurchaseDialog(
            item = item,
            lang = lang,
            onDismiss = { purchaseTargetItem = null },
            onConfirm = { qty, price, source, notes ->
                onRecordPurchase(item.id, System.currentTimeMillis(), qty, price, source, notes)
                purchaseTargetItem = null
            }
        )
    }

    usageTargetItem?.let { item ->
        RecordUsageDialog(
            item = item,
            lang = lang,
            onDismiss = { usageTargetItem = null },
            onConfirm = { qty, purpose, notes ->
                onRecordUsage(item.id, System.currentTimeMillis(), qty, purpose, notes)
                usageTargetItem = null
            }
        )
    }

    // Dialog Tambah / Edit Kontak WhatsApp Supplier
    supplierEditTarget?.let { item ->
        EditSupplierContactDialog(
            item = item,
            lang = lang,
            onDismiss = { supplierEditTarget = null },
            onConfirm = { newName, newPhone ->
                onUpdateItem(item.copy(supplierName = newName.trim(), supplierPhone = newPhone.trim()))
                supplierEditTarget = null
            }
        )
    }

    deleteConfirmTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text(if (AppStrings.isEnglish(lang)) "Delete Item?" else "Hapus Barang Gudang?", fontWeight = FontWeight.Bold) },
            text = { Text(if (AppStrings.isEnglish(lang)) "Delete \"${item.name}\"? Previous usage logs will be preserved." else "Hapus barang \"${item.name}\"? Riwayat penggunaan sebelumnya tetap tersimpan.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteItem(item.id)
                        deleteConfirmTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(if (AppStrings.isEnglish(lang)) "Delete" else "Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deleteConfirmTarget = null }) {
                    Text(if (AppStrings.isEnglish(lang)) "Cancel" else "Batal")
                }
            }
        )
    }
}

@Composable
private fun InventoryCard(
    item: InventoryItem,
    lang: String,
    onPurchaseClick: () -> Unit,
    onUsageClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onEditSupplierClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isLowStock = item.currentStock <= item.minimumStockAlert

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEDE9FE))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D28D9)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Current stock badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLowStock) Color(0xFFFEF2F2) else Color(0xFFDCFCE7))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Formatters.formatNumber(item.currentStock),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLowStock) ExpenseRed else ProfitGreen
                        )
                        Text(
                            text = item.unit,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) ExpenseRed else ProfitGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${AppStrings.minAlert(lang)}: ${Formatters.formatNumber(item.minimumStockAlert)} ${item.unit}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isLowStock) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${AppStrings.lowStockTag(lang)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            }

            // Supplier info row
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = if (item.supplierPhone.isNotBlank()) Color(0xFF16A34A) else MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.supplierPhone.isNotBlank()) {
                            "${item.supplierName.ifBlank { "Supplier" }}: ${item.supplierPhone}"
                        } else {
                            if (AppStrings.isEnglish(lang)) "Supplier WhatsApp not set" else "WhatsApp supplier belum diatur"
                        },
                        fontSize = 11.sp,
                        color = if (item.supplierPhone.isNotBlank()) Color(0xFF15803D) else MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = onEditSupplierClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Kontak",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (item.averagePricePerUnit > 0) {
                Text(
                    text = "${if (AppStrings.isEnglish(lang)) "Average Price" else "Harga Rata-rata"}: ${Formatters.formatRupiah(item.averagePricePerUnit)} / ${item.unit}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: WhatsApp Order, Beli, Pakai, Hapus
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Order Button (Always available, highlighted especially on low stock)
                Button(
                    onClick = onWhatsAppClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLowStock) Color(0xFF25D366) else Color(0xFFDCFCE7),
                        contentColor = if (isLowStock) Color.White else Color(0xFF14532D)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.supplierPhone.isNotBlank()) AppStrings.orderViaWhatsApp(lang) else (if (AppStrings.isEnglish(lang)) "+ Set WA Supplier" else "+ WA Supplier"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onUsageClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text(AppStrings.useStock(lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onPurchaseClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text(AppStrings.buyStock(lang), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Barang",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryLogCard(log: InventoryLog) {
    val isMasuk = log.type == "MASUK"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isMasuk) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMasuk) Icons.Default.Add else Icons.Default.Remove,
                    contentDescription = null,
                    tint = if (isMasuk) ProfitGreen else ExpenseRed
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.itemName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${if (isMasuk) "Pembelian" else "Pemakaian"}: ${log.purposeOrSource} • ${Formatters.formatDate(log.date)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.totalCost > 0) {
                    Text(
                        text = "Biaya: ${Formatters.formatRupiah(log.totalCost)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Text(
                text = "${if (isMasuk) "+" else "-"}${Formatters.formatNumber(log.quantity)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMasuk) ProfitGreen else ExpenseRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddInventoryItemDialog(
    categories: List<String>,
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, unit: String, initialStock: Double, minAlert: Double, price: Long, supName: String, supPhone: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var expandedCat by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("Karung") }
    var initialStockText by remember { mutableStateOf("") }
    var minAlertText by remember { mutableStateOf("5") }
    var priceText by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.addProduct(lang), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (AppStrings.isEnglish(lang)) "Item Name (e.g. Broiler Feed BR-1)" else "Nama Barang (Contoh: Pakan Broiler BR-1)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = !expandedCat }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    selectedCategory = c
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Satuan (Kg/Karung/Botol)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = initialStockText,
                        onValueChange = { initialStockText = it },
                        label = { Text("Stok Awal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = minAlertText,
                        onValueChange = { minAlertText = it },
                        label = { Text("Batas Peringatan Minimum") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // WhatsApp Supplier Fields
                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text(AppStrings.supplierName(lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = supplierPhone,
                    onValueChange = { supplierPhone = it },
                    label = { Text("${AppStrings.supplierPhone(lang)} (Contoh: 08123456789)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Satuan Beli (Rp, opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
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
                    if (name.isBlank()) {
                        errorText = "Nama barang harus diisi"
                        return@Button
                    }
                    val initStock = initialStockText.toDoubleOrNull() ?: 0.0
                    val minAlert = minAlertText.toDoubleOrNull() ?: 5.0
                    val price = priceText.toLongOrNull() ?: 0L

                    onConfirm(name.trim(), selectedCategory, unit.trim(), initStock, minAlert, price, supplierName.trim(), supplierPhone.trim(), notes.trim())
                }
            ) {
                Text("Simpan Barang")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun EditSupplierContactDialog(
    item: InventoryItem,
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: (supplierName: String, supplierPhone: String) -> Unit
) {
    var name by remember { mutableStateOf(item.supplierName) }
    var phone by remember { mutableStateOf(item.supplierPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (AppStrings.isEnglish(lang)) "Supplier WhatsApp Contact" else "Kontak WhatsApp Supplier", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${item.name} (${item.category})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.supplierName(lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(AppStrings.supplierPhone(lang)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name.trim(), phone.trim()) }) {
                Text(if (AppStrings.isEnglish(lang)) "Save Contact" else "Simpan Kontak")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (AppStrings.isEnglish(lang)) "Cancel" else "Batal")
            }
        }
    )
}

@Composable
private fun RecordPurchaseDialog(
    item: InventoryItem,
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: (qty: Double, unitPrice: Long, source: String, notes: String) -> Unit
) {
    var qtyText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf(if (item.averagePricePerUnit > 0) item.averagePricePerUnit.toString() else "") }
    var source by remember { mutableStateOf(item.supplierName) }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val qty = qtyText.toDoubleOrNull() ?: 0.0
    val price = priceText.toLongOrNull() ?: 0L
    val totalCost = (qty * price).toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${AppStrings.buyStock(lang)}: ${item.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Stok saat ini: ${Formatters.formatNumber(item.currentStock)} ${item.unit}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Jumlah Pembelian (${item.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Satuan Beli (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Total Pengeluaran Kas: ${Formatters.formatRupiah(totalCost)}",
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed,
                        fontSize = 13.sp
                    )
                }

                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text(AppStrings.supplierName(lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
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
                    if (qty <= 0) {
                        errorText = "Jumlah beli harus lebih dari 0"
                        return@Button
                    }
                    onConfirm(qty, price, source.trim(), notes.trim())
                }
            ) {
                Text("Catat Pembelian")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun RecordUsageDialog(
    item: InventoryItem,
    lang: String,
    onDismiss: () -> Unit,
    onConfirm: (qty: Double, purpose: String, notes: String) -> Unit
) {
    var qtyText by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("Kandang 1") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${AppStrings.useStock(lang)}: ${item.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Sisa stok di gudang: ${Formatters.formatNumber(item.currentStock)} ${item.unit}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Jumlah Pemakaian (${item.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text("Tujuan Pemakaian (Contoh: Kandang 1, Batch Broiler)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Tambahan") },
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
                    val qty = qtyText.toDoubleOrNull() ?: 0.0
                    if (qty <= 0) {
                        errorText = "Jumlah pakai harus lebih dari 0"
                        return@Button
                    }
                    if (qty > item.currentStock) {
                        errorText = "Jumlah pakai ($qty) melebihi stok yang tersedia (${item.currentStock})"
                        return@Button
                    }
                    onConfirm(qty, purpose.trim(), notes.trim())
                }
            ) {
                Text("Catat Pemakaian")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun openWhatsApp(context: Context, rawPhone: String, message: String) {
    try {
        var cleanPhone = rawPhone.replace(" ", "").replace("-", "")
        if (cleanPhone.startsWith("+")) {
            cleanPhone = cleanPhone.substring(1)
        }
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "62" + cleanPhone.substring(1)
        }

        val url = "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val browserUrl = "https://wa.me/?text=${Uri.encode(message)}"
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(browserUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(fallbackIntent)
    }
}
