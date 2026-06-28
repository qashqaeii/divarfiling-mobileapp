package ir.divarfiling.mobile.feature.crm.components

import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.feature.crm.CrmConstants
import java.time.LocalDate
import java.time.format.DateTimeParseException

object ContactsFilters {
    const val ALL_PRIORITIES = "همه اولویت"
    const val ALL_STATUSES = "همه وضعیت‌ها"
    const val ALL_TYPES = "همه انواع"

    fun filterContacts(
        contacts: List<ContactDto>,
        priorityFilter: String,
        statusFilter: String?,
        typeFilter: String,
        localQuery: String,
    ): List<ContactDto> {
        return contacts.filter { contact ->
            matchesPriority(contact, priorityFilter) &&
                matchesStatus(contact, statusFilter) &&
                matchesType(contact, typeFilter) &&
                matchesQuery(contact, localQuery)
        }
    }

    fun uniquePriorities(contacts: List<ContactDto>): List<String> {
        val fromData = contacts.mapNotNull { it.priority?.takeIf { p -> p.isNotBlank() } }.distinct().sorted()
        return listOf(ALL_PRIORITIES) + (if (fromData.isEmpty()) CrmConstants.PRIORITIES else fromData)
    }

    fun uniqueStatuses(contacts: List<ContactDto>): List<String> {
        val fromData = contacts.mapNotNull { it.status?.takeIf { s -> s.isNotBlank() } }.distinct()
        val merged = (CrmConstants.STATUSES + fromData).distinct()
        return listOf(ALL_STATUSES) + merged
    }

    fun uniqueTypes(contacts: List<ContactDto>): List<String> {
        val fromData = contacts.mapNotNull { it.customerType?.takeIf { t -> t.isNotBlank() } }.distinct()
        val merged = (CrmConstants.CUSTOMER_TYPES + fromData).distinct()
        return listOf(ALL_TYPES) + merged
    }

    fun totalCount(contacts: List<ContactDto>): Int = contacts.size

    fun newCount(contacts: List<ContactDto>): Int =
        contacts.count { it.status == "جدید" }

    fun followUpCount(contacts: List<ContactDto>): Int =
        contacts.count { it.status == "در حال پیگیری" }

    fun todayCount(contacts: List<ContactDto>): Int {
        val today = LocalDate.now().toString()
        return contacts.count { contact ->
            contact.updatedAt?.startsWith(today) == true ||
                parseDate(contact.updatedAt) == today
        }
    }

    private fun parseDate(value: String?): String? {
        if (value.isNullOrBlank() || value.length < 10) return null
        return try {
            LocalDate.parse(value.substring(0, 10)).toString()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun matchesPriority(contact: ContactDto, filter: String): Boolean =
        filter == ALL_PRIORITIES || contact.priority == filter

    private fun matchesStatus(contact: ContactDto, filter: String?): Boolean =
        filter.isNullOrBlank() || filter == ALL_STATUSES || contact.status == filter

    private fun matchesType(contact: ContactDto, filter: String): Boolean =
        filter == ALL_TYPES || contact.customerType == filter

    private fun matchesQuery(contact: ContactDto, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim()
        return contact.fullName.contains(q, ignoreCase = true) ||
            contact.phone?.contains(q, ignoreCase = true) == true ||
            contact.customerType?.contains(q, ignoreCase = true) == true ||
            contact.notes?.contains(q, ignoreCase = true) == true
    }
}
