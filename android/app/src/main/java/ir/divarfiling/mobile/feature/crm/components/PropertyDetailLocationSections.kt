package ir.divarfiling.mobile.feature.crm.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.network.MapNavigationDto
import ir.divarfiling.mobile.core.network.NearbyPoiGroupDto
import ir.divarfiling.mobile.core.network.NearbyPoiItemDto
import ir.divarfiling.mobile.core.network.NearbyPoisPayloadDto
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.filing.components.ListingMiniMap

@Composable
fun PropertyLocationSection(
    property: PropertyDto,
    canEdit: Boolean,
    isSubmitting: Boolean,
    onEditLocation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lat = property.latitude
    val lng = property.longitude
    val hasCoords = lat != null && lng != null && !(lat == 0.0 && lng == 0.0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfDecorImage(resId = DfDecorIcons.MapPin, size = 16.dp)
                Text("موقعیت", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            }
            if (canEdit) {
                DfSecondaryButton(
                    text = if (hasCoords) "ویرایش روی نقشه" else "ثبت روی نقشه",
                    onClick = onEditLocation,
                    enabled = !isSubmitting,
                    modifier = Modifier,
                )
            }
        }

        DfPremiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    PropertyFilters.locationLabel(property),
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                )
                property.address?.takeIf { it.isNotBlank() }?.let { address ->
                    Text(address, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                }
                if (hasCoords) {
                    ListingMiniMap(
                        latitude = lat!!,
                        longitude = lng!!,
                        title = property.title,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.CardSmall,
                        color = DfColors.SurfaceVariant.copy(alpha = 0.5f),
                    ) {
                        Text(
                            "موقعیت دقیق روی نقشه ثبت نشده است.",
                            modifier = Modifier.padding(AppSpacing.sm),
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyNavigationSection(
    mapNavigation: MapNavigationDto?,
    modifier: Modifier = Modifier,
) {
    if (mapNavigation == null) return
    val context = LocalContext.current
    val apps = buildList {
        if (mapNavigation.googleUrl.isNotBlank()) add(NavApp("گوگل مپ", mapNavigation.googleUrl))
        if (mapNavigation.neshanUrl.isNotBlank()) add(NavApp("نشان", mapNavigation.neshanUrl))
        if (mapNavigation.wazeUrl.isNotBlank()) add(NavApp("ویز", mapNavigation.wazeUrl))
        if (mapNavigation.baladUrl.isNotBlank()) add(NavApp("بلد", mapNavigation.baladUrl))
    }
    if (apps.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(DfIcons.Compass, null, tint = DfColors.Blue, modifier = Modifier.size(16.dp))
            Text("مسیریابی", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            apps.forEach { app ->
                Surface(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.url)))
                    },
                    shape = AppShapes.Chip,
                    color = DfColors.BlueLight,
                    border = BorderStroke(1.dp, DfColors.Blue.copy(alpha = 0.25f)),
                ) {
                    Text(
                        app.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.Blue,
                    )
                }
            }
        }
    }
}

private data class NavApp(val label: String, val url: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyNearbyPoisSection(
    property: PropertyDto,
    nearbyPoisCtx: ir.divarfiling.mobile.core.network.NearbyPoisContextDto?,
    payload: NearbyPoisPayloadDto?,
    isLoading: Boolean,
    canEdit: Boolean,
    onFetch: (refresh: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasCoords = nearbyPoisCtx?.hasCoords == true
        || (property.latitude != null && property.longitude != null)
    val hasStored = nearbyPoisCtx?.hasStored == true || payload != null
    val categoryCount = nearbyPoisCtx?.categoryCount ?: 22

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(DfIcons.MapPin, null, tint = DfColors.Purple, modifier = Modifier.size(16.dp))
                    Text("امکانات و دسترسی‌ها", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                }
                Text(
                    "دسترسی محلی · $categoryCount مورد · شعاع ۳ کیلومتر",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            if (canEdit && hasCoords) {
                androidx.compose.material3.TextButton(
                    onClick = { onFetch(hasStored) },
                    enabled = !isLoading,
                ) {
                    Text(
                        when {
                            isLoading -> "در حال محاسبه…"
                            hasStored -> "بروزرسانی"
                            else -> "محاسبه"
                        },
                        color = DfColors.Purple,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (!hasCoords) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.CardSmall,
                color = DfColors.AmberLight.copy(alpha = 0.5f),
            ) {
                Text(
                    "برای محاسبه امکانات اطراف، ابتدا موقعیت ملک را روی نقشه ثبت کنید.",
                    modifier = Modifier.padding(AppSpacing.sm),
                    style = AppTypography.labelSmall,
                    color = DfColors.Amber,
                )
            }
            return
        }

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = DfColors.Purple,
                )
                Text(
                    "در حال دریافت امکانات اطراف…",
                    modifier = Modifier.padding(start = AppSpacing.sm),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }

        payload?.let { data ->
            if (data.foundCount > 0 || data.groups.isNotEmpty()) {
                NearbyPoisMetaRow(payload = data)
                data.highlights.takeIf { it.isNotEmpty() }?.let { highlights ->
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        highlights.forEach { item ->
                            NearbyPoiHighlightPill(item)
                        }
                    }
                }
                data.groups.forEach { group ->
                    NearbyPoiGroupCard(group)
                }
            } else if (!isLoading) {
                Text(
                    "داده‌ای یافت نشد. دوباره «محاسبه» را بزنید.",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        } ?: if (!isLoading) {
            Text(
                "برای نمایش مترو، بیمارستان، پارک و… دکمه «محاسبه» را بزنید.",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
    }
}

@Composable
private fun NearbyPoisMetaRow(payload: NearbyPoisPayloadDto) {
    val radiusKm = payload.radiusM?.let { if (it >= 1000) "${it / 1000} کیلومتر" else "$it متر" }
    val parts = buildList {
        if (payload.foundCount > 0) add("${payload.foundCount} مورد یافت شد")
        radiusKm?.let { add("شعاع $it") }
        payload.fetchedAtLabel?.takeIf { it.isNotBlank() }?.let { add("بروز $it") }
    }
    if (parts.isEmpty()) return
    Text(
        parts.joinToString(" · "),
        style = AppTypography.labelSmall,
        color = DfColors.Purple,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun NearbyPoiHighlightPill(item: NearbyPoiItemDto) {
    Surface(
        shape = AppShapes.Chip,
        color = DfColors.PurpleContainer.copy(alpha = 0.65f),
    ) {
        Text(
            "${item.label}: ${item.name.ifBlank { item.distanceLabel }}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = AppTypography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NearbyPoiGroupCard(group: NearbyPoiGroupDto) {
    DfPremiumCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "${group.label} (${group.foundCount})",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            group.items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(color = DfThemeColors.outlineSubtle().copy(alpha = 0.35f))
                }
                NearbyPoiRow(item)
            }
        }
    }
}

@Composable
private fun NearbyPoiRow(item: NearbyPoiItemDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Text(
                when {
                    item.found && item.name.isNotBlank() -> item.name
                    item.found -> item.distanceLabel
                    else -> item.emptyLabel.ifBlank { "یافت نشد" }
                },
                style = AppTypography.bodyDescription,
                color = if (item.found) DfColors.TextPrimary else DfColors.TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (item.found) {
            Column(horizontalAlignment = Alignment.End) {
                if (item.distanceLabel.isNotBlank()) {
                    Text(item.distanceLabel, style = AppTypography.labelSmall, fontWeight = FontWeight.Bold)
                }
                if (item.walkLabel.isNotBlank()) {
                    Text(item.walkLabel, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                }
            }
        }
    }
}
