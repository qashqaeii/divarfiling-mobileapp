package ir.divarfiling.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfNbaAction
import ir.divarfiling.mobile.core.design.components.DfNbaCard
import ir.divarfiling.mobile.core.design.components.DfSectionTitle
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.feature.home.DashboardStats
import ir.divarfiling.mobile.feature.home.HomeNotificationItem
import ir.divarfiling.mobile.feature.home.HomeNotificationType
import ir.divarfiling.mobile.feature.home.HomeTaskItem
import ir.divarfiling.mobile.navigation.DeepLinkParser
import android.net.Uri
import ir.divarfiling.mobile.navigation.DeepLinkTarget

@Composable
fun HomePrioritySection(
    stats: DashboardStats,
    tasks: List<HomeTaskItem>,
    notifications: List<HomeNotificationItem>,
    recentFilesCount: Int,
    onToday: () -> Unit,
    onNotifications: () -> Unit,
    onFiling: () -> Unit,
    onContacts: () -> Unit,
    onNotificationDeepLink: (DeepLinkTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = remember(stats, tasks, notifications, recentFilesCount) {
        buildPriorityActions(stats, tasks, notifications, recentFilesCount)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfSectionTitle(title = "مهم‌ترین اقدام امروز")
        if (actions.isEmpty()) {
            DfEmptyState(
                title = "امروز کاری ندارید",
                subtitle = "وقتی پیگیری یا فایل جدیدی باشد اینجا می‌آید.",
                variant = DfEmptyVariant.Empty,
                actionLabel = "مخاطب جدید",
                onAction = onContacts,
            )
        } else {
            actions.forEachIndexed { index, item ->
                DfNbaCard(
                    compact = index > 0,
                    action = DfNbaAction(
                        title = item.title,
                        subtitle = item.subtitle,
                        cta = item.cta,
                        tone = item.tone,
                        onClick = {
                            when (item.kind) {
                                HomePriorityKind.Today -> onToday()
                                HomePriorityKind.Notifications -> {
                                    item.deepLink?.let { link ->
                                        DeepLinkParser.parse(Uri.parse(link))?.let(onNotificationDeepLink)
                                    } ?: onNotifications()
                                }
                                HomePriorityKind.Filing -> onFiling()
                                HomePriorityKind.Contacts -> onContacts()
                            }
                        },
                    ),
                )
            }
        }
    }
}

private enum class HomePriorityKind { Today, Notifications, Filing, Contacts }

private data class HomePriorityItem(
    val title: String,
    val subtitle: String,
    val cta: String,
    val kind: HomePriorityKind,
    val tone: DfStatusTone,
    val deepLink: String? = null,
)

private fun buildPriorityActions(
    stats: DashboardStats,
    tasks: List<HomeTaskItem>,
    notifications: List<HomeNotificationItem>,
    recentFilesCount: Int,
): List<HomePriorityItem> {
    val items = mutableListOf<HomePriorityItem>()
    val overdue = maxOf(stats.overdueFollowups, stats.overdueCount)
    if (overdue > 0) {
        items += HomePriorityItem(
            title = "پیگیری معوق",
            subtitle = "${DateUtils.toPersianDigits(overdue.toString())} مورد منتظر تماس است",
            cta = "تماس",
            kind = HomePriorityKind.Today,
            tone = DfStatusTone.Warning,
        )
    }
    if (stats.todayTasksRemaining > 0 || tasks.isNotEmpty()) {
        val remaining = stats.todayTasksRemaining.coerceAtLeast(tasks.size)
        items += HomePriorityItem(
            title = "کار امروز",
            subtitle = tasks.firstOrNull()?.title
                ?: "${DateUtils.toPersianDigits(remaining.toString())} کار باقی مانده",
            cta = "شروع",
            kind = HomePriorityKind.Today,
            tone = DfStatusTone.Info,
        )
    }
    notifications.firstOrNull { it.type == HomeNotificationType.NewMatch }?.let { match ->
        items += HomePriorityItem(
            title = "مخاطب دارای فایل پیشنهادی",
            subtitle = match.title,
            cta = "مشاهده",
            kind = HomePriorityKind.Notifications,
            tone = DfStatusTone.Success,
            deepLink = match.deepLink,
        )
    }
    notifications.firstOrNull {
        it.type == HomeNotificationType.License || it.type == HomeNotificationType.FollowUp
    }?.let { important ->
        if (items.none { it.kind == HomePriorityKind.Notifications }) {
            items += HomePriorityItem(
                title = "اعلان مهم",
                subtitle = important.title,
                cta = "باز کردن",
                kind = HomePriorityKind.Notifications,
                tone = DfStatusTone.Warning,
                deepLink = important.deepLink,
            )
        }
    }
    if (stats.newFilesToday > 0 || recentFilesCount > 0 && items.size < 3) {
        val count = stats.newFilesToday.coerceAtLeast(1)
        items += HomePriorityItem(
            title = "فایل جدید",
            subtitle = "${DateUtils.toPersianDigits(count.toString())} پوشه استخراج امروز",
            cta = "مشاهده",
            kind = HomePriorityKind.Filing,
            tone = DfStatusTone.Info,
        )
    }
    return items.distinctBy { it.title }.take(3)
}
