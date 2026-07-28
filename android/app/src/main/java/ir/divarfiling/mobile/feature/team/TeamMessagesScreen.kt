package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
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
import ir.divarfiling.mobile.core.design.components.DfTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMessagesScreen(
    onBack: () -> Unit,
    onOpenThread: (Long) -> Unit,
    viewModel: TeamMessagesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()

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
                    TeamMemberSelectList(
                        members = state.members,
                        selectedId = state.selectedMemberId,
                        onSelect = viewModel::onMemberSelect,
                        emptyLabel = "عضوی برای ارسال پیام نیست",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TeamAmbientBackground()
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                LazyColumn(
                    contentPadding = teamListContentPadding(bottomExtra = 72.dp),
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
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    when {
                        state.isLoading -> item {
                            DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        }
                        state.threads.isEmpty() -> item {
                            DfEmptyState(
                                title = "هنوز پیامی نیست",
                                subtitle = "اولین پیام مستقیم به همکار را از دکمه پایین بفرستید.",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        else -> items(state.threads, key = { it.id }) { thread ->
                            TeamThreadListCard(
                                thread = thread,
                                onClick = { onOpenThread(thread.id) },
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                    }
                }
            }
        }
    }
}
