package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.repository.FarmRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FarmRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FarmRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FarmRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAddLivestockAndMutation() = runBlocking {
        val livestockId = repository.addLivestock(
            name = "Ayam Broiler Super",
            category = "Ayam Pedaging",
            initialPopulation = 500,
            startDate = System.currentTimeMillis(),
            notes = "Kandang 1"
        )
        assertTrue(livestockId > 0)

        var list = repository.allLivestock.first()
        assertEquals(1, list.size)
        assertEquals(500, list[0].currentPopulation)

        // Mutasi keluar (afkir/jual 50 ekor)
        repository.recordPopulationChange(
            livestockId = livestockId,
            type = "KELUAR",
            quantity = 50,
            reason = "Terjual",
            date = System.currentTimeMillis(),
            costOrIncome = 1500000L,
            notes = "Dijual ke pasar"
        )

        list = repository.allLivestock.first()
        assertEquals(450, list[0].currentPopulation)

        val logs = repository.allPopulationLogs.first()
        assertEquals(2, logs.size) // 1 initial + 1 mutation

        val finance = repository.allFinanceTransactions.first()
        assertEquals(1, finance.size)
        assertEquals("PEMASUKAN", finance[0].type)
        assertEquals(1500000L, finance[0].amount)
    }

    @Test
    fun testHarvestAndSaleSync() = runBlocking {
        val harvestId = repository.addHarvest(
            livestockId = 1L,
            livestockName = "Ayam Petelur",
            date = System.currentTimeMillis(),
            productName = "Telur Ayam",
            quantity = 100.0,
            unit = "Kg",
            estimatedPricePerUnit = 25000L,
            notes = "Panen pagi"
        )
        assertTrue(harvestId > 0)

        // Jual 40 Kg dari hasil panen tersebut
        val saleId = repository.addSale(
            harvestId = harvestId,
            date = System.currentTimeMillis(),
            productName = "Telur Ayam",
            quantity = 40.0,
            unit = "Kg",
            unitPrice = 27000L,
            buyerNotes = "Toko Berkah"
        )
        assertTrue(saleId > 0)

        val harvests = repository.allHarvests.first()
        assertEquals(1, harvests.size)
        assertEquals(100.0, harvests[0].quantity, 0.001)
        assertEquals(60.0, harvests[0].remainingQuantity, 0.001) // 100 - 40 = 60

        val sales = repository.allSales.first()
        assertEquals(1, sales.size)
        assertEquals(1080000L, sales[0].totalPrice) // 40 * 27000 = 1080000

        val finance = repository.allFinanceTransactions.first()
        assertEquals(1, finance.size)
        assertEquals(1080000L, finance[0].amount)
        assertEquals("PEMASUKAN", finance[0].type)
    }

    @Test
    fun testBackupAndRestore() = runBlocking {
        repository.ensureInitialProfile()
        repository.addLivestock("Bebek Petelur", "Bebek", 120, System.currentTimeMillis(), "Kolam A")
        repository.addInventoryItem("Pakan Konsentrat", "Pakan", "Karung", 20.0, 5.0, 450000L, "Gudang")

        val backupJson = repository.exportBackupJson()
        assertTrue(backupJson.contains("Bebek Petelur"))
        assertTrue(backupJson.contains("Pakan Konsentrat"))

        // Restore into database
        val result = repository.restoreBackupJson(backupJson)
        assertTrue(result.isSuccess)

        val livestock = repository.allLivestock.first()
        assertEquals(1, livestock.size)
        assertEquals("Bebek Petelur", livestock[0].name)
    }
}
