package com.mx.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.mx.app.service.MockLocationService
import com.mx.app.ui.screens.HomeScreen
import com.mx.app.ui.screens.PermissionScreen
import com.mx.app.ui.theme.MXTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            checkMockLocationPermission()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()

        setContent {
            MXTheme {
                var showPermissions by remember { mutableStateOf(true) }
                var locationName by remember { mutableStateOf("") }
                var locationLat by remember { mutableDoubleStateOf(0.0) }
                var locationLng by remember { mutableDoubleStateOf(0.0) }
                var timerMinutes by remember { mutableIntStateOf(15) }
                var isRunning by remember { mutableStateOf(false) }
                var showStoppedDialog by remember { mutableStateOf(false) }

                // Check if opened from stopped notification
                LaunchedEffect(intent) {
                    if (intent?.getBooleanExtra("show_stopped_dialog", false) == true) {
                        showStoppedDialog = true
                        isRunning = false
                        intent.removeExtra("show_stopped_dialog")
                    }
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
                        timerMinutes = timerMinutes,
                        isRunning = isRunning,
                        showStoppedDialog = showStoppedDialog,
                        onStoppedDialogDismiss = { showStoppedDialog = false },
                        onLocationSelected = { name, lat, lng ->
                            locationName = name
                            locationLat = lat
                            locationLng = lng
                        },
                        onTimerChanged = { timerMinutes = it },
                        onStart = {
                            startMockService(locationName, locationLat, locationLng, timerMinutes)
                            isRunning = true
                        },
                        onStop = {
                            stopMockService()
                            isRunning = false
                        },
                        onOpenMaps = { lat, lng, name ->
                            val query = if (name.isNotBlank()) "$name $lat,$lng" else "$lat,$lng"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps?q=${Uri.encode(query)}"))
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun checkMockLocationPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun startMockService(name: String, lat: Double, lng: Double, timerMinutes: Int) {
        val intent = Intent(this, MockLocationService::class.java).apply {
            action = MockLocationService.ACTION_START
            putExtra("name", name)
            putExtra("latitude", lat)
            putExtra("longitude", lng)
            putExtra("timer_minutes", timerMinutes)
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
