package com.mx.app.ui.screens

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mx.app.ui.theme.*
import com.mx.app.update.UpdateInfo
import com.mx.app.update.UpdateBadge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun HomeScreen(
    locationName: String,
    locationLat: Double,
    locationLng: Double,
    isRunning: Boolean,
    showStoppedDialog: Boolean = false,
    updateInfo: UpdateInfo? = null,
    onStoppedDialogDismiss: () -> Unit = {},
    onLocationSelected: (String, Double, Double) -> Unit,
    onUpdateClick: () -> Unit = {},
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenMaps: (Double, Double, String) -> Unit
) {
    var showLocationDialog by remember { mutableStateOf(false) }
    var showStoppedDialogState by remember { mutableStateOf(showStoppedDialog) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showStoppedDialog) {
        showStoppedDialogState = showStoppedDialog
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MX",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UpdateBadge(
                    updateInfo = updateInfo,
                    onClick = onUpdateClick
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isRunning) Color(0x331DB954) else Color(0xFF1A1A1A))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isRunning) "● Active" else "Inactive",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRunning) Color(0xFF1DB954) else Color(0xFF888888)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                        .clickable { showAboutDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ℹ",
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0A1628))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x081DB954))
            )

            if (locationLat != 0.0 && locationLng != 0.0) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xBB000000))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📍 SELECTED LOCATION",
                        fontSize = 10.sp,
                        color = Color(0xFF1DB954),
                        letterSpacing = 1.sp
                    )
                    if (locationName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = locationName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format("%.6f, %.6f", locationLat, locationLng),
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View in Google Maps →",
                        fontSize = 11.sp,
                        color = Color(0xFF1DB954),
                        modifier = Modifier.clickable {
                            onOpenMaps(locationLat, locationLng, locationName)
                        }
                    )
                }
            } else {
                Text(
                    text = "📍",
                    fontSize = 56.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xBB000000))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .clickable { showLocationDialog = true }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (locationLat != 0.0) "📍 Change Location" else "📍 Tap to Select Location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isRunning) Color(0xFFFF4444) else Color(0xFF1DB954)
                    )
                    .clickable {
                        if (isRunning) {
                            onStop()
                            showStoppedDialogState = true
                        } else {
                            onStart()
                        }
                    }
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRunning) "⏹ STOP" else "▶ START",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "MX will send fake location instead of real GPS",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showLocationDialog) {
        LocationInputDialog(
            initialName = locationName,
            initialLat = locationLat,
            initialLng = locationLng,
            onConfirm = { name, lat, lng ->
                onLocationSelected(name, lat, lng)
                showLocationDialog = false
            },
            onDismiss = { showLocationDialog = false },
            onOpenMaps = onOpenMaps
        )
    }

    if (showStoppedDialogState) {
        AlertDialog(
            onDismissRequest = {
                showStoppedDialogState = false
                onStoppedDialogDismiss()
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Text(
                    text = "MX Mock Stopped",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Your fake location has been stopped.\n\nYou can now send your real location on WhatsApp.",
                    fontSize = 14.sp,
                    color = Color(0xFFBBBBBB),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStoppedDialogState = false
                        onStoppedDialogDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1DB954),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            },
            dismissButton = {}
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = "About MX",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "MX sends fake GPS location on WhatsApp instead of your real location.",
                    fontSize = 14.sp,
                    color = Color(0xFFBBBBBB),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "FIRST TIME SETUP (Required)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1DB954),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. Allow Location permission\n2. Allow Notification (optional)\n3. Open Developer Options\n4. Enable \"Mock location app\"\n5. Select \"MX\" from the list",
                    fontSize = 13.sp,
                    color = Color(0xFFBBBBBB),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Without this, MX will NOT work.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HOW TO USE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1DB954),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. Tap \"Tap to Select Location\"\n2. Enter place name or coordinates\n3. Tap \"Confirm\"\n4. Tap \"START\" to begin\n5. Open WhatsApp → Share Live Location\n6. Tap \"STOP\" when done",
                    fontSize = 13.sp,
                    color = Color(0xFFBBBBBB),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color(0xFF333333))

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• MX runs as foreground service\n• Stop MX from app or notification bar\n• Android only — not supported on iOS",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "v1.1",
                    fontSize = 11.sp,
                    color = Color(0xFF555555),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {}
    )
}

