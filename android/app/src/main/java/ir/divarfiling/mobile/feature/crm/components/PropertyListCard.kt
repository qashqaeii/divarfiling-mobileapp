package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import ir.divarfiling.mobile.core.design.components.DfListingImage
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
    val cover = property.images.firstOrNull()

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(dealAccent, statusColor.copy(alpha = 0.55f)),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        property.dealMode?.let {
                            PropertyBadge(text = it, color = dealAccent, bg = dealAccent.copy(alpha = 0.12f))
                        }
                        PropertyBadge(text = txStatus, color = statusColor, bg = statusBg)
                        property.propertyType?.let {
                            PropertyBadge(
                                text = it,
                                color = DfColors.TextSecondary,
                                bg = DfColors.SurfaceVariant,
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
                            metrics.forEach { PropertyMetricChip(label = it) }
                        }
                    }

                    HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
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

                Box(
                    modifier = Modifier
                        .size(width = 96.dp, height = 96.dp)
                        .clip(AppShapes.CardSmall),
                ) {
                    DfListingImage(
                        thumbnailUrl = cover,
                        images = property.images,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        shape = AppShapes.CardSmall,
                    )
                    property.publishStatus?.takeIf { it.isNotBlank() }?.let { pub ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(6.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PropertyFilters.publishDotColor(pub)),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyBadge(text: String, color: Color, bg: Color) {
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
    Surface(shape = AppShapes.Chip, color = DfColors.SurfaceVariant.copy(alpha = 0.7f)) {
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
