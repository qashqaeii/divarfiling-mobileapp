package ir.divarfiling.mobile.feature.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.network.ForumMessageDto
import ir.divarfiling.mobile.core.network.ForumPinnedNoticeDto

@Composable
fun ForumNoticeStrip(
    notices: List<ForumPinnedNoticeDto>,
    modifier: Modifier = Modifier,
) {
    if (notices.isEmpty()) return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
        notices.forEach { notice ->
            Surface(
                shape = AppShapes.CardSmall,
                color = DfThemeColors.primaryContainer().copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = notice.body,
                    style = AppTypography.meta,
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ForumPinnedBar(
    message: ForumMessageDto,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.7f),
    ) {
        Column(Modifier.padding(AppSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    DfIcons.MessageSquare,
                    contentDescription = null,
                    tint = DfThemeColors.primary(),
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "پیام سنجاق‌شده",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = AppSpacing.xs),
                )
            }
            val preview = if (message.isDeleted) "پیام حذف شده" else message.body
            Text(
                text = if (expanded) preview else preview.take(80),
                style = AppTypography.meta,
                maxLines = if (expanded) 8 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = AppSpacing.xxs),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ForumMessageRow(
    message: ForumMessageDto,
    selected: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMine = message.isMine
    val bubbleColor = when {
        message.isDeleted -> DfThemeColors.surfaceVariant().copy(alpha = 0.5f)
        isMine -> DfThemeColors.primary().copy(alpha = 0.12f)
        else -> DfThemeColors.surface()
    }
    val align = if (isMine) Alignment.End else Alignment.Start
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xxs)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        horizontalAlignment = align,
    ) {
        if (!isMine) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = AppSpacing.xxs),
            ) {
                ForumAvatar(url = message.avatarUrl, label = message.senderLabel)
                Text(
                    text = message.senderLabel,
                    style = AppTypography.labelSmall,
                    fontWeight = if (message.senderIsModerator) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(start = AppSpacing.xs),
                )
                if (message.senderIsModerator) {
                    Text(
                        text = " · مدیر",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.primary(),
                    )
                }
            }
        }
        message.replyTo?.let { reply ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DfThemeColors.surfaceVariant().copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .padding(bottom = AppSpacing.xxs),
            ) {
                Column(Modifier.padding(AppSpacing.sm)) {
                    Text(
                        text = if (reply.unavailable) "پیام در دسترس نیست" else reply.senderLabel,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.primary(),
                    )
                    if (!reply.unavailable) {
                        Text(
                            text = reply.bodyPreview.orEmpty(),
                            style = AppTypography.meta,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        Surface(
            shape = AppShapes.Card,
            color = bubbleColor,
            border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, DfThemeColors.primary()) else null,
            modifier = Modifier.fillMaxWidth(if (isMine) 0.88f else 0.92f),
        ) {
            Column(Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)) {
                Text(
                    text = if (message.isDeleted) "این پیام حذف شده است" else message.body,
                    style = AppTypography.body,
                    color = if (message.isDeleted) DfThemeColors.textSecondary() else MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        Text(
                            text = DateUtils.formatRelativeTimeAgo(message.createdAt),
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
                        if (message.isEdited && !message.isDeleted) {
                            Text(
                                text = "ویرایش‌شده",
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textSecondary(),
                            )
                        }
                    }
                    if (!message.isDeleted && message.viewCount > 0) {
                        Text(
                            text = DateUtils.toPersianDigits(message.viewCount.toString()) + " بازدید",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                }
                if (message.reactions.isNotEmpty() && !message.isDeleted) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                        modifier = Modifier.padding(top = AppSpacing.xxs),
                    ) {
                        message.reactions.forEach { reaction ->
                            val mine = message.myReaction == reaction.emoji
                            Surface(
                                shape = CircleShape,
                                color = if (mine) {
                                    DfThemeColors.primary().copy(alpha = 0.15f)
                                } else {
                                    DfThemeColors.surfaceVariant().copy(alpha = 0.4f)
                                },
                            ) {
                                Text(
                                    text = "${reaction.emoji} ${DateUtils.toPersianDigits(reaction.count.toString())}",
                                    style = AppTypography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
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
fun ForumAvatar(url: String?, label: String, modifier: Modifier = Modifier) {
    val initial = label.firstOrNull()?.toString() ?: "?"
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(DfThemeColors.primaryContainer()),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            DfAsyncImage(
                url = url,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
            )
        } else {
            Text(text = initial, style = AppTypography.labelLarge, color = DfThemeColors.primary())
        }
    }
}

@Composable
fun ForumComposerBar(
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    replyLabel: String?,
    onCancelReply: () -> Unit,
    editMode: Boolean,
    onCancelEdit: () -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
    isSending: Boolean,
    pendingFailed: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DfThemeColors.surface())
            .padding(AppSpacing.sm),
    ) {
        if (pendingFailed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("ارسال ناموفق", style = AppTypography.labelSmall, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry) { Text("تلاش مجدد") }
            }
        }
        if (editMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("ویرایش پیام", style = AppTypography.labelLarge)
                TextButton(onClick = onCancelEdit) { Text("لغو") }
            }
        }
        replyLabel?.let { label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppSpacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "پاسخ به: $label",
                    style = AppTypography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onCancelReply, modifier = Modifier.size(32.dp)) {
                    Icon(DfIcons.X, contentDescription = "لغو پاسخ", tint = DfThemeColors.textSecondary())
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(placeholder) },
                minLines = 1,
                maxLines = 5,
            )
            IconButton(
                onClick = onSend,
                enabled = sendEnabled,
                modifier = Modifier.padding(start = AppSpacing.xs),
            ) {
                Icon(
                    DfIcons.Send,
                    contentDescription = "ارسال",
                    tint = if (sendEnabled) DfThemeColors.primary() else DfThemeColors.textSecondary(),
                )
            }
        }
        if (isSending) {
            Text(
                text = "در حال ارسال…",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
                modifier = Modifier.padding(top = AppSpacing.xxs),
            )
        }
    }
}

@Composable
fun ForumMessageActionBar(
    message: ForumMessageDto,
    canModerate: Boolean,
    onReply: () -> Unit,
    onReaction: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    onPin: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
        ) {
            ForumActionChip("پاسخ", onReply)
            ForumActionChip("واکنش", onReaction)
            ForumActionChip("کپی", onCopy)
            if (message.isMine && !message.isDeleted) ForumActionChip("ویرایش", onEdit)
            if ((message.isMine || canModerate) && !message.isDeleted) ForumActionChip("حذف", onDelete)
            if (!message.isMine) ForumActionChip("گزارش", onReport)
            if (canModerate && !message.isDeleted) {
                ForumActionChip(if (message.isPinned) "برداشتن سنجاق" else "سنجاق", onPin)
            }
            ForumActionChip("بستن", onDismiss)
        }
    }
}

@Composable
private fun ForumActionChip(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(label, style = AppTypography.labelSmall)
    }
}

@Composable
fun ForumReactionPicker(
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        FORUM_ALLOWED_REACTIONS.forEach { emoji ->
            Text(
                text = emoji,
                style = AppTypography.pageTitle,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onPick(emoji)
                        onDismiss()
                    }
                    .padding(AppSpacing.sm),
            )
        }
    }
}
