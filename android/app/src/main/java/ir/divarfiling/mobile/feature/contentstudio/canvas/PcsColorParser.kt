package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import kotlin.math.max

object PcsColorParser {
    fun parseColor(raw: String?, fallback: Int = Color.TRANSPARENT): Int {
        if (raw.isNullOrBlank()) return fallback
        val s = raw.trim()
        return try {
            when {
                s.startsWith("#") -> Color.parseColor(s)
                s.startsWith("rgba(", ignoreCase = true) -> parseRgba(s) ?: fallback
                s.startsWith("rgb(", ignoreCase = true) -> parseRgb(s) ?: fallback
                else -> Color.parseColor(s)
            }
        } catch (_: IllegalArgumentException) {
            fallback
        }
    }

    fun parseBackgroundPaint(
        raw: String?,
        width: Float,
        height: Float,
        fallback: Int,
    ): android.graphics.Paint {
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        if (raw.isNullOrBlank()) {
            paint.color = fallback
            return paint
        }
        val gradient = parseLinearGradient(raw, width, height)
        if (gradient != null) {
            paint.shader = gradient
        } else {
            paint.color = parseColor(raw, fallback)
        }
        return paint
    }

    private fun parseLinearGradient(raw: String, width: Float, height: Float): Shader? {
        if (!raw.startsWith("linear-gradient", ignoreCase = true)) return null
        val colors = rgbaRegex.findAll(raw).map { parseRgba(it.value) ?: Color.TRANSPARENT }.toList()
        if (colors.size < 2) return null
        return LinearGradient(0f, 0f, max(width, 1f), max(height, 1f), colors.toIntArray(), null, Shader.TileMode.CLAMP)
    }

    private val rgbaRegex = Regex("rgba\\([^)]+\\)", RegexOption.IGNORE_CASE)

    private fun parseRgba(s: String): Int? {
        val inner = s.substringAfter('(').substringBefore(')')
        val parts = inner.split(',').map { it.trim() }
        if (parts.size < 4) return null
        val r = parts[0].toFloatOrNull()?.toInt() ?: return null
        val g = parts[1].toFloatOrNull()?.toInt() ?: return null
        val b = parts[2].toFloatOrNull()?.toInt() ?: return null
        val a = (parts[3].toFloatOrNull() ?: 1f).coerceIn(0f, 1f)
        return Color.argb((a * 255).toInt(), r, g, b)
    }

    private fun parseRgb(s: String): Int? {
        val inner = s.substringAfter('(').substringBefore(')')
        val parts = inner.split(',').map { it.trim() }
        if (parts.size < 3) return null
        val r = parts[0].toIntOrNull() ?: return null
        val g = parts[1].toIntOrNull() ?: return null
        val b = parts[2].toIntOrNull() ?: return null
        return Color.rgb(r, g, b)
    }
}
