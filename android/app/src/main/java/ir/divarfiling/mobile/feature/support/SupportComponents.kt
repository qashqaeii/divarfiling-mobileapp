package ir.divarfiling.mobile.feature.support

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.network.SupportTicketDto
import ir.divarfiling.mobile.core.network.SupportTicketMessageDto
import ir.divarfiling.mobile.core.network.SupportTicketStatsDto
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SupportStatsRow(
    stats: SupportTicketStatsDto,
    selectedFilter: SupportTicketFilter,
    onFilterSelect: (SupportTicketFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    val metrics = listOf(
        Triple(SupportTicketFilter.All, stats.total, DfColors.PurpleContainer),
        Triple(SupportTicketFilter.Open, stats.open, DfColors.BlueLight),
        Triple(SupportTicketFilter.WaitingUser, stats.waitingUser, DfColors.AmberLight),
        Triple(SupportTicketFilter.Closed, stats.closed, DfColors.LockedContainer),
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            metrics.take(2).forEach { (filter, value, bg) ->
                SupportMetricCard(
                    value = numberFormat.format(value),
                    label = filter.label,
                    background = bg,
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelect(filter) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            metrics.drop(2).forEach { (filter, value, bg) ->
                SupportMetricCard(
                    value = numberFormat.format(value),
                    label = filter.label,
                    background = bg,
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelect(filter) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SupportMetricCard(
    value: String,
    label: String,
    background: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(
        modifier = modifier,
        onClick = onClick,
        containerColor = if (selected) DfThemeColors.primaryContainer() else DfThemeColors.surface(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = AppShapes.IconContainer,
                color = background,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = DfIcons.MessageCircle,
                        contentDescription = null,
                        tint = DfThemeColors.primary(),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun SupportUnreadBanner(
    unreadCount: Int,
    modifier: Modifier = Modifier,
) {
    if (unreadCount <= 0) return
    DfStatusBanner(
        modifier = modifier,
        title = "$unreadCount پاسخ جدید از پشتیبانی",
        message = "تیکت‌هایی که پاسخ جدید دارند را بررسی کنید.",
        tone = DfStatusTone.Info,
    )
}

@Composable
fun SupportTicketTimeline(
    status: String,
    modifier: Modifier = Modifier,
) {
    val currentStep = ticketStatusStep(status)
    DfCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = DfThemeColors.surface(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = "پیشرفت تیکت",
                style = AppTypography.bodyDescription,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                supportTimelineSteps.forEach { step ->
                    val isDone = currentStep >= step.step
                    val isCurrent = status == step.key
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> DfThemeColors.primary()
                                        isDone -> DfThemeColors.primary().copy(alpha = 0.35f)
                                        else -> DfThemeColors.surfaceVariant()
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = step.step.toString(),
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent || isDone) {
                                    DfThemeColors.onPrimary()
                                } else {
                                    DfThemeColors.textMuted()
                                },
                            )
                        }
                        Text(
                            text = step.label,
                            style = AppTypography.labelSmall,
                            color = if (isCurrent) {
                                DfThemeColors.primary()
                            } else {
                                DfThemeColors.textMuted()
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupportTicketListCard(
    ticket: SupportTicketDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (badgeBg, badgeFg) = ticketStatusColors(ticket.status)
    val preview = ticket.description.trim().ifBlank { ticket.subject }
    DfCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = if (ticket.userHasUnread) {
            DfThemeColors.primaryContainer().copy(alpha = 0.35f)
        } else {
            DfThemeColors.surface()
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    ticket.subject,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (ticket.userHasUnread) {
                    DfBadge(
                        text = "پاسخ جدید",
                        color = AppColors.RoseLight,
                        textColor = AppColors.Rose,
                    )
                }
            }
            Text(
                preview,
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "#${ticket.ticketNumber}",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
                DfBadge(
                    text = ticketStatusLabel(ticket.status),
                    color = badgeBg,
                    textColor = badgeFg,
                )
                ticket.priority.takeIf { it.isNotBlank() }?.let { priority ->
                    val (pBg, pFg) = supportPriorityColors(priority)
                    DfBadge(
                        text = supportPriorityLabel(priority),
                        color = pBg,
                        textColor = pFg,
                    )
                }
            }
            ticket.category.takeIf { it.isNotBlank() }?.let { category ->
                Text(
                    supportCategoryLabel(category),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            val timeLabel = ticket.lastMessageAt ?: ticket.updatedAt ?: ticket.createdAt
            timeLabel?.let {
                Text(
                    DateUtils.formatForDisplay(it),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                )
            }
        }
    }
}

@Composable
fun SupportMessageBubble(
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
                    messageAuthorLabel(message),
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isStaff) DfThemeColors.primary() else DfThemeColors.onPrimaryContainer(),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportChipGroup(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                enabled = enabled,
            )
        }
    }
}
