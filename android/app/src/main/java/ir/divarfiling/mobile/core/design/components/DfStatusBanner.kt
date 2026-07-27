package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors

enum class DfStatusTone {
    Success,
    Warning,
    Error,
    Info,
    Locked,
}

data class DfStatusColors(
    val container: Color,
    val content: Color,
    val accent: Color,
)

@Composable
fun DfStatusTone.colors(): DfStatusColors = when (this) {
    DfStatusTone.Success -> DfStatusColors(
        container = DfThemeColors.successContainer(),
        content = DfThemeColors.onSuccess(),
        accent = DfThemeColors.success(),
    )
    DfStatusTone.Warning -> DfStatusColors(
        container = DfThemeColors.warningContainer(),
        content = DfThemeColors.onWarning(),
        accent = DfThemeColors.warning(),
    )
    DfStatusTone.Error -> DfStatusColors(
        container = DfThemeColors.errorContainer(),
        content = DfThemeColors.onError(),
        accent = DfThemeColors.error(),
    )
    DfStatusTone.Info -> DfStatusColors(
        container = DfThemeColors.infoContainer(),
        content = DfThemeColors.onInfo(),
        accent = DfThemeColors.info(),
    )
    DfStatusTone.Locked -> DfStatusColors(
        container = DfThemeColors.lockedContainer(),
        content = DfThemeColors.onLocked(),
        accent = DfThemeColors.locked(),
    )
}

@Composable
fun DfStatusTone.defaultIcon(): ImageVector = when (this) {
    DfStatusTone.Success -> DfIcons.CircleCheck
    DfStatusTone.Warning -> DfIcons.TriangleAlert
    DfStatusTone.Error -> DfIcons.TriangleAlert
    DfStatusTone.Info -> DfIcons.Sparkles
    DfStatusTone.Locked -> DfIcons.Sparkles
}

/**
 * Semantic status banner — Success / Warning / Error / Info / Locked.
 * Replaces ad-hoc error strips; [DfErrorBanner] delegates here.
 */
@Composable
fun DfStatusBanner(
    message: String,
    tone: DfStatusTone = DfStatusTone.Error,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = tone.defaultIcon(),
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = tone.colors()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Banner,
        color = colors.container,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (!title.isNullOrBlank()) {
                    Text(
                        text = title,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.content,
                    )
                }
                Text(
                    text = message,
                    style = AppTypography.bodyDescription,
                    color = colors.content,
                )
            }
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(
                        text = actionLabel,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                    )
                }
            }
        }
    }
}
