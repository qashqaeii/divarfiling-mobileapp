package ir.divarfiling.mobile.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColors = lightColorScheme(
    primary = DfColors.Purple,
    onPrimary = Color.White,
    primaryContainer = DfColors.PurpleContainer,
    onPrimaryContainer = DfColors.PurpleDark,
    secondary = DfColors.Blue,
    tertiary = DfColors.Green,
    background = DfColors.Background,
    surface = DfColors.Surface,
    surfaceVariant = DfColors.SurfaceVariant,
    onSurface = DfColors.TextPrimary,
    onSurfaceVariant = DfColors.TextSecondary,
    outline = DfColors.Outline,
    error = DfColors.Rose,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFF60A5FA),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
)

@Composable
fun DivarFilingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            content = content,
        )
    }
}
