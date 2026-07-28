package ir.divarfiling.mobile.feature.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.SupportTicketDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketsScreen(
    onBack: () -> Unit,
    onOpenTicket: (Long) -> Unit,
    viewModel: SupportTicketsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var selectedFilter by remember { androidx.compose.runtime.mutableStateOf(SupportTicketFilter.All) }
    val filteredTickets = remember(state.tickets, selectedFilter) {
        when (selectedFilter) {
            SupportTicketFilter.All -> state.tickets
            SupportTicketFilter.Unread -> state.tickets.filter { it.userHasUnread }
            SupportTicketFilter.Open -> state.tickets.filter { it.status == "open" || it.status == "in_review" }
            SupportTicketFilter.WaitingUser -> state.tickets.filter { it.status == "waiting_user" }
            SupportTicketFilter.Closed -> state.tickets.filter { it.status == "closed" }
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
                            state.body.isNotBlank(),
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
                        subtitle = "تیکت‌ها و درخواست‌های کمک",
                        titleIconRes = DfDecorIcons.Phone,
                        onBack = onBack,
                    )
                }
                if (state.tickets.isNotEmpty()) {
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.screenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            SupportTicketFilter.entries.forEach { filter ->
                                val count = filter.count(state.tickets)
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { selectedFilter = filter },
                                    label = {
                                        Text(if (count > 0) "${filter.label} $count" else filter.label)
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
                } else if (state.tickets.isEmpty()) {
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
                            title = "تیکتی با این فیلتر نیست",
                            subtitle = "فیلتر دیگری را انتخاب کنید یا همه تیکت‌ها را ببینید.",
                            variant = DfEmptyVariant.NoResults,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(filteredTickets, key = { it.id }) { ticket ->
                        TicketCard(
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

private enum class SupportTicketFilter(val label: String) {
    All("همه"),
    Unread("جدید"),
    Open("باز"),
    WaitingUser("منتظر شما"),
    Closed("بسته");

    fun count(items: List<SupportTicketDto>): Int = when (this) {
        All -> items.size
        Unread -> items.count { it.userHasUnread }
        Open -> items.count { it.status == "open" || it.status == "in_review" }
        WaitingUser -> items.count { it.status == "waiting_user" }
        Closed -> items.count { it.status == "closed" }
    }
}

@Composable
private fun TicketCard(
    ticket: SupportTicketDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (badgeBg, badgeFg) = ticketStatusColors(ticket.status)
    DfCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = DfThemeColors.surface(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    ticket.subject,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (ticket.userHasUnread) {
                    DfBadge(
                        text = "جدید",
                        color = AppColors.RoseLight,
                        textColor = AppColors.Rose,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "#${ticket.ticketNumber}",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
                DfBadge(
                    text = ticketStatusLabel(ticket.status),
                    color = badgeBg,
                    textColor = badgeFg,
                )
                ticket.priority.takeIf { it.isNotBlank() }?.let {
                    DfBadge(
                        text = supportPriorityLabel(it),
                        color = AppColors.PurpleContainer,
                        textColor = AppColors.PurpleDark,
                    )
                }
            }
            ticket.category.takeIf { it.isNotBlank() }?.let {
                Text(
                    supportCategoryLabel(it),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            ticket.lastMessageAt?.let {
                Text(
                    DateUtils.formatForDisplay(it),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                )
            } ?: ticket.createdAt?.let {
                Text(
                    DateUtils.formatForDisplay(it),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                )
            }
        }
    }
}

@Composable
private fun SupportChipGroup(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun ticketStatusColors(status: String): Pair<Color, Color> =
    when (status) {
        "open" -> AppColors.BlueLight to AppColors.Blue
        "in_review" -> AppColors.AmberLight to AppColors.Amber
        "answered" -> AppColors.GreenLight to AppColors.Green
        "waiting_user" -> AppColors.PurpleContainer to AppColors.PurpleDark
        "closed" -> DfThemeColors.lockedContainer() to DfThemeColors.onLocked()
        else -> DfThemeColors.primaryContainer() to DfThemeColors.onPrimaryContainer()
    }

private val supportCategoryOptions = listOf(
    "other" to "عمومی",
    "billing" to "پرداخت و اشتراک",
    "technical" to "مشکل فنی",
    "crm" to "CRM",
    "filing" to "فایلینگ و استخراج",
)

private val supportPriorityOptions = listOf(
    "low" to "کم",
    "normal" to "عادی",
    "high" to "زیاد",
    "urgent" to "فوری",
)

private fun supportCategoryLabel(value: String): String =
    supportCategoryOptions.firstOrNull { it.first == value }?.second ?: value

private fun supportPriorityLabel(value: String): String =
    supportPriorityOptions.firstOrNull { it.first == value }?.second ?: value
