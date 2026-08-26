package ir.divarfiling.mobile.feature.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextField

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SupportTicketsScreen(
    onBack: () -> Unit,
    onOpenTicket: (Long) -> Unit,
    viewModel: SupportTicketsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val filteredTickets = remember(state.tickets, state.statusFilter) {
        state.statusFilter.applyClientFilter(state.tickets)
    }

    LaunchedEffect(state.navigateToTicketId) {
        state.navigateToTicketId?.let { ticketId ->
            viewModel.consumeNavigation()
            onOpenTicket(ticketId)
        }
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    if (state.showCreateDialog) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleCreateDialog(false) }) {
            DfSheetScaffold(
                title = "تیکت جدید",
                subtitle = "موضوع و شرح درخواست خود را بنویسید",
                icon = DfIcons.MessageCircle,
                onClose = { viewModel.toggleCreateDialog(false) },
                footer = {
                    DfSheetActions(
                        primaryText = if (state.isSubmitting) "در حال ثبت…" else "ثبت تیکت",
                        onPrimary = viewModel::createTicket,
                        primaryEnabled = !state.isSubmitting &&
                            state.subject.isNotBlank() &&
                            state.body.trim().length >= 10,
                        isSubmitting = state.isSubmitting,
                        onSecondary = { viewModel.toggleCreateDialog(false) },
                    )
                },
            ) {
                DfSheetSection(title = "جزئیات درخواست") {
                    DfTextField(
                        value = state.subject,
                        onValueChange = viewModel::onSubjectChange,
                        label = "موضوع",
                        enabled = !state.isSubmitting,
                    )
                    DfTextField(
                        value = state.body,
                        onValueChange = viewModel::onBodyChange,
                        label = "متن درخواست",
                        singleLine = false,
                        minLines = 4,
                        enabled = !state.isSubmitting,
                    )
                }
                DfSheetSection(title = "دسته‌بندی") {
                    SupportChipGroup(
                        options = supportCategoryOptions,
                        selected = state.category,
                        onSelect = viewModel::onCategoryChange,
                        enabled = !state.isSubmitting,
                    )
                }
                DfSheetSection(title = "اولویت") {
                    SupportChipGroup(
                        options = supportPriorityOptions,
                        selected = state.priority,
                        onSelect = viewModel::onPriorityChange,
                        enabled = !state.isSubmitting,
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            DfExtendedFab(
                text = "تیکت جدید",
                icon = DfIcons.Plus,
                onClick = { viewModel.toggleCreateDialog(true) },
            )
        },
    ) { padding ->
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
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance + AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "پشتیبانی",
                        subtitle = "مرکز تیکت و درخواست کمک",
                        sectionLabel = DfHeaderSections.SUPPORT,
                        titleIconRes = DfDecorIcons.Phone,
                        onBack = onBack,
                    )
                }
                if (!state.isLoading || state.stats.total > 0) {
                    item {
                        SupportStatsRow(
                            stats = state.stats,
                            selectedFilter = state.statusFilter,
                            onFilterSelect = viewModel::onStatusFilterChange,
                        )
                    }
                }
                if (state.stats.unreadReplies > 0) {
                    item {
                        SupportUnreadBanner(
                            unreadCount = state.stats.unreadReplies,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                item {
                    DfSearchField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchChange,
                        placeholder = "جستجو در عنوان یا شماره تیکت…",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                if (state.tickets.isNotEmpty() || state.stats.total > 0) {
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.screenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            SupportTicketFilter.entries.forEach { filter ->
                                val count = filter.count(state.stats, state.tickets)
                                FilterChip(
                                    selected = state.statusFilter == filter,
                                    onClick = { viewModel.onStatusFilterChange(filter) },
                                    label = {
                                        Text(if (count > 0) "${filter.label} ($count)" else filter.label)
                                    },
                                )
                            }
                        }
                    }
                }
                state.error?.let { error ->
                    item {
                        DfStatusBanner(
                            message = error,
                            tone = DfStatusTone.Error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.isLoading) {
                    item {
                        DfCardListSkeleton(
                            count = 4,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (state.tickets.isEmpty() && state.searchQuery.isBlank()) {
                    item {
                        DfEmptyState(
                            title = "تیکتی ثبت نشده",
                            subtitle = "برای ارتباط با پشتیبانی یک تیکت جدید بسازید.",
                            variant = DfEmptyVariant.Empty,
                            actionLabel = "تیکت جدید",
                            onAction = { viewModel.toggleCreateDialog(true) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (filteredTickets.isEmpty()) {
                    item {
                        DfEmptyState(
                            title = "نتیجه‌ای یافت نشد",
                            subtitle = "عبارت جستجو یا فیلتر را تغییر دهید.",
                            variant = DfEmptyVariant.NoResults,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(filteredTickets, key = { it.id }) { ticket ->
                        SupportTicketListCard(
                            ticket = ticket,
                            onClick = { onOpenTicket(ticket.id) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }
}
