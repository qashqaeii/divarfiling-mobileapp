package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
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
        !canExtractNow -> DfThemeColors.error()
        usagePercent >= 80 -> DfThemeColors.warning()
        else -> DfThemeColors.primary()
    }

    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            ExtractSectionTitle(title = "سهمیه امروز", iconRes = ir.divarfiling.mobile.core.design.components.DfDecorIcons.Database)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp),
                ) {
                    CircularProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.size(72.dp),
                        color = accent,
                        trackColor = DfThemeColors.surfaceVariant(),
                        strokeWidth = 6.dp,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = DateUtils.toPersianDigits(remainingToday.toString()),
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                        Text(
                            text = "باقی",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(
                        text = "از ${DateUtils.toPersianDigits(dailyLimit.toString())} آگهی مجاز",
                        style = AppTypography.bodyDescription,
                        fontWeight = FontWeight.Medium,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        text = "${DateUtils.toPersianDigits(usagePercent.toString())}٪ مصرف شده",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accent,
                        trackColor = DfThemeColors.surfaceVariant(),
                    )
                    if (!canExtractNow) {
                        Text(
                            text = "سقف امروز تکمیل شده",
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.error(),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                StatTile(
                    label = "آخرین استخراج",
                    value = lastExtractionLabel,
                    accent = DfThemeColors.warning(),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "استخراج امروز",
                    value = DateUtils.toPersianDigits(extractionsToday.toString()),
                    accent = DfThemeColors.primary(),
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
                    accent = DfThemeColors.success(),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = "میانگین زمان",
                    value = averageTimeLabel,
                    accent = DfThemeColors.info(),
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
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = accent.copy(alpha = if (DfThemeColors.isDark()) 0.12f else 0.08f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent,
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
    return count?.takeIf { it > 0 }?.let { DateUtils.toPersianDigits(it.toString()) } ?: "—"
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
            successfulCountLabel = "۱۲۰",
            averageTimeLabel = "۲.۴ دقیقه",
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
