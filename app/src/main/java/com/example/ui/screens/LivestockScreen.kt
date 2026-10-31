package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Livestock
import com.example.data.entity.PopulationLog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivestockScreen(
    livestockList: List<Livestock>,
    populationLogs: List<PopulationLog>,
    onAddLivestock: (name: String, category: String, initialPop: Int, startDate: Long, notes: String) -> Unit,
    onDeleteLivestock: (Long) -> Unit,
    onRecordMutation: (livestockId: Long, type: String, quantity: Int, reason: String, date: Long, costOrIncome: Long, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var mutationTargetLivestock by remember { mutableStateOf<Livestock?>(null) }
    var deleteConfirmTarget by remember { mutableStateOf<Livestock?>(null) }

    val categories = listOf(
        "Ayam Petelur",
        "Ayam Pedaging",
        "Bebek",
        "Puyuh",
        "Kambing",
        "Sapi",
        "Ikan",
        "Lainnya"
    )

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
                    text = { Text("Daftar Ternak (${livestockList.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Riwayat Populasi", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTabIndex == 0) {
                val totalPop = livestockList.sumOf { it.currentPopulation }
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
                                text = "Total Populasi Ternak",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$totalPop Ekor",
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
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (livestockList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Belum ada data ternak",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tekan tombol + Tambah Ternak untuk mulai mendata populasi ternak Anda.",
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
                        items(livestockList, key = { it.id }) { livestock ->
                            LivestockCard(
                                livestock = livestock,
                                onMutateClick = { mutationTargetLivestock = livestock },
                                onDeleteClick = { deleteConfirmTarget = livestock }
                            )
                        }
                    }
                }
            } else {
                if (populationLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat mutasi ternak",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(populationLogs, key = { it.id }) { log ->
                            PopulationLogCard(log = log)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_livestock"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Ternak")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Ternak", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AddLivestockDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, cat, pop, notes ->
                onAddLivestock(name, cat, pop, System.currentTimeMillis(), notes)
                showAddDialog = false
            }
        )
    }

    mutationTargetLivestock?.let { livestock ->
        MutatePopulationDialog(
            livestock = livestock,
            onDismiss = { mutationTargetLivestock = null },
            onConfirm = { type, qty, reason, costOrIncome, notes ->
                onRecordMutation(
                    livestock.id,
                    type,
                    qty,
                    reason,
                    System.currentTimeMillis(),
                    costOrIncome,
                    notes
                )
                mutationTargetLivestock = null
            }
        )
    }

    deleteConfirmTarget?.let { livestock ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text("Hapus Ternak?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menghapus data \"${livestock.name}\"? Riwayat yang berkaitan tetap tersimpan.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteLivestock(livestock.id)
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
private fun LivestockCard(
    livestock: Livestock,
    onMutateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = livestock.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = livestock.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${livestock.currentPopulation}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ProfitGreen
                        )
                        Text(
                            text = "Ekor",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfitGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Mulai pelihara: ${Formatters.formatDate(livestock.startDate)} • Awal: ${livestock.initialPopulation} ekor",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (livestock.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catatan: ${livestock.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Ternak",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }

                Button(
                    onClick = onMutateClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mutasi Populasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PopulationLogCard(log: PopulationLog) {
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
                    text = log.livestockName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${log.reason} • ${Formatters.formatDate(log.date)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (log.notes.isNotBlank()) {
                    Text(
                        text = log.notes,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isMasuk) "+" else "-"}${log.quantity} ekor",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMasuk) ProfitGreen else ExpenseRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLivestockDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, initialPop: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var expandedCategory by remember { mutableStateOf(false) }
    var customCategory by remember { mutableStateOf("") }
    var initialPopText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Jenis Ternak", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Ternak (Contoh: Ayam Lohmann Brown)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori Ternak") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                if (selectedCategory == "Lainnya") {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("Ketik Kategori Ternak Sendiri") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = initialPopText,
                    onValueChange = { initialPopText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Populasi Awal (Ekor)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Lokasi Kandang (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Nama ternak tidak boleh kosong."
                        return@Button
                    }
                    val pop = initialPopText.toIntOrNull() ?: 0
                    val finalCategory = if (selectedCategory == "Lainnya" && customCategory.isNotBlank()) {
                        customCategory.trim()
                    } else selectedCategory

                    onConfirm(name.trim(), finalCategory, pop, notes.trim())
                }
            ) {
                Text("Simpan Ternak")
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
private fun MutatePopulationDialog(
    livestock: Livestock,
    onDismiss: () -> Unit,
    onConfirm: (type: String, qty: Int, reason: String, costOrIncome: Long, notes: String) -> Unit
) {
    var type by remember { mutableStateOf("MASUK") }
    var quantityText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("Beli Baru") }
    var moneyText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val masukReasons = listOf("Beli Baru", "Penetasan", "Kelahiran", "Hibah/Transfer")
    val keluarReasons = listOf("Kematian", "Afkir", "Terjual", "Penyembelihan", "Hilang/Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Mutasi: ${livestock.name}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Populasi saat ini: ${livestock.currentPopulation} ekor",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            type = "MASUK"
                            reason = masukReasons[0]
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "MASUK") ProfitGreen else Color(0xFFE2E8F0),
                            contentColor = if (type == "MASUK") Color.White else Color(0xFF475569)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+ Penambahan (Masuk)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            type = "KELUAR"
                            reason = keluarReasons[0]
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "KELUAR") ExpenseRed else Color(0xFFE2E8F0),
                            contentColor = if (type == "KELUAR") Color.White else Color(0xFF475569)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("- Pengurangan (Keluar)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Ternak (Ekor)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Alasan Mutasi:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val currentReasons = if (type == "MASUK") masukReasons else keluarReasons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    currentReasons.take(3).forEach { r ->
                        val isSelected = r == reason
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9))
                                .clickable { reason = r }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = r,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = moneyText,
                    onValueChange = { moneyText = it.filter { ch -> ch.isDigit() } },
                    label = { Text(if (type == "MASUK") "Total Biaya Beli (Rp, opsional)" else "Pendapatan Jual (Rp, jika dijual)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Mutasi") },
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
                    val qty = quantityText.toIntOrNull() ?: 0
                    if (qty <= 0) {
                        errorText = "Jumlah harus lebih besar dari 0"
                        return@Button
                    }
                    if (type == "KELUAR" && qty > livestock.currentPopulation) {
                        errorText = "Jumlah keluar ($qty) melebihi populasi saat ini (${livestock.currentPopulation})"
                        return@Button
                    }
                    val money = moneyText.toLongOrNull() ?: 0L
                    onConfirm(type, qty, reason, money, notes.trim())
                }
            ) {
                Text("Simpan Mutasi")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
