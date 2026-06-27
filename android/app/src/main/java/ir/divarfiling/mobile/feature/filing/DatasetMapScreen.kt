package ir.divarfiling.mobile.feature.filing

import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfGlassButton
import ir.divarfiling.mobile.core.design.components.DfGlassCard
import ir.divarfiling.mobile.core.design.components.DfGlassChip
import ir.divarfiling.mobile.core.design.components.DfGlassTopBar
import ir.divarfiling.mobile.core.design.components.DfLiquidBackground
import ir.divarfiling.mobile.core.network.MapMarkerDto
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun DatasetMapScreen(
    onBack: () -> Unit,
    onListingClick: (String) -> Unit,
    viewModel: DatasetMapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    LaunchedEffect(state.filteredMarkers, state.displayMode) {
        mapView.overlays.clear()
        val points = mutableListOf<GeoPoint>()
        state.filteredMarkers.forEach { marker ->
            val point = GeoPoint(marker.lat, marker.lng)
            points.add(point)
            val overlay = Marker(mapView).apply {
                position = point
                title = marker.title
                snippet = buildMarkerSnippet(marker)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = markerDrawable(context, marker, state.displayMode)
                setOnMarkerClickListener { m, _ ->
                    viewModel.selectMarker(marker)
                    m.showInfoWindow()
                    true
                }
            }
            mapView.overlays.add(overlay)
        }
        if (points.isNotEmpty()) {
            val lats = points.map { it.latitude }
            val lngs = points.map { it.longitude }
            val box = BoundingBox(
                lats.maxOrNull() ?: 0.0,
                lngs.maxOrNull() ?: 0.0,
                lats.minOrNull() ?: 0.0,
                lngs.minOrNull() ?: 0.0,
            )
            mapView.zoomToBoundingBox(box.increaseByScale(1.15f), true)
        }
        mapView.invalidate()
    }

    Box(Modifier.fillMaxSize()) {
        DfLiquidBackground()
        Column(Modifier.fillMaxSize()) {
            DfGlassTopBar(
                title = state.mapData?.dataset?.name?.let { "نقشه $it" } ?: "نقشه فایل",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Column(Modifier.padding(16.dp)) {
                    DfErrorBanner(state.error!!)
                }
                state.mapData == null -> DfEmptyState(
                    title = "نقشه در دسترس نیست",
                    subtitle = "برای این فایل موقعیت مکانی ثبت نشده",
                )
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val modes = if (state.mapData?.config?.isRent == true) {
                            listOf(MapDisplayMode.Markers, MapDisplayMode.Market, MapDisplayMode.FullDeposit)
                        } else {
                            listOf(MapDisplayMode.Markers, MapDisplayMode.Market, MapDisplayMode.Value)
                        }
                        modes.forEach { mode ->
                            DfGlassChip(
                                text = mode.label,
                                selected = state.displayMode == mode,
                                onClick = { viewModel.setDisplayMode(mode) },
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SellerFilter.entries.forEach { filter ->
                            DfGlassChip(
                                text = filter.label,
                                selected = state.sellerFilter == filter,
                                onClick = { viewModel.setSellerFilter(filter) },
                            )
                        }
                        DfGlassChip(
                            text = "زیر بازار",
                            selected = state.underMarketOnly,
                            onClick = { viewModel.setUnderMarketOnly(!state.underMarketOnly) },
                        )
                    }

                    Text(
                        "${state.filteredMarkers.size} آگهی روی نقشه",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(12.dp),
                    ) {
                        AndroidView(
                            factory = { mapView },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    state.selectedMarker?.let { marker ->
                        DfGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(marker.title ?: "آگهی", style = AppTypography.cardTitle)
                                Text(buildMarkerSnippet(marker), style = AppTypography.bodyDescription)
                                marker.token?.let { token ->
                                    DfGlassButton(
                                        text = "جزئیات آگهی",
                                        onClick = { onListingClick(token) },
                                        selected = true,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildMarkerSnippet(marker: MapMarkerDto): String =
    listOfNotNull(
        marker.priceLabel?.takeIf { it.isNotBlank() },
        marker.depositLabel?.takeIf { it.isNotBlank() },
        marker.rentLabel?.takeIf { it.isNotBlank() },
        marker.areaLabel?.takeIf { it.isNotBlank() },
        marker.neighborhood?.takeIf { it.isNotBlank() },
        marker.verdict?.takeIf { it.isNotBlank() },
    ).joinToString(" · ")

private fun markerDrawable(
    context: android.content.Context,
    marker: MapMarkerDto,
    mode: MapDisplayMode,
): android.graphics.drawable.Drawable {
    val color = when (mode) {
        MapDisplayMode.Markers -> if (marker.isConsultant) 0xFF7C3AED.toInt() else 0xFF2563EB.toInt()
        MapDisplayMode.Market -> when (marker.marketTier) {
            "under" -> 0xFF10B981.toInt()
            "over" -> 0xFFF43F5E.toInt()
            else -> 0xFFF59E0B.toInt()
        }
        MapDisplayMode.FullDeposit, MapDisplayMode.Value -> 0xFF8B5CF6.toInt()
    }
    val size = (28 * context.resources.displayMetrics.density).toInt().coerceAtLeast(24)
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setSize(size, size)
        setStroke((2 * context.resources.displayMetrics.density).toInt(), 0xFFFFFFFF.toInt())
    }
}

private fun BoundingBox.increaseByScale(scale: Float): BoundingBox {
    val latPad = (latNorth - latSouth) * (scale - 1f) / 2f
    val lonPad = (lonEast - lonWest) * (scale - 1f) / 2f
    return BoundingBox(latNorth + latPad, lonEast + lonPad, latSouth - latPad, lonWest - lonPad)
}
