package com.mx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mx.app.ui.theme.*

@Composable
fun HomeScreen(
    locationName: String,
    locationLat: Double,
    locationLng: Double,
    timerMinutes: Int,
    isRunning: Boolean,
    showStoppedDialog: Boolean = false,
    onStoppedDialogDismiss: () -> Unit = {},
    onLocationSelected: (String, Double, Double) -> Unit,
    onTimerChanged: (Int) -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenMaps: (Double, Double, String) -> Unit
) {
    var showLocationDialog by remember { mutableStateOf(false) }
    var showStoppedDialogState by remember { mutableStateOf(showStoppedDialog) }

    // Sync external state
    LaunchedEffect(showStoppedDialog) {
        showStoppedDialogState = showStoppedDialog
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header
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
        }

        // Map Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0A1628))
        ) {
            // Grid pattern
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x081DB954))
            )

            if (locationLat != 0.0 && locationLng != 0.0) {
                // Selected location info
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
                // Pin icon
                Text(
                    text = "📍",
                    fontSize = 56.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Select location button
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

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Timer
            Text(
                text = "Auto-stop after",
                fontSize = 12.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val timers = listOf(5 to "5m", 15 to "15m", 30 to "30m", 60 to "1h", 480 to "8h", 0 to "∞")
                timers.forEach { (minutes, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 2.dp,
                                color = if (timerMinutes == minutes) Color(0xFF1DB954) else Color(0xFF222222),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(
                                if (timerMinutes == minutes) Color(0x1A1DB954) else Color.Transparent
                            )
                            .clickable { onTimerChanged(minutes) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (timerMinutes == minutes) Color(0xFF1DB954) else Color(0xFF888888)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start/Stop Button
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

            // Footer
            Text(
                text = "MX will send fake location instead of real GPS",
                fontSize = 11.sp,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Location Input Dialog
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

    // Mock Stopped Dialog
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
    var name by remember { mutableStateOf(initialName) }
    var latText by remember { mutableStateOf(if (initialLat != 0.0) initialLat.toString() else "") }
    var lngText by remember { mutableStateOf(if (initialLng != 0.0) initialLng.toString() else "") }
    var linkText by remember { mutableStateOf("") }
    var showPreview by remember { mutableStateOf(false) }
    var parsedLat by remember { mutableDoubleStateOf(0.0) }
    var parsedLng by remember { mutableDoubleStateOf(0.0) }

    fun updatePreview() {
        val lat = latText.toDoubleOrNull()
        val lng = lngText.toDoubleOrNull()
        if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) {
            parsedLat = lat
            parsedLng = lng
            showPreview = true
        } else {
            showPreview = false
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
                    text = "Enter location details",
                    fontSize = 13.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Place name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Place Name / Street (optional)") },
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

                Spacer(modifier = Modifier.height(12.dp))

                // Latitude
                OutlinedTextField(
                    value = latText,
                    onValueChange = {
                        latText = it
                        updatePreview()
                    },
                    label = { Text("Latitude") },
                    placeholder = { Text("e.g. 28.6315") },
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

                Spacer(modifier = Modifier.height(8.dp))

                // Longitude
                OutlinedTextField(
                    value = lngText,
                    onValueChange = {
                        lngText = it
                        updatePreview()
                    },
                    label = { Text("Longitude") },
                    placeholder = { Text("e.g. 77.2167") },
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

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "— OR paste Google Maps link —",
                    fontSize = 12.sp,
                    color = Color(0xFF555555),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = linkText,
                    onValueChange = { link ->
                        linkText = link
                        // Extract coordinates from link
                        val match = Regex("""@(-?\d+\.?\d*),(-?\d+\.?\d*)""").find(link)
                            ?: Regex("""q=(-?\d+\.?\d*),(-?\d+\.?\d*)""").find(link)
                            ?: Regex("""\?q=(-?\d+\.?\d*),(-?\d+\.?\d*)""").find(link)
                        if (match != null) {
                            latText = match.groupValues[1]
                            lngText = match.groupValues[2]
                            updatePreview()
                        }
                    },
                    label = { Text("Google Maps link") },
                    placeholder = { Text("https://maps.google.com/...") },
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

                // Preview
                if (showPreview) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF111111))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
                            .clickable { onOpenMaps(parsedLat, parsedLng, name) }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🗺", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (name.isNotBlank()) "$name (${String.format("%.4f", parsedLat)}, ${String.format("%.4f", parsedLng)})" else String.format("%.6f, %.6f", parsedLat, parsedLng),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1DB954)
                                )
                                Text(
                                    text = "Tap to verify in Google Maps",
                                    fontSize = 11.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                            Text(text = "→", fontSize = 20.sp, color = Color(0xFF1DB954))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, parsedLat, parsedLng) },
                enabled = showPreview,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showPreview) Color(0xFF1DB954) else Color(0xFF333333),
                    contentColor = if (showPreview) Color.Black else Color(0xFF666666)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    )
}
