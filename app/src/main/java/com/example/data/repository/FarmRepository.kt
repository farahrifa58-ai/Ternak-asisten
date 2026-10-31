package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.AppDatabase
import com.example.data.dao.FarmDao
import com.example.data.entity.FarmProfile
import com.example.data.entity.FinanceTransaction
import com.example.data.entity.Harvest
import com.example.data.entity.InventoryItem
import com.example.data.entity.InventoryLog
import com.example.data.entity.Livestock
import com.example.data.entity.PopulationLog
import com.example.data.entity.Sale
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FarmRepository(
    private val database: AppDatabase,
    private val dao: FarmDao = database.farmDao()
) {

    val farmProfile: Flow<FarmProfile?> = dao.getFarmProfile()
    val allLivestock: Flow<List<Livestock>> = dao.getAllLivestock()
    val allPopulationLogs: Flow<List<PopulationLog>> = dao.getAllPopulationLogs()
    val allHarvests: Flow<List<Harvest>> = dao.getAllHarvests()
    val availableHarvests: Flow<List<Harvest>> = dao.getAvailableHarvestsForSale()
    val allSales: Flow<List<Sale>> = dao.getAllSales()
    val allInventoryItems: Flow<List<InventoryItem>> = dao.getAllInventoryItems()
    val allInventoryLogs: Flow<List<InventoryLog>> = dao.getAllInventoryLogs()
    val allFinanceTransactions: Flow<List<FinanceTransaction>> = dao.getAllFinanceTransactions()

    suspend fun ensureInitialProfile() {
        val existing = dao.getFarmProfileOnce()
        if (existing == null) {
            dao.saveFarmProfile(
                FarmProfile(
                    id = 1,
                    farmName = "Peternakan Mandiri Berkah",
                    ownerName = "Pemilik Peternakan",
                    businessType = "Ayam Petelur & Pedaging",
                    phoneOrAddress = "Indonesia",
                    currency = "Rp"
                )
            )
        }
    }

    suspend fun updateFarmProfile(profile: FarmProfile) {
        dao.saveFarmProfile(profile.copy(id = 1))
    }

    suspend fun addLivestock(
        name: String,
        category: String,
        initialPopulation: Int,
        startDate: Long,
        notes: String
    ): Long {
        return database.withTransaction {
            val livestock = Livestock(
                name = name.trim(),
                category = category,
                initialPopulation = initialPopulation,
                currentPopulation = initialPopulation,
                startDate = startDate,
                notes = notes.trim()
            )
            val id = dao.insertLivestock(livestock)
            if (initialPopulation > 0) {
                dao.insertPopulationLog(
                    PopulationLog(
                        livestockId = id,
                        livestockName = livestock.name,
                        date = startDate,
                        type = "MASUK",
                        quantity = initialPopulation,
                        reason = "Populasi Awal",
                        notes = "Pendataan awal ternak"
                    )
                )
            }
            id
        }
    }

    suspend fun updateLivestockInfo(livestock: Livestock) {
        dao.updateLivestock(livestock)
    }

    suspend fun deleteLivestock(id: Long) {
        database.withTransaction {
            dao.deleteLivestock(id)
        }
    }

    suspend fun recordPopulationChange(
        livestockId: Long,
        type: String,
        quantity: Int,
        reason: String,
        date: Long,
        costOrIncome: Long = 0L,
        notes: String = ""
    ) {
        database.withTransaction {
            val livestock = dao.getLivestockById(livestockId) ?: return@withTransaction
            val newPop = if (type == "MASUK") {
                livestock.currentPopulation + quantity
            } else {
                maxOf(0, livestock.currentPopulation - quantity)
            }
            dao.updateLivestock(livestock.copy(currentPopulation = newPop))

            val logId = dao.insertPopulationLog(
                PopulationLog(
                    livestockId = livestockId,
                    livestockName = livestock.name,
                    date = date,
                    type = type,
                    quantity = quantity,
                    reason = reason,
                    costOrIncome = costOrIncome,
                    notes = notes
                )
            )

            if (costOrIncome > 0) {
                if (type == "MASUK") {
                    dao.insertFinanceTransaction(
                        FinanceTransaction(
                            type = "PENGELUARAN",
                            category = "Pembelian Ternak",
                            date = date,
                            amount = costOrIncome,
                            description = "Beli ${livestock.name} ($quantity ekor - $reason)",
                            isOperationalExpense = true,
                            sourceType = "BELI_TERNAK",
                            sourceId = logId
                        )
                    )
                } else if (reason.contains("Jual", ignoreCase = true)) {
                    dao.insertFinanceTransaction(
                        FinanceTransaction(
                            type = "PEMASUKAN",
                            category = "Penjualan Ternak",
                            date = date,
                            amount = costOrIncome,
                            description = "Penjualan ${livestock.name} ($quantity ekor)",
                            isOperationalExpense = true,
                            sourceType = "JUAL_TERNAK",
                            sourceId = logId
                        )
                    )
                }
            }
        }
    }

    suspend fun addHarvest(
        livestockId: Long,
        livestockName: String,
        date: Long,
        productName: String,
        quantity: Double,
        unit: String,
        estimatedPricePerUnit: Long,
        notes: String
    ): Long {
        val harvest = Harvest(
            livestockId = livestockId,
            livestockName = livestockName,
            date = date,
            productName = productName.trim(),
            quantity = quantity,
            remainingQuantity = quantity,
            unit = unit.trim(),
            estimatedPricePerUnit = estimatedPricePerUnit,
            notes = notes.trim()
        )
        return dao.insertHarvest(harvest)
    }

    suspend fun deleteHarvest(id: Long) {
        dao.deleteHarvest(id)
    }

    suspend fun addSale(
        harvestId: Long?,
        date: Long,
        productName: String,
        quantity: Double,
        unit: String,
        unitPrice: Long,
        buyerNotes: String
    ): Long {
        return database.withTransaction {
            val totalPrice = (quantity * unitPrice).toLong()

            if (harvestId != null && harvestId > 0) {
                val harvest = dao.getHarvestById(harvestId)
                if (harvest != null) {
                    val updatedRemaining = maxOf(0.0, harvest.remainingQuantity - quantity)
                    dao.updateHarvest(harvest.copy(remainingQuantity = updatedRemaining))
                }
            }

            val financeId = dao.insertFinanceTransaction(
                FinanceTransaction(
                    type = "PEMASUKAN",
                    category = "Penjualan Hasil Panen",
                    date = date,
                    amount = totalPrice,
                    description = "Penjualan $productName ($quantity $unit @ Rp$unitPrice)",
                    isOperationalExpense = false,
                    sourceType = "PENJUALAN"
                )
            )

            val sale = Sale(
                harvestId = harvestId,
                date = date,
                productName = productName.trim(),
                quantity = quantity,
                unit = unit.trim(),
                unitPrice = unitPrice,
                totalPrice = totalPrice,
                buyerNotes = buyerNotes.trim(),
                financeTransactionId = financeId
            )
            dao.insertSale(sale)
        }
    }

    suspend fun deleteSale(sale: Sale) {
        database.withTransaction {
            if (sale.harvestId != null && sale.harvestId > 0) {
                val harvest = dao.getHarvestById(sale.harvestId)
                if (harvest != null) {
                    dao.updateHarvest(harvest.copy(remainingQuantity = harvest.remainingQuantity + sale.quantity))
                }
            }
            if (sale.financeTransactionId != null) {
                dao.deleteFinanceTransaction(sale.financeTransactionId)
            }
            dao.deleteSale(sale.id)
        }
    }

    suspend fun addInventoryItem(
        name: String,
        category: String,
        unit: String,
        initialStock: Double,
        minimumStockAlert: Double,
        pricePerUnit: Long,
        supplierName: String = "",
        supplierPhone: String = "",
        notes: String = ""
    ): Long {
        val item = InventoryItem(
            name = name.trim(),
            category = category,
            unit = unit.trim(),
            currentStock = initialStock,
            minimumStockAlert = minimumStockAlert,
            averagePricePerUnit = pricePerUnit,
            supplierName = supplierName.trim(),
            supplierPhone = supplierPhone.trim(),
            notes = notes.trim()
        )
        return dao.insertInventoryItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        dao.updateInventoryItem(item)
    }

    suspend fun deleteInventoryItem(id: Long) {
        dao.deleteInventoryItem(id)
    }

    suspend fun recordInventoryPurchase(
        itemId: Long,
        date: Long,
        quantity: Double,
        unitPrice: Long,
        sourceOrSupplier: String,
        notes: String
    ) {
        database.withTransaction {
            val item = dao.getInventoryItemById(itemId) ?: return@withTransaction
            val totalCost = (quantity * unitPrice).toLong()
            val newStock = item.currentStock + quantity
            dao.updateInventoryItem(
                item.copy(
                    currentStock = newStock,
                    averagePricePerUnit = if (unitPrice > 0) unitPrice else item.averagePricePerUnit
                )
            )

            val financeId = dao.insertFinanceTransaction(
                FinanceTransaction(
                    type = "PENGELUARAN",
                    category = item.category,
                    date = date,
                    amount = totalCost,
                    description = "Beli stok ${item.name} ($quantity ${item.unit})",
                    isOperationalExpense = false,
                    sourceType = "BELI_STOK"
                )
            )

            dao.insertInventoryLog(
                InventoryLog(
                    itemId = itemId,
                    itemName = item.name,
                    date = date,
                    type = "MASUK",
                    quantity = quantity,
                    unitPrice = unitPrice,
                    totalCost = totalCost,
                    purposeOrSource = sourceOrSupplier.trim(),
                    notes = notes.trim(),
                    financeTransactionId = financeId
                )
            )
        }
    }

    suspend fun recordInventoryUsage(
        itemId: Long,
        date: Long,
        quantity: Double,
        purpose: String,
        notes: String
    ) {
        database.withTransaction {
            val item = dao.getInventoryItemById(itemId) ?: return@withTransaction
            val newStock = maxOf(0.0, item.currentStock - quantity)
            dao.updateInventoryItem(item.copy(currentStock = newStock))

            val unitCost = item.averagePricePerUnit
            val totalOperationalCost = (quantity * unitCost).toLong()

            val financeId = if (totalOperationalCost > 0) {
                dao.insertFinanceTransaction(
                    FinanceTransaction(
                        type = "PENGELUARAN",
                        category = item.category,
                        date = date,
                        amount = totalOperationalCost,
                        description = "Pemakaian ${item.name} ($quantity ${item.unit} untuk $purpose)",
                        isOperationalExpense = true,
                        sourceType = "PAKAI_STOK"
                    )
                )
            } else null

            dao.insertInventoryLog(
                InventoryLog(
                    itemId = itemId,
                    itemName = item.name,
                    date = date,
                    type = "KELUAR",
                    quantity = quantity,
                    unitPrice = unitCost,
                    totalCost = totalOperationalCost,
                    purposeOrSource = purpose.trim(),
                    notes = notes.trim(),
                    financeTransactionId = financeId
                )
            )
        }
    }

    suspend fun addFinanceTransaction(
        type: String,
        category: String,
        date: Long,
        amount: Long,
        description: String,
        isOperationalExpense: Boolean = true
    ): Long {
        val transaction = FinanceTransaction(
            type = type,
            category = category,
            date = date,
            amount = amount,
            description = description.trim(),
            isOperationalExpense = isOperationalExpense,
            sourceType = "MANUAL"
        )
        return dao.insertFinanceTransaction(transaction)
    }

    suspend fun deleteFinanceTransaction(id: Long) {
        dao.deleteFinanceTransaction(id)
    }

    suspend fun exportBackupJson(): String {
        val profile = dao.getFarmProfileOnce() ?: FarmProfile()
        val livestock = dao.getAllLivestockOnce()
        val popLogs = dao.getAllPopulationLogsOnce()
        val harvests = dao.getAllHarvestsOnce()
        val sales = dao.getAllSalesOnce()
        val items = dao.getAllInventoryItemsOnce()
        val itemLogs = dao.getAllInventoryLogsOnce()
        val finance = dao.getAllFinanceTransactionsOnce()

        val root = JSONObject()
        root.put("appName", "TERNAK ASISTEN PRO")
        root.put("version", "1.0")
        root.put("timestamp", System.currentTimeMillis())
        root.put("formattedDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        val pObj = JSONObject().apply {
            put("farmName", profile.farmName)
            put("ownerName", profile.ownerName)
            put("businessType", profile.businessType)
            put("phoneOrAddress", profile.phoneOrAddress)
            put("farmPhotoUri", profile.farmPhotoUri)
            put("themeMode", profile.themeMode)
            put("appLanguage", profile.appLanguage)
            put("currency", profile.currency)
        }
        root.put("farmProfile", pObj)

        val lsArr = JSONArray()
        livestock.forEach { l ->
            lsArr.put(JSONObject().apply {
                put("id", l.id)
                put("name", l.name)
                put("category", l.category)
                put("initialPopulation", l.initialPopulation)
                put("currentPopulation", l.currentPopulation)
                put("startDate", l.startDate)
                put("notes", l.notes)
            })
        }
        root.put("livestock", lsArr)

        val plArr = JSONArray()
        popLogs.forEach { pl ->
            plArr.put(JSONObject().apply {
                put("id", pl.id)
                put("livestockId", pl.livestockId)
                put("livestockName", pl.livestockName)
                put("date", pl.date)
                put("type", pl.type)
                put("quantity", pl.quantity)
                put("reason", pl.reason)
                put("costOrIncome", pl.costOrIncome)
                put("notes", pl.notes)
            })
        }
        root.put("populationLogs", plArr)

        val hArr = JSONArray()
        harvests.forEach { h ->
            hArr.put(JSONObject().apply {
                put("id", h.id)
                put("livestockId", h.livestockId)
                put("livestockName", h.livestockName)
                put("date", h.date)
                put("productName", h.productName)
                put("quantity", h.quantity)
                put("remainingQuantity", h.remainingQuantity)
                put("unit", h.unit)
                put("estimatedPricePerUnit", h.estimatedPricePerUnit)
                put("notes", h.notes)
            })
        }
        root.put("harvests", hArr)

        val sArr = JSONArray()
        sales.forEach { s ->
            sArr.put(JSONObject().apply {
                put("id", s.id)
                put("harvestId", s.harvestId ?: -1L)
                put("date", s.date)
                put("productName", s.productName)
                put("quantity", s.quantity)
                put("unit", s.unit)
                put("unitPrice", s.unitPrice)
                put("totalPrice", s.totalPrice)
                put("buyerNotes", s.buyerNotes)
                put("financeTransactionId", s.financeTransactionId ?: -1L)
            })
        }
        root.put("sales", sArr)

        val iiArr = JSONArray()
        items.forEach { it ->
            iiArr.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("category", it.category)
                put("unit", it.unit)
                put("currentStock", it.currentStock)
                put("minimumStockAlert", it.minimumStockAlert)
                put("averagePricePerUnit", it.averagePricePerUnit)
                put("supplierName", it.supplierName)
                put("supplierPhone", it.supplierPhone)
                put("notes", it.notes)
            })
        }
        root.put("inventoryItems", iiArr)

        val ilArr = JSONArray()
        itemLogs.forEach { il ->
            ilArr.put(JSONObject().apply {
                put("id", il.id)
                put("itemId", il.itemId)
                put("itemName", il.itemName)
                put("date", il.date)
                put("type", il.type)
                put("quantity", il.quantity)
                put("unitPrice", il.unitPrice)
                put("totalCost", il.totalCost)
                put("purposeOrSource", il.purposeOrSource)
                put("notes", il.notes)
                put("financeTransactionId", il.financeTransactionId ?: -1L)
            })
        }
        root.put("inventoryLogs", ilArr)

        val fArr = JSONArray()
        finance.forEach { f ->
            fArr.put(JSONObject().apply {
                put("id", f.id)
                put("type", f.type)
                put("category", f.category)
                put("date", f.date)
                put("amount", f.amount)
                put("description", f.description)
                put("isOperationalExpense", f.isOperationalExpense)
                put("sourceType", f.sourceType)
                put("sourceId", f.sourceId ?: -1L)
            })
        }
        root.put("financeTransactions", fArr)

        return root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<String> {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("version") && !root.has("farmProfile") && !root.has("livestock")) {
                return Result.failure(IllegalArgumentException("Format file backup tidak valid untuk Ternak Asisten Pro"))
            }

            val profileObj = root.optJSONObject("farmProfile")
            val newProfile = if (profileObj != null) {
                FarmProfile(
                    id = 1,
                    farmName = profileObj.optString("farmName", "Peternakan Berkah"),
                    ownerName = profileObj.optString("ownerName", "Pemilik Usaha"),
                    businessType = profileObj.optString("businessType", "Peternakan"),
                    phoneOrAddress = profileObj.optString("phoneOrAddress", ""),
                    farmPhotoUri = profileObj.optString("farmPhotoUri", ""),
                    themeMode = profileObj.optString("themeMode", "SYSTEM"),
                    appLanguage = profileObj.optString("appLanguage", "IN"),
                    currency = profileObj.optString("currency", "Rp")
                )
            } else null

            val livestockList = mutableListOf<Livestock>()
            root.optJSONArray("livestock")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    livestockList.add(
                        Livestock(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            category = obj.optString("category", "Lainnya"),
                            initialPopulation = obj.optInt("initialPopulation", 0),
                            currentPopulation = obj.optInt("currentPopulation", 0),
                            startDate = obj.optLong("startDate", System.currentTimeMillis()),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            val popLogs = mutableListOf<PopulationLog>()
            root.optJSONArray("populationLogs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    popLogs.add(
                        PopulationLog(
                            id = obj.optLong("id", 0L),
                            livestockId = obj.optLong("livestockId", 0L),
                            livestockName = obj.optString("livestockName", ""),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            type = obj.optString("type", "MASUK"),
                            quantity = obj.optInt("quantity", 0),
                            reason = obj.optString("reason", ""),
                            costOrIncome = obj.optLong("costOrIncome", 0L),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            val harvests = mutableListOf<Harvest>()
            root.optJSONArray("harvests")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    harvests.add(
                        Harvest(
                            id = obj.optLong("id", 0L),
                            livestockId = obj.optLong("livestockId", 0L),
                            livestockName = obj.optString("livestockName", ""),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            productName = obj.getString("productName"),
                            quantity = obj.optDouble("quantity", 0.0),
                            remainingQuantity = obj.optDouble("remainingQuantity", obj.optDouble("quantity", 0.0)),
                            unit = obj.optString("unit", "Kg"),
                            estimatedPricePerUnit = obj.optLong("estimatedPricePerUnit", 0L),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            val sales = mutableListOf<Sale>()
            root.optJSONArray("sales")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    sales.add(
                        Sale(
                            id = obj.optLong("id", 0L),
                            harvestId = obj.optLong("harvestId").takeIf { it > 0 },
                            date = obj.optLong("date", System.currentTimeMillis()),
                            productName = obj.getString("productName"),
                            quantity = obj.optDouble("quantity", 0.0),
                            unit = obj.optString("unit", "Kg"),
                            unitPrice = obj.optLong("unitPrice", 0L),
                            totalPrice = obj.optLong("totalPrice", 0L),
                            buyerNotes = obj.optString("buyerNotes", ""),
                            financeTransactionId = obj.optLong("financeTransactionId").takeIf { it > 0 }
                        )
                    )
                }
            }

            val items = mutableListOf<InventoryItem>()
            root.optJSONArray("inventoryItems")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    items.add(
                        InventoryItem(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            category = obj.optString("category", "Pakan"),
                            unit = obj.optString("unit", "Karung"),
                            currentStock = obj.optDouble("currentStock", 0.0),
                            minimumStockAlert = obj.optDouble("minimumStockAlert", 5.0),
                            averagePricePerUnit = obj.optLong("averagePricePerUnit", 0L),
                            supplierName = obj.optString("supplierName", ""),
                            supplierPhone = obj.optString("supplierPhone", ""),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            val itemLogs = mutableListOf<InventoryLog>()
            root.optJSONArray("inventoryLogs")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    itemLogs.add(
                        InventoryLog(
                            id = obj.optLong("id", 0L),
                            itemId = obj.optLong("itemId", 0L),
                            itemName = obj.optString("itemName", ""),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            type = obj.optString("type", "MASUK"),
                            quantity = obj.optDouble("quantity", 0.0),
                            unitPrice = obj.optLong("unitPrice", 0L),
                            totalCost = obj.optLong("totalCost", 0L),
                            purposeOrSource = obj.optString("purposeOrSource", ""),
                            notes = obj.optString("notes", ""),
                            financeTransactionId = obj.optLong("financeTransactionId").takeIf { it > 0 }
                        )
                    )
                }
            }

            val financeList = mutableListOf<FinanceTransaction>()
            root.optJSONArray("financeTransactions")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    financeList.add(
                        FinanceTransaction(
                            id = obj.optLong("id", 0L),
                            type = obj.optString("type", "PENGELUARAN"),
                            category = obj.optString("category", "Lain-lain"),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            amount = obj.optLong("amount", 0L),
                            description = obj.optString("description", ""),
                            isOperationalExpense = obj.optBoolean("isOperationalExpense", true),
                            sourceType = obj.optString("sourceType", "MANUAL"),
                            sourceId = obj.optLong("sourceId").takeIf { it > 0 }
                        )
                    )
                }
            }

            database.withTransaction {
                dao.clearAllLivestock()
                dao.clearAllPopulationLogs()
                dao.clearAllHarvests()
                dao.clearAllSales()
                dao.clearAllInventoryItems()
                dao.clearAllInventoryLogs()
                dao.clearAllFinance()

                if (newProfile != null) {
                    dao.saveFarmProfile(newProfile)
                }
                livestockList.forEach { dao.insertLivestock(it) }
                popLogs.forEach { dao.insertPopulationLog(it) }
                harvests.forEach { dao.insertHarvest(it) }
                sales.forEach { dao.insertSale(it) }
                items.forEach { dao.insertInventoryItem(it) }
                itemLogs.forEach { dao.insertInventoryLog(it) }
                financeList.forEach { dao.insertFinanceTransaction(it) }
            }

            val summary = "Berhasil memulihkan ${livestockList.size} ternak, ${harvests.size} panen, ${sales.size} penjualan, ${items.size} stok gudang, dan ${financeList.size} catatan keuangan."
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
