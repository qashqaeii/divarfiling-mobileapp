@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.matchParentSize
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
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.network.ForumMessageDto
import ir.divarfiling.mobile.core.network.ForumPinnedNoticeDto
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

@Composable
fun ForumNoticeStrip(
    notices: List<ForumPinnedNoticeDto>,
    modifier: Modifier = Modifier,
) {
    if (notices.isEmpty()) return
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
        notices.forEach { notice ->
            Surface(
                shape = AppShapes.Banner,
                color = DfThemeColors.primaryContainer().copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        DfIcons.Info,
                        contentDescription = null,
                        tint = DfThemeColors.primary(),
                        modifier = Modifier.size(18.dp).padding(top = 2.dp),
                    )
                    Text(
                        text = notice.body,
                        style = AppTypography.body,
                        modifier = Modifier.padding(start = AppSpacing.sm),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Column(Modifier.padding(AppSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        DfIcons.Pin,
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
                Icon(
                    if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                    contentDescription = null,
                    tint = DfThemeColors.textSecondary(),
                    modifier = Modifier.size(18.dp),
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

@Composable
fun ForumAvatar(url: String?, label: String, modifier: Modifier = Modifier) {
    val initial = label.firstOrNull()?.toString() ?: "?"
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(DfThemeColors.primaryContainer()),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            DfAsyncImage(
                url = url,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
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
            .padding(top = AppSpacing.sm, start = AppSpacing.sm, end = AppSpacing.sm, bottom = AppSpacing.xs),
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(DfThemeColors.primary().copy(alpha = 0.07f))
                    .padding(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 36.dp)
                        .background(DfThemeColors.primary()),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = AppSpacing.sm),
                ) {
                    Text("پاسخ", style = AppTypography.labelSmall, color = DfThemeColors.primary())
                    Text(
                        text = label,
                        style = AppTypography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onCancelReply, modifier = Modifier.size(32.dp)) {
                    Icon(DfIcons.X, contentDescription = "لغو پاسخ", tint = DfThemeColors.textSecondary())
                }
            }
        }
        Surface(
            shape = AppShapes.ButtonPill,
            color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(start = AppSpacing.sm, end = AppSpacing.xxs, top = AppSpacing.xxs, bottom = AppSpacing.xxs),
            ) {
                androidx.compose.material3.OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(placeholder, style = AppTypography.body) },
                    minLines = 1,
                    maxLines = 5,
                    shape = AppShapes.Field,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DfThemeColors.surface(),
                        unfocusedContainerColor = DfThemeColors.surface(),
                    ),
                )
                Surface(
                    onClick = onSend,
                    enabled = sendEnabled,
                    shape = CircleShape,
                    color = if (sendEnabled) DfThemeColors.primary() else DfThemeColors.surfaceVariant(),
                    modifier = Modifier.padding(start = AppSpacing.xs, bottom = 4.dp),
                ) {
                    Icon(
                        DfIcons.Send,
                        contentDescription = "ارسال",
                        tint = if (sendEnabled) androidx.compose.ui.graphics.Color.White else DfThemeColors.textSecondary(),
                        modifier = Modifier.padding(10.dp).size(20.dp),
                    )
                }
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
        shape = AppShapes.BottomNav,
        color = DfThemeColors.surface(),
        tonalElevation = 2.dp,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            ForumActionChip("پاسخ", DfIcons.MessageCircle, onReply)
            ForumActionChip("واکنش", DfIcons.Heart, onReaction)
            ForumActionChip("کپی", DfIcons.Copy, onCopy)
            if (message.isMine && !message.isDeleted) ForumActionChip("ویرایش", DfIcons.Pencil, onEdit)
            if ((message.isMine || canModerate) && !message.isDeleted) ForumActionChip("حذف", DfIcons.Trash, onDelete)
            if (!message.isMine) ForumActionChip("گزارش", DfIcons.Flag, onReport)
            if (canModerate && !message.isDeleted) {
                ForumActionChip(
                    if (message.isPinned) "برداشتن سنجاق" else "سنجاق",
                    DfIcons.Pin,
                    onPin,
                )
            }
            ForumActionChip("بستن", DfIcons.X, onDismiss)
        }
    }
}

@Composable
private fun ForumActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.Chip,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = DfThemeColors.primary())
            Text(label, style = AppTypography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumReactionPicker(
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumTopBar(
    searchOpen: Boolean,
    onBack: () -> Unit,
    onToggleSearch: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text("تالار گفتگو", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
                Text(
                    "نگه‌داشتن روی پیام · منوی اقدام",
                    style = AppTypography.meta,
                    color = DfThemeColors.textSecondary(),
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(DfIcons.ArrowRight, contentDescription = "بازگشت")
            }
        },
        actions = {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    if (searchOpen) DfIcons.X else DfIcons.Search,
                    contentDescription = "جستجو",
                    tint = if (searchOpen) DfThemeColors.primary() else DfThemeColors.textPrimary(),
                )
            }
            IconButton(onClick = onOpenProfile) {
                Icon(DfIcons.User, contentDescription = "پروفایل")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DfScreenContainerColor),
    )
}

@Composable
fun ForumNewMessagesPill(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.ButtonPill,
        color = DfThemeColors.primary(),
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
        ) {
            Icon(DfIcons.ChevronDown, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(16.dp))
            Text(
                text = "${DateUtils.toPersianDigits(count.toString())} پیام جدید",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = androidx.compose.ui.graphics.Color.White,
            )
        }
    }
}
