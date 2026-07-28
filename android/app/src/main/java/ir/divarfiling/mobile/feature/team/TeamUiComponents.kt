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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
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
    val mid = DfColors.Blue.copy(alpha = 0.05f)
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
                        DfColors.Blue.copy(alpha = 0.08f),
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
                            color = DfColors.RoseLight,
                            textColor = DfColors.Rose,
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
    Column(
        modifier = modifier
            .clip(AppShapes.StatCard)
            .background(metric.tint.copy(alpha = 0.10f))
            .border(BorderStroke(1.dp, metric.tint.copy(alpha = 0.14f)), AppShapes.StatCard)
            .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = metric.icon,
            contentDescription = null,
            tint = metric.tint,
            modifier = Modifier.size(16.dp),
        )
        Text(
            metric.value,
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = metric.tint,
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
    DfCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        containerColor = if (unread) {
            DfColors.BlueLight.copy(alpha = 0.35f)
        } else {
            DfThemeColors.surface()
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                TeamAvatar(name = title)
                if (unread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(DfColors.Blue)
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
                    color = DfThemeColors.textSecondary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    if (thread.kind == "broadcast") {
                        DfBadge(text = "گروهی", color = DfColors.AmberLight, textColor = DfColors.Amber)
                    }
                    if (thread.isStarred) {
                        DfBadge(text = "ستاره", color = DfColors.PurpleLight, textColor = DfColors.PurpleDark)
                    }
                    if (unread) {
                        DfBadge(
                            text = thread.unreadCount.toString(),
                            color = DfColors.BlueLight,
                            textColor = DfColors.Blue,
                        )
                    }
                }
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
    DfCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        containerColor = when {
            !item.isRead && important -> DfColors.RoseLight.copy(alpha = 0.45f)
            !item.isRead -> DfColors.AmberLight.copy(alpha = 0.4f)
            else -> DfThemeColors.surface()
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!item.isRead) {
                    DfBadge(text = "جدید", color = DfColors.AmberLight, textColor = DfColors.Amber)
                }
            }
            Text(
                item.bodyPreview.ifBlank { item.body },
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                if (important) {
                    DfBadge(text = "مهم", color = DfColors.RoseLight, textColor = DfColors.Rose)
                }
                if (item.isPinned) {
                    DfBadge(text = "سنجاق", color = DfColors.PurpleLight, textColor = DfColors.PurpleDark)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamLeadListCard(
    lead: TeamLeadDto,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        containerColor = if (selected) DfColors.GreenLight.copy(alpha = 0.55f) else DfThemeColors.surface(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(
                name = lead.name.ifBlank { lead.phone.ifBlank { "س" } },
                accent = if (selected) DfColors.Green else DfColors.Blue,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    lead.name.ifBlank { "بدون نام" },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                )
                if (lead.phone.isNotBlank()) {
                    Text(lead.phone, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
                }
                Text(
                    listOfNotNull(
                        lead.source.takeIf { it.isNotBlank() },
                        DateUtils.formatRelativeFa(lead.createdAt).takeIf { it.isNotBlank() },
                    ).joinToString(" · "),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) DfColors.Green else Color.Transparent)
                    .border(
                        BorderStroke(1.5.dp, if (selected) DfColors.Green else DfThemeColors.outline()),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = DfIcons.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
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
    val shape = if (mine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        if (!mine) {
            TeamAvatar(name = message.senderName, size = 32.dp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .then(
                    if (mine) {
                        Modifier.liquidGlassSurface(
                            shape = shape,
                            variant = DfGlassButtonVariant.Accent,
                            accent = DfColors.Blue,
                            elevation = AppElevations.none,
                        )
                    } else {
                        Modifier
                            .background(DfThemeColors.surface())
                            .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), shape)
                    },
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!mine) {
                Text(
                    message.senderName,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.Blue,
                )
            }
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

fun teamListContentPadding(bottomExtra: Dp = 0.dp): PaddingValues {
    return PaddingValues(bottom = AppSpacing.xxxl + bottomExtra)
}
