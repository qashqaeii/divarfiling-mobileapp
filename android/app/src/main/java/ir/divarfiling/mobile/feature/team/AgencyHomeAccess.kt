package ir.divarfiling.mobile.feature.team

import ir.divarfiling.mobile.core.network.AgencyHomeDto

/** آژانس با اشتراک/دسترسی واقعی (نه فقط پرچم has_agency بدون امکانات). */
fun AgencyHomeDto.hasActiveAgencySubscription(): Boolean {
    if (!hasAgency || agency == null) return false
    val seatPool = seats?.total ?: 0
    return seatPool > 0 ||
        permissions.messagesEnabled ||
        permissions.announcementsEnabled ||
        capabilities.canAccessAgencySpace
}
