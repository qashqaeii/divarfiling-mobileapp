package ir.divarfiling.mobile.feature.team

import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.PresentationLabels
import ir.divarfiling.mobile.core.network.AgencyAdvisorDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorSeatDto

/**
 * وضعیت تخصیص صندلی — منبع واحد UI بر پایه قرارداد API (`has_seat`, `license_id`, `status`).
 */
enum class AgencySeatAssignmentState {
    Unassigned,
    Active,
    Expired,
    Unknown,
}

private fun normalizeSeatStatus(status: String): String = status.trim().lowercase()

fun formatAgencySeatExpiry(expiresAt: String): String? {
    val raw = expiresAt.trim()
    if (raw.isBlank()) return null
    val jalali = DateUtils.formatJalaliDate(raw)
        ?: DateUtils.parseInstantMillis(raw)?.let { DateUtils.formatJalaliDateFromMillis(it) }
    return jalali?.let { "اعتبار تا $it" }
}

fun AgencyAdvisorDto.seatAssignmentState(): AgencySeatAssignmentState {
    val assigned = hasSeat || (licenseId != null && licenseId > 0L)
    if (!assigned) return AgencySeatAssignmentState.Unassigned
    return when (normalizeSeatStatus(status)) {
        "", "active", "valid", "assigned" -> AgencySeatAssignmentState.Active
        "expired", "inactive", "cancelled", "canceled", "revoked" -> AgencySeatAssignmentState.Expired
        else -> AgencySeatAssignmentState.Unknown
    }
}

fun AgencyAdvisorSeatDto.seatAssignmentState(): AgencySeatAssignmentState {
    if (!resolvedHasSeat()) return AgencySeatAssignmentState.Unassigned
    return when (normalizeSeatStatus(status)) {
        "", "active", "valid", "assigned" -> AgencySeatAssignmentState.Active
        "expired", "inactive", "cancelled", "canceled", "revoked" -> AgencySeatAssignmentState.Expired
        else -> AgencySeatAssignmentState.Unknown
    }
}

/** صندلی فعال برای استفاده از امکانات لایسنس */
fun AgencyAdvisorDto.resolvedHasSeat(): Boolean =
    seatAssignmentState() == AgencySeatAssignmentState.Active ||
        seatAssignmentState() == AgencySeatAssignmentState.Unknown

fun AgencyAdvisorSeatDto.resolvedHasSeat(): Boolean =
    hasSeat || (licenseId != null && licenseId > 0L)

fun AgencyAdvisorDto.hasSeatAssignment(): Boolean =
    seatAssignmentState() != AgencySeatAssignmentState.Unassigned

/** اعضای قابل تخصیص صندلی — هم‌راستا با `assign_seat_to_member` در بک‌اند. */
fun AgencyAdvisorDto.eligibleForSeatAssignment(): Boolean =
    isActive && role != "owner" && !hasSeatAssignment()

fun AgencyAdvisorDto.seatStatusBadgeLabel(): String = when (seatAssignmentState()) {
    AgencySeatAssignmentState.Unassigned -> "بدون صندلی"
    AgencySeatAssignmentState.Active -> "صندلی فعال"
    AgencySeatAssignmentState.Expired -> "صندلی منقضی"
    AgencySeatAssignmentState.Unknown -> "صندلی"
}

fun AgencyAdvisorDto.seatSummaryLabel(): String = when (seatAssignmentState()) {
    AgencySeatAssignmentState.Unassigned -> "بدون صندلی"
    AgencySeatAssignmentState.Expired -> {
        val base = sequence?.let { "صندلی #${DateUtils.toPersianDigits(it.toString())}" } ?: "صندلی"
        "$base · منقضی"
    }
    else -> {
        val base = sequence?.let { "صندلی #${DateUtils.toPersianDigits(it.toString())}" }
            ?: PresentationLabels.seatLabel(true)
        val planPart = plan.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""
        "$base$planPart"
    }
}

fun AgencyAdvisorDto.seatExpiryLabel(): String? = formatAgencySeatExpiry(expiresAt)

fun AgencyAdvisorSeatDto.seatSummaryLabel(): String = when (seatAssignmentState()) {
    AgencySeatAssignmentState.Unassigned -> "بدون صندلی"
    AgencySeatAssignmentState.Expired -> {
        val base = sequence?.let { "صندلی #${DateUtils.toPersianDigits(it.toString())}" } ?: "صندلی"
        "$base · منقضی"
    }
    else -> {
        val base = sequence?.let { "صندلی #${DateUtils.toPersianDigits(it.toString())}" }
            ?: PresentationLabels.seatLabel(true)
        val planPart = plan.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""
        "$base$planPart"
    }
}

fun AgencyAdvisorSeatDto.seatExpiryLabel(): String? = formatAgencySeatExpiry(expiresAt)
