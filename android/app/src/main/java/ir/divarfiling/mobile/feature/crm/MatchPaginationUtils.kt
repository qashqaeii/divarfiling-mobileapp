package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.network.ContactMatchGroupDto
import ir.divarfiling.mobile.core.network.ContactMatchesData
import ir.divarfiling.mobile.core.network.PropertyContactMatchItemDto
import ir.divarfiling.mobile.core.network.PropertyContactMatchesData
import ir.divarfiling.mobile.core.network.PropertyMatchDto

const val MATCH_PAGE_SIZE = 24

enum class ContactMatchPageSource {
    CRM,
    DIVAR,
}

fun contactMatchSelectionKey(match: PropertyMatchDto, groupId: String? = null): String {
    val sourcePart = if (match.source == "crm") {
        "crm:${match.propertyId ?: 0L}"
    } else {
        "divar:${match.token?.takeIf { it.isNotBlank() } ?: match.listingId ?: 0L}"
    }
    val intentPart = match.matchIntent?.takeIf { it.isNotBlank() }
    val groupPart = groupId?.takeIf { it.isNotBlank() }
    return listOfNotNull(groupPart, intentPart, sourcePart).joinToString("|")
}

fun resolveContactCrmHasMore(data: ContactMatchesData): Boolean {
    if (data.crmHasMore) return true
    return data.matchGroups.any { it.crmHasMore }
}

fun resolveContactDivarHasMore(data: ContactMatchesData): Boolean {
    if (data.divarHasMore) return true
    return data.matchGroups.any { it.divarHasMore }
}

fun loadedCrmMatchCount(data: ContactMatchesData): Int {
    if (data.matchGroups.isNotEmpty()) {
        return data.matchGroups.sumOf { it.crmMatches.size }
    }
    return data.crmMatches.size
}

fun loadedDivarMatchCount(data: ContactMatchesData): Int {
    if (data.matchGroups.isNotEmpty()) {
        return data.matchGroups.sumOf { it.divarMatches.size }
    }
    return data.divarMatches.size
}

fun totalCrmMatchCount(data: ContactMatchesData): Int {
    if (data.crmTotalCount > 0) return data.crmTotalCount
    if (data.matchGroups.isNotEmpty()) {
        return data.matchGroups.sumOf { group ->
            group.crmTotalCount.takeIf { it > 0 } ?: group.crmMatches.size
        }
    }
    return data.crmMatches.size
}

fun totalDivarMatchCount(data: ContactMatchesData): Int {
    if (data.divarTotalCount > 0) return data.divarTotalCount
    if (data.matchGroups.isNotEmpty()) {
        return data.matchGroups.sumOf { group ->
            group.divarTotalCount.takeIf { it > 0 } ?: group.divarMatches.size
        }
    }
    return data.divarMatches.size
}

fun mergeContactMatchesPage(
    existing: ContactMatchesData?,
    incoming: ContactMatchesData,
    appendSource: ContactMatchPageSource?,
): ContactMatchesData {
    if (existing == null || appendSource == null) return incoming

    return if (incoming.isBuilder && (existing.matchGroups.isNotEmpty() || incoming.matchGroups.isNotEmpty())) {
        mergeBuilderContactMatches(existing, incoming, appendSource)
    } else {
        mergeFlatContactMatches(existing, incoming, appendSource)
    }
}

private fun mergeFlatContactMatches(
    existing: ContactMatchesData,
    incoming: ContactMatchesData,
    appendSource: ContactMatchPageSource,
): ContactMatchesData {
    val crmMatches = when (appendSource) {
        ContactMatchPageSource.CRM -> appendUniquePropertyMatches(existing.crmMatches, incoming.crmMatches)
        ContactMatchPageSource.DIVAR -> existing.crmMatches
    }
    val divarMatches = when (appendSource) {
        ContactMatchPageSource.DIVAR -> appendUniquePropertyMatches(existing.divarMatches, incoming.divarMatches)
        ContactMatchPageSource.CRM -> existing.divarMatches
    }
    return incoming.copy(
        matchGroups = emptyList(),
        crmMatches = crmMatches,
        divarMatches = divarMatches,
        crmPage = if (appendSource == ContactMatchPageSource.CRM) incoming.crmPage else existing.crmPage,
        divarPage = if (appendSource == ContactMatchPageSource.DIVAR) incoming.divarPage else existing.divarPage,
        crmHasMore = if (appendSource == ContactMatchPageSource.CRM) {
            incoming.crmHasMore
        } else {
            existing.crmHasMore
        },
        divarHasMore = if (appendSource == ContactMatchPageSource.DIVAR) {
            incoming.divarHasMore
        } else {
            existing.divarHasMore
        },
        crmTotalCount = incoming.crmTotalCount.takeIf { it > 0 } ?: existing.crmTotalCount,
        divarTotalCount = incoming.divarTotalCount.takeIf { it > 0 } ?: existing.divarTotalCount,
        totalCount = incoming.totalCount.takeIf { it > 0 } ?: existing.totalCount,
    )
}

