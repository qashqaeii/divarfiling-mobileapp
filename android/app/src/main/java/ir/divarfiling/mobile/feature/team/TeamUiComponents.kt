package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import ir.divarfiling.mobile.core.design.components.DfSnackbarHost
import androidx.compose.material3.SnackbarHostState
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.DfHubBackButton
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
import ir.divarfiling.mobile.core.design.PresentationLabels
import ir.divarfiling.mobile.core.network.AgencyActivityDto
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
    val onClick: (() -> Unit)? = null,
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
fun TeamAgencySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    DfSnackbarHost(
        hostState = hostState,
        modifier = modifier
            .padding(horizontal = teamHorizontalPadding())
            .padding(bottom = AppSpacing.md),
    )
}

/** هدر ثابت بالای صفحات آژانس؛ inset نوار وضعیت فقط اینجا اعمال می‌شود — روی والد `statusBarsPadding` نزنید. */
@Composable
fun TeamAgencyScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector = DfIcons.Users,
    accent: AgencyHeaderAccent = AgencyHeaderAccent.Management,
    actions: @Composable () -> Unit = {},
) {
    AgencyManagementHeader(
        title = title,
        subtitle = subtitle,
        onBack = onBack,
        modifier = modifier,
        icon = icon,
        accent = accent,
        actions = actions,
    )
}

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
    if (metrics.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                rowMetrics.forEach { metric ->
                    TeamAgencyKpiCompact(metric = metric, modifier = Modifier.weight(1f))
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TeamAgencyKpiFeatured(metric: TeamMetric) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(AppShapes.Hero)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        metric.tint.copy(alpha = 0.14f),
                        DfThemeColors.surface(),
                    ),
                ),
            )
            .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), AppShapes.Hero)
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(AppShapes.IconContainer)
                .background(metric.tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(metric.icon, contentDescription = null, tint = metric.tint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                metric.label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
            )
            Text(
                metric.value,
                style = AppTypography.pageTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
            )
        }
    }
}

