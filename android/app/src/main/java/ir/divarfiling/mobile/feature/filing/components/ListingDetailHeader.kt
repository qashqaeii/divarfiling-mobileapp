package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import ir.divarfiling.mobile.core.filing.ListingMarketTier
import ir.divarfiling.mobile.core.filing.ListingSpecUtils
import ir.divarfiling.mobile.core.filing.ListingValueUtils
import ir.divarfiling.mobile.core.network.ListingDetailDto

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListingDetailHeader(
    listing: ListingDetailDto,
    onCopyAdCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val advertiserBadge = ListingAdvertiserUtils.badgeStyle(listing)
    val location = listOfNotNull(
        listing.address?.takeIf { it.isNotBlank() },
        listing.region?.takeIf { it.isNotBlank() },
        listing.district,
        listing.city,
    ).distinct().joinToString("، ")
    val isRent = listing.rent != null || listing.deposit != null
    val marketPresentation = ListingValueUtils.presentationFor(listing.market)
    val floorLabel = when {
        !listing.floor.isNullOrBlank() && !listing.totalFloors.isNullOrBlank() ->
            "${listing.floor} از ${listing.totalFloors}"
        !listing.floor.isNullOrBlank() -> listing.floor
        else -> "—"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusChip(
                    label = advertiserBadge.label,
                    color = advertiserBadge.color,
                    background = advertiserBadge.background,
                )
                StatusChip(
                    label = if (listing.isExpired) "منقضی" else "فعال",
                    color = if (listing.isExpired) DfColors.Rose else DfColors.Green,
                    background = if (listing.isExpired) DfColors.RoseLight else DfColors.GreenLight,
                )
                listing.unitStatus?.takeIf { it.isNotBlank() }?.let { status ->
                    StatusChip(
                        label = status,
                        color = DfColors.Purple,
                        background = DfColors.PurpleContainer,
                    )
                }
                marketPresentation?.let { pres ->
                    val (color, background) = when (pres.tier) {
                        ListingMarketTier.Under -> DfColors.Green to DfColors.GreenLight
                        ListingMarketTier.Over -> DfColors.Rose to DfColors.RoseLight
                        ListingMarketTier.Fair -> DfColors.Amber to DfColors.AmberLight
                        ListingMarketTier.Unknown -> DfColors.TextMuted to DfColors.SurfaceVariant
                    }
                    StatusChip(
                        label = pres.detailLabel,
                        color = color,
                        background = background,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = listing.token.takeLast(8),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                )
                IconButton(onClick = onCopyAdCode, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = DfIcons.Copy,
                        contentDescription = "کپی کد آگهی",
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
            maxLines = 3,
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

        if (isRent) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                PriceHeroCard(
                    label = "ودیعه / رهن",
                    value = listing.deposit?.takeIf { it > 0 }?.let(FormatUtils::formatPriceToman) ?: "—",
                    hint = listing.deposit?.takeIf { it > 0 }?.let(FormatUtils::formatPriceShort),
                    modifier = Modifier.weight(1f),
                )
                PriceHeroCard(
                    label = "اجاره ماهانه",
                    value = listing.rent?.takeIf { it > 0 }?.let(FormatUtils::formatPriceToman) ?: "—",
                    hint = listing.rent?.takeIf { it > 0 }?.let(FormatUtils::formatPriceShort),
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            PriceHeroCard(
                label = "قیمت فروش",
                value = listing.price?.takeIf { it > 0 }?.let(FormatUtils::formatPriceToman) ?: "—",
                hint = buildList {
                    listing.price?.takeIf { it > 0 }?.let { add(FormatUtils.formatPriceShort(it)) }
                    listing.pricePerSqm?.let { add("هر متر ${FormatUtils.formatPriceToman(it)}") }
                }.joinToString(" · ").ifBlank { null },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ListingKeyStatsGrid(
            area = listing.area?.let { FormatUtils.formatArea(it) } ?: "—",
            rooms = listing.rooms?.let { FormatUtils.formatRooms(it) } ?: "—",
            floor = floorLabel,
            yearBuilt = listing.yearBuilt?.takeIf { it.isNotBlank() } ?: "—",
        )

        ListingCoreAmenityRow(listing = listing)
    }
}

@Composable
private fun StatusChip(
    label: String,
    color: Color,
    background: Color,
) {
    Surface(shape = AppShapes.Chip, color = background) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun PriceHeroCard(
    label: String,
    value: String,
    hint: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Card,
        color = DfColors.PurpleContainer,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfColors.Purple,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value,
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.Purple,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!hint.isNullOrBlank()) {
                Text(
                    text = hint,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ListingKeyStatsGrid(
    area: String,
    rooms: String,
    floor: String,
    yearBuilt: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            KeyStatTile(label = "متراژ", value = area, icon = DfIcons.Ruler, modifier = Modifier.weight(1f))
            KeyStatTile(label = "اتاق", value = rooms, icon = DfIcons.Bed, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            KeyStatTile(label = "طبقه", value = floor, icon = DfIcons.Layers, modifier = Modifier.weight(1f))
            KeyStatTile(label = "سال ساخت", value = yearBuilt, icon = DfIcons.Calendar, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun KeyStatTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier.size(16.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
                Text(
                    text = value,
                    style = AppTypography.bodyDescription,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListingCoreAmenityRow(listing: ListingDetailDto) {
    val chips = listOf(
        AmenityChipSpec("پارکینگ", listing.hasParking, DfIcons.Car),
        AmenityChipSpec("انباری", listing.hasStorage, DfIcons.Inbox),
        AmenityChipSpec("آسانسور", listing.hasElevator, DfIcons.Layers),
        AmenityChipSpec("بالکن", listing.hasBalcony, DfIcons.Home),
        AmenityChipSpec("لابی", listing.hasLobby, DfIcons.Building),
        AmenityChipSpec("استخر", listing.hasPool, DfIcons.Bath),
        AmenityChipSpec("روف گاردن", listing.hasRoofGarden, DfIcons.Sparkles),
        AmenityChipSpec("جکوزی", listing.hasJacuzzi, DfIcons.Bath),
        AmenityChipSpec("سالن ورزش", listing.hasGym, DfIcons.Trophy),
    ).filter { it.value != null }
    if (chips.isEmpty()) return

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        chips.forEach { spec ->
            val positive = spec.value == true
            val color = if (positive) DfColors.Green else DfColors.TextMuted
            val bg = if (positive) DfColors.GreenLight else DfColors.SurfaceVariant
            Surface(shape = AppShapes.Chip, color = bg) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = spec.icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${spec.label}: ${ListingSpecUtils.boolFeatureLabel(spec.value)}",
                        style = AppTypography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

private data class AmenityChipSpec(
    val label: String,
    val value: Boolean?,
    val icon: ImageVector,
)
