package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfPremiumCard

@Composable
fun ExtractDailyUsageCard(
    extractionsToday: Int,
    remainingToday: Int,
    dailyLimit: Int,
    canExtractNow: Boolean,
    modifier: Modifier = Modifier,
) {
    val used = (dailyLimit - remainingToday).coerceIn(0, dailyLimit)
    val usagePercent = if (dailyLimit > 0) (used * 100 / dailyLimit) else 0
    val progress = if (dailyLimit > 0) used.toFloat() / dailyLimit else 0f
    val accent = when {
        !canExtractNow -> DfColors.Rose
        usagePercent >= 80 -> DfColors.Amber
        else -> DfColors.Purple
    }

    DfPremiumCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "باقی‌مانده امروز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        if (canExtractNow) "$remainingToday استخراج باقی مانده" else "سقف روزانه تکمیل شد",
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                    )
                }
                UsageRing(percent = usagePercent, accent = accent)
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium),
                color = accent,
                trackColor = DfColors.SurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                UsageStat("استخراج امروز", "$extractionsToday")
                UsageStat("باقی‌مانده", "$remainingToday")
                UsageStat("سقف پلن", "$dailyLimit")
            }
        }
    }
}

@Composable
private fun UsageRing(percent: Int, accent: Color) {
    val sweep = (percent.coerceIn(0, 100) / 100f) * 360f
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
        Canvas(modifier = Modifier.size(72.dp)) {
            drawArc(
                color = DfColors.SurfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$percent٪", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text("استفاده", style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
        }
    }
}

@Composable
private fun UsageStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DfColors.PurpleDark)
        Text(label, style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
    }
}
