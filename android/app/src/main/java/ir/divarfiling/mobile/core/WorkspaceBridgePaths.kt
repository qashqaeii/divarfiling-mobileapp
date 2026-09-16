package ir.divarfiling.mobile.core

import android.net.Uri

/**
 * مقصدهای ثابت میزکار که اپ مستقیماً برای Bridge درخواست می‌دهد.
 * مسیرهای پویا (مثلاً از API قرارداد) از اینجا عبور نمی‌کنند.
 */
object WorkspaceBridgePaths {
    const val CRM_TEAM = "/workspace/crm/team/"
    const val CONTACT_IMPORT = "/workspace/crm/contacts/import/"
    const val CONTACT_IMPORT_TEMPLATE = "/workspace/crm/contacts/import/template/"

    enum class Destination(val path: String) {
        CRM_TEAM(WorkspaceBridgePaths.CRM_TEAM),
        CONTACT_IMPORT(WorkspaceBridgePaths.CONTACT_IMPORT),
        CONTACT_IMPORT_TEMPLATE(WorkspaceBridgePaths.CONTACT_IMPORT_TEMPLATE),
    }

    private val allowedPaths: Set<String> = Destination.entries.map { it.path }.toSet()

    fun resolveClientPath(raw: String): String? {
        val normalized = normalizeWorkspacePath(raw) ?: return null
        return normalized.takeIf { it in allowedPaths }
    }

    fun normalizeWorkspacePath(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val path = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> {
                val parsed = Uri.parse(trimmed)
                val base = parsed.path ?: return null
                if (parsed.query.isNullOrBlank()) base else "$base?${parsed.query}"
            }
            trimmed.startsWith("/") -> trimmed
            else -> "/${trimmed.trimStart('/')}"
        }
        if (!path.startsWith("/workspace/")) return null
        if ("//" in path.replace("://", "")) return null
        return path
    }
}
