package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.PropertyDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyListCard(
    property: PropertyDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val txStatus = property.transactionStatus ?: "فعال"
    val (statusColor, statusBg) = PropertyFilters.txStatusColors(txStatus)
    val dealAccent = PropertyFilters.dealModeAccent(property)
    val location = PropertyFilters.locationLabel(property)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(statusColor),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(AppShapes.IconContainer)
                            .background(statusBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = PropertyFilters.propertyTypeIcon(property.propertyType),
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        property.dealMode?.let { PropertyPill(text = it, color = dealAccent, bg = dealAccent.copy(alpha = 0.12f)) }
                        PropertyPill(text = txStatus, color = statusColor, bg = statusBg)
                    }
                    property.publishStatus?.takeIf { it.isNotBlank() }?.let { pub ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PropertyFilters.publishDotColor(pub)),
                        )
                    }
                }

                Text(
                    text = property.title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (location != "—") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.MapPin,
                            contentDescription = null,
                            tint = DfColors.TextMuted,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = location,
                            style = AppTypography.labelSmall,
                            color = DfColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                val metrics = buildList {
                    PropertyFilters.formatArea(property.area)?.let { add(it) }
                    property.rooms?.takeIf { it.isNotBlank() }?.let { add("$it اتاق") }
                    PropertyFilters.formatFloor(property.floor, property.totalFloors)?.let { add(it) }
                }
                if (metrics.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        metrics.forEach { metric ->
                            PropertyMetricChip(label = metric)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = PropertyFilters.priceSummary(property),
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = dealAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val priceLabel = PropertyFilters.priceLabel(property)
                        if (priceLabel.isNotBlank()) {
                            Text(
                                text = priceLabel,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextMuted,
                            )
                        }
                    }
                    PropertyFilters.jalaliUpdated(property)?.let { updated ->
                        Text(
                            text = updated,
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyPill(text: String, color: Color, bg: Color) {
    Surface(shape = AppShapes.Chip, color = bg) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PropertyMetricChip(label: String) {
    Surface(shape = AppShapes.Chip, color = DfColors.SurfaceVariant) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = DfColors.TextSecondary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PropertyListCardPreview() {
    DivarFilingTheme {
        PropertyListCard(
            property = PropertyDto(
                id = 1,
                title = "آپارتمان ۱۲۰ متری نوساز ولنجک",
                dealMode = "فروش",
                transactionStatus = "فعال",
                propertyType = "آپارتمان",
                publishStatus = "منتشرشده",
                neighborhood = "ولنجک",
                city = "تهران",
                salePrice = 28_500_000_000,
                area = 120.0,
                rooms = "3",
                updatedAt = "2026-06-28T10:00:00Z",
            ),
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
