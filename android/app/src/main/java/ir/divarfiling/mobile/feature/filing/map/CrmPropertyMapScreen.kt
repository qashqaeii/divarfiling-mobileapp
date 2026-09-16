package ir.divarfiling.mobile.feature.filing.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.network.DatasetMapMarkerDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmPropertyMapScreen(
    onBack: () -> Unit,
    onPropertyClick: (Long) -> Unit,
    viewModel: CrmPropertyMapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val markers = state.mapData?.markers.orEmpty()
        .filter { it.propertyId != null && it.lat != null && it.lng != null }
    val selected = markers.firstOrNull { it.propertyId == state.selectedPropertyId }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        val result = snackbar.showSnackbar(message = message, actionLabel = "تلاش مجدد")
        viewModel.clearSnackbar()
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.retryAfterError()
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isInitialLoading -> DfDetailSkeleton()
                state.error != null && state.mapData == null -> {
                    Column(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        DfDetailPageHeader(
                            title = "نقشه فایل‌های شخصی",
                            subtitle = "موقعیت فایل‌ها",
                            sectionLabel = DfHeaderSections.CRM,
                            titleIconRes = DfDecorIcons.MapPin,
                            onBack = onBack,
                        )
                        DfErrorBanner(state.error!!)
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        FilingOsmdroidMap(
                            markers = markers,
                            selectedToken = selected?.token,
                            onMarkerClick = { token ->
                                val id = markers.firstOrNull { it.token == token }?.propertyId
                                viewModel.selectProperty(id)
                            },
                            modifier = Modifier.fillMaxSize(),
                            autoFitMarkers = state.mapViewport == null && markers.isNotEmpty(),
                            restoreViewport = state.mapViewport,
                            onViewportChanged = viewModel::onViewportChanged,
                        )
                        DfDetailPageHeader(
                            title = "نقشه فایل‌های شخصی",
                            subtitle = when {
                                state.isMapLoading -> "در حال به‌روزرسانی…"
                                markers.isEmpty() -> "در این محدوده فایلی نیست"
                                else -> "${markers.size} فایل در محدوده"
                            },
                            sectionLabel = DfHeaderSections.CRM,
                            titleIconRes = DfDecorIcons.MapPin,
                            onBack = onBack,
                        )
                        if (markers.isEmpty() && !state.isMapLoading && state.mapData != null) {
                            DfEmptyState(
                                title = "در این محدوده فایلی پیدا نشد",
                                subtitle = "نقشه را جابه‌جا کنید یا فیلترها را تغییر دهید",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        selected?.let { marker ->
                            PropertyMapPreviewCard(
                                marker = marker,
                                onOpen = { marker.propertyId?.let(onPropertyClick) },
                                onDismiss = { viewModel.selectProperty(null) },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyMapPreviewCard(
    marker: DatasetMapMarkerDto,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MapListingPreviewCard(
            marker = marker,
            onOpen = onOpen,
            onNavigate = onDismiss,
            onDismiss = onDismiss,
        )
        DfPrimaryButton(
            text = "مشاهده فایل",
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.xs),
        )
    }
}
