package ir.divarfiling.mobile.feature.filing

import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfPillChip
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
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

    val mapTitle = state.mapData?.dataset?.name?.let { "نقشه $it" } ?: "نقشه فایل"

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            DfDetailPageHeader(
                title = mapTitle,
                subtitle = "${state.filteredMarkers.size} آگهی",
                titleIcon = DfIcons.Map,
                onBack = onBack,
            )

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = DfColors.Purple)
                }
                state.error != null -> Column(Modifier.padding(AppSpacing.screenHorizontal)) {
                    DfErrorBanner(state.error!!)
                }
                state.mapData == null -> DfEmptyState(
                    title = "نقشه در دسترس نیست",
                    subtitle = "برای این فایل موقعیت مکانی ثبت نشده",
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        val modes = if (state.mapData?.config?.isRent == true) {
                            listOf(MapDisplayMode.Markers, MapDisplayMode.Market, MapDisplayMode.FullDeposit)
                        } else {
                            listOf(MapDisplayMode.Markers, MapDisplayMode.Market, MapDisplayMode.Value)
                        }
                        modes.forEach { mode ->
                            DfPillChip(
                                label = mode.label,
                                selected = state.displayMode == mode,
                                onClick = { viewModel.setDisplayMode(mode) },
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = AppSpacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        SellerFilter.entries.forEach { filter ->
                            DfPillChip(
                                label = filter.label,
                                selected = state.sellerFilter == filter,
                                onClick = { viewModel.setSellerFilter(filter) },
                            )
                        }
                        DfPillChip(
                            label = "زیر بازار",
                            selected = state.underMarketOnly,
                            onClick = { viewModel.setUnderMarketOnly(!state.underMarketOnly) },
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(AppSpacing.screenHorizontal),
                    ) {
                        AndroidView(
                            factory = { mapView },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    state.selectedMarker?.let { marker ->
                        DfPremiumCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.screenHorizontal),
                        ) {
                            Column(
                                modifier = Modifier.padding(AppSpacing.sm),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                Text(marker.title ?: "آگهی", style = AppTypography.cardTitle)
                                Text(buildMarkerSnippet(marker), style = AppTypography.bodyDescription)
                                marker.token?.let { token ->
                                    DfPrimaryButton(
                                        text = "جزئیات آگهی",
                                        onClick = { onListingClick(token) },
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
