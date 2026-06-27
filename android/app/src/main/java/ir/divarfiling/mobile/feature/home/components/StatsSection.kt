package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfAnimation
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfAnimatedCounter
import ir.divarfiling.mobile.core.design.components.DfShimmerBox
import ir.divarfiling.mobile.feature.home.DashboardStats
import ir.divarfiling.mobile.feature.home.StatCardData

@Composable
fun StatsSection(
    stats: DashboardStats,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val cards = listOf(
        StatCardData(
            value = stats.newFilesToday,
            label = "فایل جدید",
            delta = stats.newFilesToday,
            deltaLabel = if (stats.newFilesToday > 0) "+${stats.newFilesToday}" else "امروز",
            icon = DfIcons.File,
            tint = DfColors.Blue,
            background = DfColors.BlueLight,
        ),
        StatCardData(
            value = stats.properties,
            label = "املاک",
            delta = stats.propertiesDelta,
            deltaLabel = "${stats.propertiesDelta}",
            icon = DfIcons.Building,
            tint = DfColors.Amber,
            background = DfColors.AmberLight,
        ),
        StatCardData(
            value = stats.deals,
            label = "معاملات",
            delta = stats.dealsDelta,
            deltaLabel = "${stats.dealsDelta}",
            icon = DfIcons.Handshake,
            tint = DfColors.Green,
            background = DfColors.GreenLight,
        ),
        StatCardData(
            value = stats.contacts,
            label = "مخاطبین",
            delta = stats.contactsDelta,
            deltaLabel = if (stats.contactsDelta > 0) "+${stats.contactsDelta}" else "کل",
            icon = DfIcons.Users,
            tint = DfColors.Purple,
            background = DfColors.PurpleContainer,
        ),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        if (isLoading) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                repeat(2) {
                    DfShimmerBox(modifier = Modifier.weight(1f).height(96.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                repeat(2) {
                    DfShimmerBox(modifier = Modifier.weight(1f).height(96.dp))
                }
            }
            return
        }

        cards.chunked(2).forEach { rowCards ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                rowCards.forEach { card ->
                    StatCard(data = card, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatCard(data: StatCardData, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = DfAnimation.springGentle(),
        label = "statScale",
    )
    Surface(
        modifier = modifier
            .scale(scale)
            .height(96.dp),
        shape = AppShapes.StatCard,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
        onClick = {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(data.background.copy(alpha = 0.5f), DfColors.Surface)))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(data.tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(data.icon, contentDescription = null, tint = data.tint, modifier = Modifier.size(18.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                DfAnimatedCounter(
                    target = data.value,
                    style = AppTypography.statNumber.copy(fontSize = AppTypography.statNumber.fontSize * 0.85f),
                    color = DfColors.TextPrimary,
                )
                Text(
                    data.label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StatsSectionPreview() {
    DivarFilingTheme {
        StatsSection(
            stats = DashboardStats(
                newFilesToday = 5,
                properties = 17,
                propertiesDelta = 1,
                deals = 20,
                dealsDelta = 2,
                contacts = 29,
                contactsDelta = 3,
            ),
            isLoading = false,
        )
    }
}
