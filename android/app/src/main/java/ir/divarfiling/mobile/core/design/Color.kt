package ir.divarfiling.mobile.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Brand + semantic color tokens for Divar Filing.
 * Prefer [DfThemeColors] inside Composables so light/dark resolve correctly.
 */
object AppColors {
    // Brand
    val Purple = Color(0xFF5B4FCF)
    val PurpleDark = Color(0xFF4A3FB8)
    val PurpleLight = Color(0xFFEDE9FE)
    val PurpleContainer = Color(0xFFF3E8FF)
    val PurpleGradientStart = Color(0xFF6C63FF)
    val PurpleGradientEnd = Color(0xFF5B4FCF)

    val Blue = Color(0xFF2563EB)
    val BlueLight = Color(0xFFEFF6FF)
    val Green = Color(0xFF10B981)
    val GreenLight = Color(0xFFECFDF5)
    val Amber = Color(0xFFF59E0B)
    val AmberLight = Color(0xFFFFFBEB)
    val Rose = Color(0xFFF43F5E)
    val RoseLight = Color(0xFFFFF1F2)
    val Pink = Color(0xFFEC4899)
    val PinkLight = Color(0xFFFDF2F8)

    // Surfaces — light
    val Background = Color(0xFFF7F8FC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F5F9)
    val Outline = Color(0xFFE2E8F0)
    val OutlineSubtle = Color(0xFFF1F5F9)

    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val TextMuted = Color(0xFF94A3B8)

    val Shadow = Color(0x0F0F172A)
    val GlassOverlay = Color(0xCCFFFFFF)
    val GlassBorder = Color(0x99FFFFFF)
    val GlassHighlight = Color(0xE6FFFFFF)
    val GlassShadow = Color(0x145B4FCF)
    val LiquidPurple = Color(0x405B4FCF)
    val LiquidBlue = Color(0x302563EB)
    val LiquidPink = Color(0x28EC4899)

    val ImageOverlayStart = Color.Transparent
    val ImageOverlayEnd = Color(0x990F172A)
    val ImageScrimLight = Color(0x33000000)

    val NavActive = Purple
    val NavInactive = TextMuted
    val OverdueAccent = Rose
    val OverdueBackground = Color(0xFFFFF1F2)

    // Semantic — light
    val Success = Green
    val SuccessContainer = GreenLight
    val OnSuccess = Color(0xFF065F46)
    val Warning = Amber
    val WarningContainer = AmberLight
    val OnWarning = Color(0xFF92400E)
    val Error = Rose
    val ErrorContainer = RoseLight
    val OnError = Color(0xFF9F1239)
    val Info = Blue
    val InfoContainer = BlueLight
    val OnInfo = Color(0xFF1E3A8A)
    val Locked = Color(0xFF64748B)
    val LockedContainer = Color(0xFFF1F5F9)
    val OnLocked = Color(0xFF334155)

    // Dark palette
    object Dark {
        val Purple = Color(0xFFA78BFA)
        val PurpleDark = Color(0xFFC4B5FD)
        val PurpleContainer = Color(0xFF4C1D95)
        val OnPurpleContainer = Color(0xFFEDE9FE)

        val Blue = Color(0xFF60A5FA)
        val Green = Color(0xFF34D399)
        val Amber = Color(0xFFFBBF24)
        val Rose = Color(0xFFFB7185)

        val Background = Color(0xFF0B1220)
        val Surface = Color(0xFF152033)
        val SurfaceVariant = Color(0xFF1E2A3D)
        val Outline = Color(0xFF334155)
        val OutlineSubtle = Color(0xFF243044)

        val TextPrimary = Color(0xFFF1F5F9)
        val TextSecondary = Color(0xFF94A3B8)
        val TextMuted = Color(0xFF64748B)

        val Shadow = Color(0x66000000)
        val GlassOverlay = Color(0xCC152033)
        val GlassBorder = Color(0x33FFFFFF)
        val GlassHighlight = Color(0x22FFFFFF)
        val GlassShadow = Color(0x40000000)

        val Success = Green
        val SuccessContainer = Color(0xFF064E3B)
        val OnSuccess = Color(0xFFA7F3D0)
        val Warning = Amber
        val WarningContainer = Color(0xFF78350F)
        val OnWarning = Color(0xFFFDE68A)
        val Error = Rose
        val ErrorContainer = Color(0xFF7F1D1D)
        val OnError = Color(0xFFFECDD3)
        val Info = Blue
        val InfoContainer = Color(0xFF1E3A8A)
        val OnInfo = Color(0xFFBFDBFE)
        val Locked = Color(0xFF94A3B8)
        val LockedContainer = Color(0xFF1E293B)
        val OnLocked = Color(0xFFE2E8F0)