@Composable
fun LocationInputDialog(
    initialName: String,
    initialLat: Double,
    initialLng: Double,
    onConfirm: (String, Double, Double) -> Unit,
    onDismiss: () -> Unit,
    onOpenMaps: (Double, Double, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(initialName) }
    var latText by remember { mutableStateOf(if (initialLat != 0.0) initialLat.toString() else "") }
    var lngText by remember { mutableStateOf(if (initialLng != 0.0) initialLng.toString() else "") }
    var parsedLat by remember { mutableDoubleStateOf(if (initialLat != 0.0) initialLat else 0.0) }
    var parsedLng by remember { mutableDoubleStateOf(if (initialLng != 0.0) initialLng else 0.0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    fun hasValidCoords(): Boolean {
        val lat = latText.toDoubleOrNull()
        val lng = lngText.toDoubleOrNull()
        return lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0
    }

    fun hasName(): Boolean = name.isNotBlank()

    fun canConfirm(): Boolean = hasValidCoords() || hasName()

    fun updatePreview() {
        val lat = latText.toDoubleOrNull()
        val lng = lngText.toDoubleOrNull()
        if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
            parsedLat = lat
            parsedLng = lng
        }
    }

    fun resolveFromName(onResult: (Double, Double) -> Unit) {
        isLoading = true
        errorMessage = ""
        scope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(name, 1)
                }
                if (!results.isNullOrEmpty()) {
                    val result = results[0]
                    onResult(result.latitude, result.longitude)
                    isLoading = false
                } else {
                    errorMessage = "Place not found. Try adding lat/lng."
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Could not find location."
                isLoading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(text = "Select Location", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Enter place name or coordinates",
                    fontSize = 13.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Place Name (optional)") },
                    placeholder = { Text("e.g. Connaught Place, Delhi") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1DB954),
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedLabelColor = Color(0xFF1DB954),
                        unfocusedLabelColor = Color(0xFF888888),
                        cursorColor = Color(0xFF1DB954)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "— OR enter coordinates —",
                    fontSize = 11.sp,
                    color = Color(0xFF444444),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = {
                            latText = it
                            updatePreview()
                        },
                        label = { Text("Latitude") },
                        placeholder = { Text("28.6315") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF1DB954),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedLabelColor = Color(0xFF1DB954),
                            unfocusedLabelColor = Color(0xFF888888),
                            cursorColor = Color(0xFF1DB954)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lngText,
                        onValueChange = {
                            lngText = it
                            updatePreview()
                        },
                        label = { Text("Longitude") },
                        placeholder = { Text("77.2167") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF1DB954),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedLabelColor = Color(0xFF1DB954),
                            unfocusedLabelColor = Color(0xFF888888),
                            cursorColor = Color(0xFF1DB954)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (hasValidCoords()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF111111))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                            .clickable {
                                updatePreview()
                                onOpenMaps(parsedLat, parsedLng, name)
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🗺", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (name.isNotBlank()) "$name (${String.format("%.4f", parsedLat)}, ${String.format("%.4f", parsedLng)})" else String.format("%.6f, %.6f", latText.toDouble(), lngText.toDouble()),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1DB954)
                                )
                                Text(
                                    text = "Tap to verify in Google Maps",
                                    fontSize = 10.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                            Text(text = "→", fontSize = 18.sp, color = Color(0xFF1DB954))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (hasValidCoords()) {
                        updatePreview()
                        onConfirm(name, parsedLat, parsedLng)
                    } else if (hasName()) {
                        resolveFromName { lat, lng ->
                            onConfirm(name, lat, lng)
                        }
                    }
                },
                enabled = canConfirm() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canConfirm()) Color(0xFF1DB954) else Color(0xFF222222),
                    contentColor = if (canConfirm()) Color.Black else Color(0xFF555555)
                ),
                modifier = Modifier
                    .defaultMinSize(minWidth = 120.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Confirm", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    )
}
