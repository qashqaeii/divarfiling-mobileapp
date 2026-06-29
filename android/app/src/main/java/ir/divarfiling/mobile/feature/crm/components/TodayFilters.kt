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

    fun doneEntries(today: TodayData): List<TodayTaskEntry> {
        if (today.done.isNotEmpty()) {
            return today.done.map { TodayTaskEntry(it, isOverdue = false) }
        }
        return allEntries(today).filter { it.item.reminder?.done == true }
    }

    fun canFilterByDone(today: TodayData): Boolean = doneEntries(today).isNotEmpty()

    fun filterEntries(today: TodayData, tab: TodayFilterTab, query: String = ""): List<TodayTaskEntry> {
        val base = filterEntries(today, tab)
        if (query.isBlank()) return base
        return base.filter { matchesQuery(it, query) }
    }

    private fun matchesQuery(entry: TodayTaskEntry, query: String): Boolean {
        val q = query.trim()
        if (q.isBlank()) return true
        val item = entry.item
        val contact = item.contact
        return contact?.fullName?.contains(q, ignoreCase = true) == true ||
            contact?.phone?.contains(q, ignoreCase = true) == true ||
            item.reminder?.title?.contains(q, ignoreCase = true) == true ||
            TodayTaskLabels.typeLabel(item.type).contains(q, ignoreCase = true)
    }

    fun filterEntries(today: TodayData, tab: TodayFilterTab): List<TodayTaskEntry> {
        val all = allEntries(today)
        return when (tab) {
            TodayFilterTab.All -> all
            TodayFilterTab.Overdue -> all.filter { it.isOverdue }
            TodayFilterTab.Done -> doneEntries(today)
            TodayFilterTab.Reminders -> all.filter { it.item.reminder != null }
        }
    }

    fun todayCount(today: TodayData): Int =
        today.stats?.total ?: (today.overdue.size + today.today.size)

    fun doneCount(today: TodayData): Int = today.stats?.done ?: doneEntries(today).size

    fun overdueCount(today: TodayData): Int = today.overdue.size

    fun remindersCount(today: TodayData): Int =
        allEntries(today).count { it.item.reminder != null }

    fun entryStableKey(entry: TodayTaskEntry, index: Int): String {
        val item = entry.item
        val bucket = if (entry.isOverdue) "overdue" else "today"
        return "$bucket:$index:${item.type}:${item.reminder?.id}:${item.contact?.id}"
    }
}
