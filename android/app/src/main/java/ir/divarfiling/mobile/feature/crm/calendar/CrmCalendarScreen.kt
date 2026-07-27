package ir.divarfiling.mobile.feature.crm.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.network.TodayItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmCalendarScreen(
    onBack: () -> Unit,
    viewModel: CrmCalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "تقویم CRM",
                        subtitle = "یادآورها و پیگیری‌ها بر اساس تاریخ",
                        titleIconRes = DfDecorIcons.Calendar,
                        onBack = onBack,
                    )
                }
                state.error?.let { error ->
                    item {
                        DfErrorBanner(error, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                }
                if (state.isLoading) {
                    item {
                        DfCardListSkeleton(
                            count = 5,
                            itemHeight = 96.dp,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (state.groups.isEmpty()) {
                    item {
                        DfEmptyState(
                            title = "رویدادی یافت نشد",
                            subtitle = "یادآورهای CRM در اینجا نمایش داده می‌شوند.",
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    state.groups.forEach { group ->
                        item {
                            DfSectionHeader(group.dateLabel, group.items.size)
                        }
                        items(group.items, key = { "${group.dateLabel}-${it.reminder?.id}-${it.contact?.id}" }) { item ->
                            CalendarItemCard(
                                item = item,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarItemCard(item: TodayItemDto, modifier: Modifier = Modifier) {
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                item.reminder?.title ?: item.contact?.fullName.orEmpty(),
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
            )
            item.contact?.fullName?.takeIf { item.reminder != null }?.let { name ->
                Text(name, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
            }
            item.reminder?.dueAt?.let { due ->
                Text(due.replace('T', ' '), style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            item.type?.takeIf { it.isNotBlank() }?.let { type ->
                Text(type, style = AppTypography.labelSmall, color = DfColors.Purple)
            }
        }
    }
}
