package ir.divarfiling.mobile.feature.support

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDestructiveButton
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.design.components.DfTextField

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
    val isClosed = ticket?.status == TICKET_STATUS_CLOSED
    var showCloseConfirm by remember { mutableStateOf(false) }
    var showReopenConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var headerItemCount by remember { mutableIntStateOf(2) }
    val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::selectAttachment)
    }

    LaunchedEffect(state.scrollToLatest, ticket?.messages?.size) {
        val messages = ticket?.messages.orEmpty()
        if (messages.isNotEmpty()) {
            val targetIndex = headerItemCount + messages.lastIndex
            listState.animateScrollToItem(targetIndex)
        }
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text("بستن تیکت") },
            text = { Text("بعد از بستن، ادامه مکاتبه فقط با بازگشایی دوباره ممکن است. مطمئن هستید؟") },
            confirmButton = {
                DfDestructiveButton(
                    text = "بستن تیکت",
                    onClick = {
                        showCloseConfirm = false
                        viewModel.closeTicket()
                    },
                    enabled = !state.isSubmitting,
                )
            },
            dismissButton = {
                DfTextButton(text = "انصراف", onClick = { showCloseConfirm = false })
            },
        )
    }

    if (showReopenConfirm) {
        AlertDialog(
            onDismissRequest = { showReopenConfirm = false },
            title = { Text("بازگشایی تیکت") },
            text = { Text("تیکت دوباره باز می‌شود و می‌توانید پاسخ یا پیوست جدید ارسال کنید. ادامه می‌دهید؟") },
            confirmButton = {
                DfPrimaryButton(
                    text = "بازگشایی تیکت",
                    onClick = {
                        showReopenConfirm = false
                        viewModel.reopenTicket()
                    },
                    enabled = !state.isSubmitting,
                    loading = state.isSubmitting,
                )
            },
            dismissButton = {
                DfTextButton(text = "انصراف", onClick = { showReopenConfirm = false })
            },
        )
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
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfDetailPageHeader(
                        title = ticket?.subject ?: "تیکت",
                        subtitle = ticket?.let { "#${it.ticketNumber} · ${ticketStatusLabel(it.status)}" }
                            ?: "در حال بارگذاری…",
                        sectionLabel = DfHeaderSections.SUPPORT,
                        titleIconRes = DfDecorIcons.Phone,
                        onBack = onBack,
                    )
                }
                state.error?.let { err ->
                    item {
                        DfStatusBanner(
                            message = err,
                            tone = DfStatusTone.Error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (ticket != null) {
                    item {
                        SupportTicketTimeline(
                            status = ticket.status,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    item {
                        DfCard(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            containerColor = DfThemeColors.surface(),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                Text(
                                    "جزئیات تیکت",
                                    style = AppTypography.bodyDescription,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DfThemeColors.textPrimary(),
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val (sBg, sFg) = ticketStatusColors(ticket.status)
                                    DfBadge(
                                        text = ticketStatusLabel(ticket.status),
                                        color = sBg,
                                        textColor = sFg,
                                    )
                                    val (pBg, pFg) = supportPriorityColors(ticket.priority)
                                    DfBadge(
                                        text = supportPriorityLabel(ticket.priority),
                                        color = pBg,
                                        textColor = pFg,
                                    )
                                    DfBadge(
                                        text = supportCategoryLabel(ticket.category),
                                        color = DfThemeColors.surfaceVariant(),
                                        textColor = DfThemeColors.textSecondary(),
                                    )
                                }
                                Text(
                                    text = "شماره تیکت: #${ticket.ticketNumber}",
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.textMuted(),
                                )
                            }
                        }
                    }
                    items(ticket.messages, key = { it.id }) { message ->
                        SupportMessageBubble(
                            message = message,
                            onOpenAttachment = { url ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    if (!isClosed) {
                        item {
                            DfCard(
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                containerColor = DfThemeColors.surface(),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                                ) {
                                    Text(
                                        "پاسخ شما",
                                        style = AppTypography.bodyDescription,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DfThemeColors.textPrimary(),
                                    )
                                    DfTextField(
                                        value = state.replyText,
                                        onValueChange = viewModel::onReplyChange,
                                        placeholder = "متن پاسخ…",
                                        singleLine = false,
                                        minLines = 3,
                                        enabled = !state.isSubmitting,
                                    )
                                    Text(
                                        text = "پیوست: jpg، png، pdf، doc، xls، txt، zip — حداکثر ۱۰ مگابایت",
                                        style = AppTypography.labelSmall,
                                        color = DfThemeColors.textMuted(),
                                    )
                                    DfSecondaryButton(
                                        text = if (state.selectedAttachmentName.isBlank()) {
                                            "افزودن پیوست"
                                        } else {
                                            "تغییر پیوست"
                                        },
                                        onClick = { attachmentPicker.launch("*/*") },
                                        enabled = !state.isSubmitting,
                                    )
                                    if (state.selectedAttachmentName.isNotBlank()) {
                                        Surface(
                                            shape = AppShapes.Card,
                                            color = DfThemeColors.surfaceVariant(),
                                            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(AppSpacing.sm),
                                                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                            ) {
                                                Text(
                                                    text = "پیوست انتخاب‌شده",
                                                    style = AppTypography.labelSmall,
                                                    color = DfThemeColors.textMuted(),
                                                )
                                                Text(
                                                    text = state.selectedAttachmentName,
                                                    style = AppTypography.bodyDescription,
                                                    color = DfThemeColors.textPrimary(),
                                                )
                                                DfTextButton(
                                                    text = "حذف پیوست",
                                                    onClick = viewModel::clearAttachment,
                                                    compact = true,
                                                )
                                            }
                                        }
                                    }
                                    DfPrimaryButton(
                                        text = "ارسال پاسخ",
                                        onClick = viewModel::sendReply,
                                        enabled = !state.isSubmitting,
                                        loading = state.isSubmitting,
                                    )
                                    DfDestructiveButton(
                                        text = "بستن تیکت",
                                        onClick = { showCloseConfirm = true },
                                        enabled = !state.isSubmitting,
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
                                DfStatusBanner(
                                    message = "این تیکت بسته شده است. در صورت نیاز می‌توانید آن را بازگشایی کنید.",
                                    tone = DfStatusTone.Locked,
                                    title = "تیکت بسته",
                                )
                                DfSecondaryButton(
                                    text = "بازگشایی تیکت",
                                    onClick = { showReopenConfirm = true },
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

    LaunchedEffect(state.error, ticket) {
        headerItemCount = 1 + (if (state.error != null) 1 else 0) + (if (ticket != null) 2 else 0)
    }
}
