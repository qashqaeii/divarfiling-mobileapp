package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.filing.ListingAmenityUtils
import ir.divarfiling.mobile.core.filing.ListingMarketTier
import ir.divarfiling.mobile.core.filing.ListingPriceUtils
import ir.divarfiling.mobile.core.filing.ListingValuePresentation
import ir.divarfiling.mobile.core.filing.ListingValueUtils
import ir.divarfiling.mobile.core.network.ListingDto

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilingListingCard(
    listing: ListingDto,
    onClick: () -> Unit,
    onOpenDivar: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    datasetLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    val advertiserBadge = ListingAdvertiserUtils.badgeStyle(listing)
    val txLabel = ListingPriceUtils.transactionLabel(listing)
    val txColor = if (ListingPriceUtils.isRental(listing)) DfColors.Blue else DfColors.Pink
    val location = listOfNotNull(listing.district, listing.city).filter { it.isNotBlank() }.joinToString("، ")
    val amenities = ListingAmenityUtils.buildAmenities(listing)
    val primaryPrice = ListingPriceUtils.primaryPriceLine(listing)
    val secondaryPrices = ListingPriceUtils.secondaryPriceLines(listing)
    val valuePresentation = ListingValueUtils.presentationFor(listing)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
        tonalElevation = 0.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
                    .clip(AppShapes.Card),
            ) {
                DfListingImage(
                    thumbnailUrl = listing.thumbnailUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.08f),
                                    Color.Black.copy(alpha = 0.42f),
                                ),
                                startY = 40f,
                            ),
                        ),
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.sm)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ListingGlassTag(
                        text = advertiserBadge.label,
                        accent = advertiserBadge.color,
                    )
                    ListingGlassTag(
                        text = txLabel,
                        accent = txColor,
                    )
                    datasetLabel?.takeIf { it.isNotBlank() }?.let {
                        ListingGlassTag(
                            text = it,
                            accent = DfColors.PurpleDark,
                        )
                    }
                    valuePresentation?.let { value ->
                        ListingGlassTag(
                            text = value.shortLabel,
                            accent = valueTierAccent(value.tier),
                        )
                    }
                    if (listing.isAddedToPersonal) {
                        ListingGlassTag(
                            text = "به فایل‌های شخصی اضافه شده",
                            accent = DfColors.Green,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = listing.title?.takeIf { it.isNotBlank() } ?: "بدون عنوان",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (location.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            DfIcons.MapPin,
                            contentDescription = null,
                            tint = DfThemeColors.primary(),
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = location,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (primaryPrice != null || secondaryPrices.isNotEmpty() || valuePresentation != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.CardSmall)
                            .background(DfThemeColors.surfaceVariant().copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        valuePresentation?.let { value ->
                            ListingValueInsightRow(presentation = value)
                            if (primaryPrice != null || secondaryPrices.isNotEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = DfThemeColors.outlineSubtle().copy(alpha = 0.55f),
                                )
                            }
                        }
                        primaryPrice?.let { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = line.label,
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.textMuted(),
                                )
                                Text(
                                    text = line.value,
                                    style = AppTypography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = txColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        secondaryPrices.forEach { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = line.label,
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.textMuted(),
                                )
                                Text(
                                    text = line.value,
                                    style = AppTypography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DfThemeColors.textSecondary(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                if (amenities.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        amenities.forEach { amenity ->
                            ListingAmenityChip(
                                label = amenity.label,
                                icon = amenity.icon,
                                iconRes = amenity.iconRes,
                            )
                        }
                    }
                }

                HorizontalDivider(color = DfThemeColors.outlineSubtle().copy(alpha = 0.7f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ListingActionButton(
                        label = "جزئیات",
                        icon = DfIcons.File,
                        tint = DfThemeColors.primary(),
                        background = DfThemeColors.primaryContainer(),
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                    )
                    if (onOpenDivar != null) {
                        ListingActionButton(
                            label = "دیوار",
                            icon = DfIcons.ExternalLink,
                            tint = DfThemeColors.info(),
                            background = DfThemeColors.infoContainer(),
                            onClick = onOpenDivar,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (onShare != null) {
                        ListingActionButton(
                            label = "اشتراک",
                            icon = DfIcons.Share2,
                            tint = DfThemeColors.success(),
                            background = DfThemeColors.successContainer(),
                            onClick = onShare,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun valueTierAccent(tier: ListingMarketTier): Color = when (tier) {
    ListingMarketTier.Under -> DfThemeColors.success()
    ListingMarketTier.Over -> DfThemeColors.error()
    ListingMarketTier.Fair -> DfThemeColors.warning()
    ListingMarketTier.Unknown -> DfThemeColors.textMuted()
}

@Composable
private fun valueTierContainer(tier: ListingMarketTier): Color = when (tier) {
    ListingMarketTier.Under -> DfThemeColors.successContainer()
    ListingMarketTier.Over -> DfThemeColors.errorContainer()
    ListingMarketTier.Fair -> DfThemeColors.warningContainer()
    ListingMarketTier.Unknown -> DfThemeColors.surfaceVariant()
}

@Composable
private fun ListingValueInsightRow(
    presentation: ListingValuePresentation,
) {
    val accent = valueTierAccent(presentation.tier)
    val container = valueTierContainer(presentation.tier)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = when (presentation.tier) {
                    ListingMarketTier.Under -> DfIcons.TrendingDown
                    ListingMarketTier.Over -> DfIcons.TrendingUp
                    else -> DfIcons.Scale
                },
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "ارزش نسبت به بازار",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
        }
        Surface(
            shape = AppShapes.Chip,
            color = container.copy(alpha = if (DfThemeColors.isDark()) 0.42f else 0.82f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
        ) {
            Text(
                text = presentation.detailLabel,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = AppTypography.labelSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ListingGlassTag(
    text: String,
    accent: Color,
) {
    Surface(
        shape = AppShapes.Chip,
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.42f)),
        shadowElevation = 0.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = AppTypography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ListingAmenityChip(
    label: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
) {
    Surface(
        shape = AppShapes.Chip,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.75f),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                iconRes != null -> DfDecorImage(resId = iconRes, size = 12.dp)
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DfThemeColors.primary(),
                    modifier = Modifier.size(12.dp),
                )
            }
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingActionButton(
    label: String,
    icon: ImageVector,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = background.copy(alpha = if (DfThemeColors.isDark()) 0.34f else 0.72f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.16f)),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 5.dp),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FilingListingCardSalePreview() {
    DivarFilingTheme {
        FilingListingCard(
            listing = ListingDto(
                token = "1",
                title = "آپارتمان ۱۲۰ متری نوساز ولنجک",
                price = 28_500_000_000,
                pricePerSqm = 237_500_000,
                area = 120,
                rooms = 3,
                district = "ولنجک",
                city = "تهران",
                advertiserType = "شخصی",
                transactionType = "فروش",
                featureHighlights = listOf("پارکینگ", "آسانسور", "انباری"),
                valueScore = -18.5,
                marketTier = "under",
                verdict = "زیر بازار",
            ),
            onClick = {},
            onOpenDivar = {},
            onShare = {},
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FilingListingCardRentPreview() {
    DivarFilingTheme {
        FilingListingCard(
            listing = ListingDto(
                token = "2",
                title = "واحد ۸۵ متری دو خوابه در ونک",
                deposit = 800_000_000,
                rent = 25_000_000,
                area = 85,
                rooms = 2,
                district = "ونک",
                city = "تهران",
                advertiserType = "مشاور",
                transactionType = "اجاره",
                featureHighlights = listOf("بالکن", "گرمایش: شوفاژ"),
                valueScore = 22.0,
                marketTier = "over",
                verdict = "بالای بازار",
            ),
            onClick = {},
            onOpenDivar = {},
            onShare = {},
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
