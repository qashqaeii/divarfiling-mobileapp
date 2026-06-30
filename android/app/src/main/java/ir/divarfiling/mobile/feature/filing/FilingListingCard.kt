package ir.divarfiling.mobile.feature.filing

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.filing.ListingAmenityUtils
import ir.divarfiling.mobile.core.filing.ListingPriceUtils
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
    val isConsultant = ListingAdvertiserUtils.isConsultant(listing)
    val txLabel = ListingPriceUtils.transactionLabel(listing)
    val txColor = if (ListingPriceUtils.isRental(listing)) DfColors.Blue else DfColors.Pink
    val txBg = if (ListingPriceUtils.isRental(listing)) DfColors.BlueLight else DfColors.PinkLight
    val advColor = if (isConsultant) DfColors.Amber else DfColors.Green
    val advBg = if (isConsultant) DfColors.AmberLight else DfColors.GreenLight
    val location = listOfNotNull(listing.district, listing.city).filter { it.isNotBlank() }.joinToString("، ")
    val amenities = ListingAmenityUtils.buildAmenities(listing)
    val primaryPrice = ListingPriceUtils.primaryPriceLine(listing)
    val secondaryPrices = ListingPriceUtils.secondaryPriceLines(listing)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
        tonalElevation = 0.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp),
            ) {
                DfListingImage(
                    thumbnailUrl = listing.thumbnailUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.45f),
                                ),
                                startY = 60f,
                            ),
                        ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.sm)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ListingTag(
                        text = ListingAdvertiserUtils.badgeLabel(listing),
                        color = advColor,
                        background = advBg.copy(alpha = 0.92f),
                    )
                    ListingTag(text = txLabel, color = txColor, background = txBg.copy(alpha = 0.92f))
                    datasetLabel?.takeIf { it.isNotBlank() }?.let {
                        ListingTag(
                            text = it,
                            color = DfColors.PurpleDark,
                            background = DfColors.PurpleContainer.copy(alpha = 0.92f),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = listing.title?.takeIf { it.isNotBlank() } ?: "بدون عنوان",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (location.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            DfIcons.MapPin,
                            contentDescription = null,
                            tint = DfColors.Purple,
                            modifier = Modifier.size(14.dp),
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

                if (primaryPrice != null || secondaryPrices.isNotEmpty()) {
                    Surface(
                        shape = AppShapes.CardSmall,
                        color = DfColors.SurfaceVariant.copy(alpha = 0.65f),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            primaryPrice?.let { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = line.label,
                                        style = AppTypography.labelSmall,
                                        color = DfColors.TextMuted,
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
                                        color = DfColors.TextMuted,
                                    )
                                    Text(
                                        text = line.value,
                                        style = AppTypography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DfColors.TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
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

                HorizontalDivider(color = DfColors.GlassBorder.copy(alpha = 0.35f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ListingActionButton(
                        label = "جزئیات",
                        icon = DfIcons.File,
                        tint = DfColors.Purple,
                        background = DfColors.PurpleContainer,
                        onClick = onClick,
                        modifier = Modifier.weight(1f),
                    )
                    if (onOpenDivar != null) {
                        ListingActionButton(
                            label = "دیوار",
                            icon = DfIcons.ExternalLink,
                            tint = DfColors.Blue,
                            background = DfColors.BlueLight,
                            onClick = onOpenDivar,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (onShare != null) {
                        ListingActionButton(
                            label = "اشتراک",
                            icon = DfIcons.Share2,
                            tint = DfColors.Green,
                            background = DfColors.GreenLight,
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
private fun ListingTag(
    text: String,
    color: Color,
    background: Color,
) {
    Surface(shape = AppShapes.Chip, color = background) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = color,
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
    Surface(shape = AppShapes.Chip, color = DfColors.SurfaceVariant) {
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
                    tint = DfColors.Purple,
                    modifier = Modifier.size(12.dp),
                )
            }
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = DfColors.TextSecondary,
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
        color = background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 4.dp),
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
            ),
            onClick = {},
            onOpenDivar = {},
            onShare = {},
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
