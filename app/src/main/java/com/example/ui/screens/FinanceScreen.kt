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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
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
import com.example.data.entity.FinanceTransaction
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.util.Formatters
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun FinanceScreen(
    transactions: List<FinanceTransaction>,
    onAddTransaction: (type: String, category: String, date: Long, amount: Long, desc: String, isOper: Boolean) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeFilter by remember { mutableStateOf("Semua") }
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteConfirmTarget by remember { mutableStateOf<FinanceTransaction?>(null) }

    val totalIncome = transactions.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
    val netCash = totalIncome - totalExpense

    val filteredTransactions = when (selectedTypeFilter) {
        "Pemasukan" -> transactions.filter { it.type == "PEMASUKAN" }
        "Pengeluaran" -> transactions.filter { it.type == "PENGELUARAN" }
        else -> transactions
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Arus Kas Bersih (Cash Flow)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatRupiah(netCash),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (netCash >= 0) ProfitGreen else ExpenseRed
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ProfitGreen.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Total Pemasukan",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Formatters.formatRupiah(totalIncome),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ExpenseRed.copy(alpha = 0.12f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Total Pengeluaran",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Formatters.formatRupiah(totalExpense),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua", "Pemasukan", "Pengeluaran").forEach { filterName ->
                    item {
                        FilterChip(
                            selected = selectedTypeFilter == filterName,
                            onClick = { selectedTypeFilter = filterName },
                            label = { Text(filterName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada transaksi keuangan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Catat biaya pakan, obat, vitamin, gaji, operasional kandang, atau pemasukan lainnya.",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { item ->
                        FinanceTransactionCard(
                            transaction = item,
                            onDeleteClick = { deleteConfirmTarget = item }
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
                .testTag("fab_add_finance"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Catat Transaksi")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Catat Transaksi", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AddFinanceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { type, cat, amount, desc, isOper ->
                onAddTransaction(type, cat, System.currentTimeMillis(), amount, desc, isOper)
                showAddDialog = false
            }
        )
    }

    deleteConfirmTarget?.let { tx ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text("Hapus Catatan Keuangan?", fontWeight = FontWeight.Bold) },
            text = { Text("Hapus transaksi \"${tx.description}\" senilai ${Formatters.formatRupiah(tx.amount)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx.id)
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
private fun FinanceTransactionCard(
    transaction: FinanceTransaction,
    onDeleteClick: () -> Unit
) {
    val isIncome = transaction.type == "PEMASUKAN"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finance_card_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Top Row: Type Icon + Category Badge + Spacer + Formatted Date + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isIncome) ProfitGreen.copy(alpha = 0.15f)
                            else ExpenseRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = if (isIncome) "Pemasukan" else "Pengeluaran",
                        tint = if (isIncome) ProfitGreen else ExpenseRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = transaction.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Date is guaranteed single line, never squeezed into vertical column
                Text(
                    text = Formatters.formatDate(transaction.date),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Transaksi",
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body Row: Full Description on Left, Amount on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = transaction.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    if (transaction.isOperationalExpense && !isIncome) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Biaya Operasional",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Text(
                    text = "${if (isIncome) "+" else "-"}${Formatters.formatRupiah(transaction.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) ProfitGreen else ExpenseRed,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFinanceDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, category: String, amount: Long, desc: String, isOper: Boolean) -> Unit
) {
    var type by remember { mutableStateOf("PENGELUARAN") }

    val expenseCategories = listOf(
        "Pakan",
        "Obat",
        "Vitamin",
        "Gaji karyawan",
        "Listrik",
        "Air",
        "Transportasi",
        "Perawatan kandang",
        "Lain-lain"
    )

    val incomeCategories = listOf(
        "Penjualan Hasil Panen",
        "Penjualan Ternak",
        "Pupuk / Limbah",
        "Lain-lain"
    )

    var selectedCategory by remember { mutableStateOf(expenseCategories[0]) }
    var expandedCat by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val currentCategories = if (type == "PEMASUKAN") incomeCategories else expenseCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Transaksi Keuangan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type selector
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            type = "PENGELUARAN"
                            selectedCategory = expenseCategories[0]
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "PENGELUARAN") ExpenseRed else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type == "PENGELUARAN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("- Pengeluaran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            type = "PEMASUKAN"
                            selectedCategory = incomeCategories[0]
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "PEMASUKAN") ProfitGreen else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type == "PEMASUKAN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Pemasukan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Category selector
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
                        currentCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Transaksi (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Keterangan Transaksi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorText != null) {
                    Text(errorText ?: "", color = ExpenseRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount <= 0) {
                        errorText = "Nominal harus lebih dari 0"
                        return@Button
                    }
                    val finalDesc = if (descText.isBlank()) selectedCategory else descText.trim()
                    onConfirm(type, selectedCategory, amount, finalDesc, true)
                }
            ) {
                Text("Simpan Transaksi")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
