package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.DateUtils
import java.time.Instant
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ExtractStatsCard(
    extractionsToday: Int,
    remainingToday: Int,
    dailyLimit: Int,
    canExtractNow: Boolean,
    lastExtractionLabel: String,
    successfulCountLabel: String,
    averageTimeLabel: String,
    modifier: Modifier = Modifier,
) {
    val used = (dailyLimit - remainingToday).coerceIn(0, dailyLimit)
    val usagePercent = if (dailyLimit > 0) (used * 100f / dailyLimit).roundToInt() else 0
    val progress = if (dailyLimit > 0) used.toFloat() / dailyLimit else 0f
    val accent = when {
        !canExtractNow -> DfColors.Rose
        usagePercent >= 80 -> DfColors.Amber
        else -> DfColors.Purple
    }

    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                    Text(
                        text = "باقی‌مانده امروز",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = remainingToday.toString(),
                            style = AppTypography.sectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                        Text(
                            text = "آگهی",
                            style = AppTypography.labelSmall,
                            color = DfColors.TextSecondary,
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                ) {
                    Text(
                        text = "$usagePercent٪",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                    Text(
                        text = "از $dailyLimit",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = accent,
                trackColor = DfColors.SurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                StatTile(
                    label = "آخرین استخراج",
                    value = lastExtractionLabel,
                    background = DfColors.AmberLight,
                    valueColor = DfColors.Amber,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "استخراج امروز",
                    value = extractionsToday.toString(),
                    background = DfColors.PurpleContainer,
                    valueColor = DfColors.Purple,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                StatTile(
                    label = "استخراج موفق",
                    value = successfulCountLabel,
                    background = DfColors.GreenLight,
                    valueColor = DfColors.Green,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "میانگین زمان",
                    value = averageTimeLabel,
                    background = DfColors.BlueLight,
                    valueColor = DfColors.Blue,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    background: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(AppShapes.Card)
            .background(background.copy(alpha = 0.55f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

fun formatLastExtractionLabel(
    extractionsToday: Int,
    hasRecentUpload: Boolean,
    lastExtractionAtMs: Long? = null,
): String {
    lastExtractionAtMs?.takeIf { it > 0 }?.let { atMs ->
        val relative = DateUtils.formatRelativeTimeAgo(Instant.ofEpochMilli(atMs).toString())
        if (relative != "اخیراً") return relative
        return "امروز"
    }
    return when {
        hasRecentUpload -> "امروز"
        extractionsToday > 0 -> "امروز"
        else -> "—"
    }
}

fun formatSuccessfulCountLabel(
    persistedCount: Int?,
    sessionCount: Int?,
): String {
    val count = sessionCount ?: persistedCount
    return count?.takeIf { it > 0 }?.toString() ?: "—"
}

fun formatAverageTimeLabel(
    sessionMinutes: Double?,
    persistedAverageMinutes: Double?,
): String {
    val minutes = sessionMinutes?.takeIf { it > 0 } ?: persistedAverageMinutes?.takeIf { it > 0 }
    if (minutes == null || minutes <= 0) return "—"
    if (minutes < 1) {
        val seconds = (minutes * 60).roundToInt().coerceAtLeast(1)
        return "${DateUtils.toPersianDigits(seconds.toString())} ثانیه"
    }
    val formatted = String.format(Locale.US, "%.1f", minutes)
    return "${DateUtils.toPersianDigits(formatted)} دقیقه"
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ExtractStatsCardPreview() {
    DivarFilingTheme {
        ExtractStatsCard(
            extractionsToday = 2,
            remainingToday = 40,
            dailyLimit = 100,
            canExtractNow = true,
            lastExtractionLabel = "امروز",
            successfulCountLabel = "120",
            averageTimeLabel = "2.4 دقیقه",
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
