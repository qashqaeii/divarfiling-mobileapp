package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object PcsImagePlaceholder {
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(80, 0, 0, 0)
    }

    fun draw(canvas: Canvas, rect: RectF, alpha: Int) {
        fillPaint.color = Color.argb(40, 120, 120, 120)
        fillPaint.alpha = alpha
        canvas.drawRoundRect(rect, 12f, 12f, fillPaint)
        strokePaint.alpha = (alpha * 0.6f).toInt().coerceIn(0, 255)
        canvas.drawRoundRect(rect, 12f, 12f, strokePaint)
    }
}
