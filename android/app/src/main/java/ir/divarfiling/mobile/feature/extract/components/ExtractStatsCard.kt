package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
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
    val accent = when {
        !canExtractNow -> DfColors.Rose
        usagePercent >= 80 -> DfColors.Amber
        else -> DfColors.Purple
    }

    ExtractSectionCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.width(92.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                UsageRing(percent = usagePercent, accent = accent)
                Text(
                    text = "باقی‌مانده امروز",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Text(
                    text = "$remainingToday آگهی",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.Purple,
                    maxLines = 1,
                )
                Text(
                    text = "از $dailyLimit آگهی مجاز",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .padding(vertical = AppSpacing.xs),
            ) {
                Canvas(modifier = Modifier.fillMaxHeight().width(1.dp)) {
                    drawLine(
                        color = DfColors.Outline,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                StatColumn(
                    icon = DfIcons.Calendar,
                    iconTint = DfColors.Amber,
                    label = "تاریخ آخرین استخراج",
                    value = lastExtractionLabel,
                )
                StatDivider()
                StatColumn(
                    icon = DfIcons.ClipboardList,
                    iconTint = DfColors.Purple,
                    label = "استخراج‌های امروز",
                    value = extractionsToday.toString(),
                )
                StatDivider()
                StatColumn(
                    icon = DfIcons.CircleCheck,
                    iconTint = DfColors.Green,
                    label = "استخراج موفق",
                    value = successfulCountLabel,
                )
                StatDivider()
                StatColumn(
                    icon = DfIcons.Clock,
                    iconTint = DfColors.Blue,
                    label = "میانگین زمان",
                    value = averageTimeLabel,
                )
            }
        }
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(72.dp)
            .width(1.dp)
            .padding(vertical = AppSpacing.xs),
    ) {
        Canvas(modifier = Modifier.fillMaxHeight().width(1.dp)) {
            drawLine(
                color = DfColors.Outline,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(0f, size.height),
                strokeWidth = 1.dp.toPx(),
            )
        }
    }
}

@Composable
private fun StatColumn(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .width(88.dp)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UsageRing(percent: Int, accent: Color) {
    val sweep = (percent.coerceIn(0, 100) / 100f) * 360f
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
        Canvas(modifier = Modifier.size(64.dp)) {
            drawArc(
                color = DfColors.SurfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text(
            text = "$percent٪",
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

fun formatLastExtractionLabel(extractionsToday: Int, hasRecentUpload: Boolean): String =
    when {
        hasRecentUpload -> "امروز"
        extractionsToday > 0 -> "امروز"
        else -> "—"
    }

fun formatAverageTimeLabel(minutes: Double?): String {
    if (minutes == null || minutes <= 0) return "—"
    return String.format(Locale("fa", "IR"), "%.1f دقیقه", minutes)
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
            lastExtractionLabel = "امروز، 08:30",
            successfulCountLabel = "120",
            averageTimeLabel = "2.4 دقیقه",
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
