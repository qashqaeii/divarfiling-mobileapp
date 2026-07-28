package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
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
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.TeamChatMessageDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamThreadDetailScreen(
    onBack: () -> Unit,
    viewModel: TeamThreadDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val thread = state.thread

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DfDetailPageHeader(
                    title = thread?.participantsLabel?.ifBlank { thread.subject } ?: "مکالمه",
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
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                horizontal = AppSpacing.screenHorizontal,
                                vertical = AppSpacing.sm,
                            ),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            items(state.messages, key = { it.id }) { message ->
                                TeamBubble(message)
                            }
                        }
                        DfCard(modifier = Modifier.padding(AppSpacing.screenHorizontal)) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                DfTextField(
                                    value = state.replyBody,
                                    onValueChange = viewModel::onReplyChange,
                                    label = "پاسخ شما",
                                    singleLine = false,
                                    minLines = 2,
                                )
                                DfPrimaryButton(
                                    text = "ارسال پاسخ",
                                    onClick = viewModel::sendReply,
                                    loading = state.isSubmitting,
                                    enabled = !state.isSubmitting,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamBubble(message: TeamChatMessageDto) {
    val mine = message.isMine
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .background(
                    if (mine) DfColors.BlueLight else DfColors.Surface,
                    RoundedCornerShape(18.dp),
                )
                .padding(AppSpacing.sm),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!mine) {
                    Text(
                        message.senderName,
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.Blue,
                    )
                }
                Text(message.body.ifBlank { "—" }, style = AppTypography.bodyDescription)
                Text(
                    DateUtils.formatRelativeFa(message.createdAt),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}
