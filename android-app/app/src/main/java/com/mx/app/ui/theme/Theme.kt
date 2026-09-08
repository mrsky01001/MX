package com.mx.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MXPrimary,
    secondary = MXAccent,
    background = MXBackground,
    surface = MXSurface,
    onPrimary = MXWhite,
    onSecondary = MXBlack,
    onBackground = MXText,
    onSurface = MXText,
    error = MXError
)

@Composable
fun MXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
