package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farm_profile")
data class FarmProfile(
    @PrimaryKey val id: Int = 1,
    val farmName: String = "Peternakan Berkah",
    val ownerName: String = "Pemilik Usaha",
    val businessType: String = "Ayam Petelur & Pedaging",
    val phoneOrAddress: String = "",
    val farmPhotoUri: String = "", // Local URI or path for farm profile picture
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val appLanguage: String = "IN", // "IN", "EN"
    val currency: String = "Rp",
    val googleDriveEmail: String = "",
    val lastBackupDate: Long = 0L,
    val lastBackupFileSize: Long = 0L,
    val lastBackupStatus: String = ""
)

@Entity(tableName = "livestock")
data class Livestock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Ayam Petelur, Ayam Pedaging, Bebek, Puyuh, Kambing, Sapi, Ikan, Lainnya
    val initialPopulation: Int,
    val currentPopulation: Int,
    val startDate: Long,
    val notes: String = ""
)

@Entity(tableName = "population_log")
data class PopulationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val livestockId: Long,
    val livestockName: String,
    val date: Long,
    val type: String, // "MASUK" or "KELUAR"
    val quantity: Int,
    val reason: String, // "Beli Baru", "Tetas", "Lahir", "Mati", "Afkir", "Jual", "Lainnya"
    val costOrIncome: Long = 0L,
    val notes: String = ""
)

@Entity(tableName = "harvest")
data class Harvest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val livestockId: Long = 0,
    val livestockName: String,
    val date: Long,
    val productName: String, // "Telur", "Ayam Siap Jual", "Daging", "Susu", "Ikan", etc.
    val quantity: Double,
    val remainingQuantity: Double, // Stock available for sales
    val unit: String, // "Butir", "Kg", "Ekor", "Liter", etc.
    val estimatedPricePerUnit: Long = 0L,
    val notes: String = ""
)

@Entity(tableName = "sale")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val harvestId: Long? = null,
    val date: Long,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Long,
    val totalPrice: Long,
    val buyerNotes: String = "",
    val financeTransactionId: Long? = null
)

@Entity(tableName = "inventory_item")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Pakan", "Obat", "Vitamin", "Perlengkapan"
    val unit: String, // "Karung", "Kg", "Botol", "Sachet", "Pcs", "Liter"
    val currentStock: Double,
    val minimumStockAlert: Double = 5.0,
    val averagePricePerUnit: Long = 0L,
    val supplierName: String = "",
    val supplierPhone: String = "", // WhatsApp number for quick reorder
    val notes: String = ""
)

@Entity(tableName = "inventory_log")
data class InventoryLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val itemName: String,
    val date: Long,
    val type: String, // "MASUK" (Pembelian) or "KELUAR" (Penggunaan)
    val quantity: Double,
    val unitPrice: Long = 0L,
    val totalCost: Long = 0L,
    val purposeOrSource: String = "",
    val notes: String = "",
    val financeTransactionId: Long? = null
)

@Entity(tableName = "finance_transaction")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "PEMASUKAN" or "PENGELUARAN"
    val category: String, // Pakan, Obat, Vitamin, Gaji, Listrik, Air, Penjualan, etc.
    val date: Long,
    val amount: Long,
    val description: String,
    val isOperationalExpense: Boolean = true,
    val sourceType: String = "MANUAL", // "PENJUALAN", "BELI_STOK", "BELI_TERNAK", "MANUAL"
    val sourceId: Long? = null
)
