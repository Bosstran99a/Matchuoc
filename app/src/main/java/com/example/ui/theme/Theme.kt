package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MahjongColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = InkCharcoal,
    primaryContainer = PowerUpBronze,
    onPrimaryContainer = GoldAccent,
    secondary = ActionOrange,
    onSecondary = TileFaceIvory,
    secondaryContainer = SurfaceCardJade,
    onSecondaryContainer = TextOnTable,
    tertiary = TrayBorderNeon,
    onTertiary = SurfaceDarkJade,
    background = EmeraldFeltMid,
    onBackground = TextOnTable,
    surface = SurfaceDarkJade,
    onSurface = TextOnTable,
    surfaceVariant = SurfaceCardJade,
    onSurfaceVariant = TextMutedOnTable,
    outline = TrayBorderNeon,
)

@Composable
fun MatChuocTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MahjongColorScheme,
        typography = AppTypography,
        content = content,
    )
}
