package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.annotation.DrawableRes
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
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.filing.ListingSpecUtils
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

data class ListingSpecItem(
    val label: String,
    val value: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
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
                DfDecorImage(
                    resId = DfDecorIcons.Ruler,
                    size = 18.dp,
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
            when {
                item.iconRes != null -> DfDecorImage(
                    resId = item.iconRes,
                    size = 16.dp,
                    modifier = Modifier.padding(start = AppSpacing.xxs),
                )
                item.icon != null -> Icon(
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
}

private fun buildListingSpecItems(listing: ListingDetailDto, expanded: Boolean): List<ListingSpecItem> {
    val coreItems = listOf(
        ListingSpecItem("وضعیت", ListingSpecUtils.statusLabel(listing), iconRes = DfDecorIcons.Zap),
        ListingSpecItem("پارکینگ", ListingSpecUtils.boolFeatureLabel(listing.hasParking), iconRes = DfDecorIcons.Car),
        ListingSpecItem("انباری", ListingSpecUtils.boolFeatureLabel(listing.hasStorage), iconRes = DfDecorIcons.Storage),
        ListingSpecItem("آسانسور", ListingSpecUtils.boolFeatureLabel(listing.hasElevator), iconRes = DfDecorIcons.Elevator),
    )
    val detailItems = listOfNotNull(
        listing.area?.let {
            ListingSpecItem("متراژ", FormatUtils.formatArea(it), iconRes = DfDecorIcons.Ruler)
        },
        listing.businessType?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("نوع ملک", it, iconRes = DfDecorIcons.Building)
        },
        listing.rooms?.let {
            ListingSpecItem("اتاق", FormatUtils.formatRooms(it), icon = DfIcons.Bed)
        },
        listing.totalFloors?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("طبقات ساختمان", "$it طبقه", iconRes = DfDecorIcons.Building)
        },
        listing.yearBuilt?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("سال ساخت", it, iconRes = DfDecorIcons.Calendar)
        },
        listing.floor?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("طبقه", it, iconRes = DfDecorIcons.Building)
        },
        listing.advertiserType?.takeIf { it.isNotBlank() }?.let {
            ListingSpecItem("نوع آگهی‌دهنده", ListingAdvertiserUtils.badgeLabel(listing), iconRes = DfDecorIcons.Users)
        },
        listing.scrapedAt?.takeIf { it.isNotBlank() }?.let { scraped ->
            formatScrapedDate(scraped)?.let {
                ListingSpecItem("تاریخ استخراج", it, iconRes = DfDecorIcons.Calendar)
            }
        },
    )
    val priceItems = listOfNotNull(
        listing.price?.takeIf { it > 0 }?.let {
            ListingSpecItem("قیمت کل", FormatUtils.formatPriceToman(it), iconRes = DfDecorIcons.Tag)
        },
        listing.deposit?.takeIf { it > 0 }?.let {
            ListingSpecItem("ودیعه", FormatUtils.formatPriceShort(it), iconRes = DfDecorIcons.Tag)
        },
        listing.rent?.takeIf { it > 0 }?.let {
            ListingSpecItem("اجاره", FormatUtils.formatPriceShort(it), iconRes = DfDecorIcons.Tag)
        },
        listing.pricePerSqm?.let {
            ListingSpecItem("قیمت هر متر", FormatUtils.formatPriceToman(it), iconRes = DfDecorIcons.Tag)
        },
    )

    return if (expanded) {
        coreItems + detailItems + priceItems
    } else {
        coreItems + detailItems.take(4)
    }
}

private fun formatScrapedDate(iso: String): String? =
    DateUtils.formatJalaliDateTime(iso) ?: DateUtils.formatJalaliDate(iso)
