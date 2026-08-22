package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme

/**
 * هدر بخش داخل صفحه — یکسان در CRM، فایلینگ، تنظیمات و جزئیات.
 */
@Composable
fun DfSectionHeader(
    title: String,
    count: Int? = null,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    horizontalPadding: Dp = 0.dp,
    sectionLabel: String? = null,
    accentColor: Color? = null,
    showDivider: Boolean = false,
    useSoftCard: Boolean = true,
) {
    val resolvedAccent = accentColor ?: sectionAccentColor(sectionLabel)
    val chipBackground = resolvedAccent.copy(alpha = if (DfThemeColors.isDark()) 0.14f else 0.10f)
    val cardBorder = resolvedAccent.copy(alpha = if (DfThemeColors.isDark()) 0.20f else 0.12f)
    val cardGradientEnd = resolvedAccent.copy(alpha = if (DfThemeColors.isDark()) 0.06f else 0.04f)

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 20.dp)
                    .clip(AppShapes.Chip)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(resolvedAccent, resolvedAccent.copy(alpha = 0.55f)),
                        ),
                    ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 1,
                )
                subtitle?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                        maxLines = 2,
                    )
                }
            }
            when {
                count != null && count > 0 -> {
                    Surface(shape = AppShapes.Chip, color = chipBackground) {
                        Text(
                            text = DateUtils.toPersianDigits(count.toString()),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = AppTypography.labelSmall,
                            color = resolvedAccent,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                count != null -> {
                    Surface(shape = AppShapes.Chip, color = DfThemeColors.surfaceVariant()) {
                        Text(
                            text = "۰",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                color = cardBorder,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }
    }

    if (useSoftCard) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            shape = AppShapes.CardSmall,
            color = Color.Transparent,
            border = BorderStroke(1.dp, cardBorder),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                resolvedAccent.copy(alpha = cardGradientEnd.alpha * 1.6f),
                                cardGradientEnd,
                                Color.Transparent,
                            ),
                        ),
                    )
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            ) {
                content()
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            content = { content() },
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun DfSectionHeaderPreview() {
    DivarFilingTheme {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            DfSectionHeader(
                title = "لیست مخاطبین",
                count = 248,
                sectionLabel = DfHeaderSections.CRM,
            )
            DfSectionHeader(
                title = "معاملات",
                count = 5,
                subtitle = "پیگیری فعال",
                sectionLabel = DfHeaderSections.CRM,
            )
            DfSectionHeader(
                title = "پوشه‌های فایلینگ",
                count = 12,
                sectionLabel = DfHeaderSections.FILING,
            )
        }
    }
}
