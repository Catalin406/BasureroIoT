package com.example.basureroiot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores del mockup de Catalina
private val EsquemaDeColores = lightColorScheme(
    primary = Color(0xFFA27560),
    onPrimary = Color.White,
    secondary = Color(0xFFC98199),
    onSecondary = Color.White,
    tertiary = Color(0xFFEBD3C2),
    background = Color(0xFFFFF6F3),
    onBackground = Color(0xFF5A3E36),
    surface = Color.White,
    onSurface = Color(0xFF5A3E36),
    primaryContainer = Color(0xFFF9D0DD),
    onPrimaryContainer = Color(0xFF5A3E36)
)

@Composable
fun BasureroIoTTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaDeColores,
        typography = Typography, // viene del archivo Type.kt que creó Android Studio
        content = content
    )
}
