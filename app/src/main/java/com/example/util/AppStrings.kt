package com.example.util

object AppStrings {

    fun isEnglish(lang: String): Boolean = lang.equals("EN", ignoreCase = true)

    // Splash Screen
    fun appTitle(lang: String) = "Ternak Asisten Pro"
    fun authorCredit(lang: String) = "by Rafline Dev"
    fun splashSubtitle(lang: String) = if (isEnglish(lang)) "Modern Farm Management" else "Aplikasi Pengelolaan Peternakan Modern"
    fun skip(lang: String) = if (isEnglish(lang)) "Skip" else "Lewati"

    // Top Bar
    fun onlineStatus(lang: String) = if (isEnglish(lang)) "ONLINE" else "ONLINE"
    fun offlineStatus(lang: String) = if (isEnglish(lang)) "OFFLINE" else "OFFLINE"
    fun settings(lang: String) = if (isEnglish(lang)) "Settings & Backup" else "Pengaturan & Cadangan"

    // Tabs
    fun tabDashboard(lang: String) = if (isEnglish(lang)) "Dashboard" else "Beranda"
    fun tabLivestock(lang: String) = if (isEnglish(lang)) "Livestock" else "Ternak"
    fun tabHarvest(lang: String) = if (isEnglish(lang)) "Harvest" else "Panen"
    fun tabSales(lang: String) = if (isEnglish(lang)) "Sales" else "Penjualan"
    fun tabInventory(lang: String) = if (isEnglish(lang)) "Warehouse" else "Gudang"
    fun tabFinance(lang: String) = if (isEnglish(lang)) "Finance" else "Keuangan"

    // Time filter
    fun filterToday(lang: String) = if (isEnglish(lang)) "Today" else "Hari Ini"
    fun filterWeek(lang: String) = if (isEnglish(lang)) "This Week" else "Minggu Ini"
    fun filterMonth(lang: String) = if (isEnglish(lang)) "This Month" else "Bulan Ini"
    fun filterYear(lang: String) = if (isEnglish(lang)) "This Year" else "Tahun Ini"
    fun filterAll(lang: String) = if (isEnglish(lang)) "All Time" else "Semua"

    // Dashboard
    fun netProfit(lang: String) = if (isEnglish(lang)) "Estimated Net Profit" else "Estimasi Keuntungan Bersih"
    fun totalIncome(lang: String) = if (isEnglish(lang)) "Total Income" else "Pemasukan"
    fun totalExpense(lang: String) = if (isEnglish(lang)) "Cash Outflow" else "Pengeluaran Kas"
    fun farmActivities(lang: String) = if (isEnglish(lang)) "Farm Activities" else "Aktivitas Peternakan"
    fun activeLivestock(lang: String) = if (isEnglish(lang)) "Active Livestock" else "Ternak Aktif"
    fun totalProduction(lang: String) = if (isEnglish(lang)) "Total Production" else "Total Produksi"
    fun salesTransactions(lang: String) = if (isEnglish(lang)) "Transactions" else "Transaksi"
    fun warehouseCheck(lang: String) = if (isEnglish(lang)) "Feed & Medicine" else "Pakan & Obat"
    fun financeChart(lang: String) = if (isEnglish(lang)) "Income & Expense Chart" else "Grafik Pemasukan & Pengeluaran"
    fun quickAccess(lang: String) = if (isEnglish(lang)) "Quick Access" else "Akses Cepat"

    // Inventory & WhatsApp
    fun warehouseStock(lang: String) = if (isEnglish(lang)) "Warehouse Stock" else "Stok Gudang"
    fun stockHistory(lang: String) = if (isEnglish(lang)) "In/Out History" else "Riwayat Masuk/Keluar"
    fun lowStockWarning(count: Int, lang: String) = if (isEnglish(lang)) "Warning: $count items below minimum stock!" else "Perhatian: $count barang di bawah batas minimum stok!"
    fun orderViaWhatsApp(lang: String) = if (isEnglish(lang)) "Order via WhatsApp" else "Pesan via WhatsApp"
    fun supplierPhone(lang: String) = if (isEnglish(lang)) "Supplier WhatsApp Number" else "Nomor WhatsApp Supplier"
    fun supplierName(lang: String) = if (isEnglish(lang)) "Supplier Name" else "Nama Toko / Supplier"
    fun buyStock(lang: String) = if (isEnglish(lang)) "Buy Stock" else "Beli Stok"
    fun useStock(lang: String) = if (isEnglish(lang)) "Use" else "Pakai"
    fun addProduct(lang: String) = if (isEnglish(lang)) "Add Item" else "Tambah Barang"
    fun minAlert(lang: String) = if (isEnglish(lang)) "Min Alert" else "Batas Minimal"
    fun lowStockTag(lang: String) = if (isEnglish(lang)) "Low Stock!" else "Stok Menipis!"

