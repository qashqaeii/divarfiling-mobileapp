package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.filing.ListingSpecUtils
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
                    DfDecorImage(
                        resId = DfDecorIcons.Copy,
                        size = 14.dp,
                        contentDescription = "کپی کد",
                    )
                }
            }
        }

        Text(
            text = listing.title ?: "—",
            style = AppTypography.pageTitle,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
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

        ListingKeyStatsCards(
            price = priceLabel,
            area = listing.area?.let { FormatUtils.formatArea(it) } ?: "—",
            rooms = listing.rooms?.let { FormatUtils.formatRooms(it) } ?: "—",
            floor = floorLabel,
            propertyType = propertyType,
        )

        ListingCoreAmenityRow(
            hasParking = listing.hasParking,
            hasStorage = listing.hasStorage,
            hasElevator = listing.hasElevator,
        )
    }
}

@Composable
private fun ListingCoreAmenityRow(
    hasParking: Boolean?,
    hasStorage: Boolean?,
    hasElevator: Boolean?,
) {
    val chips = listOf(
        Triple("پارکینگ", hasParking, DfDecorIcons.Car),
        Triple("انباری", hasStorage, DfDecorIcons.Storage),
        Triple("آسانسور", hasElevator, DfDecorIcons.Elevator),
    ).filter { (_, value, _) -> value != null }
    if (chips.isEmpty()) return

    @OptIn(ExperimentalLayoutApi::class)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        chips.forEach { (label, value, iconRes) ->
            CoreAmenityChip(
                label = label,
                value = value!!,
                iconRes = iconRes,
            )
        }
    }
}

@Composable
private fun CoreAmenityChip(
    label: String,
    value: Boolean,
    iconRes: Int,
) {
    val positive = value
    val color = if (positive) DfColors.Green else DfColors.TextMuted
    val bg = if (positive) DfColors.GreenLight else DfColors.SurfaceVariant
    Surface(shape = AppShapes.Chip, color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DfDecorImage(resId = iconRes, size = 14.dp)
            Text(
                text = "$label: ${ListingSpecUtils.boolFeatureLabel(value)}",
                style = AppTypography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ListingKeyStatsCards(
    price: String,
    area: String,
    rooms: String,
    floor: String,
    propertyType: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        KeyStatCard(label = "قیمت", value = price, valueColor = DfColors.Purple)
        KeyStatCard(label = "متراژ", value = area)
        KeyStatCard(label = "اتاق", value = rooms)
        KeyStatCard(label = "طبقه", value = floor)
        KeyStatCard(label = "نوع ملک", value = propertyType)
    }
}

@Composable
private fun KeyStatCard(
    label: String,
    value: String,
    valueColor: Color = DfColors.TextPrimary,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            Text(
                text = value,
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}
