package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.filing.ListingMarketTier
import ir.divarfiling.mobile.core.filing.ListingValueUtils
import ir.divarfiling.mobile.core.network.ListingMarketBenchmarkDto
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard
import kotlin.math.abs
import kotlin.math.min

@Composable
fun ListingMarketBenchmarkCard(
    market: ListingMarketBenchmarkDto?,
    modifier: Modifier = Modifier,
) {
    val presentation = ListingValueUtils.presentationFor(market) ?: return
    val data = market ?: return
    val accent = when (presentation.tier) {
        ListingMarketTier.Under -> DfThemeColors.success()
        ListingMarketTier.Over -> DfThemeColors.error()
        ListingMarketTier.Fair -> DfThemeColors.warning()
        ListingMarketTier.Unknown -> DfThemeColors.textMuted()
    }
    val container = when (presentation.tier) {
        ListingMarketTier.Under -> DfThemeColors.successContainer()
        ListingMarketTier.Over -> DfThemeColors.errorContainer()
        ListingMarketTier.Fair -> DfThemeColors.warningContainer()
        ListingMarketTier.Unknown -> DfColors.SurfaceVariant
    }
    val icon = when (presentation.tier) {
        ListingMarketTier.Under -> DfIcons.TrendingDown
        ListingMarketTier.Over -> DfIcons.TrendingUp
        else -> DfIcons.Scale
    }
    val metricLabel = data.label.ifBlank { "قیمت هر متر" }
    val listingFmt = data.listingMetricFmt.takeIf { it.isNotBlank() && it != "—" }
    val medianFmt = data.marketMedianFmt.takeIf { it.isNotBlank() && it != "—" }
    val sampleNote = data.sampleCount.takeIf { it > 0 }?.let { count ->
        "میانه بازار بر اساس ${DateUtils.toPersianDigits(count.toString())} آگهی همین فایل"
    }
    val percentDigits = DateUtils.toPersianDigits((presentation.percent ?: 0).toString())
    val percentHero = "$percentDigits٪"
    val statusWord = when (presentation.tier) {
        ListingMarketTier.Under -> "زیر بازار"
        ListingMarketTier.Over -> "بالای بازار"
        ListingMarketTier.Fair -> "متعادل"
        ListingMarketTier.Unknown -> "نامشخص"
    }
    val deltaLabel = when {
        data.diffAmountFmt.isBlank() -> null
        presentation.tier == ListingMarketTier.Over -> "گران‌تر از میانه"
        presentation.tier == ListingMarketTier.Under -> "ارزان‌تر از میانه"
        else -> "اختلاف با میانه"
    }
    val barProgress = min(1f, abs(data.diffPct ?: 0.0).toFloat() / 40f)

    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    Column {
                        Text(
                            text = "نسبت به بازار",
                            style = AppTypography.sectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfThemeColors.textPrimary(),
                        )
                        Text(
                            text = metricLabel,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
                Surface(shape = AppShapes.Chip, color = container) {
                    Text(
                        text = presentation.detailLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.CardSmall,
                color = container,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = percentHero,
                            style = AppTypography.statNumber,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                        Text(
                            text = statusWord,
                            style = AppTypography.bodyDescription,
                            fontWeight = FontWeight.SemiBold,
                            color = accent,
                        )
                    }
                    Text(
                        text = when (presentation.tier) {
                            ListingMarketTier.Over -> "گران‌تر از میانه بازار"
                            ListingMarketTier.Under -> "ارزان‌تر از میانه بازار"
                            else -> "نزدیک به میانه بازار"
                        },
                        style = AppTypography.labelSmall,
                        color = accent,
                        modifier = Modifier.padding(start = AppSpacing.sm),
                    )
                }
            }

            LinearProgressIndicator(
                progress = { barProgress },
                modifier = Modifier.fillMaxWidth(),
                color = accent,
                trackColor = container,
                strokeCap = StrokeCap.Round,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                MarketMetricTile(
                    label = "این آگهی",
                    value = listingFmt ?: "—",
                    modifier = Modifier.weight(1f),
                )
                MarketMetricTile(
                    label = "میانه بازار",
                    value = medianFmt ?: "—",
                    modifier = Modifier.weight(1f),
                )
                MarketMetricTile(
                    label = deltaLabel ?: "اختلاف با بازار",
                    value = data.diffAmountFmt.takeIf { it.isNotBlank() } ?: "—",
                    modifier = Modifier.weight(1f),
                )
            }

            val advice = data.verdict.takeIf { it.isNotBlank() }
            if (!advice.isNullOrBlank()) {
                Text(
                    text = advice,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                )
            }
            data.note.takeIf { it.isNotBlank() }?.let { note ->
                Text(text = note, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
            sampleNote?.let {
                Text(text = it, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
        }
    }
}

@Composable
private fun MarketMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.7f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), maxLines = 1)
            Text(
                text = value,
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
