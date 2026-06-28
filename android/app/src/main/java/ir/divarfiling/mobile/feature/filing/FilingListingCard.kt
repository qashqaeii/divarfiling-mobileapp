package ir.divarfiling.mobile.feature.filing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.filing.ListingAdvertiserUtils
import ir.divarfiling.mobile.core.image.ImageUrlFormatter
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.network.ListingDto

@Composable
fun FilingListingCard(
    listing: ListingDto,
    onClick: () -> Unit,
    onOpenDivar: (() -> Unit)? = null,
    datasetLabel: String? = null,
    modifier: Modifier = Modifier,
) {
    val thumb = ImageUrlFormatter.normalize(listing.thumbnailUrl)
    val isConsultant = ListingAdvertiserUtils.isConsultant(listing)
    val cardColor = if (isConsultant) DfColors.AmberLight.copy(alpha = 0.35f) else DfColors.GreenLight.copy(alpha = 0.45f)
    val badgeColor = if (isConsultant) DfColors.AmberLight else DfColors.BlueLight
    val badgeTextColor = if (isConsultant) DfColors.Amber else DfColors.Blue

    DfCard(onClick = onClick, modifier = modifier, containerColor = cardColor) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfBadge(
                    text = ListingAdvertiserUtils.badgeLabel(listing),
                    color = badgeColor,
                    textColor = badgeTextColor,
                )
                datasetLabel?.takeIf { it.isNotBlank() }?.let {
                    DfBadge(
                        text = it,
                        color = DfColors.PurpleContainer,
                        textColor = DfColors.PurpleDark,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DfColors.SurfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (thumb != null) {
                        DfAsyncImage(
                            url = thumb,
                            modifier = Modifier.fillMaxWidth().height(96.dp),
                            contentScale = ContentScale.Crop,
                            shape = RoundedCornerShape(12.dp),
                        )
                    } else {
                        Icon(
                            DfIcons.Building,
                            contentDescription = null,
                            tint = DfColors.TextMuted,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        listing.title ?: "بدون عنوان",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val location = listOfNotNull(listing.district, listing.city)
                        .filter { it.isNotBlank() }
                        .joinToString("، ")
                    if (location.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                DfIcons.MapPin,
                                contentDescription = null,
                                tint = DfColors.TextMuted,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                location,
                                style = MaterialTheme.typography.bodySmall,
                                color = DfColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        listing.price?.takeIf { it > 0 }?.let {
                            DfBadge(
                                text = FormatUtils.formatPriceShort(it),
                                color = DfColors.Purple.copy(alpha = 0.12f),
                                textColor = DfColors.PurpleDark,
                            )
                        }
                        listing.area?.let {
                            DfBadge(
                                text = FormatUtils.formatArea(it),
                                color = DfColors.BlueLight,
                                textColor = DfColors.Blue,
                            )
                        }
                        listing.rooms?.let {
                            DfBadge(
                                text = FormatUtils.formatRooms(it),
                                color = DfColors.SurfaceVariant,
                                textColor = DfColors.TextSecondary,
                            )
                        }
                    }

                    if (onOpenDivar != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            androidx.compose.material3.TextButton(onClick = onOpenDivar) {
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text("دیوار", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
