package ir.divarfiling.mobile.feature.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.network.ForumMessageDto

@Composable
fun ForumMessageContextSheet(
    message: ForumMessageDto,
    canModerate: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
    onPin: () -> Unit,
    onReaction: (String) -> Unit,
) {
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "پیام",
            subtitle = message.senderLabel,
            icon = DfIcons.MessageCircle,
            onClose = onDismiss,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                ForumContextMessagePreview(message = message)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    FORUM_ALLOWED_REACTIONS.forEach { emoji ->
                        val selected = message.myReaction == emoji
                        Box(
                            modifier = Modifier
                                .clip(AppShapes.Chip)
                                .background(
                                    if (selected) {
                                        DfThemeColors.primaryContainer().copy(alpha = 0.65f)
                                    } else {
                                        DfThemeColors.surfaceVariant().copy(alpha = 0.35f)
                                    },
                                )
                                .clickable {
                                    onReaction(emoji)
                                    onDismiss()
                                }
                                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = emoji, style = AppTypography.pageTitle)
                        }
                    }
                }
                HorizontalDivider(color = DfThemeColors.outline().copy(alpha = 0.25f))
                ForumContextActionRow(DfIcons.MessageCircle, "پاسخ", onClick = { onReply(); onDismiss() })
                ForumContextActionRow(DfIcons.Copy, "کپی متن", onClick = { onCopy(); onDismiss() })
                if (message.isMine && !message.isDeleted) {
                    ForumContextActionRow(DfIcons.Pencil, "ویرایش", onClick = { onEdit(); onDismiss() })
                }
                if ((message.isMine || canModerate) && !message.isDeleted) {
                    ForumContextActionRow(
                        DfIcons.Trash,
                        "حذف",
                        destructive = true,
                        onClick = { onDelete(); onDismiss() },
                    )
                }
                if (!message.isMine) {
                    ForumContextActionRow(DfIcons.Flag, "گزارش", onClick = { onReport(); onDismiss() })
                }
                if (canModerate && !message.isDeleted) {
                    ForumContextActionRow(
                        DfIcons.Pin,
                        if (message.isPinned) "برداشتن سنجاق" else "سنجاق در تالار",
                        onClick = { onPin(); onDismiss() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ForumContextMessagePreview(message: ForumMessageDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Card)
            .background(DfThemeColors.surfaceVariant().copy(alpha = 0.35f))
            .padding(AppSpacing.md),
    ) {
        Text(
            text = message.senderLabel,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = DfThemeColors.primary(),
        )
        Text(
            text = if (message.isDeleted) "این پیام حذف شده است" else message.body,
            style = AppTypography.body,
            modifier = Modifier.padding(top = AppSpacing.xxs),
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ForumContextActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Chip)
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (destructive) DfThemeColors.error() else DfThemeColors.textPrimary(),
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = AppTypography.body,
            color = if (destructive) DfThemeColors.error() else DfThemeColors.textPrimary(),
        )
    }
}

@Composable
fun ForumChatDaySeparator(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = DfThemeColors.textSecondary(),
            modifier = Modifier
                .clip(AppShapes.ButtonPill)
                .background(DfThemeColors.surfaceVariant().copy(alpha = 0.55f))
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.xxs),
        )
    }
}
