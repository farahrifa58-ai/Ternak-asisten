package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.FarmProfile
import com.example.data.entity.FinanceTransaction
import com.example.data.entity.Harvest
import com.example.data.entity.InventoryItem
import com.example.data.entity.InventoryLog
import com.example.data.entity.Livestock
import com.example.data.entity.PopulationLog
import com.example.data.entity.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface FarmDao {

    @Query("SELECT * FROM farm_profile WHERE id = 1 LIMIT 1")
    fun getFarmProfile(): Flow<FarmProfile?>

    @Query("SELECT * FROM farm_profile WHERE id = 1 LIMIT 1")
    suspend fun getFarmProfileOnce(): FarmProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFarmProfile(profile: FarmProfile)

    @Query("SELECT * FROM livestock ORDER BY name ASC")
    fun getAllLivestock(): Flow<List<Livestock>>

    @Query("SELECT * FROM livestock ORDER BY name ASC")
    suspend fun getAllLivestockOnce(): List<Livestock>

    @Query("SELECT * FROM livestock WHERE id = :id LIMIT 1")
    suspend fun getLivestockById(id: Long): Livestock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLivestock(livestock: Livestock): Long

    @Update
    suspend fun updateLivestock(livestock: Livestock)

    @Query("DELETE FROM livestock WHERE id = :id")
    suspend fun deleteLivestock(id: Long)

    @Query("SELECT * FROM population_log ORDER BY date DESC, id DESC")
    fun getAllPopulationLogs(): Flow<List<PopulationLog>>

    @Query("SELECT * FROM population_log ORDER BY date DESC, id DESC")
    suspend fun getAllPopulationLogsOnce(): List<PopulationLog>

    @Query("SELECT * FROM population_log WHERE livestockId = :livestockId ORDER BY date DESC")
    fun getPopulationLogsForLivestock(livestockId: Long): Flow<List<PopulationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPopulationLog(log: PopulationLog): Long

    @Query("DELETE FROM population_log WHERE id = :id")
    suspend fun deletePopulationLog(id: Long)

    @Query("SELECT * FROM harvest ORDER BY date DESC, id DESC")
    fun getAllHarvests(): Flow<List<Harvest>>

    @Query("SELECT * FROM harvest ORDER BY date DESC, id DESC")
    suspend fun getAllHarvestsOnce(): List<Harvest>

    @Query("SELECT * FROM harvest WHERE remainingQuantity > 0 ORDER BY date DESC")
    fun getAvailableHarvestsForSale(): Flow<List<Harvest>>

    @Query("SELECT * FROM harvest WHERE id = :id LIMIT 1")
    suspend fun getHarvestById(id: Long): Harvest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: Harvest): Long

    @Update
    suspend fun updateHarvest(harvest: Harvest)

    @Query("DELETE FROM harvest WHERE id = :id")
    suspend fun deleteHarvest(id: Long)

    @Query("SELECT * FROM sale ORDER BY date DESC, id DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sale ORDER BY date DESC, id DESC")
    suspend fun getAllSalesOnce(): List<Sale>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Query("DELETE FROM sale WHERE id = :id")
    suspend fun deleteSale(id: Long)

    @Query("SELECT * FROM inventory_item ORDER BY category ASC, name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_item ORDER BY category ASC, name ASC")
    suspend fun getAllInventoryItemsOnce(): List<InventoryItem>

    @Query("SELECT * FROM inventory_item WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemById(id: Long): InventoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem): Long

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Query("DELETE FROM inventory_item WHERE id = :id")
    suspend fun deleteInventoryItem(id: Long)

    @Query("SELECT * FROM inventory_log ORDER BY date DESC, id DESC")
    fun getAllInventoryLogs(): Flow<List<InventoryLog>>

    @Query("SELECT * FROM inventory_log ORDER BY date DESC, id DESC")
    suspend fun getAllInventoryLogsOnce(): List<InventoryLog>

    @Query("SELECT * FROM inventory_log WHERE itemId = :itemId ORDER BY date DESC")
    fun getInventoryLogsForItem(itemId: Long): Flow<List<InventoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryLog(log: InventoryLog): Long

    @Query("DELETE FROM inventory_log WHERE id = :id")
    suspend fun deleteInventoryLog(id: Long)

    @Query("SELECT * FROM finance_transaction ORDER BY date DESC, id DESC")
    fun getAllFinanceTransactions(): Flow<List<FinanceTransaction>>

    @Query("SELECT * FROM finance_transaction ORDER BY date DESC, id DESC")
    suspend fun getAllFinanceTransactionsOnce(): List<FinanceTransaction>

    @Query("SELECT * FROM finance_transaction WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getFinanceTransactionsBetween(startDate: Long, endDate: Long): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinanceTransaction(transaction: FinanceTransaction): Long

    @Query("DELETE FROM finance_transaction WHERE id = :id")
    suspend fun deleteFinanceTransaction(id: Long)

    @Query("DELETE FROM finance_transaction WHERE sourceType = :sourceType AND sourceId = :sourceId")
    suspend fun deleteFinanceTransactionBySource(sourceType: String, sourceId: Long)

    @Query("DELETE FROM livestock")
    suspend fun clearAllLivestock()

    @Query("DELETE FROM population_log")
    suspend fun clearAllPopulationLogs()

    @Query("DELETE FROM harvest")
    suspend fun clearAllHarvests()

    @Query("DELETE FROM sale")
    suspend fun clearAllSales()

    @Query("DELETE FROM inventory_item")
    suspend fun clearAllInventoryItems()

    @Query("DELETE FROM inventory_log")
    suspend fun clearAllInventoryLogs()

    @Query("DELETE FROM finance_transaction")
    suspend fun clearAllFinance()
}
