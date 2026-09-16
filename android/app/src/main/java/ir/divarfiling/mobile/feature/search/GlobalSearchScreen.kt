package ir.divarfiling.mobile.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.GlobalSearchDestinationDto
import ir.divarfiling.mobile.core.network.GlobalSearchItemDto
import ir.divarfiling.mobile.navigation.Routes

@Composable
fun GlobalSearchScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: GlobalSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item {
                DfDetailPageHeader(
                    title = "جستجو",
                    subtitle = "مخاطب، فایل شخصی، معامله، آگهی و فایلینگ",
                    sectionLabel = DfHeaderSections.CRM,
                    titleIconRes = DfDecorIcons.Search,
                    onBack = onBack,
                )
            }
            item {
                DfTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenHorizontal)
                        .focusRequester(focusRequester),
                    label = "جستجو",
                    placeholder = "حداقل ${state.minQueryLength} کاراکتر…",
                    singleLine = true,
                )
            }
            item {
                DfPullRefresh(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    when {
                        state.error != null && state.groups.isEmpty() -> {
                            DfErrorBanner(
                                state.error!!,
                                onRetry = viewModel::refresh,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        state.isLoading -> {
                            DfCardListSkeleton(
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        state.query.length < state.minQueryLength -> {
                            DfEmptyState(
                                title = "عبارت جستجو را وارد کنید",
                                subtitle = "حداقل ${state.minQueryLength} کاراکتر",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        state.groups.isEmpty() -> {
                            DfEmptyState(
                                title = "نتیجه‌ای پیدا نشد",
                                subtitle = "عبارت دیگری امتحان کنید",
                                variant = DfEmptyVariant.NoResults,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        else -> {
                            Column(
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                state.groups.forEach { group ->
                                    Text(
                                        text = group.label,
                                        style = AppTypography.sectionTitle,
                                        fontWeight = FontWeight.Bold,
                                        color = DfThemeColors.textPrimary(),
                                        modifier = Modifier.padding(
                                            top = AppSpacing.sm,
                                            bottom = AppSpacing.xxs,
                                        ),
                                    )
                                    group.items.forEach { item ->
                                        GlobalSearchRow(
                                            item = item,
                                            onClick = { onNavigate(routeForDestination(item.destination)) },
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
}

@Composable
private fun GlobalSearchRow(
    item: GlobalSearchItemDto,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Chip,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacing.md,
                    vertical = AppSpacing.sm,
                ),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textMuted(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

fun routeForDestination(dest: GlobalSearchDestinationDto): String {
    return when (dest.kind.ifBlank { dest.id.orEmpty() }) {
        "contact" -> Routes.contactDetail(dest.contactId ?: 0)
        "property" -> Routes.propertyDetail(dest.propertyId ?: 0)
        "deal" -> Routes.dealDetail(dest.dealId ?: 0)
        "listing" -> Routes.listingDetail(dest.token.orEmpty())
        "dataset" -> Routes.listings(dest.datasetId.orEmpty())
        else -> when {
            dest.contactId != null -> Routes.contactDetail(dest.contactId)
            dest.propertyId != null -> Routes.propertyDetail(dest.propertyId)
            dest.dealId != null -> Routes.dealDetail(dest.dealId)
            !dest.token.isNullOrBlank() -> Routes.listingDetail(dest.token)
            !dest.datasetId.isNullOrBlank() -> Routes.listings(dest.datasetId)
            else -> Routes.HOME
        }
    }
}
