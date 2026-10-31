package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DashboardSummary
import com.example.ui.MainTab
import com.example.ui.TimeFilter
import com.example.ui.components.FinanceChart
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.util.Formatters

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    summary: DashboardSummary,
    selectedFilter: TimeFilter,
    onFilterSelect: (TimeFilter) -> Unit,
    onNavigateTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Periode Laporan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TimeFilter.entries.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterSelect(filter) },
                            label = { Text(filter.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (summary.operationalProfit >= 0) {
                        Color(0xFFF0FDF4)
                    } else {
                        Color(0xFFFEF2F2)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimasi Keuntungan Bersih",
                                fontSize = 13.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatRupiah(summary.operationalProfit),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (summary.operationalProfit >= 0) ProfitGreen else ExpenseRed
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (summary.operationalProfit >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (summary.operationalProfit >= 0) Icons.Default.TrendingUp else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (summary.operationalProfit >= 0) ProfitGreen else ExpenseRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ProfitGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pemasukan", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Text(
                                text = Formatters.formatRupiah(summary.totalIncome),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfitGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(Color(0xFFE2E8F0))
                        )

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pengeluaran Kas", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Text(
                                text = Formatters.formatRupiah(summary.totalExpense),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "Aktivitas Peternakan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Populasi Ternak",
                        value = "${summary.totalLivestockPopulation} Ekor",
                        subtitle = "Ternak Aktif",
                        icon = Icons.Default.Pets,
                        iconColor = Color(0xFF0F766E),
                        bgColor = Color(0xFFCCFBF1),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainTab.LIVESTOCK) }
                    )

                    MetricCard(
                        title = "Hasil Panen",
                        value = Formatters.formatNumber(summary.totalHarvestQty),
                        subtitle = "Total Produksi",
                        icon = Icons.Default.Egg,
                        iconColor = Color(0xFFD97706),
                        bgColor = Color(0xFFFEF3C7),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainTab.HARVEST) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Penjualan",
                        value = "${summary.totalSalesCount} Transaksi",
                        subtitle = Formatters.formatRupiah(summary.totalSalesIncome),
                        icon = Icons.Default.PointOfSale,
                        iconColor = Color(0xFF2563EB),
                        bgColor = Color(0xFFDBEAFE),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainTab.SALES) }
                    )

                    MetricCard(
                        title = "Gudang & Stok",
                        value = "Cek Stok",
                        subtitle = "Pakan & Obat",
                        icon = Icons.Default.Inventory2,
                        iconColor = Color(0xFF7C3AED),
                        bgColor = Color(0xFFEDE9FE),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(MainTab.INVENTORY) }
                    )
                }
            }
        }

        item {
            FinanceChart(chartData = summary.chartData)
        }

        item {
            Column {
                Text(
                    text = "Akses Cepat",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionButton(
                        label = "Ternak",
                        icon = Icons.Default.Pets,
                        onClick = { onNavigateTab(MainTab.LIVESTOCK) }
                    )
                    QuickActionButton(
                        label = "Panen",
                        icon = Icons.Default.Egg,
                        onClick = { onNavigateTab(MainTab.HARVEST) }
                    )
                    QuickActionButton(
                        label = "Jual",
                        icon = Icons.Default.PointOfSale,
                        onClick = { onNavigateTab(MainTab.SALES) }
                    )
                    QuickActionButton(
                        label = "Stok",
                        icon = Icons.Default.Inventory2,
                        onClick = { onNavigateTab(MainTab.INVENTORY) }
                    )
                    QuickActionButton(
                        label = "Keuangan",
                        icon = Icons.Default.MonetizationOn,
                        onClick = { onNavigateTab(MainTab.FINANCE) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = iconColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
