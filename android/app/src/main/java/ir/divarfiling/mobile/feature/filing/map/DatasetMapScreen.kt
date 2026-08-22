@file:OptIn(ExperimentalMaterial3Api::class)

package ir.divarfiling.mobile.feature.filing.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.network.DatasetMapMarkerDto
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private enum class MapSellerFilter { ALL, PERSONAL, CONSULTANT, DISGUISED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetMapScreen(
    onBack: () -> Unit,
    onListingClick: (String) -> Unit,
    viewModel: DatasetMapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mapData = state.mapData
    val allMarkers = remember(mapData) {
        mapData?.markers.orEmpty().filter { it.lat != null && it.lng != null && !it.token.isNullOrBlank() }
    }
    var sellerFilter by remember { mutableStateOf(MapSellerFilter.ALL) }
    var selectedToken by remember { mutableStateOf<String?>(null) }
    val visibleMarkers = remember(allMarkers, sellerFilter) {
        allMarkers.filter { marker ->
            when (sellerFilter) {
                MapSellerFilter.ALL -> true
                MapSellerFilter.PERSONAL -> !marker.isConsultant && !marker.isDisguised
                MapSellerFilter.CONSULTANT -> marker.isConsultant && !marker.isDisguised
                MapSellerFilter.DISGUISED -> marker.isDisguised
            }
        }
    }
    val selected = visibleMarkers.firstOrNull { it.token == selectedToken }
        ?: allMarkers.firstOrNull { it.token == selectedToken }
    val personalCount = allMarkers.count { !it.isConsultant && !it.isDisguised }
    val consultantCount = mapData?.consultantCount?.takeIf { it > 0 }
        ?: allMarkers.count { it.isConsultant && !it.isDisguised }
    val disguisedCount = allMarkers.count { it.isDisguised }
    val context = LocalContext.current

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                state.error != null && mapData == null -> {
                    Column(
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        DfHubPageHeader(
                            title = "نقشه فایل",
                            subtitle = "موقعیت آگهی‌ها روی نقشه",
                            titleIconRes = DfDecorIcons.MapPin,
                            sectionLabel = DfHeaderSections.FILING,
                            onBack = onBack,
                        )
                        DfErrorBanner(state.error!!)
                        DfEmptyState(
                            title = "بارگذاری ناموفق",
                            subtitle = "اتصال را بررسی کنید و دوباره تلاش کنید",
                            variant = DfEmptyVariant.Error,
                            actionLabel = "تلاش مجدد",
                            onAction = viewModel::refresh,
                        )
                    }
                }
                allMarkers.isEmpty() -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DfHubPageHeader(
                            title = mapData?.dataset?.name ?: "نقشه فایل",
                            subtitle = "موقعیت آگهی‌ها روی نقشه",
                            titleIconRes = DfDecorIcons.MapPin,
                            sectionLabel = DfHeaderSections.FILING,
                            onBack = onBack,
                        )
                        DfEmptyState(
                            title = "موقعیت جغرافیایی یافت نشد",
                            subtitle = "برای این فایل مختصات کافی برای نمایش نقشه وجود ندارد.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        DatasetOsmdroidMap(
                            markers = visibleMarkers,
                            selectedToken = selectedToken,
                            onMarkerClick = { token -> selectedToken = token },
                            modifier = Modifier.fillMaxSize(),
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                        ) {
                            MapTopBar(
                                title = mapData?.dataset?.name ?: "نقشه فایل",
                                city = mapData?.config?.datasetCity ?: mapData?.dataset?.city,
                                shown = visibleMarkers.size,
                                total = mapData?.geoCount ?: allMarkers.size,
                                onBack = onBack,
                            )
                            MapFilterBar(
                                selected = sellerFilter,
                                personalCount = personalCount,
                                consultantCount = consultantCount,
                                disguisedCount = disguisedCount,
                                onSelect = {
                                    sellerFilter = it
                                    selectedToken = null
                                },
                            )
                            MapLegendRow()
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(AppSpacing.sm),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            selected?.let { marker ->
                                MapListingPreviewCard(
                                    marker = marker,
                                    onOpen = { marker.token?.let(onListingClick) },
                                    onNavigate = {
                                        val lat = marker.lat ?: return@MapListingPreviewCard
                                        val lng = marker.lng ?: return@MapListingPreviewCard
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng")),
                                        )
                                    },
                                    onDismiss = { selectedToken = null },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapTopBar(
    title: String,
    city: String?,
    shown: Int,
    total: Int,
    onBack: () -> Unit,
) {
    DfDetailPageHeader(
        title = title,
        subtitle = listOfNotNull(
            city?.takeIf { it.isNotBlank() },
            "${DateUtils.toPersianDigits(shown.toString())} از ${DateUtils.toPersianDigits(total.toString())} موقعیت",
        ).joinToString(" · "),
        sectionLabel = DfHeaderSections.FILING,
        titleIconRes = DfDecorIcons.MapPin,
        onBack = onBack,
        showBottomDivider = false,
    )
}

@Composable
private fun MapFilterBar(
    selected: MapSellerFilter,
    personalCount: Int,
    consultantCount: Int,
    disguisedCount: Int,
    onSelect: (MapSellerFilter) -> Unit,
) {
    Surface(color = DfThemeColors.surface().copy(alpha = 0.94f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            listOf(
                Triple(MapSellerFilter.ALL, "همه", personalCount + consultantCount + disguisedCount),
                Triple(MapSellerFilter.PERSONAL, "مالک", personalCount),
                Triple(MapSellerFilter.CONSULTANT, "مشاور", consultantCount),
                Triple(MapSellerFilter.DISGUISED, "پنهان", disguisedCount),
            ).forEach { (value, label, count) ->
                DfSoftChip(
                    text = "$label ${DateUtils.toPersianDigits(count.toString())}",
                    selected = selected == value,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Composable
private fun MapLegendRow() {
    Surface(color = DfThemeColors.surface().copy(alpha = 0.9f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            LegendDot(color = DfColors.Green, label = "مالک واقعی")
            LegendDot(color = DfColors.Blue, label = "مشاور")
            LegendDot(color = DfColors.Amber, label = "مشاور پنهان")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(text = label, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
    }
}

@Composable
private fun MapListingPreviewCard(
    marker: DatasetMapMarkerDto,
    onOpen: () -> Unit,
    onNavigate: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sellerLabel = when {
        marker.isDisguised -> "مشاور پنهان"
        marker.isConsultant -> marker.sellerType?.takeIf { it.isNotBlank() } ?: "مشاور"
        else -> "مالک واقعی"
    }
    val sellerColor = when {
        marker.isDisguised -> DfColors.Amber
        marker.isConsultant -> DfColors.Blue
        else -> DfColors.Green
    }
    Surface(
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        shadowElevation = AppElevations.raised,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfListingImage(
                    thumbnailUrl = marker.thumb,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    contentDescription = marker.title,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = marker.title?.ifBlank { "آگهی بدون عنوان" } ?: "آگهی بدون عنوان",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = marker.priceLabel ?: marker.depositLabel ?: marker.rentLabel ?: "قیمت نامشخص",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.primary(),
                        maxLines = 1,
                    )
                    val specs = listOfNotNull(
                        marker.areaLabel?.takeIf { it.isNotBlank() },
                        marker.rooms?.takeIf { it.isNotBlank() }?.let { rooms ->
                            if ("خواب" in rooms || "اتاق" in rooms) rooms else "$rooms خواب"
                        },
                        marker.neighborhood?.takeIf { it.isNotBlank() },
                    ).joinToString(" · ")
                    if (specs.isNotBlank()) {
                        Text(
                            text = specs,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = DfThemeColors.surfaceVariant(),
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(DfIcons.X, contentDescription = "بستن", tint = DfThemeColors.textSecondary(), modifier = Modifier.size(16.dp))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = AppShapes.Chip, color = sellerColor.copy(alpha = 0.14f)) {
                    Text(
                        text = sellerLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = sellerColor,
                    )
                }
                marker.verdict?.takeIf { it.isNotBlank() }?.let { verdict ->
                    Surface(shape = AppShapes.Chip, color = DfThemeColors.primaryContainer()) {
                        Text(
                            text = verdict,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.primary(),
                        )
                    }
                }
                marker.locationLabel?.takeIf { it.isNotBlank() }?.let { loc ->
                    Text(text = loc, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                DfPrimaryButton(
                    text = "مشاهده فایل",
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                )
                DfSecondaryButton(
                    text = "مسیریابی",
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DatasetOsmdroidMap(
    markers: List<DatasetMapMarkerDto>,
    selectedToken: String?,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var fittedKey by remember { mutableStateOf("") }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                )
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(13.0)
                mapView = this
            }
        },
        update = { view ->
            view.overlays.removeAll { it is Marker }
            val points = markers.mapNotNull { marker ->
                val lat = marker.lat ?: return@mapNotNull null
                val lng = marker.lng ?: return@mapNotNull null
                GeoPoint(lat, lng)
            }
            markers.forEach { item ->
                val lat = item.lat ?: return@forEach
                val lng = item.lng ?: return@forEach
                val token = item.token ?: return@forEach
                val overlay = Marker(view).apply {
                    position = GeoPoint(lat, lng)
                    title = item.title.orEmpty()
                    snippet = item.priceLabel ?: item.depositLabel
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = mapPinDrawable(
                        view.context,
                        pinColorInt(item),
                        highlighted = token == selectedToken,
                    )
                    setOnMarkerClickListener { _, _ ->
                        onMarkerClick(token)
                        true
                    }
                }
                view.overlays.add(overlay)
            }
            val key = markers.joinToString { it.token.orEmpty() }
            if (points.isNotEmpty() && key != fittedKey) {
                fittedKey = key
                val bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(points)
                view.post { view.zoomToBoundingBox(bounds, true, 120) }
            }
            view.invalidate()
        },
    )
    DisposableEffect(Unit) {
        mapView?.onResume()
        onDispose { mapView?.onPause() }
    }
}

private fun pinColorInt(marker: DatasetMapMarkerDto): Int {
    return when {
        marker.isDisguised -> 0xFFF59E0B.toInt()
        marker.isConsultant -> 0xFF2563EB.toInt()
        else -> 0xFF10B981.toInt()
    }
}

private fun mapPinDrawable(context: android.content.Context, color: Int, highlighted: Boolean): Drawable {
    val density = context.resources.displayMetrics.density
    val size = ((if (highlighted) 44 else 36) * density).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val cx = size / 2f
    val cy = size * 0.42f
    val radius = size * 0.28f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    canvas.drawCircle(cx, cy, radius, paint)
    val path = Path().apply {
        moveTo(cx - radius * 0.72f, cy + radius * 0.35f)
        lineTo(cx, size * 0.92f)
        lineTo(cx + radius * 0.72f, cy + radius * 0.35f)
        close()
    }
    canvas.drawPath(path, paint)
    val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE }
    canvas.drawCircle(cx, cy, radius * 0.38f, inner)
    if (highlighted) {
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 3f * density
        }
        canvas.drawCircle(cx, cy, radius + 4f * density, ring)
    }
    return BitmapDrawable(context.resources, bmp)
}
