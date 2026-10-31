package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.entity.FarmProfile
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.util.AppStrings
import com.example.util.Formatters
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    profile: FarmProfile,
    isOnline: Boolean,
    onUpdateProfile: (FarmProfile) -> Unit,
    onConnectGoogleDrive: (String) -> Unit,
    onDisconnectGoogleDrive: () -> Unit,
    onBackupToGoogleDrive: () -> Unit,
    onRestoreFromGoogleDrive: () -> Unit,
    onGetBackupJson: suspend () -> String,
    onRestoreFromJson: (String, (Boolean, String) -> Unit) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var farmName by remember(profile) { mutableStateOf(profile.farmName) }
    var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
    var businessType by remember(profile) { mutableStateOf(profile.businessType) }
    var phoneAddress by remember(profile) { mutableStateOf(profile.phoneOrAddress) }
    var photoUri by remember(profile) { mutableStateOf(profile.farmPhotoUri) }
    var selectedTheme by remember(profile) { mutableStateOf(profile.themeMode) }
    var selectedLang by remember(profile) { mutableStateOf(profile.appLanguage) }

    var showGoogleConnectDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showLocalRestoreDialog by remember { mutableStateOf(false) }
    var showAboutAppDialog by remember { mutableStateOf(false) }

    val isDriveConnected = profile.googleDriveEmail.isNotBlank()
    val isEn = AppStrings.isEnglish(selectedLang)

    // Photo picker launcher (Zero-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Copy selected image to internal storage so URI persists across reboots
                val inputStream = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "farm_profile_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(targetFile)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                val savedPath = targetFile.absolutePath
                photoUri = savedPath
                onUpdateProfile(profile.copy(farmPhotoUri = savedPath))
            } catch (_: Exception) {
                photoUri = uri.toString()
                onUpdateProfile(profile.copy(farmPhotoUri = uri.toString()))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppStrings.settings(selectedLang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("btn_close_settings")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profil Usaha Peternakan & Upload Foto
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.farmProfileTitle(selectedLang),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Foto Profil Peternakan Display & Upload Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (photoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = photoUri,
                                        contentDescription = "Foto Profil Peternakan",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.app_logo),
                                        contentDescription = "Logo Standar",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.farmPhoto(selectedLang),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isEn) "Pick Photo" else "Pilih Foto", fontSize = 11.sp)
                                    }

                                    if (photoUri.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                photoUri = ""
                                                onUpdateProfile(profile.copy(farmPhotoUri = ""))
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = ExpenseRed)
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = farmName,
                            onValueChange = { farmName = it },
                            label = { Text(if (isEn) "Farm Name" else "Nama Peternakan") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text(if (isEn) "Owner Name" else "Nama Pemilik") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = businessType,
                            onValueChange = { businessType = it },
                            label = { Text(if (isEn) "Primary Livestock Type (e.g. Broiler & Layer)" else "Jenis Usaha Utama (Contoh: Ayam Broiler & Telur)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phoneAddress,
                            onValueChange = { phoneAddress = it },
                            label = { Text(if (isEn) "Contact / Farm Location" else "Kontak / Lokasi Kandang") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                onUpdateProfile(
                                    profile.copy(
                                        farmName = farmName.trim(),
                                        ownerName = ownerName.trim(),
                                        businessType = businessType.trim(),
                                        phoneOrAddress = phoneAddress.trim(),
                                        farmPhotoUri = photoUri,
                                        themeMode = selectedTheme,
                                        appLanguage = selectedLang
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEn) "Save Profile" else "Simpan Perubahan Profil", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Pengaturan Tema Terang & Gelap
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SettingsBrightness,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.themeSelection(selectedLang),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionButton(
                                label = if (isEn) "Light" else "Terang",
                                icon = Icons.Default.LightMode,
                                isSelected = selectedTheme == "LIGHT",
                                onClick = {
                                    selectedTheme = "LIGHT"
                                    onUpdateProfile(profile.copy(themeMode = "LIGHT"))
                                },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeOptionButton(
                                label = if (isEn) "Dark" else "Gelap",
                                icon = Icons.Default.DarkMode,
                                isSelected = selectedTheme == "DARK",
                                onClick = {
                                    selectedTheme = "DARK"
                                    onUpdateProfile(profile.copy(themeMode = "DARK"))
                                },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeOptionButton(
                                label = if (isEn) "System" else "Sistem",
                                icon = Icons.Default.SettingsBrightness,
                                isSelected = selectedTheme == "SYSTEM",
                                onClick = {
                                    selectedTheme = "SYSTEM"
                                    onUpdateProfile(profile.copy(themeMode = "SYSTEM"))
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Pengaturan Bahasa (Indonesian & English)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.languageSelection(selectedLang),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    selectedLang = "IN"
                                    onUpdateProfile(profile.copy(appLanguage = "IN"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedLang == "IN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedLang == "IN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (selectedLang == "IN") {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("Bahasa Indonesia", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    selectedLang = "EN"
                                    onUpdateProfile(profile.copy(appLanguage = "EN"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedLang == "EN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedLang == "EN") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (selectedLang == "EN") {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("English", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4. Google Drive Backup & Restore
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Google Drive Backup & Restore",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) "Safely save data to Google Drive appDataFolder" else "Simpan data aman di folder khusus Google Drive Anda",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDriveConnected) Color(0xFFF0FDF4) else Color(0xFFF1F5F9))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isDriveConnected) (if (isEn) "Connected Google Account" else "Akun Google Terhubung") else (if (isEn) "Not Connected to Google Drive" else "Belum Terhubung ke Google Drive"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDriveConnected) ProfitGreen else Color(0xFF475569)
                                    )
                                    if (isDriveConnected) {
                                        Text(
                                            text = profile.googleDriveEmail,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                if (isDriveConnected) {
                                    TextButton(onClick = onDisconnectGoogleDrive) {
                                        Text(if (isEn) "Disconnect" else "Putuskan", color = ExpenseRed, fontSize = 12.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = { showGoogleConnectDialog = true },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (isEn) "Connect" else "Hubungkan", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (profile.lastBackupDate > 0L) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF8FAFC))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Status: ${profile.lastBackupStatus}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ProfitGreen
                                    )
                                    Text(
                                        text = "Ukuran file: ${Formatters.formatNumber(profile.lastBackupFileSize / 1024.0)} KB",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (!isOnline) {
                            Text(
                                text = if (isEn) "⚠️ Google Drive backup requires active internet (Currently OFFLINE)" else "⚠️ Backup Google Drive memerlukan koneksi internet (Saat ini OFFLINE)",
                                fontSize = 11.sp,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Button(
                            onClick = onBackupToGoogleDrive,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_backup_drive_now"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = isDriveConnected && isOnline
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isEn) "BACKUP NOW" else "BACKUP SEKARANG", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showRestoreConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_restore_drive"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = isDriveConnected && isOnline && profile.lastBackupDate > 0L
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isEn) "RESTORE FROM DRIVE" else "PULIHKAN DATA DARI DRIVE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 5. Cadangan File Lokal (JSON)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isEn) "Local File Backup (Offline)" else "Cadangan File Lokal (Offline)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEn) "Export and import JSON backup files without internet" else "Ekspor dan impor file JSON langsung di perangkat Anda tanpa perlu internet",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val json = onGetBackupJson()
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "TernakAsistenPro_Backup.json")
                                            putExtra(Intent.EXTRA_TEXT, json)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan / Simpan File Cadangan"))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Export JSON" else "Ekspor JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showLocalRestoreDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isEn) "Restore JSON" else "Pulihkan JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 6. Menu "Tentang Aplikasi" (Buka info aplikasi, fitur & versi 1.1)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showAboutAppDialog = true }
                        .testTag("btn_about_app"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.aboutAppTitle(selectedLang),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = AppStrings.aboutAppSubtitle(selectedLang),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "v1.1",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showGoogleConnectDialog) {
        var emailInput by remember { mutableStateOf(profile.googleDriveEmail.ifBlank { "farahrifa58@gmail.com" }) }
        AlertDialog(
            onDismissRequest = { showGoogleConnectDialog = false },
            title = { Text(if (isEn) "Connect Google Drive" else "Hubungkan Google Drive", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEn) "Enter your Google email address to save backups securely in Google Drive appDataFolder." else "Masukkan alamat email Google Anda untuk menyimpan cadangan otomatis di folder aplikasi Google Drive.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Akun Google") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isNotBlank()) {
                            onConnectGoogleDrive(emailInput.trim())
                            showGoogleConnectDialog = false
                        }
                    }
                ) {
                    Text(if (isEn) "Connect" else "Hubungkan Akun")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showGoogleConnectDialog = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text(if (isEn) "Confirm Restore Data?" else "Konfirmasi Pulihkan Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text(if (isEn) "This will restore your farm data with the latest Google Drive backup (${profile.lastBackupStatus}). Continue?" else "Proses ini akan memperbarui data peternakan lokal dengan file cadangan terakhir dari Google Drive (${profile.lastBackupStatus}). Lanjutkan?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreFromGoogleDrive()
                        showRestoreConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isEn) "Yes, Restore" else "Ya, Pulihkan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    if (showLocalRestoreDialog) {
        var jsonInput by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showLocalRestoreDialog = false },
            title = { Text(if (isEn) "Restore from JSON Text/File" else "Pulihkan dari Teks/File JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEn) "Paste the JSON backup contents below:" else "Tempelkan (Paste) isi file cadangan JSON yang sebelumnya Anda ekspor:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        label = { Text("Isi JSON Cadangan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 8
                    )
                    if (errorMsg != null) {
                        Text(errorMsg ?: "", color = ExpenseRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonInput.isBlank()) {
                            errorMsg = "Teks JSON tidak boleh kosong"
                            return@Button
                        }
                        onRestoreFromJson(jsonInput) { success, msg ->
                            if (success) {
                                showLocalRestoreDialog = false
                            } else {
                                errorMsg = msg
                            }
                        }
                    }
                ) {
                    Text(if (isEn) "Validate & Restore" else "Validasi & Pulihkan")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLocalRestoreDialog = false }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    if (showAboutAppDialog) {
        val scrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = { showAboutAppDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = AppStrings.aboutAppTitle(selectedLang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo & App Name Header
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Logo Ternak Asisten Pro",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Ternak Asisten Pro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "by Rafline Dev",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Versi 1.1",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Deskripsi Aplikasi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isEn) "About This App" else "Tentang Aplikasi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = AppStrings.aboutAppDescription(selectedLang),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Fitur Utama Peternakan
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = AppStrings.aboutAppFeaturesTitle(selectedLang),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            AboutFeatureBullet(
                                icon = "📊",
                                title = if (isEn) "Business Dashboard" else "Dashboard Usaha",
                                desc = if (isEn) "Real-time livestock, harvest, sales, and net profit estimation." else "Ringkasan populasi, panen, penjualan, arus kas & estimasi laba bersih."
                            )
                            AboutFeatureBullet(
                                icon = "🐣",
                                title = if (isEn) "Livestock & Mutations" else "Data Populasi Ternak",
                                desc = if (isEn) "Initial stock, additions, and mortality/sales mutation tracking." else "Catat populasi awal, mutasi masuk, dan mutasi keluar (mati/sakit/terjual)."
                            )
                            AboutFeatureBullet(
                                icon = "🥚",
                                title = if (isEn) "Harvest Records" else "Hasil Panen",
                                desc = if (isEn) "Record eggs, meat, milk, fish produce with estimated prices." else "Pencatatan produksi telur, daging, susu, ikan dengan harga perkiraan."
                            )
                            AboutFeatureBullet(
                                icon = "🏷️",
                                title = if (isEn) "Sales Transactions" else "Penjualan Cepat",
                                desc = if (isEn) "Direct harvest stock deduction and automated revenue tracking." else "Penjualan otomatis memotong stok panen dengan hitungan total instan."
                            )
                            AboutFeatureBullet(
                                icon = "📦",
                                title = if (isEn) "Warehouse & WhatsApp" else "Gudang & WhatsApp Supplier",
                                desc = if (isEn) "Feed & medicine supplies, low stock alert, and WhatsApp orders." else "Stok pakan & obat, peringatan menipis, dan pesan otomatis ke supplier."
                            )
                            AboutFeatureBullet(
                                icon = "💰",
                                title = if (isEn) "Finance & Cash Flow" else "Keuangan & Arus Kas",
                                desc = if (isEn) "Separates cash flow from profit to avoid double-counting inventory." else "Pemisahan arus kas & laba agar pembelian stok tidak dihitung ganda."
                            )
                            AboutFeatureBullet(
                                icon = "💾",
                                title = if (isEn) "Offline Storage & Backup" else "Penyimpanan Offline & Backup",
                                desc = if (isEn) "Device-local data security with Google Drive & JSON backup support." else "Data aman di HP (offline-first) dengan cadangan Google Drive / JSON."
                            )
                        }
                    }

                    // Pembaruan di Versi 1.1
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = AppStrings.aboutAppWhatsNewTitle(selectedLang),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (isEn)
                                    "• Direct WhatsApp supplier ordering for low stock items\n• Farm profile photo upload via Gallery\n• Light and Dark theme modes\n• Indonesian and English language support\n• Precision aligned 6-tab navigation layout"
                                else
                                    "• Fitur pesan langsung ke WhatsApp supplier saat stok menipis\n• Upload foto profil peternakan dari galeri foto\n• Pilihan tema Tampilan Terang & Gelap\n• Dukungan Bahasa Indonesia & English\n• Susunan menu navigasi bawah yang rapi dan presisi di semua layar HP",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Text(
                        text = "© 2026 Rafline Dev. All Rights Reserved.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutAppDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(AppStrings.close(selectedLang), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun AboutFeatureBullet(icon: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
