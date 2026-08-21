package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
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
                    color = DfThemeColors.textPrimary(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                visibleItems.forEachIndexed { index, item ->
                    if (index > 0) HorizontalDivider(color = DfThemeColors.outlineSubtle())
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
                            color = DfThemeColors.textPrimary(),
                        )
                        Text(
                            text = desc,
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                }
            }

            Surface(
                onClick = { expanded = !expanded },
                shape = AppShapes.ButtonPill,
                color = DfThemeColors.surfaceVariant(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.sm),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (expanded) "بستن" else "مشاهده بیشتر",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.primary(),
                        fontWeight = FontWeight.Medium,
                    )
                    Icon(
                        imageVector = if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                        contentDescription = null,
                        tint = DfThemeColors.primary(),
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.label,
            style = AppTypography.labelSmall,
            color = DfThemeColors.textMuted(),
            modifier = Modifier.weight(0.42f),
            maxLines = 1,
        )
        Text(
            text = item.value,
            style = AppTypography.bodyDescription,
            fontWeight = FontWeight.Medium,
            color = DfThemeColors.textPrimary(),
            modifier = Modifier.weight(0.58f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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
        ListingSpecItem("نوع آگهی‌دهنده", ListingAdvertiserUtils.displayLabel(listing), iconRes = DfDecorIcons.Users),
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
