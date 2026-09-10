package com.mx.app.ui.screens

import android.Manifest
import android.content.Context
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private const val PREFS_NAME = "mx_prefs"
private const val KEY_PERMISSIONS_DONE = "permissions_done"
private const val KEY_MOCK_SETUP_DONE = "mock_setup_done"

fun isPermissionsDone(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_PERMISSIONS_DONE, false)
}

fun markPermissionsDone(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_PERMISSIONS_DONE, true).apply()
}

fun isMockSetupDone(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_MOCK_SETUP_DONE, false)
}

fun markMockSetupDone(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_MOCK_SETUP_DONE, true).apply()
}

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

    var step by remember {
        mutableIntStateOf(
            when {
                !locationGranted -> 1
                else -> 2
            }
        )
    }

    when (step) {
        1 -> LocationPermissionStep(
            onGranted = { step = 2 }
        )
        2 -> NotificationPermissionStep(
            onGranted = {
                markPermissionsDone(context)
                step = 3
            },
            onSkip = {
                markPermissionsDone(context)
                step = 3
            }
        )
        3 -> MockLocationStep(
            onContinue = {
                markMockSetupDone(context)
                onAllGranted()
            }
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
private fun NotificationPermissionStep(
    onGranted: () -> Unit,
    onSkip: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) onGranted()
        }

        NotificationUI(
            onAllow = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
            onSkip = onSkip
        )
    } else {
        LaunchedEffect(Unit) { onGranted() }
    }
}

@Composable
private fun MockLocationStep(onContinue: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var settingsOpened by remember { mutableStateOf(false) }

    val isDevOptionsEnabled = remember {
        Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
    }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && settingsOpened) {
                settingsOpened = false
                onContinue()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    PermissionUI(
        step = 3,
        of = 3,
        icon = "⚙",
        title = "Mock Location",
        subtitle = "Select MX as mock location app in Developer Options",
        showContinueOnly = true,
        onContinue = {
            if (isDevOptionsEnabled) {
                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                context.startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)
            }
            settingsOpened = true
        }
    )
}

@Composable
private fun NotificationUI(
    onAllow: () -> Unit,
    onSkip: () -> Unit
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
            Text(text = "🔔", fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Notifications",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Show mock location active status in notification bar",
            fontSize = 13.sp,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Step 2 of 3",
            fontSize = 11.sp,
            color = Color(0xFF444444),
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAllow,
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
                text = "Allow Notifications",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Skip",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            modifier = Modifier
                .clickable { onSkip() }
                .padding(12.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PermissionUI(
    step: Int,
    of: Int,
    icon: String,
    title: String,
    subtitle: String,
    buttonText: String = "",
    showContinue: Boolean = false,
    showContinueOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
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

        if (!showContinueOnly && onClick != null) {
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
        }

        if (showContinue) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showContinueOnly || showContinue) {
            Button(
                onClick = { onContinue?.invoke() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showContinueOnly) Color(0xFF1DB954) else Color(0xFF222222),
                    contentColor = if (showContinueOnly) Color.Black else Color.White
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
