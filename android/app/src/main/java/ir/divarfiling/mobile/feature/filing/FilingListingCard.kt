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
import coil.compose.AsyncImage
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
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
    val thumb = listing.thumbnailUrl?.takeIf { it.isNotBlank() }
    val advertiser = listing.advertiserType
        ?: listing.businessType?.takeIf { it.isNotBlank() }

    DfCard(onClick = onClick, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            datasetLabel?.takeIf { it.isNotBlank() }?.let {
                DfBadge(
                    text = it,
                    color = DfColors.PurpleContainer,
                    textColor = DfColors.PurpleDark,
                )
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
                    AsyncImage(
                        model = thumb,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(96.dp),
                        contentScale = ContentScale.Crop,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    advertiser?.let {
                        DfBadge(
                            text = it,
                            color = DfColors.GreenLight,
                            textColor = DfColors.Green,
                        )
                    }
                    if (onOpenDivar != null) {
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
