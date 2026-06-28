package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

data class ListingSpecItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
)

@Composable
fun ListingSpecsCard(
    listing: ListingDetailDto,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val primaryItems = buildListingSpecItems(listing, expanded = false)
    val allItems = buildListingSpecItems(listing, expanded = true)
    val visibleItems = if (expanded) allItems else primaryItems

    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = DfIcons.ClipboardList,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "مشخصات ملک",
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                visibleItems.forEach { item ->
                    SpecCard(item = item, modifier = Modifier.fillMaxWidth())
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                listing.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                        Text(
                            text = "توضیحات",
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = desc,
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextSecondary,
                        )
                    }
                }
            }

            Surface(
                onClick = { expanded = !expanded },
                shape = AppShapes.ButtonPill,
                color = DfColors.SurfaceVariant,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (expanded) "بستن" else "مشاهده بیشتر",
                        style = AppTypography.labelSmall,
                        color = DfColors.Purple,
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        imageVector = if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecCard(
    item: ListingSpecItem,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Field,
        color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                )
                Text(
                    text = item.value,
                    style = AppTypography.bodyDescription,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = DfColors.Purple,
                modifier = Modifier
                    .padding(start = AppSpacing.xxs)
                    .size(16.dp),
            )
        }
    }
}

private fun buildListingSpecItems(listing: ListingDetailDto, expanded: Boolean): List<ListingSpecItem> {
    val items = listOfNotNull(
        listing.area?.let {
            ListingSpecItem("متراژ", FormatUtils.formatArea(it), DfIcons.Ruler)
        },
        listing.businessType?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("نوع ملک", it, DfIcons.Home)
        },
        listing.rooms?.let {
            ListingSpecItem("اتاق", FormatUtils.formatRooms(it), DfIcons.Bed)
        },
        listing.totalFloors?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("طبقات ساختمان", "$it طبقه", DfIcons.Building)
        },
        listing.yearBuilt?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("سال ساخت", it, DfIcons.Calendar)
        },
        listing.floor?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("طبقه", it, DfIcons.Building)
        },
        listing.advertiserType?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("نوع آگهی‌دهنده", ListingAdvertiserUtils.badgeLabel(listing), DfIcons.Users)
        },
    )

    if (!expanded) return items.take(4)

    return items + listOfNotNull(
        listing.price?.takeIf { it > 0 }?.let {
            ListingSpecItem("قیمت کل", FormatUtils.formatPriceToman(it), DfIcons.Tag)
        },
        listing.deposit?.takeIf { it > 0 }?.let {
            ListingSpecItem("ودیعه", FormatUtils.formatPriceShort(it), DfIcons.Tag)
        },
        listing.rent?.takeIf { it > 0 }?.let {
            ListingSpecItem("اجاره", FormatUtils.formatPriceShort(it), DfIcons.Tag)
        },
        listing.pricePerSqm?.let {
            ListingSpecItem("قیمت هر متر", FormatUtils.formatPriceToman(it.toLong()), DfIcons.Tag)
        },
        listing.scrapedAt?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("تاریخ استخراج", it.take(16), DfIcons.Calendar)
        },
    )
}
