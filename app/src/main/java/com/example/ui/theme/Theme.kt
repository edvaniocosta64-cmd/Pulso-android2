package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class TileColorPair(
    val fill: Color,
    val lip: Color,
    val content: Color,
    val glow: Color
)

@Immutable
data class PulsoPalette(
    val solar: TileColorPair,
    val onda: TileColorPair,
    val chama: TileColorPair,
    val folha: TileColorPair,
    val energyAccent: Color,
    val successAccent: Color,
    val heroScrimBottom: Color
)

private val DarkPulsoPalette = PulsoPalette(
    solar = TileColorPair(
        fill = Color(0xFFFFB800),
        lip = Color(0xFFC28500),
        content = Color(0xFF281900),
        glow = Color(0x66FFB800)
    ),
    onda = TileColorPair(
        fill = Color(0xFF00C8F8),
        lip = Color(0xFF008AB8),
        content = Color(0xFF00202D),
        glow = Color(0x6600C8F8)
    ),
    chama = TileColorPair(
        fill = Color(0xFFFF5C6C),
        lip = Color(0xFFC22B3B),
        content = Color(0xFF33040A),
        glow = Color(0x66FF5C6C)
    ),
    folha = TileColorPair(
        fill = Color(0xFF10E59D),
        lip = Color(0xFF00A36D),
        content = Color(0xFF002A1B),
        glow = Color(0x6610E59D)
    ),
    energyAccent = AmberGlow,
    successAccent = MintEmerald,
    heroScrimBottom = MidnightBg
)

private val LightPulsoPalette = PulsoPalette(
    solar = TileColorPair(
        fill = Color(0xFFF59E0B),
        lip = Color(0xFFB45309),
        content = Color(0xFF281500),
        glow = Color(0x55F59E0B)
    ),
    onda = TileColorPair(
        fill = Color(0xFF0EA5E9),
        lip = Color(0xFF0369A1),
        content = Color(0xFFFFFFFF),
        glow = Color(0x550EA5E9)
    ),
    chama = TileColorPair(
        fill = Color(0xFFF43F5E),
        lip = Color(0xFFBE123C),
        content = Color(0xFFFFFFFF),
        glow = Color(0x55F43F5E)
    ),
    folha = TileColorPair(
        fill = Color(0xFF10B981),
        lip = Color(0xFF047857),
        content = Color(0xFF002417),
        glow = Color(0x5510B981)
    ),
    energyAccent = Color(0xFFD97706),
    successAccent = Color(0xFF059669),
    heroScrimBottom = LightBg
)

val LocalPulsoPalette = staticCompositionLocalOf { DarkPulsoPalette }

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF00202C),
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = Color(0xFFB8EAFF),
    secondary = AmberGlow,
    onSecondary = Color(0xFF281900),
    secondaryContainer = AmberContainer,
    onSecondaryContainer = Color(0xFFFFE08D),
    tertiary = MintEmerald,
    onTertiary = Color(0xFF00281A),
    tertiaryContainer = MintContainer,
    onTertiaryContainer = Color(0xFFA7FFE0),
    error = CoralFlame,
    onError = Color(0xFF33040A),
    errorContainer = CoralContainer,
    onErrorContainer = Color(0xFFFFD9DD),
    background = MidnightBg,
    onBackground = TextPrimaryDark,
    surface = MidnightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = MidnightOutline
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = Color(0xFF001F28),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF451A03),
    tertiary = Color(0xFF047857),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error = Color(0xFFE11D48),
    onError = Color.White,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutline
)

private val PulsoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PulsoTheme(
    darkTheme: Boolean = true, // Default Midnight Arcade Lab look; supports system light if toggled
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val pulsoPalette = if (darkTheme) DarkPulsoPalette else LightPulsoPalette

    CompositionLocalProvider(LocalPulsoPalette provides pulsoPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PulsoTypography,
            shapes = PulsoShapes,
            content = content
        )
    }
}
