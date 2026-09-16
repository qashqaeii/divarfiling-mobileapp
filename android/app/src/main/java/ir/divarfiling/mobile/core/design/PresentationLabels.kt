package ir.divarfiling.mobile.core.design

/**
 * Maps backend enum/status values to Persian UI labels (presentation layer only).
 */
object PresentationLabels {

    fun invitationStatus(status: String): String = when (status.trim().lowercase()) {
        "accepted" -> "پذیرفته‌شده"
        "pending" -> "در انتظار"
        "cancelled", "canceled" -> "لغوشده"
        "expired" -> "منقضی‌شده"
        else -> status.takeIf { it.isNotBlank() } ?: "نامشخص"
    }

    fun memberActiveLabel(isActive: Boolean): String =
        if (isActive) "فعال" else "غیرفعال"

    fun seatLabel(hasSeat: Boolean): String =
        if (hasSeat) "دارای صندلی" else "بدون صندلی"

    /**
     * Prefer a human-readable name; fall back to labeled phone when name is missing or too weak.
     */
    fun memberDisplayName(name: String, phone: String = ""): String {
        val trimmed = name.trim()
        if (trimmed.isNotBlank() && !isWeakDisplayName(trimmed)) return trimmed
        val p = phone.trim()
        return if (p.isNotBlank()) "موبایل: $p" else "عضو بدون نام"
    }

    fun attentionTitle(title: String): String = title.trim()

    fun attentionSubtitle(subtitle: String, fallbackPhone: String = ""): String {
        val raw = subtitle.trim()
        if (raw.isBlank()) return fallbackPhone.takeIf { it.isNotBlank() }?.let { "موبایل: $it" }.orEmpty()
        // "Fa ,0919..." → formatted Persian labels
        val parts = raw.split(',', '·', '|').map { it.trim() }.filter { it.isNotBlank() }
        if (parts.size >= 2) {
            val namePart = parts[0]
            val phonePart = parts.drop(1).firstOrNull { it.any { ch -> ch.isDigit() } }.orEmpty()
            val name = memberDisplayName(namePart, phonePart)
            return if (phonePart.isNotBlank() && !name.contains(phonePart)) {
                "$name · $phonePart"
            } else {
                name
            }
        }
        return memberDisplayName(raw, fallbackPhone)
    }

    fun isWeakDisplayName(name: String): Boolean {
        val n = name.trim()
        if (n.isBlank()) return true
        if (n.length <= 2) return true
        if (n.all { it.isLetter() || it.isWhitespace() } && n.length <= 3) return true
        return false
    }
}
