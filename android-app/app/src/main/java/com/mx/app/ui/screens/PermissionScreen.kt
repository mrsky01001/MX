package com.mx.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat

@Composable
fun PermissionScreen(
    onAllGranted: () -> Unit
) {
    val context = LocalContext.current

    val locationGranted = remember {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val notificationGranted = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    var step by remember {
        mutableIntStateOf(
            when {
                !locationGranted -> 1
                !notificationGranted -> 2
                else -> 3
            }
        )
    }

    when (step) {
        1 -> LocationPermissionStep(
            onGranted = {
                if (notificationGranted) step = 3 else step = 2
            }
        )
        2 -> NotificationPermissionStep(
            onGranted = { step = 3 }
        )
        3 -> MockLocationStep(
            onContinue = { onAllGranted() }
        )
    }
}

@Composable
private fun LocationPermissionStep(onGranted: () -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onGranted()
    }

    PermissionUI(
        step = 1,
        of = 3,
        icon = "📍",
        title = "Location Access",
        subtitle = "MX needs your location to mock GPS",
        buttonText = "Allow Location",
        onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
    )
}

@Composable
private fun NotificationPermissionStep(onGranted: () -> Unit) {
    val context = LocalContext.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) onGranted()
        }

        PermissionUI(
            step = 2,
            of = 3,
            icon = "🔔",
            title = "Notifications",
            subtitle = "Show mock location active status",
            buttonText = "Allow Notifications",
            onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        )
    } else {
        LaunchedEffect(Unit) { onGranted() }
    }
}

@Composable
private fun MockLocationStep(onContinue: () -> Unit) {
    val context = LocalContext.current

    PermissionUI(
        step = 3,
        of = 3,
        icon = "⚙",
        title = "Mock Location",
        subtitle = "Select MX as mock location app in Developer Options",
        buttonText = "Open Developer Options",
        showContinue = true,
        onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            context.startActivity(intent)
        },
        onContinue = onContinue
    )
}

@Composable
private fun PermissionUI(
    step: Int,
    of: Int,
    icon: String,
    title: String,
    subtitle: String,
    buttonText: String,
    showContinue: Boolean = false,
    onClick: () -> Unit,
    onContinue: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "MX",
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Fake Live Location for WhatsApp",
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF111111)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Step $step of $of",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1DB954),
                contentColor = Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = buttonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showContinue) {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onContinue?.invoke() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF222222),
                    contentColor = Color.White
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
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
