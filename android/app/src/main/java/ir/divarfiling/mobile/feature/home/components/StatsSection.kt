package ir.divarfiling.mobile.feature.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import ir.divarfiling.mobile.core.design.DfAnimation
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.DfSpacing
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
            label = "فایل‌های جدید امروز",
            delta = stats.newFilesToday,
            deltaLabel = if (stats.newFilesToday > 0) "+${stats.newFilesToday} امروز" else "بدون فایل جدید",
            icon = DfIcons.File,
            tint = DfColors.Blue,
            background = DfColors.BlueLight,
        ),
        StatCardData(
            value = stats.properties,
            label = "املاک",
            delta = stats.propertiesDelta,
            deltaLabel = if (stats.propertiesDelta > 0) "+${stats.propertiesDelta} امروز" else "کل آگهی‌ها",
            icon = DfIcons.Building,
            tint = DfColors.Amber,
            background = DfColors.AmberLight,
        ),
        StatCardData(
            value = stats.deals,
            label = "معاملات",
            delta = stats.dealsDelta,
            deltaLabel = if (stats.dealsDelta > 0) "+${stats.dealsDelta} امروز" else "کارهای امروز",
            icon = DfIcons.Handshake,
            tint = DfColors.Green,
            background = DfColors.GreenLight,
        ),
        StatCardData(
            value = stats.contacts,
            label = "مخاطبین",
            delta = stats.contactsDelta,
            deltaLabel = if (stats.contactsDelta > 0) "+${stats.contactsDelta} امروز" else "کل مخاطبین",
            icon = DfIcons.Users,
            tint = DfColors.Purple,
            background = DfColors.PurpleContainer,
        ),
    )

    if (isLoading) {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = DfSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(DfSpacing.sm),
        ) {
            items(4) {
                DfShimmerBox(
                    modifier = Modifier
                        .size(width = 140.dp, height = 120.dp),
                )
            }
        }
        return
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = DfSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(DfSpacing.sm),
    ) {
        items(cards, key = { it.label }) { card ->
            StatCard(card)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatCard(data: StatCardData) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = DfAnimation.springGentle(),
        label = "statScale",
    )
    Surface(
        modifier = Modifier
            .scale(scale)
            .size(width = 140.dp, height = 128.dp),
        shape = DfShapes.StatCard,
        color = DfColors.Surface,
        shadowElevation = 2.dp,
        onClick = {},
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(data.background, DfColors.Surface),
                    ),
                )
                .padding(DfSpacing.sm),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DfSpacing.xs),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(data.tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        tint = data.tint,
                        modifier = Modifier.size(20.dp),
                    )
                }
                DfAnimatedCounter(
                    target = data.value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = DfColors.TextPrimary,
                )
                Text(
                    text = data.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = DfColors.TextSecondary,
                    maxLines = 2,
                )
                Text(
                    text = data.deltaLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = data.tint,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
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
