package ir.divarfiling.mobile.feature.support

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.network.SupportTicketMessageDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketDetailScreen(
    onBack: () -> Unit,
    viewModel: SupportTicketDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val ticket = state.ticket
    val isClosed = ticket?.status == "closed"

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
                        title = ticket?.subject ?: "تیکت",
                        subtitle = ticket?.let { "#${it.ticketNumber} · ${ticketStatusLabel(it.status)}" }
                            ?: "در حال بارگذاری…",
                        titleIconRes = DfDecorIcons.Phone,
                        onBack = onBack,
                    )
                }
                state.error?.let { err ->
                    item {
                        DfErrorBanner(err, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                }
                if (ticket != null) {
                    items(ticket.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            onOpenAttachment = { url ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    if (!isClosed) {
                        item {
                            DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppSpacing.cardPadding),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                ) {
                                    Text(
                                        "پاسخ شما",
                                        style = AppTypography.bodyDescription,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    OutlinedTextField(
                                        value = state.replyText,
                                        onValueChange = viewModel::onReplyChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3,
                                        placeholder = { Text("متن پاسخ…") },
                                        enabled = !state.isSubmitting,
                                    )
                                    DfPrimaryButton(
                                        text = "ارسال پاسخ",
                                        onClick = { viewModel.sendReply() },
                                        enabled = !state.isSubmitting,
                                        loading = state.isSubmitting,
                                    )
                                    DfTextButton(
                                        text = "بستن تیکت",
                                        onClick = viewModel::closeTicket,
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Column(
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                Text(
                                    "این تیکت بسته شده است.",
                                    style = AppTypography.bodyDescription,
                                    color = DfColors.TextMuted,
                                )
                                DfPrimaryButton(
                                    text = "بازگشایی تیکت",
                                    onClick = viewModel::reopenTicket,
                                    enabled = !state.isSubmitting,
                                    loading = state.isSubmitting,
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
private fun MessageBubble(
    message: SupportTicketMessageDto,
    onOpenAttachment: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isStaff = message.isStaffReply
    val align = if (isStaff) Alignment.CenterStart else Alignment.CenterEnd
    val bg = if (isStaff) DfColors.SurfaceVariant else DfColors.PurpleContainer
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = align) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                if (isStaff) "پشتیبانی" else "شما",
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.Purple,
            )
            Text(message.body, style = AppTypography.bodyDescription, color = DfColors.TextPrimary)
            message.attachments.forEach { att ->
                if (att.url.isNotBlank()) {
                    DfTextButton(
                        text = att.originalFilename.ifBlank { "پیوست" },
                        onClick = { onOpenAttachment(att.url) },
                        compact = true,
                    )
                }
            }
            message.createdAt?.let {
                Text(
                    DateUtils.formatForDisplay(it),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }
    }
}

fun ticketStatusLabel(status: String): String = when (status) {
    "open" -> "باز"
    "in_review" -> "در حال بررسی"
    "answered" -> "پاسخ داده‌شده"
    "waiting_user" -> "منتظر شما"
    "closed" -> "بسته"
    else -> status
}
