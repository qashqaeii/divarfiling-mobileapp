package ir.divarfiling.mobile.feature.team

import ir.divarfiling.mobile.core.network.AgencyAdvisorDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorSeatDto

/**
 * Single source for seat UI state on the client.
 * Uses API `has_seat` and corroborates with `license_id` when both are present.
 */
fun AgencyAdvisorDto.resolvedHasSeat(): Boolean =
    hasSeat || (licenseId != null && licenseId > 0L)

fun AgencyAdvisorSeatDto.resolvedHasSeat(): Boolean =
    hasSeat || (licenseId != null && licenseId > 0L)

fun AgencyAdvisorDto.seatSummaryLabel(): String = when {
    resolvedHasSeat() && sequence != null ->
        "صندلی #${sequence}${plan.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}"
    resolvedHasSeat() -> "دارای صندلی"
    else -> "بدون صندلی"
}

fun AgencyAdvisorSeatDto.seatSummaryLabel(): String = when {
    resolvedHasSeat() && sequence != null ->
        "صندلی #${sequence}${plan.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}"
    resolvedHasSeat() -> "دارای صندلی"
    else -> "بدون صندلی"
}
