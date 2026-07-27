package ir.divarfiling.mobile.feature.extract

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import android.net.Uri
import ir.divarfiling.mobile.core.AppLinks
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.LicenseGateBanner
import ir.divarfiling.mobile.feature.extract.components.ExtractFiltersCard
import ir.divarfiling.mobile.feature.extract.components.ExtractHeader
import ir.divarfiling.mobile.feature.extract.components.ExtractLoadingExperience
import ir.divarfiling.mobile.feature.extract.components.ExtractLocationCard
import ir.divarfiling.mobile.feature.extract.components.ExtractScheduleCard
import ir.divarfiling.mobile.feature.extract.components.ExtractStartButton
import ir.divarfiling.mobile.feature.extract.components.ExtractStatsCard
import ir.divarfiling.mobile.feature.extract.components.extractPhaseFromProgress
import ir.divarfiling.mobile.feature.extract.components.formatAverageTimeLabel
import ir.divarfiling.mobile.feature.extract.components.formatSuccessfulCountLabel
import ir.divarfiling.mobile.feature.extract.components.formatLastExtractionLabel
import ir.divarfiling.mobile.feature.extract.components.rememberDebouncedQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractScreen(
    onViewDataset: (String) -> Unit,
    onOpenSchedules: () -> Unit = {},
    onBack: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    viewModel: ExtractViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    fun openWeb(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
    val canExtract = state.license.canUseLightExtract && state.gateMessage == null
    val tx = ExtractCategories.transactionTypes.firstOrNull { it.label == state.transactionType }
    val subcategories = tx?.subcategories.orEmpty()
    val districtName = state.districts.firstOrNull { it.id == state.districtId }?.name

    rememberDebouncedQuery(state.placeQuery) { viewModel.onPlaceSearchDebounced(it) }

    DfPullRefresh(
        isRefreshing = false,
        onRefresh = viewModel::refreshGate,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = AppSpacing.xxxl + AppSpacing.bottomNavHeight),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                ExtractHeader(
                    userName = state.userName,
                    notificationCount = state.notificationBadgeCount,
                    onNotificationsClick = onNotificationsClick,
                    onMenuClick = onMenuClick,
                    onBack = onBack,
                )
            }

            state.gateMessage?.let { gate ->
                item {
                    LicenseGateBanner(
                        message = gate,
                        onBuyLicense = { openWeb(AppLinks.SHOP_BOT) },
                        onOpenDashboard = { openWeb(AppLinks.DASHBOARD_LICENSES) },
                        onRefresh = viewModel::refreshGate,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            if (state.gateMessage == null && !state.canExtractNow && state.remainingToday != null) {
                item {
                    DfStatusBanner(
                        message = "سقف استخراج روزانه تکمیل شده است. فردا دوباره تلاش کنید.",
                        tone = DfStatusTone.Warning,
                        title = "محدودیت روزانه",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            if (state.remainingToday != null && state.extractionsDailyLimit != null) {
                item {
                    ExtractStatsCard(
                        extractionsToday = state.extractionsToday ?: 0,
                        remainingToday = state.remainingToday ?: 0,
                        dailyLimit = state.extractionsDailyLimit ?: 0,
                        canExtractNow = state.canExtractNow,
                        lastExtractionLabel = formatLastExtractionLabel(
                            extractionsToday = state.extractionsToday ?: 0,
                            hasRecentUpload = state.lastUploadStats != null,
                            lastExtractionAtMs = state.lastExtractionAtMs,
                        ),
                        successfulCountLabel = formatSuccessfulCountLabel(
                            persistedCount = state.lastSuccessfulIngestedCount,
                            sessionCount = state.lastUploadStats?.ingestedCount,
                        ),
                        averageTimeLabel = formatAverageTimeLabel(
                            sessionMinutes = state.lastExtractionDurationMinutes,
                            persistedAverageMinutes = state.averageExtractionDurationMinutes,
                        ),
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            item {
                ExtractLocationCard(
                    query = state.placeQuery,
                    suggestions = state.placeSuggestions,
                    provinceName = state.provinceName,
                    cityName = state.cityName,
                    districtName = districtName,
                    provinces = state.provinces,
                    cities = state.cities,
                    districts = state.districts,
                    districtId = state.districtId,
                    enabled = canExtract && !state.isRunning,
                    onQueryChange = viewModel::onPlaceQueryChange,
                    onSuggestionSelect = viewModel::onPlaceSuggestionSelect,
                    onProvinceChange = viewModel::onProvinceChange,
                    onCityChange = viewModel::onCityChange,
                    onDistrictChange = viewModel::onDistrictChange,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            item {
                ExtractFiltersCard(
                    state = state,
                    subcategories = subcategories,
                    enabled = canExtract && !state.isRunning,
                    onTransactionTypeChange = viewModel::onTransactionTypeChange,
                    onSubcategoryChange = viewModel::onSubcategoryChange,
                    onSortChange = viewModel::onSortChange,
                    onAdvertiserFilterChange = viewModel::onAdvertiserFilterChange,
                    onMaxItemsChange = viewModel::onMaxItemsChange,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    advancedFilters = {
                        AdvancedFilters(state, canExtract, viewModel)
                    },
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            if (state.isRunning) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        ExtractLoadingExperience(
                            phase = extractPhaseFromProgress(
                                state.progressCurrent,
                                state.progressTotal,
                                state.isRunning,
                            ),
                            progressCurrent = state.progressCurrent,
                            progressTotal = state.progressTotal,
                        )
                        DfPrimaryButton(text = "لغو استخراج", onClick = viewModel::cancel)
                    }
                }
            } else {
                item {
                    ExtractStartButton(
                        enabled = canExtract && state.canExtractNow && state.maxItems > 0,
                        onClick = viewModel::startExtraction,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            item {
                ExtractScheduleCard(
                    selectedHours = state.scheduleIntervalHours,
                    enabled = canExtract && !state.isRunning,
                    onSelect = viewModel::onScheduleIntervalSelect,
                    onOpenSchedules = onOpenSchedules,
                    onCreateSchedule = viewModel::createSchedule,
                    canCreateSchedule = canExtract && !state.isRunning,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            state.message?.let {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        DfStatusBanner(
                            message = it,
                            tone = DfStatusTone.Success,
                            title = "استخراج انجام شد",
                        )
                        state.lastUploadStats?.let { stats ->
                            IngestStatsCard(stats)
                        }
                        state.lastDatasetId?.let { id ->
                            DfPrimaryButton(text = "مشاهده در فایلینگ", onClick = { onViewDataset(id) })
                        }
                    }
                }
            }

            state.error?.let { error ->
                item {
                    DfErrorBanner(
                        error,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedFilters(
    state: ExtractUiState,
    canExtract: Boolean,
    viewModel: ExtractViewModel,
) {
    val fieldsEnabled = canExtract && !state.isRunning
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (state.isRent) {
            FilterRangeRow(
                leftLabel = "ودیعه از",
                leftValue = state.depositMin,
                onLeftChange = viewModel::onDepositMinChange,
                rightLabel = "ودیعه تا",
                rightValue = state.depositMax,
                onRightChange = viewModel::onDepositMaxChange,
                enabled = fieldsEnabled,
            )
            FilterRangeRow(
                leftLabel = "اجاره از",
                leftValue = state.rentMin,
                onLeftChange = viewModel::onRentMinChange,
                rightLabel = "اجاره تا",
                rightValue = state.rentMax,
                onRightChange = viewModel::onRentMaxChange,
                enabled = fieldsEnabled,
            )
        } else {
            FilterRangeRow(
                leftLabel = "قیمت از",
                leftValue = state.priceMin,
                onLeftChange = viewModel::onPriceMinChange,
                rightLabel = "قیمت تا",
                rightValue = state.priceMax,
                onRightChange = viewModel::onPriceMaxChange,
                enabled = fieldsEnabled,
            )
        }
        FilterRangeRow(
            leftLabel = "متراژ از",
            leftValue = state.areaMin,
            onLeftChange = viewModel::onAreaMinChange,
            rightLabel = "متراژ تا",
            rightValue = state.areaMax,
            onRightChange = viewModel::onAreaMaxChange,
            enabled = fieldsEnabled,
        )
        FilterRangeRow(
            leftLabel = "سال ساخت از",
            leftValue = state.yearMin,
            onLeftChange = viewModel::onYearMinChange,
            rightLabel = "سال ساخت تا",
            rightValue = state.yearMax,
            onRightChange = viewModel::onYearMaxChange,
            enabled = fieldsEnabled,
        )
        OutlinedTextField(
            value = state.rooms,
            onValueChange = viewModel::onRoomsChange,
            label = { Text("تعداد اتاق") },
            placeholder = { Text("مثلاً ۲ یا ۲,۳") },
            modifier = Modifier.fillMaxWidth(),
            enabled = fieldsEnabled,
            singleLine = true,
            shape = ir.divarfiling.mobile.core.design.AppShapes.Field,
        )
    }
}

@Composable
private fun FilterRangeRow(
    leftLabel: String,
    leftValue: String,
    onLeftChange: (String) -> Unit,
    rightLabel: String,
    rightValue: String,
    onRightChange: (String) -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        OutlinedTextField(
            value = leftValue,
            onValueChange = onLeftChange,
            label = { Text(leftLabel) },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            singleLine = true,
            shape = ir.divarfiling.mobile.core.design.AppShapes.Field,
        )
        OutlinedTextField(
            value = rightValue,
            onValueChange = onRightChange,
            label = { Text(rightLabel) },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            singleLine = true,
            shape = ir.divarfiling.mobile.core.design.AppShapes.Field,
        )
    }
}

@Composable
private fun IngestStatsCard(stats: ir.divarfiling.mobile.core.network.ExtractionUploadData) {
    DfCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            stats.datasetName?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
            }
            IngestStatRow("پردازش‌شده", stats.ingestedCount)
            IngestStatRow("جدید", stats.createdCount)
            IngestStatRow("به‌روزرسانی", stats.updatedCount)
            if (stats.genuinePersonalCount > 0) {
                IngestStatRow("مالک واقعی", stats.genuinePersonalCount)
            }
            if (stats.disguisedConsultantCount > 0) {
                IngestStatRow("مشاور پنهان", stats.disguisedConsultantCount)
            }
            if (stats.duplicateCount > 0) {
                IngestStatRow("تکراری/ردشده", stats.duplicateCount + stats.skippedCount)
            } else if (stats.skippedCount > 0) {
                IngestStatRow("ردشده", stats.skippedCount)
            }
            IngestStatRow("کل فایلینگ", stats.totalInDataset)
            if (stats.datasetMerged) {
                Text(
                    "با فایلینگ قبلی ادغام شد",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
        }
    }
}

@Composable
private fun IngestStatRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
        Text(
            value.toString(),
            style = AppTypography.bodyDescription,
            fontWeight = FontWeight.SemiBold,
            color = DfThemeColors.textPrimary(),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun ExtractScreenPreview() {
    DivarFilingTheme {
        ExtractScreenContentPreview()
    }
}

@Composable
internal fun ExtractScreenContentPreview() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl + AppSpacing.bottomNavHeight),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item {
            ExtractHeader(
                userName = "حسین",
                notificationCount = 9,
                onNotificationsClick = {},
                onMenuClick = {},
            )
        }
        item {
            ExtractStatsCard(
                extractionsToday = 2,
                remainingToday = 40,
                dailyLimit = 100,
                canExtractNow = true,
                lastExtractionLabel = "امروز، 08:30",
                successfulCountLabel = "120",
                averageTimeLabel = "2.4 دقیقه",
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }
    }
}
