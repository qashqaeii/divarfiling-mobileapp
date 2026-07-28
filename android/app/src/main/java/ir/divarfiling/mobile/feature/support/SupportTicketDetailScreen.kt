package ir.divarfiling.mobile.feature.support

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDestructiveButton
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.DfBadge
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
    var showCloseConfirm by remember { mutableStateOf(false) }
    var showReopenConfirm by remember { mutableStateOf(false) }
    val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(viewModel::selectAttachment)
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
                        DfStatusBanner(
                            message = err,
                            tone = DfStatusTone.Error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (ticket != null) {
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
                                    DfBadge(
                                        text = ticketStatusLabel(ticket.status),
                                        color = ticketStatusColors(ticket.status).first,
                                        textColor = ticketStatusColors(ticket.status).second,
                                    )
                                    DfBadge(
                                        text = supportPriorityLabel(ticket.priority),
                                        color = DfThemeColors.primaryContainer(),
                                        textColor = DfThemeColors.onPrimaryContainer(),
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
}

@Composable
private fun MessageBubble(
    message: SupportTicketMessageDto,
    onOpenAttachment: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isStaff = message.isStaffReply
    val align = if (isStaff) Alignment.CenterStart else Alignment.CenterEnd
    val container = if (isStaff) DfThemeColors.surfaceVariant() else DfThemeColors.primaryContainer()
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = align) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = AppShapes.Card,
            color = container,
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            shadowElevation = AppElevations.subtle,
            tonalElevation = AppElevations.none,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    if (isStaff) "پشتیبانی" else "شما",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.primary(),
                )
                Text(
                    message.body,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textPrimary(),
                )
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
                        color = DfThemeColors.textMuted(),
                    )
                }
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

private fun supportCategoryLabel(value: String): String = when (value) {
    "billing" -> "پرداخت و اشتراک"
    "technical" -> "مشکل فنی"
    "crm" -> "CRM"
    "filing" -> "فایلینگ و استخراج"
    else -> "عمومی"
}

private fun supportPriorityLabel(value: String): String = when (value) {
    "low" -> "کم"
    "high" -> "زیاد"
    "urgent" -> "فوری"
    else -> "عادی"
}

private fun ticketStatusColors(status: String): Pair<Color, Color> = when (status) {
    "open" -> AppColors.BlueLight to AppColors.Blue
    "in_review" -> AppColors.AmberLight to AppColors.Amber
    "answered" -> AppColors.GreenLight to AppColors.Green
    "waiting_user" -> AppColors.PurpleContainer to AppColors.PurpleDark
    "closed" -> AppColors.LockedContainer to AppColors.OnLocked
    else -> AppColors.SurfaceVariant to AppColors.TextSecondary
}
