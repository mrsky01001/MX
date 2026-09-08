package com.mx.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionScreen(
    onAllGranted: () -> Unit
) {
    val context = LocalContext.current
    var permLocation by remember { mutableStateOf(false) }
    var permNotification by remember { mutableStateOf(false) }
    var permMock by remember { mutableStateOf(context.isMockLocationEnabled()) }

    val allGranted = permLocation && permNotification && permMock

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permNotification = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        } else {
            permNotification = true
        }
        permMock = context.isMockLocationEnabled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "MX",
            fontSize = 80.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Fake Live Location for WhatsApp",
            fontSize = 13.sp,
            color = Color(0xFF888888)
        )

        Spacer(modifier = Modifier.height(50.dp))

        // Permission items
        PermissionItem(
            icon = "📍",
            title = "Location Access",
            subtitle = "Required to mock GPS",
            granted = permLocation,
            onClick = {
                val perms = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(perms.toTypedArray())
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionItem(
            icon = "🔔",
            title = "Notifications",
            subtitle = "Show mock status",
            granted = permNotification,
            onClick = {
                val perms = mutableListOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(perms.toTypedArray())
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PermissionItem(
            icon = "⚙",
            title = "Mock Location",
            subtitle = "Developer Options",
            granted = permMock,
            onClick = {
                permMock = context.isMockLocationEnabled()
                if (!permMock) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                    context.startActivity(intent)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onAllGranted() },
            enabled = allGranted,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allGranted) Color(0xFF1DB954) else Color(0xFF222222),
                contentColor = if (allGranted) Color.Black else Color(0xFF555555)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "All permissions are required.\nYou can disable them in phone Settings later.",
            fontSize = 11.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PermissionItem(
    icon: String,
    title: String,
    subtitle: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111111))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (granted) Color(0xFF1DB954) else Color(0xFF333333)),
            contentAlignment = Alignment.Center
        ) {
            if (granted) {
                Text(
                    text = "✓",
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun Context.isMockLocationEnabled(): Boolean {
    return try {
        Settings.Secure.getInt(contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION) != 0
    } catch (e: Exception) {
        false
    }
}
