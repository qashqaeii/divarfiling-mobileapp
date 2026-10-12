package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.network.TeamPanelNotificationDto

private data class AgencyHubDestination(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val metric: String? = null,
    val highlighted: Boolean = false,
    val onClick: (() -> Unit)?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamHubScreen(
    onBack: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenAnnouncements: () -> Unit,
    onOpenInbox: () -> Unit,
    onOpenPerformance: () -> Unit = {},
    onOpenSeats: () -> Unit = {},
    onOpenAdvanced: () -> Unit = {},
    onOpenActivity: () -> Unit = {},
    onOpenAgencySpace: () -> Unit = {},
    onNavigateWebBridge: (String) -> Unit = {},
    viewModel: TeamHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val home = state.home
    val pad = teamHorizontalPadding()

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    LaunchedEffect(state.webBridgeUrl) {
        state.webBridgeUrl?.let {
            onNavigateWebBridge(it)
            viewModel.consumeWebBridge()
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TeamAmbientBackground()
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                when {
                    state.isLoading -> {
                        Column {
                            DfHubPageHeader(
                                title = "آژانس",
                                subtitle = "هاب آژانس",
                                sectionLabel = DfHeaderSections.TEAM,
                                titleIconRes = DfDecorIcons.Users,
                                onBack = onBack,
                            )
                            DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        }
                    }
                    home?.hasAgency != true -> {
                        LazyColumn(
                            contentPadding = teamListContentPadding(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                        ) {
                            item {
                                DfHubPageHeader(
                                    title = "آژانس",
                                    subtitle = "هنوز به آژانسی وصل نیستید",
                                    sectionLabel = DfHeaderSections.TEAM,
                                titleIconRes = DfDecorIcons.Users,
                                    userName = state.userName,
                                    onBack = onBack,
                                )
                            }
                            item {
                                DfEmptyState(
                                    title = "آژانسی پیدا نشد",
                                    subtitle = "برای پیام داخلی، اعضا و اعلامیه‌ها باید عضو یک آژانس باشید.",
                                    variant = DfEmptyVariant.Empty,
                                    actionLabel = "باز کردن میزکار تیم",
                                    onAction = { viewModel.openTeamWorkspaceBridge() },
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                        }
                    }
                    else -> {
                        val agency = home.agency
                        val membership = home.membership
                        val perms = home.permissions
                        val unread = home.unread
                        val kpis = home.kpis
                        val seats = home.seats
                        LazyColumn(
                            contentPadding = teamListContentPadding(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                        ) {
                            item {
                                AgencyHubHeader(
                                    agencyName = agency?.name ?: "میزکار آژانس",
                                    logoUrl = agency?.logoUrl,
                                    roleLabel = membership?.roleLabel.orEmpty(),
                                    unreadTotal = unread.messages + unread.announcements + unread.notifications,
                                    onBack = onBack,
                                )
                            }
                            item {
                                val primary = DfThemeColors.primary()
                                val success = DfThemeColors.success()
                                val warning = DfThemeColors.warning()
                                val info = DfThemeColors.info()
                                TeamMetricsRow(
                                    metrics = listOf(
                                        TeamMetric(
                                            "اعضا",
                                            DateUtils.toPersianDigits(kpis.activeMembers.toString()),
                                            primary,
                                            DfIcons.Users,
                                            onClick = onOpenMembers,
                                        ),
                                        TeamMetric(
                                            "سرنخ",
                                            DateUtils.toPersianDigits(kpis.unassignedLeads.toString()),
                                            success,
                                            DfIcons.UserPlus,
                                            onClick = if (perms.canOperateInbox) onOpenInbox else null,
                                        ),
                                        TeamMetric(
                                            "پیگیری",
                                            DateUtils.toPersianDigits(kpis.teamFollowupsToday.toString()),
                                            warning,
                                            DfIcons.Clock,
                                        ),
                                        TeamMetric(
                                            "معامله",
                                            DateUtils.toPersianDigits(kpis.activeDeals.toString()),
                                            info,
                                            DfIcons.Handshake,
                                            onClick = if (perms.canManage) onOpenPerformance else null,
                                        ),
                                    ),
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            item {
                                TeamSectionLabel(
                                    title = "میزکار آژانس",
                                    subtitle = "مقاصد اصلی",
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            item {
                                val caps = home.capabilities
                                val spaceKpi = home.spaceKpis?.activeProperties?.let {
                                    "${DateUtils.toPersianDigits(it.toString())} فایل فعال"
                                }
                                val destinations = buildList {
                                    when {
                                        caps.canAccessAgencySpace -> add(
                                            AgencyHubDestination(
                                                title = "فضای آژانس",
                                                subtitle = "فایل‌ها و مخاطبین منتشرشده",
                                                icon = DfIcons.Layers,
                                                metric = spaceKpi,
                                                highlighted = true,
                                                onClick = onOpenAgencySpace,
                                            ),
                                        )
                                        caps.agencySpaceEnabled -> add(
                                            AgencyHubDestination(
                                                title = "فضای آژانس",
                                                subtitle = "برای دسترسی، عضویت فعال و اشتراک آژانس لازم است",
                                                icon = DfIcons.Layers,
                                                onClick = null,
                                            ),
                                        )
                                        perms.canManageAgency -> add(
                                            AgencyHubDestination(
                                                title = "فضای آژانس",
                                                subtitle = "قابلیت در آژانس فعال نیست — از میزکار وب فعال کنید",
                                                icon = DfIcons.Layers,
                                                onClick = null,
                                            ),
                                        )
                                    }
                                    add(
                                        AgencyHubDestination(
                                            title = "مشاوران",
                                            subtitle = "نقش، صندلی و دعوت‌ها",
                                            icon = DfIcons.Users,
                                            onClick = onOpenMembers,
                                        ),
                                    )
                                    if (perms.canManageSeats) {
                                        add(
                                            AgencyHubDestination(
                                                title = "صندلی‌ها",
                                                subtitle = "تخصیص و آزادسازی لایسنس",
                                                icon = DfIcons.Building,
                                                onClick = onOpenSeats,
                                            ),
                                        )
                                    }
                                    if (perms.messagesEnabled) {
                                        add(
                                            AgencyHubDestination(
                                                title = "صندوق پیام",
                                                subtitle = "${DateUtils.toPersianDigits(unread.messages.toString())} خوانده‌نشده",
                                                icon = DfIcons.MessageCircle,
                                                onClick = onOpenMessages,
                                            ),
                                        )
                                    }
                                    if (perms.canOperateInbox) {
                                        add(
                                            AgencyHubDestination(
                                                title = "صندوق سرنخ",
                                                subtitle = "${DateUtils.toPersianDigits(home.inboxLeadsCount.toString())} در صف",
                                                icon = DfIcons.ListTodo,
                                                onClick = onOpenInbox,
                                            ),
                                        )
                                    }
                                    if (perms.announcementsEnabled) {
                                        add(
                                            AgencyHubDestination(
                                                title = "اطلاعیه‌ها",
                                                subtitle = "${DateUtils.toPersianDigits(unread.announcements.toString())} جدید",
                                                icon = DfIcons.Sparkles,
                                                onClick = onOpenAnnouncements,
                                            ),
                                        )
                                    }
                                    if (perms.canManage) {
                                        add(
                                            AgencyHubDestination(
                                                title = "عملکرد تیم",
                                                subtitle = "مقایسه مشاوران",
                                                icon = DfIcons.BarChart,
                                                onClick = onOpenPerformance,
                                            ),
                                        )
                                        add(
                                            AgencyHubDestination(
                                                title = "فعالیت آژانس",
                                                subtitle = "Timeline رویدادها",
                                                icon = DfIcons.Clock,
                                                onClick = onOpenActivity,
                                            ),
                                        )
                                    }
                                }
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = pad),
                                ) {
                                    val twoCol = maxWidth >= 340.dp
                                    if (twoCol) {
                                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                            destinations.chunked(2).forEach { row ->
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    row.forEach { dest ->
                                                        TeamDestinationCompact(
                                                            title = dest.title,
                                                            subtitle = dest.subtitle,
                                                            icon = dest.icon,
                                                            metric = dest.metric,
                                                            highlighted = dest.highlighted,
                                                            enabled = dest.onClick != null,
                                                            onClick = dest.onClick,
                                                            modifier = Modifier.weight(1f),
                                                        )
                                                    }
                                                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                            destinations.forEach { dest ->
                                                TeamDestinationCompact(
                                                    title = dest.title,
                                                    subtitle = dest.subtitle,
                                                    icon = dest.icon,
                                                    metric = dest.metric,
                                                    highlighted = dest.highlighted,
                                                    enabled = dest.onClick != null,
                                                    onClick = dest.onClick,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (perms.canManageSeats && seats != null) {
                                item {
                                    TeamSeatsOverviewBar(
                                        seats = seats,
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            if (home.attention.isNotEmpty()) {
                                item {
                                    TeamSectionLabel(
                                        title = "نیازمند توجه",
                                        subtitle = "موارد مهم برای امروز",
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                                items(home.attention.take(4), key = { it.kind + it.title }) { item ->
                                    val action: (() -> Unit)? = when (item.kind) {
                                        "pending_invites" -> onOpenMembers
                                        "members_without_seat" -> if (perms.canManageSeats) onOpenSeats else onOpenMembers
                                        "inbox_leads" -> if (perms.canOperateInbox) onOpenInbox else null
                                        else -> null
                                    }
                                    TeamAttentionCard(
                                        item = item,
                                        onClick = action,
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            if (perms.canInvite || perms.canOperateInbox || perms.canManageSeats) {
                                item {
                                    TeamSectionLabel(
                                        title = "اقدام سریع",
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                                item {
                                    TeamQuickActionGrid(
                                        actions = buildList {
                                            if (perms.canInvite) {
                                                add(TeamQuickAction("دعوت مشاور", DfIcons.UserPlus, onOpenMembers))
                                            }
                                            if (perms.canOperateInbox) {
                                                add(TeamQuickAction("تخصیص سرنخ", DfIcons.ListTodo, onOpenInbox))
                                            }
                                            if (perms.canManageSeats) {
                                                add(TeamQuickAction("مدیریت صندلی", DfIcons.Building, onOpenSeats))
                                            }
                                            if (perms.canManage) {
                                                add(TeamQuickAction("عملکرد", DfIcons.BarChart, onOpenPerformance))
                                            }
                                        },
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            if (home.teamActivity.isNotEmpty() && perms.canManage) {
                                item {
                                    TeamSectionLabel(
                                        title = "فعالیت اخیر",
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                                items(home.teamActivity.take(5), key = { it.id }) { ev ->
                                    val time = ev.createdAt.takeIf { it.isNotBlank() }?.let { iso ->
                                        DateUtils.parseInstantMillis(iso)?.let { ms ->
                                            DateUtils.formatJalaliDateTimeLatinFromMillis(ms)
                                        }
                                    } ?: ""
                                    TeamActivityTimelineRow(
                                        title = ev.displayTitle(),
                                        actorName = ev.displaySubtitle(),
                                        timeLabel = time,
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            if (state.notifications.isNotEmpty()) {
                                item {
                                    TeamSectionLabel(
                                        title = "اعلان‌های پنل",
                                        subtitle = if (unread.notifications > 0) {
                                            "${unread.notifications} مورد منتظر بررسی"
                                        } else {
                                            "آخرین رویدادهای آژانس"
                                        },
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                                item {
                                    DfCard(modifier = Modifier.padding(horizontal = pad)) {
                                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                            if (unread.notifications > 0) {
                                                TextButton(
                                                    onClick = viewModel::markAllNotificationsRead,
                                                    modifier = Modifier.align(Alignment.End),
                                                ) {
                                                    Text("همه خوانده شد", color = DfThemeColors.info())
                                                }
                                            }
                                            state.notifications.forEach { note ->
                                                TeamNotificationRow(
                                                    note = note,
                                                    onClick = { viewModel.markNotificationRead(note.id) },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                if (perms.canManageAgency) {
                                    DfSecondaryButton(
                                        text = "تنظیمات پیشرفته",
                                        onClick = onOpenAdvanced,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = pad),
                                    )
                                } else {
                                    Text(
                                        text = "وب‌هوک، ممیزی و TV فقط برای مدیر آژانس در میزکار در دسترس است.",
                                        style = AppTypography.bodyDescription,
                                        color = DfThemeColors.textMuted(),
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamNotificationRow(
    note: TeamPanelNotificationDto,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (note.isRead) {
            DfThemeColors.surfaceVariant().copy(alpha = 0.35f)
        } else {
            DfThemeColors.infoContainer().copy(alpha = 0.55f)
        },
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TeamAvatar(name = note.title, size = 36.dp, accent = DfThemeColors.info())
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    note.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = DfThemeColors.textPrimary(),
                )
                Text(
                    note.body,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    DateUtils.formatRelativeFa(note.createdAt),
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            if (!note.isRead) {
                DfBadge(text = "جدید", color = DfThemeColors.infoContainer(), textColor = DfThemeColors.info())
            }
        }
    }
}

@Composable
fun TeamWorkspaceScreen(
    onBack: () -> Unit,
    onOpenMessages: () -> Unit = {},
    onOpenMembers: () -> Unit = {},
    onOpenAnnouncements: () -> Unit = {},
    onOpenInbox: () -> Unit = {},
) {
    TeamHubScreen(
        onBack = onBack,
        onOpenMessages = onOpenMessages,
        onOpenMembers = onOpenMembers,
        onOpenAnnouncements = onOpenAnnouncements,
        onOpenInbox = onOpenInbox,
    )
}
