package com.mx.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.mx.app.service.MockLocationService
import com.mx.app.ui.screens.HomeScreen
import com.mx.app.ui.screens.PermissionScreen
import com.mx.app.ui.theme.MXTheme
import com.mx.app.update.UpdateChecker
import com.mx.app.update.UpdateDownloader
import com.mx.app.update.UpdateInfo
import com.mx.app.update.UpdateAvailableDialog
import com.mx.app.update.UpdateProgressDialog
import com.mx.app.update.UpdateBadge
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var updateChecker: UpdateChecker
    private lateinit var updateDownloader: UpdateDownloader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateChecker = UpdateChecker(this)
        updateDownloader = UpdateDownloader(this)

        setContent {
            MXTheme {
                var showPermissions by remember { mutableStateOf(true) }
                var locationName by remember { mutableStateOf("") }
                var locationLat by remember { mutableDoubleStateOf(0.0) }
                var locationLng by remember { mutableDoubleStateOf(0.0) }
                var isRunning by remember { mutableStateOf(false) }
                var showStoppedDialog by remember { mutableStateOf(false) }

                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                var showUpdateDialog by remember { mutableStateOf(false) }
                var showDownloadProgress by remember { mutableStateOf(false) }
                var downloadProgress by remember { mutableIntStateOf(0) }
                var downloadComplete by remember { mutableStateOf(false) }
                var showError by remember { mutableStateOf(false) }
                var errorMessage by remember { mutableStateOf("") }

                val scope = rememberCoroutineScope()

                LaunchedEffect(intent) {
                    if (intent?.getBooleanExtra("show_stopped_dialog", false) == true) {
                        showStoppedDialog = true
                        isRunning = false
                        intent.removeExtra("show_stopped_dialog")
                    }
                }

                LaunchedEffect(Unit) {
                    val result = updateChecker.checkForUpdate()
                    updateInfo = result
                }

                if (showPermissions) {
                    PermissionScreen(
                        onAllGranted = { showPermissions = false }
                    )
                } else {
                    HomeScreen(
                        locationName = locationName,
                        locationLat = locationLat,
                        locationLng = locationLng,
                        isRunning = isRunning,
                        showStoppedDialog = showStoppedDialog,
                        updateInfo = updateInfo,
                        onStoppedDialogDismiss = { showStoppedDialog = false },
                        onLocationSelected = { name, lat, lng ->
                            locationName = name
                            locationLat = lat
                            locationLng = lng
                        },
                        onUpdateClick = { showUpdateDialog = true },
                        onStart = {
                            startMockService(locationName, locationLat, locationLng)
                            isRunning = true
                        },
                        onStop = {
                            stopMockService()
                            isRunning = false
                        },
                        onOpenMaps = { lat, lng, name ->
                            val query = if (name.isNotBlank()) "$name $lat,$lng" else "$lat,$lng"
                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps?q=${Uri.encode(query)}"))
                            startActivity(mapIntent)
                        }
                    )
                }

                if (showUpdateDialog && updateInfo != null) {
                    UpdateAvailableDialog(
                        updateInfo = updateInfo!!,
                        onDismiss = { showUpdateDialog = false },
                        onUpdate = {
                            showUpdateDialog = false
                            showDownloadProgress = true
                            downloadProgress = 0
                            downloadComplete = false

                            scope.launch {
                                val result = updateDownloader.downloadApk(
                                    url = updateInfo!!.downloadUrl
                                ) { progress ->
                                    downloadProgress = progress
                                }

                                if (result != null) {
                                    downloadComplete = true
                                } else {
                                    showError = true
                                    errorMessage = "Download failed. Please try again."
                                    showDownloadProgress = false
                                }
                            }
                        }
                    )
                }

                if (showDownloadProgress) {
                    UpdateProgressDialog(
                        progress = downloadProgress,
                        isComplete = downloadComplete,
                        onDismiss = {
                            showDownloadProgress = false
                            downloadComplete = false
                        },
                        onInstall = {
                            showDownloadProgress = false
                            val apkFile = updateDownloader.getDownloadedApkFile()
                            if (apkFile != null) {
                                updateDownloader.installApk(apkFile)
                            }
                        }
                    )
                }

                if (showError) {
                    UpdateAvailableDialog(
                        updateInfo = updateInfo!!,
                        onDismiss = {
                            showError = false
                            errorMessage = ""
                        },
                        onUpdate = { }
                    )
                }
            }
        }
    }

    private fun startMockService(name: String, lat: Double, lng: Double) {
        val intent = Intent(this, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_START
            putExtra("name", name)
            putExtra("latitude", lat)
            putExtra("longitude", lng)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopMockService() {
        val intent = Intent(this, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_STOP
        }
        startService(intent)
    }
}
