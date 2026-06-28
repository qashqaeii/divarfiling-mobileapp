package ir.divarfiling.mobile.feature.crm.components

import ir.divarfiling.mobile.core.network.TodayData
import ir.divarfiling.mobile.core.network.TodayItemDto

enum class TodayFilterTab(val id: String) {
    All("all"),
    Overdue("overdue"),
    Done("done"),
    Reminders("reminders"),
}

data class TodayTaskEntry(
    val item: TodayItemDto,
    val isOverdue: Boolean,
)

object TodayFilters {
    fun allEntries(today: TodayData): List<TodayTaskEntry> {
        val overdue = today.overdue.map { TodayTaskEntry(it, isOverdue = true) }
        val current = today.today.map { TodayTaskEntry(it, isOverdue = false) }
        return overdue + current
    }

    fun filterEntries(today: TodayData, tab: TodayFilterTab): List<TodayTaskEntry> {
        val all = allEntries(today)
        return when (tab) {
            TodayFilterTab.All -> all
            TodayFilterTab.Overdue -> all.filter { it.isOverdue }
            TodayFilterTab.Done -> emptyList()
            TodayFilterTab.Reminders -> all.filter { it.item.reminder != null }
        }
    }

    fun todayCount(today: TodayData): Int =
        today.stats?.total ?: (today.overdue.size + today.today.size)

    fun doneCount(today: TodayData): Int = today.stats?.done ?: 0

    fun overdueCount(today: TodayData): Int = today.overdue.size

    fun remindersCount(today: TodayData): Int =
        allEntries(today).count { it.item.reminder != null }
}