@Composable
private fun TeamAgencyKpiCompact(metric: TeamMetric, modifier: Modifier = Modifier) {
    val shape = AppShapes.StatCard
    val base = modifier
        .height(68.dp)
        .clip(shape)
        .background(DfThemeColors.surface())
        .border(BorderStroke(1.dp, DfThemeColors.outlineSubtle()), shape)
    val sized = if (metric.onClick != null) {
        base.then(Modifier.clickable(onClick = metric.onClick))
    } else {
        base
    }
    Column(
        modifier = sized.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(metric.icon, contentDescription = null, tint = metric.tint, modifier = Modifier.size(14.dp))
            Text(
                metric.label,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            metric.value,
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.textPrimary(),
            maxLines = 1,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
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
fun TeamMessageFolderBar(
    selected: TeamMessageFolder,
    onSelect: (TeamMessageFolder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        TeamMessageFolder.entries.forEach { folder ->
            val active = folder == selected
            Surface(
                onClick = { onSelect(folder) },
                modifier = Modifier.weight(1f),
                shape = AppShapes.CardSmall,
                color = if (active) DfThemeColors.primary() else DfThemeColors.surface(),
                border = BorderStroke(
                    1.dp,
                    if (active) DfThemeColors.primary() else DfThemeColors.outlineSubtle(),
                ),
            ) {
                Text(
                    folder.label,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = AppSpacing.xs),
                    style = AppTypography.labelSmall,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    color = if (active) Color.White else DfThemeColors.textSecondary(),
                    maxLines = 1,
                )
            }
        }
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
    val accent = teamAccentFor(title)
    val preview = thread.lastMessage?.body?.ifBlank { "بدون متن" } ?: "مکالمه خالی"
    val timeLabel = DateUtils.formatRelativeFa(thread.updatedAt)
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = if (unread) {
            DfThemeColors.primaryContainer().copy(alpha = 0.22f)
        } else {
            DfThemeColors.background()
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                TeamAvatar(
                    name = title,
                    size = 52.dp,
                    accent = if (unread) DfThemeColors.primary() else accent,
                )
                if (unread) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .clip(CircleShape)
                            .background(DfThemeColors.primary())
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            DateUtils.toPersianDigits(thread.unreadCount.coerceAtMost(99).toString()),
                            style = AppTypography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        style = AppTypography.labelLarge,
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        timeLabel,
                        style = AppTypography.labelSmall,
                        color = if (unread) DfThemeColors.primary() else DfThemeColors.textMuted(),
                        fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
                Text(
                    preview,
                    style = AppTypography.bodyDescription,
                    color = if (unread) DfThemeColors.textPrimary() else DfThemeColors.textSecondary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (thread.kind == "broadcast" || thread.isStarred) {
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
                                text = "ستاره‌دار",
                                color = DfThemeColors.primaryContainer(),
                                textColor = DfThemeColors.primary(),
                            )
                        }
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
    val unread = !item.isRead
    val accent = when {
        unread && important -> DfThemeColors.error()
        unread -> DfThemeColors.primary()
        else -> DfThemeColors.outlineSubtle()
    }
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = if (unread) DfThemeColors.primaryContainer().copy(alpha = 0.2f) else DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shape = AppShapes.CardSmall,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
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
                    style = AppTypography.bodyDescription,
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
                            color = DfThemeColors.warningContainer(),
                            textColor = DfThemeColors.warning(),
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
    selectable: Boolean = false,
    onAssign: (() -> Unit)? = null,
    onOpenContact: (() -> Unit)? = null,
    assigneeHint: String? = null,
) {
    val displayName = lead.name.ifBlank { "بدون نام" }
    val accent = teamAccentFor(displayName)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (selectable || onAssign == null) Modifier.clickable(onClick = onToggle) else Modifier,
            ),
        shape = AppShapes.CardSmall,
        color = if (selected) {
            DfThemeColors.successContainer().copy(alpha = 0.35f)
        } else {
            DfThemeColors.surface()
        },
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selectable) {
                    androidx.compose.material3.Checkbox(
                        checked = selected,
                        onCheckedChange = { onToggle() },
                    )
                }
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
            if (!selectable && (onAssign != null || onOpenContact != null)) {
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

sealed class TeamChatTimelineEntry {
    data class Date(val label: String) : TeamChatTimelineEntry()
    data class Message(val message: TeamChatMessageDto) : TeamChatTimelineEntry()
}

fun buildTeamChatTimeline(messages: List<TeamChatMessageDto>): List<TeamChatTimelineEntry> {
    if (messages.isEmpty()) return emptyList()
    val out = mutableListOf<TeamChatTimelineEntry>()
    var lastDay = ""
    messages.forEach { message ->
        val dayKey = message.createdAt.trim().take(10)
        if (dayKey.isNotBlank() && dayKey != lastDay) {
            val label = DateUtils.formatJalaliDate(dayKey) ?: DateUtils.formatForDisplay(message.createdAt)
            out.add(TeamChatTimelineEntry.Date(label))
            lastDay = dayKey
        }
        out.add(TeamChatTimelineEntry.Message(message))
    }
    return out
}

@Composable
fun TeamChatDatePill(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DfThemeColors.surfaceVariant().copy(alpha = 0.85f),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        ) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
                fontWeight = FontWeight.Medium,
            )
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
    val bubbleColor = if (mine) DfThemeColors.primary() else DfThemeColors.surface()
    val textColor = if (mine) Color.White else DfThemeColors.textPrimary()
    val metaColor = if (mine) Color.White.copy(alpha = 0.78f) else DfThemeColors.textMuted()
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
            TeamAvatar(name = message.senderName, size = 32.dp, accent = accent)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
                    .background(bubbleColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    message.body.ifBlank { "—" },
                    style = AppTypography.bodyDescription,
                    color = textColor,
                )
                Text(
                    DateUtils.formatRelativeFa(message.createdAt),
                    style = AppTypography.labelSmall,
                    color = metaColor,
                    modifier = Modifier.align(Alignment.End),
                )
            }
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
    label: String = "پیام",
    sendLabel: String = "ارسال",
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
        color = DfThemeColors.surface(),
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            DfTextField(
                value = value,
                onValueChange = onValueChange,
                label = label,
                singleLine = false,
                minLines = 1,
                modifier = Modifier.weight(1f),
            )
            val canSend = !isSubmitting && value.isNotBlank()
            val sendContainer = if (canSend) {
                DfThemeColors.primary()
            } else {
                DfThemeColors.surface()
            }
            val sendIconTint = if (canSend) {
                Color.White
            } else {
                DfThemeColors.textMuted().copy(alpha = 0.55f)
            }
            FloatingActionButton(
                onClick = { if (canSend) onSend() },
                modifier = Modifier
                    .size(48.dp)
                    .then(
                        if (!canSend) {
                            Modifier.border(
                                width = 1.dp,
                                color = DfThemeColors.outlineSubtle().copy(alpha = 0.65f),
                                shape = CircleShape,
                            )
                        } else {
                            Modifier
                        },
                    ),
                containerColor = sendContainer,
                contentColor = sendIconTint,
                elevation = if (canSend) {
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 6.dp,
                    )
                } else {
                    FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp)
                },
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = DfIcons.Send,
                        contentDescription = sendLabel,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun TeamMessagesComposeSheetContent(
    members: List<TeamMemberDto>,
    query: String,
    onQueryChange: (String) -> Unit,
    selectedMemberId: Long?,
    onMemberSelect: (Long) -> Unit,
    body: String,
    onBodyChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    showMessageField: Boolean = true,
) {
    val filtered = remember(members, query) {
        val q = query.trim()
        if (q.isBlank()) members
        else members.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.phone.contains(q) ||
                it.title.contains(q, ignoreCase = true)
        }
    }
    val selected = members.firstOrNull { it.id == selectedMemberId }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        DfTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "جستجوی همکار",
            placeholder = "نام یا موبایل…",
            singleLine = true,
        )
        if (selected != null) {
            Surface(
                shape = AppShapes.CardSmall,
                color = DfThemeColors.primaryContainer().copy(alpha = 0.35f),
                border = BorderStroke(1.dp, DfThemeColors.primary().copy(alpha = 0.25f)),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    TeamAvatar(name = selected.name, size = 40.dp, accent = roleAccent(selected.role))
                    Column(Modifier.weight(1f)) {
                        Text("گیرنده", style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                        Text(selected.name, fontWeight = FontWeight.SemiBold)
                    }
                    DfBadge(
                        text = selected.roleLabel.ifBlank { "عضو" },
                        color = DfThemeColors.primaryContainer(),
                        textColor = DfThemeColors.primary(),
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 180.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "نتیجه‌ای پیدا نشد",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
            } else {
                items(filtered, key = { it.id }) { member ->
                    TeamMemberSelectRow(
                        member = member,
                        selected = member.id == selectedMemberId,
                        onClick = { onMemberSelect(member.id) },
                    )
                }
            }
        }
        if (showMessageField) {
            DfTextField(
                value = body,
                onValueChange = onBodyChange,
                label = "متن پیام",
                singleLine = false,
                minLines = 3,
            )
        }
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
    onClick: (() -> Unit)? = null,
) {
    val warning = item.level == "warning" || item.level == "danger"
    val tint = if (warning) DfThemeColors.warning() else DfThemeColors.info()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
                    val seatBadge = advisor.seatStatusBadgeLabel()
                    val (seatBg, seatFg) = when (advisor.seatAssignmentState()) {
                        AgencySeatAssignmentState.Active,
                        AgencySeatAssignmentState.Unknown,
                        -> DfThemeColors.primaryContainer() to DfThemeColors.primary()
                        AgencySeatAssignmentState.Expired -> DfThemeColors.errorContainer() to DfThemeColors.error()
                        AgencySeatAssignmentState.Unassigned -> DfThemeColors.warningContainer() to DfThemeColors.warning()
                    }
                    DfBadge(text = seatBadge, color = seatBg, textColor = seatFg)
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
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = onResend,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            "ارسال مجدد",
                            color = DfThemeColors.primary(),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    androidx.compose.material3.TextButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            "حذف",
                            color = DfThemeColors.error(),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
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
    highlighted: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val primary = DfThemeColors.primary()
    val borderColor = if (highlighted) primary.copy(alpha = 0.35f) else DfThemeColors.outlineSubtle()
    val container = if (highlighted) primary.copy(alpha = 0.06f) else DfThemeColors.surface()
    Surface(
        onClick = { onClick?.invoke() },
        enabled = enabled && onClick != null,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 92.dp),
        shape = AppShapes.CardSmall,
        color = container,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(AppShapes.IconContainer)
                        .background(primary.copy(alpha = if (highlighted) 0.18f else 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = primary, modifier = Modifier.size(16.dp))
                }
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
                buildString {
                    append(subtitle)
                    if (!metric.isNullOrBlank()) {
                        if (subtitle.isNotBlank()) append(" · ")
                        append(metric)
                    }
                },
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = true),
            )
        }
    }
}

