package ir.divarfiling.mobile.feature.filing.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.network.DatasetMapMarkerDto
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetMapScreen(
    onBack: () -> Unit,
    onListingClick: (String) -> Unit,
    viewModel: DatasetMapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mapData = state.mapData
    val markers = remember(mapData) {
        mapData?.markers.orEmpty().filter { it.lat != null && it.lng != null && !it.token.isNullOrBlank() }
    }

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
                            onBack = onBack,
                        )
                        DfErrorBanner(state.error!!)
                    }
                }
                markers.isEmpty() -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DfHubPageHeader(
                            title = mapData?.dataset?.name ?: "نقشه فایل",
                            subtitle = "موقعیت آگهی‌ها روی نقشه",
                            titleIconRes = DfDecorIcons.MapPin,
                            onBack = onBack,
                        )
                        DfEmptyState(
                            title = "موقعیت جغرافیایی یافت نشد",
                            subtitle = "برای این فایل مختصات کافی برای نمایش نقشه وجود ندارد.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        DfHubPageHeader(
                            title = mapData?.dataset?.name ?: "نقشه فایل",
                            subtitle = "${mapData?.markersShown ?: markers.size} از ${mapData?.geoCount ?: markers.size} موقعیت",
                            titleIconRes = DfDecorIcons.MapPin,
                            onBack = onBack,
                        )
                        state.error?.let { error ->
                            DfErrorBanner(
                                error,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.sm),
                        ) {
                            DatasetOsmdroidMap(
                                markers = markers,
                                onMarkerClick = { token -> onListingClick(token) },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatasetOsmdroidMap(
    markers: List<DatasetMapMarkerDto>,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier) {
        var mapView by remember { mutableStateOf<MapView?>(null) }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)
                    val points = markers.mapNotNull { marker ->
                        val lat = marker.lat ?: return@mapNotNull null
                        val lng = marker.lng ?: return@mapNotNull null
                        GeoPoint(lat, lng)
                    }
                    if (points.isNotEmpty()) {
                        val bounds = org.osmdroid.util.BoundingBox.fromGeoPoints(points)
                        post { zoomToBoundingBox(bounds, true, 64) }
                    }
                    markers.forEach { item ->
                        val lat = item.lat ?: return@forEach
                        val lng = item.lng ?: return@forEach
                        val token = item.token ?: return@forEach
                        val overlay = Marker(this).apply {
                            position = GeoPoint(lat, lng)
                            title = item.title.orEmpty()
                            snippet = item.priceLabel ?: item.price
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { marker, _ ->
                                onMarkerClick(token)
                                marker.showInfoWindow()
                                true
                            }
                        }
                        overlays.add(overlay)
                    }
                    mapView = this
                }
            },
        )
        DisposableEffect(Unit) {
            mapView?.onResume()
            onDispose { mapView?.onPause() }
        }
    }
}
