package ir.divarfiling.mobile.feature.team

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.TeamThreadDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMessagesScreen(
    onBack: () -> Unit,
    onOpenThread: (Long) -> Unit,
    viewModel: TeamMessagesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    if (state.showCompose) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleCompose(false) }) {
            DfSheetScaffold(
                title = "پیام جدید",
                subtitle = "ارسال مستقیم به یک عضو تیم",
                icon = DfIcons.MessageCircle,
                onClose = { viewModel.toggleCompose(false) },
                footer = {
                    DfSheetActions(
                        primaryText = if (state.isSubmitting) "در حال ارسال…" else "ارسال",
                        onPrimary = viewModel::sendDirect,
                        primaryEnabled = !state.isSubmitting &&
                            state.selectedMemberId != null &&
                            state.composeBody.isNotBlank(),
                        isSubmitting = state.isSubmitting,
                        onSecondary = { viewModel.toggleCompose(false) },
                    )
                },
            ) {
                DfSheetSection(title = "گیرنده") {
                    DfFilterChipRow(
                        options = state.members.map { DfFilterOption(it.id, it.name) },
                        selected = state.selectedMemberId ?: -1L,
                        onSelect = viewModel::onMemberSelect,
                    )
                }
                DfSheetSection(title = "متن پیام") {
                    DfTextField(
                        value = state.composeBody,
                        onValueChange = viewModel::onComposeBodyChange,
                        label = "پیام",
                        singleLine = false,
                        minLines = 4,
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
                text = "پیام جدید",
                icon = DfIcons.MessageCircle,
                onClick = { viewModel.toggleCompose(true) },
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "صندوق پیام",
                        subtitle = "گفت‌وگوهای داخلی آژانس",
                        titleIconRes = DfDecorIcons.Users,
                        onBack = onBack,
                    )
                }
                item {
                    DfFilterChipRow(
                        options = TeamMessageFolder.entries.map { DfFilterOption(it, it.label) },
                        selected = state.folder,
                        onSelect = viewModel::setFolder,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                when {
                    state.isLoading -> item {
                        DfCardListSkeleton(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                    state.threads.isEmpty() -> item {
                        DfEmptyState(
                            title = "پیامی نیست",
                            subtitle = "اولین پیام مستقیم به همکار را از دکمه پایین بفرستید.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    else -> items(state.threads, key = { it.id }) { thread ->
                        TeamThreadCard(
                            thread = thread,
                            onClick = { onOpenThread(thread.id) },
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
fun TeamThreadCard(
    thread: TeamThreadDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(onClick = onClick, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    thread.participantsLabel.ifBlank { thread.subject },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (thread.unreadCount > 0) {
                    DfBadge(
                        text = thread.unreadCount.toString(),
                        color = DfColors.BlueLight,
                        textColor = DfColors.Blue,
                    )
                }
            }
            Text(
                thread.lastMessage?.body?.ifBlank { "بدون متن" } ?: "مکالمه خالی",
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                if (thread.kind == "broadcast") {
                    DfBadge(text = "گروهی", color = DfColors.AmberLight, textColor = DfColors.Amber)
                }
                if (thread.isStarred) {
                    DfBadge(text = "ستاره", color = DfColors.PurpleLight, textColor = DfColors.PurpleDark)
                }
                Text(
                    DateUtils.formatRelativeFa(thread.updatedAt),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                )
            }
        }
    }
}
