package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.network.ListingDetailDto

@Composable
fun ListingDetailHeader(
    listing: ListingDetailDto,
    onCopyAdCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isConsultant = ListingAdvertiserUtils.isConsultant(listing)
    val location = listOfNotNull(listing.district, listing.city).joinToString("، ")
    val priceLabel = when {
        listing.price != null && listing.price > 0 -> FormatUtils.formatPriceToman(listing.price)
        listing.rent != null && listing.rent > 0 -> "اجاره ${FormatUtils.formatPriceShort(listing.rent)}"
        listing.deposit != null && listing.deposit > 0 -> "ودیعه ${FormatUtils.formatPriceShort(listing.deposit)}"
        else -> "—"
    }
    val floorLabel = when {
        !listing.floor.isNullOrBlank() && !listing.totalFloors.isNullOrBlank() ->
            "${listing.floor} از ${listing.totalFloors}"
        !listing.floor.isNullOrBlank() -> listing.floor
        else -> "—"
    }
    val propertyType = listing.businessType?.takeIf { it.isNotBlank() } ?: "آپارتمان"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = AppShapes.Chip,
                color = if (isConsultant) DfColors.AmberLight else DfColors.GreenLight,
            ) {
                Text(
                    text = ListingAdvertiserUtils.badgeLabel(listing),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isConsultant) DfColors.Amber else DfColors.Green,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "کد آگهی: ${listing.token.takeLast(8)}",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onCopyAdCode, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = DfIcons.Copy,
                        contentDescription = "کپی کد",
                        tint = DfColors.TextMuted,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        Text(
            text = listing.title ?: "—",
            style = AppTypography.pageTitle,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
        )

        if (location.isNotBlank()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = DfIcons.MapPin,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = location,
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        ListingKeyStatsRow(
            price = priceLabel,
            area = listing.area?.let { FormatUtils.formatArea(it) } ?: "—",
            rooms = listing.rooms?.let { FormatUtils.formatRooms(it) } ?: "—",
            floor = floorLabel,
            propertyType = propertyType,
        )
    }
}

@Composable
private fun ListingKeyStatsRow(
    price: String,
    area: String,
    rooms: String,
    floor: String,
    propertyType: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = AppSpacing.sm),
        ) {
            KeyStatCell(label = "قیمت", value = price, valueColor = DfColors.Purple, modifier = Modifier.weight(1.15f))
            KeyStatDivider()
            KeyStatCell(label = "متراژ", value = area, modifier = Modifier.weight(0.85f))
            KeyStatDivider()
            KeyStatCell(label = "اتاق", value = rooms, modifier = Modifier.weight(0.85f))
            KeyStatDivider()
            KeyStatCell(label = "طبقه", value = floor, modifier = Modifier.weight(0.85f))
            KeyStatDivider()
            KeyStatCell(label = "نوع ملک", value = propertyType, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun KeyStatDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = AppSpacing.xs),
    ) {
        Canvas(modifier = Modifier.fillMaxHeight().size(width = 1.dp, height = 48.dp)) {
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
private fun KeyStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = DfColors.TextPrimary,
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = value,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
