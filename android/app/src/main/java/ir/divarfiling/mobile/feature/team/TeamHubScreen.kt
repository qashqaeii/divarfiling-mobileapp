package ir.divarfiling.mobile.feature.team

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.network.TeamPanelNotificationDto
import ir.divarfiling.mobile.feature.crm.components.CrmHubFeatureCard
import ir.divarfiling.mobile.feature.crm.components.CrmHubStatChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamHubScreen(
    onBack: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenMembers: () -> Unit,
    onOpenAnnouncements: () -> Unit,
    onOpenInbox: () -> Unit,
    viewModel: TeamHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val overview = state.overview

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isLoading -> {
                    Column {
                        DfHubPageHeader(
                            title = "تیم",
                            subtitle = "هاب آژانس",
                            titleIconRes = DfDecorIcons.Users,
                            onBack = onBack,
                        )
                        DfCardListSkeleton(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                }
                overview?.hasAgency != true -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfHubPageHeader(
                                title = "تیم",
                                subtitle = "هنوز به آژانسی وصل نیستید",
                                titleIconRes = DfDecorIcons.Users,
                                userName = state.userName,
                                onBack = onBack,
                            )
                        }
                        item {
                            DfEmptyState(
                                title = "آژانسی پیدا نشد",
                                subtitle = "برای پیام داخلی، اعضا و اعلامیه‌ها باید عضو یک آژانس باشید. ساخت یا عضویت فعلاً از وب‌ورک‌اسپیس انجام می‌شود.",
                                variant = DfEmptyVariant.Empty,
                                actionLabel = "باز کردن میزکار تیم",
                                onAction = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.WORKSPACE_TEAM)),
                                    )
                                },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
                else -> {
                    val agency = overview.agency
                    val membership = overview.membership
                    val perms = overview.permissions
                    val unread = overview.unread
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfHubPageHeader(
                                title = agency?.name ?: "تیم",
                                subtitle = listOfNotNull(
                                    membership?.roleLabel,
                                    membership?.title?.takeIf { it.isNotBlank() },
                                ).joinToString(" · ").ifBlank { "میزکار آژانس" },
                                titleIconRes = DfDecorIcons.Users,
                                userName = state.userName,
                                onBack = onBack,
                            )
                        }
                        item {
                            TeamPulseStrip(
                                messages = unread.messages,
                                announcements = unread.announcements,
                                notifications = unread.notifications,
                                members = overview.membersCount,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        if (perms.messagesEnabled) {
                            item {
                                CrmHubFeatureCard(
                                    title = "صندوق پیام",
                                    subtitle = "گفت‌وگوی داخلی تیم",
                                    tint = DfColors.Blue,
                                    background = DfColors.BlueLight,
                                    icon = DfIcons.MessageCircle,
                                    stats = listOf(
                                        CrmHubStatChip("خوانده‌نشده", unread.messages.toString(), icon = DfIcons.Bell),
                                    ),
                                    onClick = onOpenMessages,
                                    illustration = {
                                        TeamTileGlyph(DfIcons.MessageCircle, DfColors.Blue, DfColors.BlueLight)
                                    },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }
                        if (perms.announcementsEnabled) {
                            item {
                                CrmHubFeatureCard(
                                    title = "اعلامیه‌ها",
                                    subtitle = "تابلو اطلاع‌رسانی آژانس",
                                    tint = DfColors.Amber,
                                    background = DfColors.AmberLight,
                                    icon = DfIcons.Sparkles,
                                    stats = listOf(
                                        CrmHubStatChip("جدید", unread.announcements.toString(), icon = DfIcons.Sparkles),
                                    ),
                                    onClick = onOpenAnnouncements,
                                    illustration = {
                                        TeamTileGlyph(DfIcons.Sparkles, DfColors.Amber, DfColors.AmberLight)
                                    },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }
                        item {
                            CrmHubFeatureCard(
                                title = "اعضا",
                                subtitle = "نقش‌ها و دسترسی‌های تیم",
                                tint = DfColors.Purple,
                                background = DfColors.PurpleContainer,
                                icon = DfIcons.Users,
                                stats = listOf(
                                    CrmHubStatChip("نفر", overview.membersCount.toString(), icon = DfIcons.Users),
                                ),
                                onClick = onOpenMembers,
                                illustration = {
                                    TeamTileGlyph(DfIcons.Users, DfColors.Purple, DfColors.PurpleLight)
                                },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        if (perms.canOperateInbox) {
                            item {
                                CrmHubFeatureCard(
                                    title = "صندوق سرنخ",
                                    subtitle = "تخصیص سریع لیدهای تیم",
                                    tint = DfColors.Green,
                                    background = DfColors.GreenLight,
                                    icon = DfIcons.UserPlus,
                                    stats = listOf(
                                        CrmHubStatChip("در صف", overview.inboxLeadsCount.toString(), icon = DfIcons.UserPlus),
                                    ),
                                    onClick = onOpenInbox,
                                    illustration = {
                                        TeamTileGlyph(DfIcons.UserPlus, DfColors.Green, DfColors.GreenLight)
                                    },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }
                        if (state.notifications.isNotEmpty()) {
                            item {
                                DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                "اعلان‌های پنل",
                                                style = AppTypography.cardTitle,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            if (unread.notifications > 0) {
                                                androidx.compose.material3.TextButton(
                                                    onClick = viewModel::markAllNotificationsRead,
                                                ) {
                                                    Text("همه خوانده شد", color = DfColors.Blue)
                                                }
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
                            DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                    Text(
                                        "ابزارهای پیشرفته وب",
                                        style = AppTypography.cardTitle,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        "TV Mode، گزارش CSV و تنظیمات کامل آژانس روی وب باقی مانده‌اند.",
                                        style = AppTypography.bodyDescription,
                                        color = DfColors.TextSecondary,
                                    )
                                    DfPrimaryButton(
                                        text = "TV Mode",
                                        onClick = {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.WORKSPACE_TEAM_TV)),
                                            )
                                        },
                                    )
                                    DfSecondaryButton(
                                        text = "گزارش عملکرد CSV",
                                        onClick = {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.WORKSPACE_TEAM_REPORT)),
                                            )
                                        },
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

@Composable
private fun TeamPulseStrip(
    messages: Int,
    announcements: Int,
    notifications: Int,
    members: Int,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text("نبض امروز تیم", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                TeamPulseChip("پیام", messages, DfColors.Blue, Modifier.weight(1f))
                TeamPulseChip("اعلامیه", announcements, DfColors.Amber, Modifier.weight(1f))
                TeamPulseChip("اعلان", notifications, DfColors.Rose, Modifier.weight(1f))
                TeamPulseChip("اعضا", members, DfColors.Purple, Modifier.weight(1f))
            }
            if (messages + announcements + notifications > 0) {
                DfStatusBanner(
                    message = "چند مورد خوانده‌نشده دارید؛ از کارت‌های زیر شروع کنید.",
                    tone = DfStatusTone.Info,
                    title = "کارهای باز تیم",
                )
            }
        }
    }
}

@Composable
private fun TeamPulseChip(
    label: String,
    value: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(tint.copy(alpha = 0.12f), shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .padding(vertical = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            value.toString(),
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = tint,
        )
        Text(label, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
    }
}

@Composable
private fun TeamTileGlyph(icon: ImageVector, tint: Color, background: Color) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun TeamNotificationRow(
    note: TeamPanelNotificationDto,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = if (note.isRead) Color.Transparent else DfColors.BlueLight.copy(alpha = 0.35f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    note.body,
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    DateUtils.formatRelativeFa(note.createdAt),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                )
            }
            if (!note.isRead) {
                DfBadge(text = "جدید", color = DfColors.BlueLight, textColor = DfColors.Blue)
            }
        }
    }
}

/** Backward-compatible entry used by older navigation imports. */
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
