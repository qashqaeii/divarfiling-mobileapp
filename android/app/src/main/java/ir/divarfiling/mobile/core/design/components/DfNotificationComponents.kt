package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme

enum class DfNotificationSectionKind {
    Action,
    Unread,
    Older,
}

data class DfNotificationVisual(
    val icon: ImageVector,
    val accent: Color,
    val container: Color,
)

@Composable
fun DfNotificationSection(
    title: String,
    count: Int,
    kind: DfNotificationSectionKind,
    modifier: Modifier = Modifier,
) {
    val accent = when (kind) {
        DfNotificationSectionKind.Action -> DfThemeColors.error()
        DfNotificationSectionKind.Unread -> DfThemeColors.primary()
        DfNotificationSectionKind.Older -> DfThemeColors.textMuted()
    }
    val chipBackground = accent.copy(alpha = if (DfThemeColors.isDark()) 0.16f else 0.10f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            text = title,
            style = AppTypography.sectionTitle,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.textPrimary(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (count > 0) {
            Surface(shape = AppShapes.Chip, color = chipBackground, shadowElevation = 0.dp) {
                Text(
                    text = DateUtils.toPersianDigits(count.toString()),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = AppTypography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun DfNotificationToolbar(
    unreadCount: Int,
    isMarkingAllRead: Boolean,
    onMarkAllRead: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (unreadCount <= 0 && !isMarkingAllRead) return

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        val enabled = unreadCount > 0 && !isMarkingAllRead
        Surface(
            onClick = onMarkAllRead,
            enabled = enabled,
            shape = AppShapes.Chip,
            color = DfThemeColors.primaryContainer().copy(
                alpha = if (DfThemeColors.isDark()) 0.28f else 0.55f,
            ),
            border = BorderStroke(
                1.dp,
                DfThemeColors.primary().copy(alpha = if (enabled) 0.28f else 0.12f),
            ),
            shadowElevation = 0.dp,
            modifier = Modifier
                .defaultMinSize(minHeight = 40.dp)
                .semantics { role = Role.Button },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isMarkingAllRead) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 1.5.dp,
                        color = DfThemeColors.primary(),
                    )
                } else {
                    Icon(
                        imageVector = DfIcons.Check,
                        contentDescription = null,
                        tint = DfThemeColors.primary(),
                        modifier = Modifier.size(14.dp),
                    )
                }
                Text(
                    text = if (isMarkingAllRead) "در حال ثبت…" else "همه خوانده شد",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled || isMarkingAllRead) {
                        DfThemeColors.primary()
                    } else {
                        DfThemeColors.textMuted()
                    },
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun DfNotificationUnreadBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return
    val label = if (count > 99) "99+" else count.toString()
    Surface(
        modifier = modifier.defaultMinSize(minWidth = 22.dp, minHeight = 22.dp),
        shape = AppShapes.Chip,
        color = DfThemeColors.primary(),
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = DateUtils.toPersianDigits(label),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun DfNotificationItem(
    title: String,
    body: String,
    timeAgo: String,
    visual: DfNotificationVisual,
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: DfNotificationSectionKind = if (isRead) {
        DfNotificationSectionKind.Older
    } else {
        DfNotificationSectionKind.Unread
    },
) {
    val compact = kind == DfNotificationSectionKind.Older
    val containerColor = when (kind) {
        DfNotificationSectionKind.Action -> visual.container.copy(
            alpha = if (DfThemeColors.isDark()) 0.35f else 0.55f,
        )
        DfNotificationSectionKind.Unread -> DfThemeColors.surface()
        DfNotificationSectionKind.Older -> DfThemeColors.surface().copy(
            alpha = if (DfThemeColors.isDark()) 0.72f else 0.92f,
        )
    }
    val borderColor = when (kind) {
        DfNotificationSectionKind.Action -> visual.accent.copy(alpha = 0.28f)
        DfNotificationSectionKind.Unread -> if (!isRead) {
            visual.accent.copy(alpha = 0.16f)
        } else {
            DfThemeColors.outlineSubtle()
        }
        DfNotificationSectionKind.Older -> DfThemeColors.outlineSubtle()
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = if (compact) 56.dp else AppSpacing.listRowMinHeight)
                .padding(
                    horizontal = 14.dp,
                    vertical = if (compact) 12.dp else 14.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !isRead && kind == DfNotificationSectionKind.Action -> visual.accent
                            !isRead -> DfThemeColors.primary()
                            else -> DfThemeColors.textMuted().copy(alpha = 0.45f)
                        },
                    ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    style = AppTypography.cardTitle,
                    fontWeight = if (isRead) FontWeight.Medium else FontWeight.Bold,
                    color = if (isRead) {
                        DfThemeColors.textPrimary().copy(alpha = 0.82f)
                    } else {
                        DfThemeColors.textPrimary()
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (body.isNotBlank()) {
                    Text(
                        text = body,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = timeAgo,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .size(if (compact) 32.dp else 36.dp)
                    .clip(AppShapes.IconContainer)
                    .background(visual.container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.accent,
                    modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, name = "Notification item 360")
@Composable
private fun DfNotificationItemPreview360() {
    DivarFilingTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DfNotificationSection("نیاز به اقدام", 2, DfNotificationSectionKind.Action)
            DfNotificationItem(
                title = "چند تا پیگیری مونده",
                body = "۱ مشتری منتظر پیگیریه. بهتره امروز تماس بگیرید.",
                timeAgo = "۲ ساعت پیش",
                visual = DfNotificationVisual(
                    icon = DfIcons.Phone,
                    accent = DfThemeColors.warning(),
                    container = DfThemeColors.warningContainer(),
                ),
                isRead = false,
                kind = DfNotificationSectionKind.Action,
                onClick = {},
            )
            DfNotificationToolbar(unreadCount = 3, isMarkingAllRead = false, onMarkAllRead = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 320, name = "Notification item 320")
@Composable
private fun DfNotificationItemPreview320() {
    DivarFilingTheme {
        DfNotificationItem(
            title = "فایل پیشنهادی برای مشتری",
            body = "یک فایل جدید با شرایط مشتری شما تطبیق داده شد.",
            timeAgo = "۴۵ دقیقه پیش",
            visual = DfNotificationVisual(
                icon = DfIcons.Star,
                accent = DfThemeColors.success(),
                container = DfThemeColors.successContainer(),
            ),
            isRead = false,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
