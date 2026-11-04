package ir.divarfiling.mobile.feature.forum

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.network.ForumMessageDto
import ir.divarfiling.mobile.core.network.ForumReplyPreviewDto

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ForumMessageRow(
    message: ForumMessageDto,
    showSenderName: Boolean = true,
    showAvatar: Boolean = true,
    isContextTarget: Boolean = false,
    onLongPress: () -> Unit,
    onReactionClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isMine = message.isMine
    val bubbleShape = bubbleShapeFor(isMine, showAvatar || isMine)
    val bubbleColor = when {
        message.isDeleted -> DfThemeColors.surfaceVariant().copy(alpha = 0.5f)
        isMine -> DfThemeColors.primary().copy(alpha = 0.18f)
        else -> DfThemeColors.surface()
    }
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = if (showSenderName) AppSpacing.xxs else 1.dp)
            .combinedClickable(
                onClick = { },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
            ),
        horizontalArrangement = if (isMine) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!isMine) {
            if (showAvatar) {
                ForumAvatar(
                    url = message.avatarUrl,
                    label = message.senderLabel,
                    modifier = Modifier
                        .size(34.dp)
                        .padding(end = AppSpacing.xxs),
                )
            } else {
                Box(Modifier.size(34.dp).padding(end = AppSpacing.xxs))
            }
        }
        Column(
            horizontalAlignment = if (isMine) Alignment.Start else Alignment.End,
            modifier = Modifier.fillMaxWidth(if (isMine) 0.82f else 0.88f),
        ) {
            if (!isMine && showSenderName) {
                Text(
                    text = message.senderLabel,
                    style = AppTypography.labelSmall,
                    fontWeight = if (message.senderIsModerator) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (message.senderIsModerator) DfThemeColors.primary() else DfThemeColors.textSecondary(),
                    modifier = Modifier.padding(bottom = 2.dp, start = AppSpacing.xxs),
                )
            }
            message.replyTo?.let { reply ->
                ForumReplyQuote(reply = reply, alignEnd = !isMine)
            }
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                tonalElevation = if (isMine) 0.dp else 1.dp,
                shadowElevation = if (isContextTarget) 4.dp else 1.dp,
                border = if (isContextTarget) {
                    androidx.compose.foundation.BorderStroke(1.5.dp, DfThemeColors.primary())
                } else {
                    null
                },
            ) {
                Column(Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)) {
                    Text(
                        text = if (message.isDeleted) "این پیام حذف شده است" else message.body,
                        style = AppTypography.body,
                        color = if (message.isDeleted) {
                            DfThemeColors.textSecondary()
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (message.isEdited && !message.isDeleted) {
                            Text(
                                text = "ویرایش",
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textSecondary(),
                            )
                        }
                        Text(
                            text = ForumChatTimeline.messageClock(message.createdAt),
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                }
            }
            if (message.reactions.isNotEmpty() && !message.isDeleted) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    message.reactions.forEach { reaction ->
                        val mine = message.myReaction == reaction.emoji
                        Surface(
                            shape = CircleShape,
                            color = if (mine) {
                                DfThemeColors.primary().copy(alpha = 0.16f)
                            } else {
                                DfThemeColors.surfaceVariant().copy(alpha = 0.45f)
                            },
                            modifier = Modifier.clickable(enabled = onReactionClick != null) {
                                onReactionClick?.invoke(reaction.emoji)
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

private fun bubbleShapeFor(isMine: Boolean, tail: Boolean): RoundedCornerShape {
    return if (isMine) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = if (tail) 4.dp else 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
        )
    } else {
        RoundedCornerShape(
            topStart = if (tail) 4.dp else 16.dp,
            topEnd = 16.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp,
        )
    }
}

@Composable
private fun ForumReplyQuote(reply: ForumReplyPreviewDto, alignEnd: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppSpacing.xxs),
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(10.dp))
                .background(DfThemeColors.primary().copy(alpha = 0.08f)),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 48.dp)
                    .background(DfThemeColors.primary()),
            )
            Column(Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs)) {
                Text(
                    text = if (reply.unavailable) "پیام در دسترس نیست" else reply.senderLabel,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
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
}
