package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfThemeColors

/** چیدمان فشرده هدر — بدون قاب Hero و بدون المان تزئینی. */
internal object DfHeaderMetrics {
    val horizontalPadding = AppSpacing.screenHorizontal
    val verticalPadding = AppSpacing.xs
    val titleSubtitleGap = AppSpacing.xxs
    val iconContainerSize = 36.dp
    val iconSize = 18.dp
    val avatarSize = 36.dp
    val actionTouchSize = 36.dp
    val actionIconSize = 20.dp
}

enum class DfHeaderVariant {
    Hub,
    Detail,
    Greeting,
}

enum class DfSectionLabelMode {
    Hidden,
    Eyebrow,
}

internal fun resolveSectionLabelMode(
    variant: DfHeaderVariant,
    sectionLabel: String?,
    title: String,
    showSectionLabel: Boolean?,
): DfSectionLabelMode {
    if (sectionLabel.isNullOrBlank()) return DfSectionLabelMode.Hidden
    showSectionLabel?.let { return if (it) DfSectionLabelMode.Eyebrow else DfSectionLabelMode.Hidden }
    return when (variant) {
        DfHeaderVariant.Hub, DfHeaderVariant.Greeting -> DfSectionLabelMode.Hidden
        DfHeaderVariant.Detail -> {
            if (isRedundantSectionLabel(sectionLabel, title)) {
                DfSectionLabelMode.Hidden
            } else {
                DfSectionLabelMode.Eyebrow
            }
        }
    }
}

internal fun isRedundantSectionLabel(sectionLabel: String, title: String): Boolean {
    val label = sectionLabel.trim()
    val heading = title.trim()
    if (label.isBlank() || heading.isBlank()) return true
    if (heading.startsWith(label)) return true
    if (heading.contains(label)) return true
    return false
}

internal fun greetingTitleFor(userName: String): String {
    val trimmed = userName.trim()
    if (trimmed.isBlank()) return "سلام"
    val firstName = trimmed.substringBefore(' ').ifBlank { trimmed }
    return "سلام، $firstName"
}

/**
 * پس‌زمینه بسیار ظریف + padding استاندارد — نه کارت جداگانه.
 */
@Composable
fun DfHeaderFrame(
    theme: DfHeaderTheme,
    modifier: Modifier = Modifier,
    showSurfaceTint: Boolean = true,
    showBottomHairline: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (showSurfaceTint) {
                    Modifier.background(theme.surfaceTint)
                } else {
                    Modifier
                },
            )
            .padding(
                horizontal = DfHeaderMetrics.horizontalPadding,
                vertical = DfHeaderMetrics.verticalPadding,
            ),
        content = content,
    )
    if (showBottomHairline) {
        HorizontalDivider(
            color = DfThemeColors.outlineSubtle().copy(alpha = 0.55f),
            thickness = 0.5.dp,
        )
    }
}
