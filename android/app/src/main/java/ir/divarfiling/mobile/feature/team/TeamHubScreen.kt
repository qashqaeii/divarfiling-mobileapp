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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
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
                                DfHubPageHeader(
                                    title = agency?.name ?: "میزکار آژانس",
                                    subtitle = buildString {
                                        append(membership?.roleLabel.orEmpty())
                                        seats?.let { s ->
                                            if (s.total > 0) {
                                                append(" · ")
                                                append(DateUtils.toPersianDigits(s.assigned.toString()))
                                                append("/")
                                                append(DateUtils.toPersianDigits(s.total.toString()))
                                                append(" صندلی")
                                            }
                                        }
                                    },
                                    sectionLabel = DfHeaderSections.TEAM,
                                titleIconRes = DfDecorIcons.Users,
                                    userName = state.userName,
                                    onBack = onBack,
                                )
                            }
                            item {
                                TeamMetricsRow(
                                    metrics = listOf(
                                        TeamMetric("اعضا", DateUtils.toPersianDigits(kpis.activeMembers.toString()), DfColors.Purple, DfIcons.Users),
                                        TeamMetric("سرنخ", DateUtils.toPersianDigits(kpis.unassignedLeads.toString()), DfColors.Green, DfIcons.UserPlus),
                                        TeamMetric("پیگیری", DateUtils.toPersianDigits(kpis.teamFollowupsToday.toString()), DfColors.Amber, DfIcons.Clock),
                                        TeamMetric("معامله", DateUtils.toPersianDigits(kpis.activeDeals.toString()), DfColors.Blue, DfIcons.Handshake),
                                    ),
                                    modifier = Modifier.padding(horizontal = pad),
                                )
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
                                    TeamAttentionCard(
                                        item = item,
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            item {
                                TeamSectionLabel(
                                    title = "اقدام سریع",
                                    subtitle = "عملیات روزانه مدیر آژانس",
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            item {
                                TeamQuickActionGrid(
                                    actions = buildList {
                                        if (perms.canInvite) {
                                            add(TeamQuickAction("دعوت مشاور", DfIcons.UserPlus, onOpenMembers))
                                        }
                                        add(TeamQuickAction("اعضا", DfIcons.Users, onOpenMembers))
                                        if (perms.canOperateInbox) {
                                            add(TeamQuickAction("Inbox سرنخ", DfIcons.ListTodo, onOpenInbox))
                                        }
                                        if (perms.canManage) {
                                            add(TeamQuickAction("عملکرد تیم", DfIcons.BarChart, onOpenPerformance))
                                        }
                                        if (perms.canManageSeats) {
                                            add(TeamQuickAction("صندلی‌ها", DfIcons.Building, onOpenSeats))
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            item {
                                TeamSectionLabel(
                                    title = "مدیریت روزانه",
                                    subtitle = "میانبرهای پرکاربرد تیم",
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            if (home.capabilities.canAccessAgencySpace) {
                                item {
                                    TeamDestinationCard(
                                        destination = TeamDestination(
                                            title = "فضای آژانس",
                                            subtitle = "فایل‌ها و مخاطبین منتشرشده تیم",
                                            metricLabel = "فعال",
                                            metricValue = home.spaceKpis?.activeProperties?.toString() ?: "—",
                                            tint = DfColors.Purple,
                                            wash = DfColors.PurpleLight,
                                            icon = DfIcons.Layers,
                                            onClick = onOpenAgencySpace,
                                        ),
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            if (perms.messagesEnabled) {
                                item {
                                    TeamDestinationCard(
                                        destination = TeamDestination(
                                            title = "صندوق پیام",
                                            subtitle = "گفت‌وگوی داخلی و پیگیری سریع همکاران",
                                            metricLabel = "خوانده‌نشده",
                                            metricValue = DateUtils.toPersianDigits(unread.messages.toString()),
                                            tint = DfColors.Blue,
                                            wash = DfColors.BlueLight,
                                            icon = DfIcons.MessageCircle,
                                            onClick = onOpenMessages,
                                        ),
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            if (perms.announcementsEnabled) {
                                item {
                                    TeamDestinationCard(
                                        destination = TeamDestination(
                                            title = "اعلامیه‌ها",
                                            subtitle = "تابلو اطلاع‌رسانی و نکات مهم آژانس",
                                            metricLabel = "جدید",
                                            metricValue = DateUtils.toPersianDigits(unread.announcements.toString()),
                                            tint = DfColors.Amber,
                                            wash = DfColors.AmberLight,
                                            icon = DfIcons.Sparkles,
                                            onClick = onOpenAnnouncements,
                                        ),
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            item {
                                TeamDestinationCard(
                                    destination = TeamDestination(
                                        title = "اعضا",
                                        subtitle = "نقش‌ها، عنوان‌ها و دسترسی‌های تیم",
                                        metricLabel = "نفر",
                                        metricValue = home.membersCount.toString(),
                                        tint = DfColors.Purple,
                                        wash = DfColors.PurpleLight,
                                        icon = DfIcons.Users,
                                        onClick = onOpenMembers,
                                    ),
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                            if (perms.canOperateInbox) {
                                item {
                                    TeamDestinationCard(
                                        destination = TeamDestination(
                                            title = "صندوق سرنخ",
                                            subtitle = "تخصیص سریع لیدهای در صف",
                                            metricLabel = "در صف",
                                            metricValue = home.inboxLeadsCount.toString(),
                                            tint = DfColors.Green,
                                            wash = DfColors.GreenLight,
                                            icon = DfIcons.UserPlus,
                                            onClick = onOpenInbox,
                                        ),
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
                                                    Text("همه خوانده شد", color = DfColors.Blue)
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
            DfColors.BlueLight.copy(alpha = 0.45f)
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
            TeamAvatar(name = note.title, size = 36.dp, accent = DfColors.Blue)
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
                DfBadge(text = "جدید", color = DfColors.BlueLight, textColor = DfColors.Blue)
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
