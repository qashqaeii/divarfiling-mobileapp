package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
import ir.divarfiling.mobile.core.design.PresentationLabels
import ir.divarfiling.mobile.core.network.AgencyAttentionDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorDto
import ir.divarfiling.mobile.core.network.AgencyInvitationDto
import ir.divarfiling.mobile.core.network.AgencySeatsSummaryDto
import ir.divarfiling.mobile.core.network.TeamAnnouncementDto
import ir.divarfiling.mobile.core.network.TeamChatMessageDto
import ir.divarfiling.mobile.core.network.TeamLeadDto
import ir.divarfiling.mobile.core.network.TeamMemberDto
import ir.divarfiling.mobile.core.network.TeamThreadDto
import kotlin.math.absoluteValue

data class TeamMetric(
    val label: String,
    val value: String,
    val tint: Color,
    val icon: ImageVector,
)

data class TeamDestination(
    val title: String,
    val subtitle: String,
    val metricLabel: String,
    val metricValue: String,
    val tint: Color,
    val wash: Color,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun teamHorizontalPadding(): Dp {
    val width = LocalConfiguration.current.screenWidthDp
    return when {
        width < 360 -> 14.dp
        width > 600 -> 28.dp
        else -> AppSpacing.screenHorizontal
    }
}

@Composable
fun TeamAmbientBackground(modifier: Modifier = Modifier) {
    val top = DfThemeColors.primary().copy(alpha = 0.08f)
    val mid = DfThemeColors.info().copy(alpha = 0.05f)
    val bottom = DfThemeColors.background()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(top, mid, bottom, bottom),
                ),
            ),
    )
}

