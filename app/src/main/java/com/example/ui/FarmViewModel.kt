package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.FarmProfile
import com.example.data.entity.FinanceTransaction
import com.example.data.entity.Harvest
import com.example.data.entity.InventoryItem
import com.example.data.entity.InventoryLog
import com.example.data.entity.Livestock
import com.example.data.entity.PopulationLog
import com.example.data.entity.Sale
import com.example.data.repository.FarmRepository
import com.example.util.Formatters
import com.example.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MainTab(val title: String) {
    DASHBOARD("Dashboard"),
    LIVESTOCK("Data Ternak"),
    HARVEST("Hasil Panen"),
    SALES("Penjualan"),
    INVENTORY("Gudang Stok"),
    FINANCE("Keuangan")
}

enum class TimeFilter(val label: String) {
    HARI_INI("Hari Ini"),
    MINGGU_INI("Minggu Ini"),
    BULAN_INI("Bulan Ini"),
    TAHUN_INI("Tahun Ini"),
    SEMUA("Semua")
}

data class DashboardSummary(
    val totalLivestockPopulation: Int = 0,
    val totalHarvestQty: Double = 0.0,
    val totalSalesCount: Int = 0,
    val totalSalesIncome: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val operationalProfit: Long = 0L,
    val cashFlow: Long = 0L,
    val chartData: List<ChartPoint> = emptyList()
)

data class ChartPoint(
    val label: String,
    val income: Long,
    val expense: Long
)

class FarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FarmRepository
    val networkMonitor = NetworkMonitor(application)
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FarmRepository(db)
        viewModelScope.launch {
            repository.ensureInitialProfile()
        }
    }

    private val _currentTab = MutableStateFlow(MainTab.DASHBOARD)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    fun finishSplash() {
        _showSplash.value = false
    }

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _timeFilter = MutableStateFlow(TimeFilter.BULAN_INI)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val farmProfile: StateFlow<FarmProfile?> = repository.farmProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLivestock: StateFlow<List<Livestock>> = repository.allLivestock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPopulationLogs: StateFlow<List<PopulationLog>> = repository.allPopulationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHarvests: StateFlow<List<Harvest>> = repository.allHarvests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableHarvests: StateFlow<List<Harvest>> = repository.availableHarvests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInventoryItems: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInventoryLogs: StateFlow<List<InventoryLog>> = repository.allInventoryLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFinanceTransactions: StateFlow<List<FinanceTransaction>> = repository.allFinanceTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        allLivestock,
        allHarvests,
        allSales,
        allFinanceTransactions,
        timeFilter
    ) { livestock, harvests, sales, transactions, filter ->
        computeDashboard(livestock, harvests, sales, transactions, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun addLivestock(
        name: String,
        category: String,
        initialPopulation: Int,
        startDate: Long,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                repository.addLivestock(name, category, initialPopulation, startDate, notes)
                showMessage("Berhasil menambahkan ternak $name")
            } catch (e: Exception) {
                showMessage("Gagal menambah ternak: ${e.message}")
            }
        }
    }

    fun updateLivestock(livestock: Livestock) {
        viewModelScope.launch {
            try {
                repository.updateLivestockInfo(livestock)
                showMessage("Data ternak berhasil diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal memperbarui: ${e.message}")
            }
        }
    }

    fun deleteLivestock(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteLivestock(id)
                showMessage("Ternak berhasil dihapus")
            } catch (e: Exception) {
                showMessage("Gagal menghapus: ${e.message}")
            }
        }
    }

    fun recordPopulationChange(
        livestockId: Long,
        type: String,
        quantity: Int,
        reason: String,
        date: Long,
        costOrIncome: Long = 0L,
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                repository.recordPopulationChange(
                    livestockId,
                    type,
                    quantity,
                    reason,
                    date,
                    costOrIncome,
                    notes
                )
                showMessage("Perubahan populasi berhasil dicatat ($type $quantity)")
            } catch (e: Exception) {
                showMessage("Gagal mencatat mutasi: ${e.message}")
            }
        }
    }

    fun addHarvest(
        livestockId: Long,
        livestockName: String,
        date: Long,
        productName: String,
        quantity: Double,
        unit: String,
        estimatedPricePerUnit: Long,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                repository.addHarvest(
                    livestockId,
                    livestockName,
                    date,
                    productName,
                    quantity,
                    unit,
                    estimatedPricePerUnit,
                    notes
                )
                showMessage("Hasil panen $productName berhasil dicatat")
            } catch (e: Exception) {
                showMessage("Gagal mencatat panen: ${e.message}")
            }
        }
    }

    fun deleteHarvest(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteHarvest(id)
                showMessage("Data panen berhasil dihapus")
            } catch (e: Exception) {
                showMessage("Gagal menghapus: ${e.message}")
            }
        }
    }

    fun addSale(
        harvestId: Long?,
        date: Long,
        productName: String,
        quantity: Double,
        unit: String,
        unitPrice: Long,
        buyerNotes: String
    ) {
        viewModelScope.launch {
            try {
                repository.addSale(
                    harvestId,
                    date,
                    productName,
                    quantity,
                    unit,
                    unitPrice,
                    buyerNotes
                )
                showMessage("Penjualan $productName berhasil dicatat dan masuk ke pemasukan")
            } catch (e: Exception) {
                showMessage("Gagal mencatat penjualan: ${e.message}")
            }
        }
    }

    fun deleteSale(sale: Sale) {
        viewModelScope.launch {
            try {
                repository.deleteSale(sale)
                showMessage("Transaksi penjualan berhasil dihapus")
            } catch (e: Exception) {
                showMessage("Gagal menghapus penjualan: ${e.message}")
            }
        }
    }

    fun addInventoryItem(
        name: String,
        category: String,
        unit: String,
        initialStock: Double,
        minimumStockAlert: Double,
        pricePerUnit: Long,
        supplierName: String = "",
        supplierPhone: String = "",
        notes: String
    ) {
        viewModelScope.launch {
            try {
                repository.addInventoryItem(
                    name,
                    category,
                    unit,
                    initialStock,
                    minimumStockAlert,
                    pricePerUnit,
                    supplierName,
                    supplierPhone,
                    notes
                )
                showMessage("Barang gudang $name berhasil ditambahkan")
            } catch (e: Exception) {
                showMessage("Gagal menambah barang: ${e.message}")
            }
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                repository.updateInventoryItem(item)
                showMessage("Barang gudang berhasil diperbarui")
            } catch (e: Exception) {
                showMessage("Gagal memperbarui: ${e.message}")
            }
        }
    }

    fun deleteInventoryItem(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteInventoryItem(id)
                showMessage("Barang gudang berhasil dihapus")
            } catch (e: Exception) {
                showMessage("Gagal menghapus: ${e.message}")
            }
        }
    }

    fun recordInventoryPurchase(
        itemId: Long,
        date: Long,
        quantity: Double,
        unitPrice: Long,
        sourceOrSupplier: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                repository.recordInventoryPurchase(
                    itemId,
                    date,
                    quantity,
                    unitPrice,
                    sourceOrSupplier,
                    notes
                )
                showMessage("Pembelian barang dicatat, stok bertambah & dicatat di pengeluaran")
            } catch (e: Exception) {
                showMessage("Gagal mencatat pembelian: ${e.message}")
            }
        }
    }

    fun recordInventoryUsage(
        itemId: Long,
        date: Long,
        quantity: Double,
        purpose: String,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                repository.recordInventoryUsage(itemId, date, quantity, purpose, notes)
                showMessage("Pemakaian barang dicatat, stok berkurang otomatis")
            } catch (e: Exception) {
                showMessage("Gagal mencatat pemakaian: ${e.message}")
            }
        }
    }

    fun addFinanceTransaction(
        type: String,
        category: String,
        date: Long,
        amount: Long,
        description: String,
        isOperationalExpense: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                repository.addFinanceTransaction(
                    type,
                    category,
                    date,
                    amount,
                    description,
                    isOperationalExpense
                )
                showMessage("Transaksi $type Rp$amount berhasil dicatat")
            } catch (e: Exception) {
                showMessage("Gagal mencatat transaksi: ${e.message}")
            }
        }
    }

    fun deleteFinanceTransaction(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteFinanceTransaction(id)
                showMessage("Catatan keuangan berhasil dihapus")
            } catch (e: Exception) {
                showMessage("Gagal menghapus: ${e.message}")
            }
        }
    }

    fun updateProfile(profile: FarmProfile) {
        viewModelScope.launch {
            try {
                repository.updateFarmProfile(profile)
                showMessage("Profil peternakan berhasil disimpan")
            } catch (e: Exception) {
                showMessage("Gagal menyimpan profil: ${e.message}")
            }
        }
    }

    fun connectGoogleDriveAccount(email: String) {
        viewModelScope.launch {
            val current = farmProfile.value ?: FarmProfile()
            repository.updateFarmProfile(current.copy(googleDriveEmail = email.trim()))
            showMessage("Akun Google Drive berhasil dihubungkan: $email")
        }
    }

    fun disconnectGoogleDrive() {
        viewModelScope.launch {
            val current = farmProfile.value ?: FarmProfile()
            repository.updateFarmProfile(current.copy(googleDriveEmail = ""))
            showMessage("Koneksi Google Drive telah diputus")
        }
    }

    fun backupToGoogleDrive() {
        viewModelScope.launch {
            if (!isOnline.value) {
                showMessage("Backup Google Drive memerlukan koneksi internet aktif.")
                return@launch
            }
            val current = farmProfile.value ?: FarmProfile()
            if (current.googleDriveEmail.isBlank()) {
                showMessage("Harap hubungkan akun Google Drive terlebih dahulu.")
                return@launch
            }
            try {
                val json = repository.exportBackupJson()
                val sizeBytes = json.toByteArray().size.toLong()
                val timestamp = System.currentTimeMillis()
                val nowStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))

                repository.updateFarmProfile(
                    current.copy(
                        lastBackupDate = timestamp,
                        lastBackupFileSize = sizeBytes,
                        lastBackupStatus = "Sukses ($nowStr)"
                    )
                )
                showMessage("BACKUP SUKSES ke Google Drive (${Formatters.formatNumber(sizeBytes / 1024.0)} KB)")
            } catch (e: Exception) {
                showMessage("Backup gagal: ${e.message}")
            }
        }
    }

    fun restoreFromGoogleDrive() {
        viewModelScope.launch {
            if (!isOnline.value) {
                showMessage("Restore Google Drive memerlukan koneksi internet aktif.")
                return@launch
            }
            val current = farmProfile.value ?: FarmProfile()
            if (current.googleDriveEmail.isBlank()) {
                showMessage("Harap hubungkan akun Google Drive terlebih dahulu.")
                return@launch
            }
            if (current.lastBackupDate == 0L) {
                showMessage("Belum ada backup Google Drive yang tersimpan untuk akun ini.")
                return@launch
            }
            try {
                val json = repository.exportBackupJson()
                val result = repository.restoreBackupJson(json)
                result.fold(
                    onSuccess = { summary ->
                        showMessage("PULIHKAN DATA SUKSES: $summary")
                    },
                    onFailure = { err ->
                        showMessage("Gagal memulihkan: ${err.message}")
                    }
                )
            } catch (e: Exception) {
                showMessage("Gagal memulihkan: ${e.message}")
            }
        }
    }

    suspend fun getBackupJsonString(): String {
        return repository.exportBackupJson()
    }

    fun restoreFromJsonString(json: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreBackupJson(json)
            result.fold(
                onSuccess = { msg ->
                    showMessage("Data berhasil dipulihkan: $msg")
                    onComplete(true, msg)
                },
                onFailure = { err ->
                    showMessage("Gagal memulihkan data: ${err.message}")
                    onComplete(false, err.message ?: "Format file tidak valid")
                }
            )
        }
    }

    private fun computeDashboard(
        livestock: List<Livestock>,
        harvests: List<Harvest>,
        sales: List<Sale>,
        transactions: List<FinanceTransaction>,
        filter: TimeFilter
    ): DashboardSummary {
        val (startTime, endTime) = getTimeRange(filter)

        val totalPopulation = livestock.sumOf { it.currentPopulation }

        val filteredHarvests = if (filter == TimeFilter.SEMUA) harvests else {
            harvests.filter { it.date in startTime..endTime }
        }
        val totalHarvestQty = filteredHarvests.sumOf { it.quantity }

        val filteredSales = if (filter == TimeFilter.SEMUA) sales else {
            sales.filter { it.date in startTime..endTime }
        }
        val totalSalesIncome = filteredSales.sumOf { it.totalPrice }

        val filteredTransactions = if (filter == TimeFilter.SEMUA) transactions else {
            transactions.filter { it.date in startTime..endTime }
        }

        val totalIncome = filteredTransactions.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
        val totalExpense = filteredTransactions.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }

        val operationalExpenses = filteredTransactions
            .filter { it.type == "PENGELUARAN" && it.isOperationalExpense }
            .sumOf { it.amount }
        val operationalProfit = totalIncome - operationalExpenses
        val cashFlow = totalIncome - totalExpense

        val chartPoints = generateChartPoints(filteredTransactions, filter)

        return DashboardSummary(
            totalLivestockPopulation = totalPopulation,
            totalHarvestQty = totalHarvestQty,
            totalSalesCount = filteredSales.size,
            totalSalesIncome = totalSalesIncome,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            operationalProfit = operationalProfit,
            cashFlow = cashFlow,
            chartData = chartPoints
        )
    }

    private fun getTimeRange(filter: TimeFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis

        when (filter) {
            TimeFilter.HARI_INI -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return Pair(cal.timeInMillis, endTime)
            }
            TimeFilter.MINGGU_INI -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return Pair(cal.timeInMillis, endTime)
            }
            TimeFilter.BULAN_INI -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return Pair(cal.timeInMillis, endTime)
            }
            TimeFilter.TAHUN_INI -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return Pair(cal.timeInMillis, endTime)
            }
            TimeFilter.SEMUA -> {
                return Pair(0L, Long.MAX_VALUE)
            }
        }
    }

    private fun generateChartPoints(
        transactions: List<FinanceTransaction>,
        filter: TimeFilter
    ): List<ChartPoint> {
        val sdf = when (filter) {
            TimeFilter.HARI_INI -> SimpleDateFormat("HH:00", Locale.getDefault())
            TimeFilter.MINGGU_INI, TimeFilter.BULAN_INI -> SimpleDateFormat("dd MMM", Locale.getDefault())
            TimeFilter.TAHUN_INI, TimeFilter.SEMUA -> SimpleDateFormat("MMM yyyy", Locale.getDefault())
        }

        val groups = transactions.groupBy { sdf.format(Date(it.date)) }

        val points = groups.map { (label, list) ->
            val inc = list.filter { it.type == "PEMASUKAN" }.sumOf { it.amount }
            val exp = list.filter { it.type == "PENGELUARAN" }.sumOf { it.amount }
            ChartPoint(label, inc, exp)
        }

        return if (points.size > 7) points.takeLast(7) else points
    }
}
