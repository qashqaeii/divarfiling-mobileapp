package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
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

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
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
                Column(modifier = Modifier.fillMaxSize()) {
                    DfDetailPageHeader(
                        title = thread?.participantsLabel?.ifBlank { thread.subject } ?: "مکالمه",
                        subtitle = if (thread?.kind == "broadcast") "پیام گروهی" else "گفت‌وگوی مستقیم",
                        sectionLabel = DfHeaderSections.TEAM,
                        onBack = onBack,
                        titleIconRes = DfDecorIcons.Users,
                        actions = {
                            TextButton(onClick = viewModel::toggleStar) {
                                Text(
                                    if (thread?.isStarred == true) "حذف ستاره" else "ستاره‌دار",
                                    color = DfColors.Amber,
                                )
                            }
                        },
                    )
                    when {
                        state.isLoading -> DfCardListSkeleton(
                            modifier = Modifier.padding(horizontal = pad),
                        )
                        state.messages.isEmpty() -> {
                            DfEmptyState(
                                title = "هنوز پیامی در این مکالمه نیست",
                                subtitle = "اولین پاسخ را از پایین ارسال کنید.",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = pad),
                            )
                            TeamComposerBar(
                                value = state.replyBody,
                                onValueChange = viewModel::onReplyChange,
                                onSend = viewModel::sendReply,
                                isSubmitting = state.isSubmitting,
                                modifier = Modifier.padding(horizontal = pad, vertical = AppSpacing.sm),
                            )
                        }
                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(
                                    horizontal = pad,
                                    vertical = AppSpacing.sm,
                                ),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                items(state.messages, key = { it.id }) { message ->
                                    TeamChatBubble(message)
                                }
                            }
                            TeamComposerBar(
                                value = state.replyBody,
                                onValueChange = viewModel::onReplyChange,
                                onSend = viewModel::sendReply,
                                isSubmitting = state.isSubmitting,
                                modifier = Modifier.padding(horizontal = pad, vertical = AppSpacing.sm),
                            )
                        }
                    }
                }
            }
        }
    }
}
