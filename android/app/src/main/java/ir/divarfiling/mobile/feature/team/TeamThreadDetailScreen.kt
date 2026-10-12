package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamThreadDetailScreen(
    onBack: () -> Unit,
    viewModel: TeamThreadDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val thread = state.thread
    val pad = teamHorizontalPadding()
    val listState = rememberLazyListState()
    val timeline = remember(state.messages) { buildTeamChatTimeline(state.messages) }
    val title = thread?.participantsLabel?.ifBlank { thread.subject } ?: "مکالمه"

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    LaunchedEffect(timeline.size) {
        if (timeline.isNotEmpty()) {
            listState.animateScrollToItem(timeline.lastIndex)
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (!state.isLoading) {
                TeamComposerBar(
                    value = state.replyBody,
                    onValueChange = viewModel::onReplyChange,
                    onSend = viewModel::sendReply,
                    isSubmitting = state.isSubmitting,
                )
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
                    title = title,
                    subtitle = if (thread?.kind == "broadcast") "پیام گروهی" else "گفت‌وگوی مستقیم",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = viewModel::toggleStar) {
                            Icon(
                                imageVector = if (thread?.isStarred == true) DfIcons.Star else DfIcons.Star,
                                contentDescription = null,
                                tint = if (thread?.isStarred == true) {
                                    DfThemeColors.warning()
                                } else {
                                    DfThemeColors.textMuted()
                                },
                            )
                        }
                    },
                )
                state.error?.let { err ->
                    if (!state.isLoading) {
                        DfErrorBanner(
                            message = err,
                            modifier = Modifier.padding(horizontal = pad, vertical = AppSpacing.xs),
                        )
                    }
                }
                DfPullRefresh(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.weight(1f),
                ) {
                    when {
                        state.isLoading -> DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        state.messages.isEmpty() -> {
                            DfEmptyState(
                                title = "هنوز پیامی در این مکالمه نیست",
                                subtitle = "اولین پاسخ را از پایین بنویسید.",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = pad),
                            )
                        }
                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    horizontal = pad,
                                    vertical = AppSpacing.sm,
                                ),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                items(timeline, key = { entry ->
                                    when (entry) {
                                        is TeamChatTimelineEntry.Date -> "d-${entry.label}"
                                        is TeamChatTimelineEntry.Message -> "m-${entry.message.id}"
                                    }
                                }) { entry ->
                                    when (entry) {
                                        is TeamChatTimelineEntry.Date -> TeamChatDatePill(entry.label)
                                        is TeamChatTimelineEntry.Message -> TeamChatBubble(entry.message)
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