private fun mergeBuilderContactMatches(
    existing: ContactMatchesData,
    incoming: ContactMatchesData,
    appendSource: ContactMatchPageSource,
): ContactMatchesData {
    val existingGroups = existing.matchGroups.ifEmpty { incoming.matchGroups }
    val incomingById = incoming.matchGroups.associateBy { groupKey(it) }
    val mergedGroups = existingGroups.map { group ->
        val patch = incomingById[groupKey(group)] ?: return@map group
        when (appendSource) {
            ContactMatchPageSource.CRM -> group.copy(
                crmMatches = appendUniquePropertyMatches(group.crmMatches, patch.crmMatches, group.id),
                crmTotalCount = patch.crmTotalCount.takeIf { it > 0 } ?: group.crmTotalCount,
                crmPage = patch.crmPage.takeIf { it > 0 } ?: group.crmPage,
                crmHasMore = patch.crmHasMore,
            )
            ContactMatchPageSource.DIVAR -> group.copy(
                divarMatches = appendUniquePropertyMatches(group.divarMatches, patch.divarMatches, group.id),
                divarTotalCount = patch.divarTotalCount.takeIf { it > 0 } ?: group.divarTotalCount,
                divarPage = patch.divarPage.takeIf { it > 0 } ?: group.divarPage,
                divarHasMore = patch.divarHasMore,
            )
        }
    }
    val crmMatches = mergedGroups.flatMap { it.crmMatches }
    val divarMatches = mergedGroups.flatMap { it.divarMatches }
    return incoming.copy(
        matchGroups = mergedGroups,
        crmMatches = crmMatches,
        divarMatches = divarMatches,
        crmPage = if (appendSource == ContactMatchPageSource.CRM) incoming.crmPage else existing.crmPage,
        divarPage = if (appendSource == ContactMatchPageSource.DIVAR) incoming.divarPage else existing.divarPage,
        crmHasMore = if (appendSource == ContactMatchPageSource.CRM) {
            resolveContactCrmHasMore(incoming)
        } else {
            resolveContactCrmHasMore(existing)
        },
        divarHasMore = if (appendSource == ContactMatchPageSource.DIVAR) {
            resolveContactDivarHasMore(incoming)
        } else {
            resolveContactDivarHasMore(existing)
        },
        crmTotalCount = incoming.crmTotalCount.takeIf { it > 0 } ?: existing.crmTotalCount,
        divarTotalCount = incoming.divarTotalCount.takeIf { it > 0 } ?: existing.divarTotalCount,
        totalCount = incoming.totalCount.takeIf { it > 0 } ?: existing.totalCount,
    )
}

private fun groupKey(group: ContactMatchGroupDto): String =
    group.id.takeIf { it.isNotBlank() } ?: group.title

private fun appendUniquePropertyMatches(
    existing: List<PropertyMatchDto>,
    incoming: List<PropertyMatchDto>,
    groupId: String? = null,
): List<PropertyMatchDto> {
    if (incoming.isEmpty()) return existing
    val seen = existing.map { contactMatchSelectionKey(it, groupId) }.toMutableSet()
    val merged = existing.toMutableList()
    incoming.forEach { item ->
        val key = contactMatchSelectionKey(item, groupId)
        if (seen.add(key)) merged.add(item)
    }
    return merged
}

fun mergePropertyContactMatchesPage(
    existing: PropertyContactMatchesData?,
    incoming: PropertyContactMatchesData,
    append: Boolean,
): PropertyContactMatchesData {
    if (!append || existing == null) return incoming
    val seen = existing.matches.map { it.customerId }.toMutableSet()
    val merged = existing.matches.toMutableList()
    incoming.matches.forEach { item ->
        if (seen.add(item.customerId)) merged.add(item)
    }
    return incoming.copy(
        matches = merged,
        totalCount = incoming.totalCount.takeIf { it > 0 } ?: existing.totalCount,
        hasMore = incoming.hasMore,
        matchPage = incoming.matchPage,
    )
}
