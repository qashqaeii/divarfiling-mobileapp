package ir.divarfiling.mobile.navigation

import android.net.Uri

object DeepLinkParser {
    fun parse(uri: Uri?): DeepLinkTarget? {
        if (uri == null || uri.scheme != "divarfiling") return null
        val host = uri.host ?: return null
        val segments = uri.pathSegments
        return when (host) {
            "filing" -> when {
                segments.isEmpty() -> DeepLinkTarget.Filing
                segments.size == 1 -> DeepLinkTarget.FilingDataset(segments[0])
                segments.size >= 2 && segments[0] == "listing" -> DeepLinkTarget.ListingDetail(segments[1])
                else -> DeepLinkTarget.FilingDataset(segments[0])
            }
            "crm" -> when {
                segments.firstOrNull() == "contacts" && segments.size >= 3 && segments[2] == "matches" ->
                    segments[1].toLongOrNull()?.let { DeepLinkTarget.ContactMatches(it) }
                segments.firstOrNull() == "contacts" && segments.size >= 2 ->
                    segments[1].toLongOrNull()?.let { DeepLinkTarget.ContactDetail(it) }
                segments.firstOrNull() == "today" -> DeepLinkTarget.Today
                else -> DeepLinkTarget.Crm
            }
            "extract" -> when {
                segments.firstOrNull() == "schedules" -> DeepLinkTarget.ExtractSchedules
                else -> DeepLinkTarget.Extract
            }
            "settings" -> DeepLinkTarget.Settings
            else -> null
        }
    }
}

sealed class DeepLinkTarget {
    data object Filing : DeepLinkTarget()
    data class FilingDataset(val datasetId: String) : DeepLinkTarget()
    data class ListingDetail(val token: String) : DeepLinkTarget()
    data object Crm : DeepLinkTarget()
    data class ContactDetail(val contactId: Long) : DeepLinkTarget()
    data class ContactMatches(val contactId: Long) : DeepLinkTarget()
    data object Today : DeepLinkTarget()
    data object Extract : DeepLinkTarget()
    data object ExtractSchedules : DeepLinkTarget()
    data object Settings : DeepLinkTarget()
}