    // WhatsApp Message Generator
    fun generateWhatsAppMessage(
        supplierName: String,
        itemName: String,
        currentStock: String,
        unit: String,
        lang: String
    ): String {
        return if (isEnglish(lang)) {
            val sup = if (supplierName.isNotBlank()) " $supplierName" else ""
            "Hello Supplier$sup, I would like to order more stock of $itemName. Our remaining stock is $currentStock $unit. Please let me know the availability and current price. Thank you! (Ternak Asisten Pro)"
        } else {
            val sup = if (supplierName.isNotBlank()) " $supplierName" else ""
            "Halo Supplier$sup, saya ingin memesan stok $itemName. Stok saat ini tersisa $currentStock $unit. Mohon info harga dan ketersediaannya. Terima kasih (Ternak Asisten Pro)."
        }
    }

    // Settings & About
    fun farmProfileTitle(lang: String) = if (isEnglish(lang)) "Farm Business Profile" else "Profil Usaha Peternakan"
    fun farmPhoto(lang: String) = if (isEnglish(lang)) "Farm Profile Photo" else "Foto Peternakan / Kandang"
    fun choosePhoto(lang: String) = if (isEnglish(lang)) "Change / Upload Photo" else "Pilih / Ganti Foto Profil"
    fun removePhoto(lang: String) = if (isEnglish(lang)) "Remove Photo" else "Hapus Foto"
    fun themeSelection(lang: String) = if (isEnglish(lang)) "Theme Mode" else "Pilihan Tema Tampilan"
    fun themeLight(lang: String) = if (isEnglish(lang)) "Light Theme" else "Tema Terang"
    fun themeDark(lang: String) = if (isEnglish(lang)) "Dark Theme" else "Tema Gelap"
    fun themeSystem(lang: String) = if (isEnglish(lang)) "System Default" else "Ikuti Sistem"
    fun languageSelection(lang: String) = if (isEnglish(lang)) "Language" else "Pilihan Bahasa"
    fun appVersionLabel(lang: String) = "Versi 1.1 • Ternak Asisten Pro by Rafline Dev"

    // About Application
    fun aboutAppTitle(lang: String) = if (isEnglish(lang)) "About Application" else "Tentang Aplikasi"
    fun aboutAppSubtitle(lang: String) = if (isEnglish(lang)) "App info, features & version 1.1" else "Informasi lengkap, fitur & versi 1.1"
    fun aboutAppDescription(lang: String) = if (isEnglish(lang)) {
        "Ternak Asisten Pro is a modern, simple, and practical farm management application designed for farm owners to manage livestock populations, harvests, sales, warehouse supplies with WhatsApp supplier ordering, and business finances with ease."
    } else {
        "Ternak Asisten Pro adalah aplikasi manajemen usaha peternakan modern yang sederhana, praktis, dan mudah dipahami. Dirancang khusus untuk membantu pemilik peternakan mengelola populasi ternak, hasil panen, penjualan, stok gudang pakan & obat, serta pembukuan arus kas dan laba usaha tanpa ribet."
    }
    fun aboutAppFeaturesTitle(lang: String) = if (isEnglish(lang)) "Key Features" else "Fitur Utama Peternakan"
    fun aboutAppVersionBadge(lang: String) = "Versi 1.1"
    fun aboutAppDeveloper(lang: String) = "by Rafline Dev"
    fun aboutAppWhatsNewTitle(lang: String) = if (isEnglish(lang)) "What's New in v1.1" else "Pembaruan Versi 1.1"
    fun close(lang: String) = if (isEnglish(lang)) "Close" else "Tutup"
}
