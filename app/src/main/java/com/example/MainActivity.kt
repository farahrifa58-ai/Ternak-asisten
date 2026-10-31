package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.FarmProfile
import com.example.ui.FarmViewModel
import com.example.ui.MainTab
import com.example.ui.components.TopFarmBar
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.HarvestScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LivestockScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppStrings

class MainActivity : ComponentActivity() {

    private val viewModel: FarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profile by viewModel.farmProfile.collectAsStateWithLifecycle()
            val themeMode = profile?.themeMode ?: "SYSTEM"

            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                FarmAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FarmAppRoot(viewModel: FarmViewModel) {
    val showSplash by viewModel.showSplash.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val profile by viewModel.farmProfile.collectAsStateWithLifecycle()
    val livestockList by viewModel.allLivestock.collectAsStateWithLifecycle()
    val populationLogs by viewModel.allPopulationLogs.collectAsStateWithLifecycle()
    val harvests by viewModel.allHarvests.collectAsStateWithLifecycle()
    val availableHarvests by viewModel.availableHarvests.collectAsStateWithLifecycle()
    val sales by viewModel.allSales.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.allInventoryItems.collectAsStateWithLifecycle()
    val inventoryLogs by viewModel.allInventoryLogs.collectAsStateWithLifecycle()
    val financeTransactions by viewModel.allFinanceTransactions.collectAsStateWithLifecycle()

    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val timeFilter by viewModel.timeFilter.collectAsStateWithLifecycle()

    val currentFarmProfile = profile ?: FarmProfile()
    val lang = currentFarmProfile.appLanguage

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // 1. Initial Splash Screen with Green-White Gradient, Logo, and by Rafline Dev
    if (showSplash) {
        SplashScreen(
            lang = lang,
            onFinished = { viewModel.finishSplash() }
        )
        return
    }

    // BackHandler
    BackHandler(enabled = isSettingsOpen || currentTab != MainTab.DASHBOARD) {
        if (isSettingsOpen) {
            viewModel.closeSettings()
        } else {
            viewModel.selectTab(MainTab.DASHBOARD)
        }
    }

    if (isSettingsOpen) {
        SettingsScreen(
            profile = currentFarmProfile,
            isOnline = isOnline,
            onUpdateProfile = { viewModel.updateProfile(it) },
            onConnectGoogleDrive = { viewModel.connectGoogleDriveAccount(it) },
            onDisconnectGoogleDrive = { viewModel.disconnectGoogleDrive() },
            onBackupToGoogleDrive = { viewModel.backupToGoogleDrive() },
            onRestoreFromGoogleDrive = { viewModel.restoreFromGoogleDrive() },
            onGetBackupJson = { viewModel.getBackupJsonString() },
            onRestoreFromJson = { json, cb -> viewModel.restoreFromJsonString(json, cb) },
            onClose = { viewModel.closeSettings() }
        )
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isTablet = maxWidth >= 600.dp

            Scaffold(
                topBar = {
                    TopFarmBar(
                        farmName = currentFarmProfile.farmName,
                        farmPhotoUri = currentFarmProfile.farmPhotoUri,
                        isOnline = isOnline,
                        onSettingsClick = { viewModel.openSettings() }
                    )
                },
                bottomBar = {
                    if (!isTablet) {
                        FarmBottomNavBar(
                            currentTab = currentTab,
                            lang = lang,
                            onTabSelect = { viewModel.selectTab(it) }
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (isTablet) {
                        FarmNavRail(
                            currentTab = currentTab,
                            lang = lang,
                            onTabSelect = { viewModel.selectTab(it) }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentTab) {
                            MainTab.DASHBOARD -> DashboardScreen(
                                summary = dashboardSummary,
                                selectedFilter = timeFilter,
                                onFilterSelect = { viewModel.setTimeFilter(it) },
                                onNavigateTab = { viewModel.selectTab(it) }
                            )
                            MainTab.LIVESTOCK -> LivestockScreen(
                                livestockList = livestockList,
                                populationLogs = populationLogs,
                                onAddLivestock = { name, cat, pop, startDate, notes ->
                                    viewModel.addLivestock(name, cat, pop, startDate, notes)
                                },
                                onDeleteLivestock = { viewModel.deleteLivestock(it) },
                                onRecordMutation = { id, type, qty, reason, date, money, notes ->
                                    viewModel.recordPopulationChange(id, type, qty, reason, date, money, notes)
                                }
                            )
                            MainTab.HARVEST -> HarvestScreen(
                                harvestList = harvests,
                                livestockList = livestockList,
                                onAddHarvest = { lId, lName, date, prod, qty, unit, estPrice, notes ->
                                    viewModel.addHarvest(lId, lName, date, prod, qty, unit, estPrice, notes)
                                },
                                onDeleteHarvest = { viewModel.deleteHarvest(it) }
                            )
                            MainTab.SALES -> SalesScreen(
                                salesList = sales,
                                availableHarvests = availableHarvests,
                                onAddSale = { hId, date, prod, qty, unit, price, notes ->
                                    viewModel.addSale(hId, date, prod, qty, unit, price, notes)
                                },
                                onDeleteSale = { viewModel.deleteSale(it) }
                            )
                            MainTab.INVENTORY -> InventoryScreen(
                                items = inventoryItems,
                                logs = inventoryLogs,
                                lang = lang,
                                onAddItem = { name, cat, unit, initStock, minAlert, price, supName, supPhone, notes ->
                                    viewModel.addInventoryItem(name, cat, unit, initStock, minAlert, price, supName, supPhone, notes)
                                },
                                onUpdateItem = { viewModel.updateInventoryItem(it) },
                                onDeleteItem = { viewModel.deleteInventoryItem(it) },
                                onRecordPurchase = { itemId, date, qty, price, source, notes ->
                                    viewModel.recordInventoryPurchase(itemId, date, qty, price, source, notes)
                                },
                                onRecordUsage = { itemId, date, qty, purpose, notes ->
                                    viewModel.recordInventoryUsage(itemId, date, qty, purpose, notes)
                                }
                            )
                            MainTab.FINANCE -> FinanceScreen(
                                transactions = financeTransactions,
                                onAddTransaction = { type, cat, date, amount, desc, isOper ->
                                    viewModel.addFinanceTransaction(type, cat, date, amount, desc, isOper)
                                },
                                onDeleteTransaction = { viewModel.deleteFinanceTransaction(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FarmBottomNavBar(
    currentTab: MainTab,
    lang: String = "IN",
    onTabSelect: (MainTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 0.5.dp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(
                    Triple(MainTab.DASHBOARD, Icons.Default.Dashboard, AppStrings.tabDashboard(lang)),
                    Triple(MainTab.LIVESTOCK, Icons.Default.Pets, AppStrings.tabLivestock(lang)),
                    Triple(MainTab.HARVEST, Icons.Default.Egg, AppStrings.tabHarvest(lang)),
                    Triple(MainTab.SALES, Icons.Default.PointOfSale, AppStrings.tabSales(lang)),
                    Triple(MainTab.INVENTORY, Icons.Default.Inventory2, AppStrings.tabInventory(lang)),
                    Triple(MainTab.FINANCE, Icons.Default.MonetizationOn, AppStrings.tabFinance(lang))
                )

                tabs.forEach { (tab, icon, label) ->
                    val isSelected = currentTab == tab
                    val interactionSource = remember(tab) { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = true, radius = 28.dp),
                                onClick = { onTabSelect(tab) }
                            )
                            .padding(vertical = 3.dp)
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Compact active indicator pill
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = label,
                            fontSize = 9.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            letterSpacing = (-0.2).sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmNavRail(
    currentTab: MainTab,
    lang: String = "IN",
    onTabSelect: (MainTab) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        val tabs = listOf(
            Triple(MainTab.DASHBOARD, Icons.Default.Dashboard, AppStrings.tabDashboard(lang)),
            Triple(MainTab.LIVESTOCK, Icons.Default.Pets, AppStrings.tabLivestock(lang)),
            Triple(MainTab.HARVEST, Icons.Default.Egg, AppStrings.tabHarvest(lang)),
            Triple(MainTab.SALES, Icons.Default.PointOfSale, AppStrings.tabSales(lang)),
            Triple(MainTab.INVENTORY, Icons.Default.Inventory2, AppStrings.tabInventory(lang)),
            Triple(MainTab.FINANCE, Icons.Default.MonetizationOn, AppStrings.tabFinance(lang))
        )

        tabs.forEach { (tab, icon, label) ->
            NavigationRailItem(
                selected = currentTab == tab,
                onClick = { onTabSelect(tab) },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp) }
            )
        }
    }
}
