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
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.filing.ListingSpecUtils
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

private data class ListingSpecItem(
    val label: String,
    val value: String,
)

private data class ListingSpecSection(
    val title: String,
    val icon: ImageVector,
    val items: List<ListingSpecItem>,
)

@Composable
fun ListingSpecsCard(
    listing: ListingDetailDto,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val sections = remember(listing) { buildListingSpecSections(listing) }
    val visibleSections = if (expanded) sections else sections.take(2)

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
                    tint = DfThemeColors.primary(),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "اطلاعات آگهی",
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
            }

            visibleSections.forEachIndexed { index, section ->
                if (index > 0) HorizontalDivider(color = DfThemeColors.outlineSubtle())
                SpecSection(section = section)
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                listing.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                        HorizontalDivider(color = DfThemeColors.outlineSubtle())
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

            if (sections.size > 2 || !listing.description.isNullOrBlank()) {
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
}

@Composable
private fun SpecSection(section: ListingSpecSection) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = null,
                tint = DfThemeColors.primary(),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = section.title,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.primary(),
            )
        }
        section.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
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
    }
}

private fun buildListingSpecSections(listing: ListingDetailDto): List<ListingSpecSection> {
    val main = listOfNotNull(
        ListingSpecItem("وضعیت آگهی", if (listing.isExpired) "منقضی" else "فعال"),
        ListingSpecItem("آگهی‌دهنده", listing.businessType?.takeIf { it.isNotBlank() } ?: ListingAdvertiserUtils.displayLabel(listing)),
        listing.unitStatus?.takeIf { it.isNotBlank() }?.let { ListingSpecItem("وضعیت سکونت", it) },
        ListingSpecItem("تخلیه", if (listing.isVacant) "تخلیه است" else "تخلیه نیست"),
        listing.scrapedAt?.takeIf { it.isNotBlank() }?.let { scraped ->
            formatScrapedDate(scraped)?.let { ListingSpecItem("تاریخ استخراج", it) }
        },
    )
    val money = listOfNotNull(
        listing.price?.takeIf { it > 0 }?.let { ListingSpecItem("قیمت فروش", FormatUtils.formatPriceToman(it)) },
        listing.deposit?.takeIf { it > 0 }?.let { ListingSpecItem("ودیعه / رهن", FormatUtils.formatPriceToman(it)) },
        listing.rent?.takeIf { it > 0 }?.let { ListingSpecItem("اجاره ماهانه", FormatUtils.formatPriceToman(it)) },
        listing.pricePerSqm?.let { ListingSpecItem("قیمت هر متر", FormatUtils.formatPriceToman(it)) },
    )
    val owner = listOfNotNull(
        listing.ownerName?.takeIf { it.isNotBlank() }?.let { ListingSpecItem("نام مالک", it) },
        listing.ownerPhone?.takeIf { it.isNotBlank() }?.let { ListingSpecItem("تلفن مالک", it) },
        listing.tenantName?.takeIf { it.isNotBlank() }?.let { ListingSpecItem("مستاجر فعلی", it) },
        listing.tenantPhone?.takeIf { it.isNotBlank() }?.let { ListingSpecItem("تلفن مستاجر", it) },
    )
    val specs = listOfNotNull(
        listing.area?.let { ListingSpecItem("متراژ", FormatUtils.formatArea(it)) },
        listing.rooms?.let { ListingSpecItem("اتاق", FormatUtils.formatRooms(it)) },
        listing.floor?.takeIf { it.isNotBlank() }?.let { ListingSpecItem("طبقه", it) },
        listing.totalFloors?.takeIf { it.isNotBlank() }?.let { ListingSpecItem("تعداد کل طبقات", it) },
        listing.yearBuilt?.takeIf { it.isNotBlank() }?.let { ListingSpecItem("سال ساخت", it) },
        ListingSpecItem("پارکینگ", ListingSpecUtils.boolFeatureLabel(listing.hasParking)),
        ListingSpecItem("انباری", ListingSpecUtils.boolFeatureLabel(listing.hasStorage)),
        ListingSpecItem("آسانسور", ListingSpecUtils.boolFeatureLabel(listing.hasElevator)),
        listing.hasBalcony?.let { ListingSpecItem("بالکن", ListingSpecUtils.boolFeatureLabel(it)) },
        listing.hasLobby?.let { ListingSpecItem("لابی", ListingSpecUtils.boolFeatureLabel(it)) },
        listing.hasPool?.let { ListingSpecItem("استخر", ListingSpecUtils.boolFeatureLabel(it)) },
        listing.hasRoofGarden?.let { ListingSpecItem("روف گاردن", ListingSpecUtils.boolFeatureLabel(it)) },
        listing.hasJacuzzi?.let { ListingSpecItem("جکوزی", ListingSpecUtils.boolFeatureLabel(it)) },
        listing.hasGym?.let { ListingSpecItem("سالن ورزش", ListingSpecUtils.boolFeatureLabel(it)) },
    )

    return listOfNotNull(
        ListingSpecSection("اطلاعات اصلی", DfIcons.File, main).takeIf { it.items.isNotEmpty() },
        ListingSpecSection("اطلاعات مالی", DfIcons.Coins, money).takeIf { it.items.isNotEmpty() },
        ListingSpecSection("مالک و سکونت", DfIcons.User, owner).takeIf { it.items.isNotEmpty() },
        ListingSpecSection("مشخصات و امکانات", DfIcons.Building, specs).takeIf { it.items.isNotEmpty() },
    )
}

private fun formatScrapedDate(iso: String): String? =
    DateUtils.formatJalaliDateTime(iso) ?: DateUtils.formatJalaliDate(iso)
