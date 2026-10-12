package ir.divarfiling.mobile.feature.team

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.showDfMessage
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

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
    val unreadThreads = state.threads.sumOf { it.unreadCount }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showDfMessage(it); viewModel.clearMessage() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { TeamAgencySnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleCompose(true) },
                containerColor = DfThemeColors.primary(),
                contentColor = Color.White,
            ) {
                Icon(DfIcons.MessageCircle, contentDescription = "پیام جدید")
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TeamAmbientBackground()
            Column(modifier = Modifier.fillMaxSize()) {
                TeamAgencyScreenHeader(
                    title = "صندوق پیام",
                    icon = DfIcons.MessageCircle,
                    accent = AgencyHeaderAccent.Conversation,
                    subtitle = if (unreadThreads > 0) {
                        "${DateUtils.toPersianDigits(unreadThreads.toString())} پیام خوانده‌نشده"
                    } else {
                        "گفت‌وگوهای داخلی آژانس"
                    },
                    onBack = onBack,
                )
                DfPullRefresh(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        contentPadding = teamListContentPadding(bottomExtra = 64.dp),
                    ) {
                        item {
                            TeamMessageFolderBar(
                                selected = state.folder,
                                onSelect = viewModel::setFolder,
                                modifier = Modifier.padding(horizontal = pad, vertical = AppSpacing.xs),
                            )
                        }
                        state.error?.let { err ->
                            if (!state.isLoading && state.threads.isEmpty()) {
                                item {
                                    DfErrorBanner(message = err, modifier = Modifier.padding(horizontal = pad))
                                }
                            }
                        }
                        when {
                            state.isLoading -> item {
                                DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad, vertical = AppSpacing.sm))
                            }
                            state.threads.isEmpty() && state.error == null -> item {
                                DfEmptyState(
                                    title = "هنوز پیامی ندارید",
                                    subtitle = "اولین پیام را با دکمه + بفرستید.",
                                    variant = DfEmptyVariant.Empty,
                                    modifier = Modifier.padding(horizontal = pad, vertical = AppSpacing.lg),
                                )
                            }
                            else -> items(state.threads, key = { it.id }) { thread ->
                                TeamThreadListCard(
                                    thread = thread,
                                    onClick = { onOpenThread(thread.id) },
                                )
                                Divider(
                                    color = DfThemeColors.outlineSubtle().copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
        if (state.showCompose) {
            BackHandler { viewModel.toggleCompose(false) }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DfScreenContainerColor),
                color = DfScreenContainerColor,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding(),
                ) {
                    AgencyConversationHeader(
                        title = "پیام جدید",
                        subtitle = "ارسال مستقیم به همکار",
                        onBack = { viewModel.toggleCompose(false) },
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = pad),
                    ) {
                        TeamMessagesComposeSheetContent(
                            members = state.members,
                            query = state.composeMemberQuery,
                            onQueryChange = viewModel::onComposeMemberQueryChange,
                            selectedMemberId = state.selectedMemberId,
                            onMemberSelect = viewModel::onMemberSelect,
                            body = state.composeBody,
                            onBodyChange = viewModel::onComposeBodyChange,
                        )
                    }
                    DfPrimaryButton(
                        text = if (state.isSubmitting) "در حال ارسال…" else "ارسال پیام",
                        onClick = viewModel::sendDirect,
                        enabled = !state.isSubmitting &&
                            state.selectedMemberId != null &&
                            state.composeBody.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = pad, vertical = AppSpacing.sm),
                    )
                }
            }
        }
    }
}
