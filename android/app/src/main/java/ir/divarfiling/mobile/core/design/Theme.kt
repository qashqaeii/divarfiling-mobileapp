package ir.divarfiling.mobile.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColors = lightColorScheme(
    primary = AppColors.Purple,
    onPrimary = Color.White,
    primaryContainer = AppColors.PurpleContainer,
    onPrimaryContainer = AppColors.PurpleDark,
    secondary = AppColors.Blue,
    onSecondary = Color.White,
    secondaryContainer = AppColors.BlueLight,
    onSecondaryContainer = AppColors.OnInfo,
    tertiary = AppColors.Green,
    onTertiary = Color.White,
    tertiaryContainer = AppColors.GreenLight,
    onTertiaryContainer = AppColors.OnSuccess,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Outline,
    outlineVariant = AppColors.OutlineSubtle,
    error = AppColors.Error,
    onError = Color.White,
    errorContainer = AppColors.ErrorContainer,
    onErrorContainer = AppColors.OnError,
    inverseSurface = AppColors.TextPrimary,
    inverseOnSurface = AppColors.Surface,
    inversePrimary = AppColors.Dark.Purple,
    scrim = Color.Black.copy(alpha = 0.4f),
)

private val DarkColors = darkColorScheme(
    primary = AppColors.Dark.Purple,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = AppColors.Dark.PurpleContainer,
    onPrimaryContainer = AppColors.Dark.OnPurpleContainer,
    secondary = AppColors.Dark.Blue,
    onSecondary = Color(0xFF0B1220),
    secondaryContainer = AppColors.Dark.InfoContainer,
    onSecondaryContainer = AppColors.Dark.OnInfo,
    tertiary = AppColors.Dark.Green,
    onTertiary = Color(0xFF0B1220),
    tertiaryContainer = AppColors.Dark.SuccessContainer,
    onTertiaryContainer = AppColors.Dark.OnSuccess,
    background = AppColors.Dark.Background,
    onBackground = AppColors.Dark.TextPrimary,
    surface = AppColors.Dark.Surface,
    onSurface = AppColors.Dark.TextPrimary,
    surfaceVariant = AppColors.Dark.SurfaceVariant,
    onSurfaceVariant = AppColors.Dark.TextSecondary,
    outline = AppColors.Dark.Outline,
    outlineVariant = AppColors.Dark.OutlineSubtle,
    error = AppColors.Dark.Error,
    onError = Color(0xFF450A0A),
    errorContainer = AppColors.Dark.ErrorContainer,
    onErrorContainer = AppColors.Dark.OnError,
    inverseSurface = AppColors.Dark.TextPrimary,
    inverseOnSurface = AppColors.Dark.Background,
    inversePrimary = AppColors.Purple,
    scrim = Color.Black.copy(alpha = 0.6f),
)

private val AppMaterialShapes = Shapes(
    small = AppShapes.Chip,
    medium = AppShapes.Card,
    large = AppShapes.Hero,
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
            typography = DfTypography,
            shapes = AppMaterialShapes,
            content = content,
        )
    }
}