@Composable
fun TeamIdentityHero(
    agencyName: String,
    roleLabel: String,
    title: String,
    unreadTotal: Int,
    modifier: Modifier = Modifier,
) {
    val accent = teamAccentFor(agencyName)
    val surface = DfThemeColors.surface()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.Hero)
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.18f),
                        DfThemeColors.info().copy(alpha = 0.08f),
                        surface.copy(alpha = 0.96f),
                    ),
                ),
            )
            .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), AppShapes.Hero)
            .padding(AppSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = agencyName, size = 56.dp, accent = accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    agencyName,
                    style = AppTypography.pageTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull(roleLabel.takeIf { it.isNotBlank() }, title.takeIf { it.isNotBlank() })
                        .joinToString(" · ")
                        .ifBlank { "میزکار آژانس" },
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    DfBadge(
                        text = roleLabel.ifBlank { "عضو" },
                        color = accent.copy(alpha = 0.14f),
                        textColor = accent,
                    )
                    if (unreadTotal > 0) {
                        DfBadge(
                            text = "$unreadTotal خوانده‌نشده",
                            color = DfThemeColors.errorContainer(),
                            textColor = DfThemeColors.error(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamMetricsRow(
    metrics: List<TeamMetric>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 360.dp
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                metrics.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        row.forEach { metric ->
                            TeamMetricChip(metric = metric, modifier = Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                metrics.forEach { metric ->
                    TeamMetricChip(metric = metric, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TeamMetricChip(
    metric: TeamMetric,
    modifier: Modifier = Modifier,
) {
    val primary = DfThemeColors.primary()
    Column(
        modifier = modifier
            .clip(AppShapes.StatCard)
            .background(DfThemeColors.surface())
            .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), AppShapes.StatCard)
            .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = metric.icon,
            contentDescription = null,
            tint = metric.tint.copy(alpha = 0.92f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            metric.value,
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = primary,
        )
        Text(
            metric.label,
            style = AppTypography.labelSmall,
            color = DfThemeColors.textSecondary(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDestinationCard(
    destination: TeamDestination,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = destination.onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
        tonalElevation = 0.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(108.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(destination.tint, destination.tint.copy(alpha = 0.35f)),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(AppShapes.IconContainer)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    destination.wash,
                                    destination.tint.copy(alpha = 0.12f),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null,
                        tint = destination.tint,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        destination.title,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        destination.subtitle,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    DfBadge(
                        text = "${destination.metricLabel}: ${destination.metricValue}",
                        color = destination.wash,
                        textColor = destination.tint,
                    )
                }
                Icon(
                    imageVector = DfIcons.ChevronLeft,
                    contentDescription = null,
                    tint = DfThemeColors.textMuted(),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun TeamAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    accent: Color = teamAccentFor(name),
) {
    val initials = remember(name) { teamInitials(name) }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.55f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = if (size >= 52.dp) AppTypography.cardTitle else AppTypography.labelSmall,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamThreadListCard(
    thread: TeamThreadDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = thread.participantsLabel.ifBlank { thread.subject }
    val unread = thread.unreadCount > 0
    val accent = if (unread) DfThemeColors.info() else DfThemeColors.outlineSubtle()
    DfCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        containerColor = if (unread) {
            DfThemeColors.infoContainer().copy(alpha = 0.35f)
        } else {
            DfThemeColors.surface()
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    TeamAvatar(name = title, accent = if (unread) DfThemeColors.info() else teamAccentFor(title))
                    if (unread) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(DfThemeColors.info())
                                .border(2.dp, DfThemeColors.surface(), CircleShape),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            title,
                            style = AppTypography.cardTitle,
                            fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            color = DfThemeColors.textPrimary(),
                        )
                        Text(
                            DateUtils.formatRelativeFa(thread.updatedAt),
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                    Text(
                        thread.lastMessage?.body?.ifBlank { "بدون متن" } ?: "مکالمه خالی",
                        style = AppTypography.bodyDescription,
                        color = if (unread) DfThemeColors.textPrimary() else DfThemeColors.textSecondary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (unread) FontWeight.Medium else FontWeight.Normal,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        if (thread.kind == "broadcast") {
                            DfBadge(
                                text = "گروهی",
                                color = DfThemeColors.warningContainer(),
                                textColor = DfThemeColors.warning(),
                            )
                        }
                        if (thread.isStarred) {
                            DfBadge(
                                text = "ستاره",
                                color = DfThemeColors.primaryContainer(),
                                textColor = DfThemeColors.primary(),
                            )
                        }
                        if (unread) {
                            DfBadge(
                                text = "${DateUtils.toPersianDigits(thread.unreadCount.toString())} خوانده‌نشده",
                                color = DfThemeColors.infoContainer(),
                                textColor = DfThemeColors.info(),
                            )
                        }
                    }
                }
                Icon(
                    imageVector = DfIcons.ChevronLeft,
                    contentDescription = null,
                    tint = DfThemeColors.textMuted(),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
fun TeamMemberListCard(
    member: TeamMemberDto,
    modifier: Modifier = Modifier,
) {
    val accent = roleAccent(member.role)
    DfCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = member.name, accent = accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    member.name,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (member.title.isNotBlank()) {
                    Text(
                        member.title,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (member.phone.isNotBlank()) {
                    Text(
                        member.phone,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                }
            }
            DfBadge(
                text = member.roleLabel.ifBlank { member.role },
                color = accent.copy(alpha = 0.14f),
                textColor = accent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMemberSelectRow(
    member: TeamMemberDto,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = roleAccent(member.role)
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = if (selected) accent.copy(alpha = 0.10f) else DfThemeColors.surface(),
        border = BorderStroke(
            1.dp,
            if (selected) accent.copy(alpha = 0.45f) else DfThemeColors.outlineSubtle(),
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = member.name, size = 40.dp, accent = accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    member.name,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull(
                        member.roleLabel.ifBlank { member.role }.takeIf { it.isNotBlank() },
                        member.title.takeIf { it.isNotBlank() },
                    ).joinToString(" · "),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                Icon(
                    imageVector = DfIcons.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun TeamMemberSelectList(
    members: List<TeamMemberDto>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    emptyLabel: String = "عضوی برای انتخاب نیست",
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        if (members.isEmpty()) {
            Text(
                emptyLabel,
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
            )
        } else {
            members.forEach { member ->
                TeamMemberSelectRow(
                    member = member,
                    selected = member.id == selectedId,
                    onClick = { onSelect(member.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamAnnouncementListCard(
    item: TeamAnnouncementDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val important = item.importance == "important" || item.importance == "urgent"
    val unread = !item.isRead
    val accent = when {
        unread && important -> DfThemeColors.error()
        unread -> DfThemeColors.primary()
        else -> DfThemeColors.outlineSubtle()
    }
    DfCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.title,
                    style = AppTypography.labelLarge,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (unread) {
                    DfBadge(
                        text = "جدید",
                        color = DfThemeColors.primaryContainer(),
                        textColor = DfThemeColors.primary(),
                    )
                }
            }
            Text(
                item.bodyPreview.ifBlank { item.body },
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                if (important) {
                    DfBadge(
                        text = "مهم",
                        color = DfThemeColors.errorContainer(),
                        textColor = DfThemeColors.error(),
                    )
                }
                if (item.isPinned) {
                    DfBadge(
                        text = "سنجاق",
                        color = DfThemeColors.primaryContainer(),
                        textColor = DfThemeColors.primary(),
                    )
                }
                Text(
                    DateUtils.formatRelativeFa(item.publishedAt),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamLeadListCard(
    lead: TeamLeadDto,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onAssign: (() -> Unit)? = null,
    onOpenContact: (() -> Unit)? = null,
    assigneeHint: String? = null,
) {
    val displayName = lead.name.ifBlank { "بدون نام" }
    val accent = teamAccentFor(displayName)
    DfCard(
        onClick = if (onAssign == null) onToggle else null,
        modifier = modifier.fillMaxWidth(),
        containerColor = if (selected) {
            DfThemeColors.successContainer().copy(alpha = 0.45f)
        } else {
            DfThemeColors.surface()
        },
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAvatar(
                    name = lead.name.ifBlank { lead.phone.ifBlank { "س" } },
                    accent = accent,
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        displayName,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    if (lead.phone.isNotBlank()) {
                        Text(
                            lead.phone,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                    if (lead.needSummary.isNotBlank()) {
                        Text(
                            lead.needSummary,
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        lead.source.takeIf { it.isNotBlank() }?.let { src ->
                            DfBadge(
                                text = src,
                                color = DfThemeColors.surfaceVariant(),
                                textColor = DfThemeColors.textSecondary(),
                            )
                        }
                        if (lead.status.isNotBlank()) {
                            DfBadge(
                                text = lead.status,
                                color = DfThemeColors.warningContainer(),
                                textColor = DfThemeColors.warning(),
                            )
                        }
                        assigneeHint?.let { hint ->
                            DfBadge(
                                text = hint,
                                color = DfThemeColors.infoContainer(),
                                textColor = DfThemeColors.info(),
                            )
                        }
                        Text(
                            DateUtils.formatRelativeFa(lead.createdAt),
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
            }
            if (onAssign != null || onOpenContact != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    onAssign?.let {
                        DfPrimaryButton(
                            text = "واگذاری",
                            onClick = it,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    onOpenContact?.let {
                        DfSecondaryButton(
                            text = "مشاهده مخاطب",
                            onClick = it,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TeamChatBubble(
    message: TeamChatMessageDto,
    modifier: Modifier = Modifier,
) {
    val mine = message.isMine
    val accent = if (mine) DfThemeColors.primary() else DfThemeColors.info()
    val shape = if (mine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        if (!mine) {
            TeamAvatar(name = message.senderName, size = 30.dp, accent = accent)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!mine && message.senderName.isNotBlank()) {
                Text(
                    message.senderName,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(
                modifier = Modifier
                    .clip(shape)
                    .background(
                        if (mine) {
                            DfThemeColors.primaryContainer().copy(alpha = 0.85f)
                        } else {
                            DfThemeColors.surface()
                        },
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (mine) accent.copy(alpha = 0.2f) else DfThemeColors.outlineSubtle(),
                        ),
                        shape,
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    message.body.ifBlank { "—" },
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textPrimary(),
                )
                Text(
                    DateUtils.formatRelativeFa(message.createdAt),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
        if (mine) {
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun TeamComposerBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier,
    label: String = "پاسخ شما",
    sendLabel: String = "ارسال",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .liquidGlassSurface(
                shape = AppShapes.Hero,
                variant = DfGlassButtonVariant.Secondary,
                elevation = AppElevations.raised,
            )
            .navigationBarsPadding()
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            singleLine = false,
            minLines = 2,
        )
        DfPrimaryButton(
            text = sendLabel,
            onClick = onSend,
            loading = isSubmitting,
            enabled = !isSubmitting && value.isNotBlank(),
        )
    }
}

@Composable
fun TeamSectionLabel(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            title,
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.textPrimary(),
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                subtitle,
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
            )
        }
    }
}

@Composable
fun TeamAttentionCard(
    item: AgencyAttentionDto,
    modifier: Modifier = Modifier,
) {
    val warning = item.level == "warning" || item.level == "danger"
    val tint = if (warning) DfThemeColors.warning() else DfThemeColors.info()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs + 2.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tint),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    PresentationLabels.attentionTitle(item.title),
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = PresentationLabels.attentionSubtitle(item.subtitle)
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (item.count > 0) {
                Text(
                    DateUtils.toPersianDigits(item.count.toString()),
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = tint,
                )
            }
        }
    }
}

data class TeamQuickAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamQuickActionGrid(
    actions: List<TeamQuickAction>,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                rowActions.forEach { action ->
                    Surface(
                        onClick = action.onClick,
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.CardSmall,
                        color = DfThemeColors.surface(),
                        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
                        shadowElevation = AppElevations.subtle,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(action.icon, contentDescription = null, tint = DfThemeColors.primary(), modifier = Modifier.size(18.dp))
                            Text(
                                action.label,
                                style = AppTypography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (rowActions.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamAdvisorCompactCard(
    advisor: AgencyAdvisorDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayName = PresentationLabels.memberDisplayName(advisor.name, advisor.phone)
    val accent = roleAccent(advisor.role)
    val active = advisor.isActive
    DfCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.cardPadding),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAvatar(name = displayName, accent = accent, size = 48.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        displayName,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = DfThemeColors.textPrimary(),
                    )
                    if (advisor.phone.isNotBlank() && advisor.name.isNotBlank()) {
                        Text(
                            advisor.phone,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                        )
                    }
                    Text(
                        "${advisor.roleLabel.ifBlank { "مشاور" }} · ${advisor.seatSummaryLabel()}",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 1,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        DfBadge(
                            text = "سرنخ ${DateUtils.toPersianDigits(advisor.activeLeads.toString())}",
                            color = DfThemeColors.infoContainer(),
                            textColor = DfThemeColors.info(),
                        )
                        DfBadge(
                            text = "معامله ${DateUtils.toPersianDigits(advisor.activeDeals.toString())}",
                            color = DfThemeColors.primaryContainer(),
                            textColor = DfThemeColors.primary(),
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (advisor.resolvedHasSeat()) {
                        DfBadge(
                            text = "صندلی",
                            color = DfThemeColors.primaryContainer(),
                            textColor = DfThemeColors.primary(),
                        )
                    } else if (active) {
                        DfBadge(
                            text = "بدون صندلی",
                            color = DfThemeColors.warningContainer(),
                            textColor = DfThemeColors.warning(),
                        )
                    }
                    DfBadge(
                        text = PresentationLabels.memberActiveLabel(active),
                        color = if (active) DfThemeColors.successContainer() else DfThemeColors.errorContainer(),
                        textColor = if (active) DfThemeColors.success() else DfThemeColors.error(),
                    )
                    Icon(
                        imageVector = DfIcons.ChevronLeft,
                        contentDescription = null,
                        tint = DfThemeColors.textMuted(),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun TeamInvitationCompactCard(
    invitation: AgencyInvitationDto,
    onResend: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = PresentationLabels.invitationStatus(invitation.status)
    val pending = invitation.status == "pending"
    val accent = DfThemeColors.warning()
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamAvatar(name = invitation.phone.ifBlank { "د" }, accent = accent, size = 44.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        invitation.phone.ifBlank { "بدون شماره" },
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        "دعوت به آژانس",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                }
                DfBadge(
                    text = statusLabel,
                    color = DfThemeColors.warningContainer(),
                    textColor = accent,
                )
            }
            if (pending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    DfPrimaryButton(
                        text = "ارسال مجدد",
                        onClick = onResend,
                        modifier = Modifier.weight(1f),
                    )
                    DfSecondaryButton(
                        text = "لغو دعوت",
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun TeamPerformanceAdvisorRow(
    rank: Int?,
    name: String,
    scorePct: Int,
    calls: Int,
    visits: Int,
    activeDeals: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val displayName = PresentationLabels.memberDisplayName(name)
    DfCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DfColors.PurpleLight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    rank?.let { DateUtils.toPersianDigits(it.toString()) } ?: "—",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.PurpleDark,
                )
            }
            TeamAvatar(name = displayName, size = 40.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(displayName, style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { scorePct.coerceIn(0, 100) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "تماس ${DateUtils.toPersianDigits(calls.toString())} · بازدید ${DateUtils.toPersianDigits(visits.toString())} · معامله ${DateUtils.toPersianDigits(activeDeals.toString())}",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            Text(
                "${DateUtils.toPersianDigits(scorePct.toString())}٪",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.primary(),
            )
        }
    }
}

fun teamAccentFor(seed: String): Color {
    val palette = listOf(
        DfColors.Purple,
        DfColors.Blue,
        DfColors.Green,
        DfColors.Amber,
        DfColors.Pink,
        Color(0xFF0F766E),
        Color(0xFF7C3AED),
    )
    val index = seed.trim().hashCode().absoluteValue % palette.size
    return palette[index]
}

fun roleAccent(role: String): Color = when (role) {
    "owner" -> DfColors.Amber
    "manager" -> DfColors.Purple
    "secretary" -> DfColors.Blue
    else -> DfColors.Green
}

fun teamInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "ت"
        parts.size == 1 -> parts[0].take(2)
        else -> "${parts.first().take(1)}${parts.last().take(1)}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDestinationCompact(
    title: String,
    subtitle: String,
    icon: ImageVector,
    metric: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val primary = DfThemeColors.primary()
    Surface(
        onClick = { onClick?.invoke() },
        enabled = enabled && onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = primary, modifier = Modifier.size(18.dp))
                Text(
                    title,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                subtitle,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!metric.isNullOrBlank()) {
                Text(
                    metric,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = primary,
                )
            }
        }
    }
}

@Composable
fun TeamSeatsOverviewBar(
    seats: AgencySeatsSummaryDto?,
    modifier: Modifier = Modifier,
) {
    if (seats == null || seats.total <= 0) return
    val primary = DfThemeColors.primary()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(DfThemeColors.surface())
            .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), AppShapes.CardSmall)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("خلاصه صندلی‌ها", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "${DateUtils.toPersianDigits(seats.assigned.toString())} تخصیص · ${DateUtils.toPersianDigits(seats.available.toString())} آزاد",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
        }
        Text(
            "${DateUtils.toPersianDigits(seats.assigned.toString())}/${DateUtils.toPersianDigits(seats.total.toString())}",
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = primary,
        )
    }
}

@Composable
fun TeamActivityTimelineRow(
    title: String,
    actorName: String,
    timeLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(DfThemeColors.primary().copy(alpha = 0.55f)),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 2)
            if (actorName.isNotBlank()) {
                Text(actorName, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
            if (timeLabel.isNotBlank()) {
                Text(timeLabel, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSettingsNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Icon(icon, contentDescription = null, tint = DfThemeColors.primary(), modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), maxLines = 2)
            }
            Icon(DfIcons.ChevronLeft, contentDescription = null, tint = DfThemeColors.textMuted(), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun TeamAdvisorProfileHeader(
    name: String,
    phone: String,
    roleLabel: String,
    isActive: Boolean,
    seatLabel: String,
    modifier: Modifier = Modifier,
) {
    val displayName = PresentationLabels.memberDisplayName(name, phone)
    val accent = teamAccentFor(displayName)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = displayName, size = 56.dp, accent = accent)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(displayName, style = AppTypography.pageTitle, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(roleLabel.ifBlank { "مشاور" }, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
                Text(seatLabel, style = AppTypography.labelSmall, color = DfThemeColors.primary(), fontWeight = FontWeight.SemiBold)
                DfBadge(
                    text = PresentationLabels.memberActiveLabel(isActive),
                    color = if (isActive) DfThemeColors.successContainer() else DfThemeColors.errorContainer(),
                    textColor = if (isActive) DfThemeColors.success() else DfThemeColors.error(),
                )
            }
        }
    }
}

fun teamListContentPadding(bottomExtra: Dp = 0.dp): PaddingValues {
    return PaddingValues(bottom = AppSpacing.xxxl + AppSpacing.sheetFooterInset + bottomExtra)
}