@Composable
fun TeamSeatsOverviewBar(
    seats: AgencySeatsSummaryDto?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    if (seats == null) return
    AgencyHubSeatsSummaryCard(seats = seats, onClick = onClick, modifier = modifier)
}

@Composable
fun TeamActivityTimelineRow(
    title: String,
    actorName: String,
    timeLabel: String,
    modifier: Modifier = Modifier,
    eventType: String = "",
    isLast: Boolean = false,
) {
    val visual = activityEventVisual(eventType)
    val tint = when (visual.tintKey) {
        AgencyActivityTint.Primary -> DfThemeColors.primary()
        AgencyActivityTint.Info -> DfThemeColors.info()
        AgencyActivityTint.Success -> DfThemeColors.success()
        AgencyActivityTint.Warning -> DfThemeColors.warning()
        AgencyActivityTint.Error -> DfThemeColors.error()
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(AppShapes.IconContainer)
                    .background(tint.copy(alpha = 0.12f))
                    .border(BorderStroke(1.dp, tint.copy(alpha = 0.22f)), AppShapes.IconContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(visual.icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(DfThemeColors.outlineSubtle()),
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                title,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
                maxLines = 2,
            )
            if (actorName.isNotBlank()) {
                Text(
                    actorName,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 2,
                )
            }
            if (timeLabel.isNotBlank()) {
                Text(
                    timeLabel,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
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