        val OverdueAccent = Rose
        val OverdueBackground = Color(0xFF7F1D1D)
    }
}

/**
 * Theme-aware color accessors for Composables.
 */
object DfThemeColors {
    @Composable
    @ReadOnlyComposable
    fun isDark(): Boolean = isSystemInDarkTheme()

    @Composable
    @ReadOnlyComposable
    fun background(): Color = if (isDark()) AppColors.Dark.Background else AppColors.Background

    @Composable
    @ReadOnlyComposable
    fun surface(): Color = if (isDark()) AppColors.Dark.Surface else AppColors.Surface

    @Composable
    @ReadOnlyComposable
    fun surfaceVariant(): Color = if (isDark()) AppColors.Dark.SurfaceVariant else AppColors.SurfaceVariant

    @Composable
    @ReadOnlyComposable
    fun outline(): Color = if (isDark()) AppColors.Dark.Outline else AppColors.Outline

    @Composable
    @ReadOnlyComposable
    fun outlineSubtle(): Color = if (isDark()) AppColors.Dark.OutlineSubtle else AppColors.OutlineSubtle

    @Composable
    @ReadOnlyComposable
    fun textPrimary(): Color = if (isDark()) AppColors.Dark.TextPrimary else AppColors.TextPrimary

    @Composable
    @ReadOnlyComposable
    fun textSecondary(): Color = if (isDark()) AppColors.Dark.TextSecondary else AppColors.TextSecondary

    @Composable
    @ReadOnlyComposable
    fun textMuted(): Color = if (isDark()) AppColors.Dark.TextMuted else AppColors.TextMuted

    @Composable
    @ReadOnlyComposable
    fun primary(): Color = if (isDark()) AppColors.Dark.Purple else AppColors.Purple

    @Composable
    @ReadOnlyComposable
    fun primaryContainer(): Color = if (isDark()) AppColors.Dark.PurpleContainer else AppColors.PurpleContainer

    @Composable
    @ReadOnlyComposable
    fun onPrimaryContainer(): Color = if (isDark()) AppColors.Dark.OnPurpleContainer else AppColors.PurpleDark

    @Composable
    @ReadOnlyComposable
    fun success(): Color = if (isDark()) AppColors.Dark.Success else AppColors.Success

    @Composable
    @ReadOnlyComposable
    fun successContainer(): Color = if (isDark()) AppColors.Dark.SuccessContainer else AppColors.SuccessContainer

    @Composable
    @ReadOnlyComposable
    fun onSuccess(): Color = if (isDark()) AppColors.Dark.OnSuccess else AppColors.OnSuccess

    @Composable
    @ReadOnlyComposable
    fun warning(): Color = if (isDark()) AppColors.Dark.Warning else AppColors.Warning

    @Composable
    @ReadOnlyComposable
    fun warningContainer(): Color = if (isDark()) AppColors.Dark.WarningContainer else AppColors.WarningContainer

    @Composable
    @ReadOnlyComposable
    fun onWarning(): Color = if (isDark()) AppColors.Dark.OnWarning else AppColors.OnWarning

    @Composable
    @ReadOnlyComposable
    fun error(): Color = if (isDark()) AppColors.Dark.Error else AppColors.Error

    @Composable
    @ReadOnlyComposable
    fun errorContainer(): Color = if (isDark()) AppColors.Dark.ErrorContainer else AppColors.ErrorContainer

    @Composable
    @ReadOnlyComposable
    fun onError(): Color = if (isDark()) AppColors.Dark.OnError else AppColors.OnError

    @Composable
    @ReadOnlyComposable
    fun info(): Color = if (isDark()) AppColors.Dark.Info else AppColors.Info

    @Composable
    @ReadOnlyComposable
    fun infoContainer(): Color = if (isDark()) AppColors.Dark.InfoContainer else AppColors.InfoContainer

    @Composable
    @ReadOnlyComposable
    fun onInfo(): Color = if (isDark()) AppColors.Dark.OnInfo else AppColors.OnInfo

    @Composable
    @ReadOnlyComposable
    fun locked(): Color = if (isDark()) AppColors.Dark.Locked else AppColors.Locked

    @Composable
    @ReadOnlyComposable
    fun lockedContainer(): Color = if (isDark()) AppColors.Dark.LockedContainer else AppColors.LockedContainer

    @Composable
    @ReadOnlyComposable
    fun onLocked(): Color = if (isDark()) AppColors.Dark.OnLocked else AppColors.OnLocked

    @Composable
    @ReadOnlyComposable
    fun shadow(): Color = if (isDark()) AppColors.Dark.Shadow else AppColors.Shadow
}

typealias DfColors = AppColors
