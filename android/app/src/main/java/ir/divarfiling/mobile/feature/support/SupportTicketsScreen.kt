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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
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

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleCreateDialog(false) },
            title = { Text("تیکت جدید") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    OutlinedTextField(
                        value = state.subject,
                        onValueChange = viewModel::onSubjectChange,
                        label = { Text("موضوع") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.body,
                        onValueChange = viewModel::onBodyChange,
                        label = { Text("متن درخواست") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::createTicket, enabled = !state.isSubmitting) {
                    Text("ثبت")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleCreateDialog(false) }) {
                    Text("انصراف")
                }
            },
        )
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
                state.error?.let { error ->
                    item {
                        DfErrorBanner(error, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                }
                if (state.isLoading) {
                    item {
                        DfCardListSkeleton(count = 4, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                } else if (state.tickets.isEmpty()) {
                    item {
                        DfEmptyState(
                            title = "تیکتی ثبت نشده",
                            subtitle = "برای ارتباط با پشتیبانی یک تیکت جدید بسازید.",
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(state.tickets, key = { it.id }) { ticket ->
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketCard(
    ticket: SupportTicketDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    ticket.subject,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (ticket.userHasUnread) {
                    Text("جدید", style = AppTypography.labelSmall, color = DfColors.Rose, fontWeight = FontWeight.Bold)
                }
            }
            Text("#${ticket.ticketNumber}", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Text(ticketStatusLabel(ticket.status), style = AppTypography.labelSmall, color = DfColors.Purple)
            ticket.lastMessageAt?.let {
                Text(DateUtils.formatForDisplay(it), style = AppTypography.labelSmall, color = DfColors.TextSecondary)
            } ?: ticket.createdAt?.let {
                Text(DateUtils.formatForDisplay(it), style = AppTypography.labelSmall, color = DfColors.TextSecondary)
            }
        }
    }
}
