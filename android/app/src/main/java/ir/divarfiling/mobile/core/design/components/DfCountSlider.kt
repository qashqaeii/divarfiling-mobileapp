package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.license.ExtractLightLimits
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfCountSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "تعداد آگهی",
    valueRange: ClosedFloatingPointRange<Float> = 1f..ExtractLightLimits.MAX_ITEMS.toFloat(),
    enabled: Boolean = true,
) {
    val min = valueRange.start.roundToInt().coerceAtLeast(1)
    val max = valueRange.endInclusive.roundToInt().coerceAtLeast(min + 1)
    val stepSize = when {
        max - min <= 50 -> 1
        max - min <= 100 -> 2
        else -> 5
    }
    val presets = remember(min, max) {
        listOf(50, 100, 150, 200, max)
            .filter { it in min..max }
            .distinct()
    }
    val fraction = ((value - min).toFloat() / (max - min).toFloat()).coerceIn(0f, 1f)
    val persianValue = DateUtils.toPersianDigits(value.toString())

    fun snap(raw: Int): Int {
        val stepped = (raw.toFloat() / stepSize).roundToInt() * stepSize
        return stepped.coerceIn(min, max)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "سقف ${DateUtils.toPersianDigits(max.toString())} آگهی در هر دور",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DfThemeColors.primaryContainer(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = persianValue,
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.onPrimaryContainer(),
                    )
                    Text(
                        text = "آگهی",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.primary(),
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }

        CountTrack(
            fraction = fraction,
            enabled = enabled,
            onFractionChange = { f ->
                val raw = min + (f * (max - min)).roundToInt()
                val snapped = snap(raw)
                if (snapped != value) onValueChange(snapped)
            },
        )

        val tickValues = remember(min, max) { listOf(min, (min + max) / 2, max) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            tickValues.forEach { tick ->
                val isActive = abs(tick - value) <= stepSize
                Text(
                    text = DateUtils.toPersianDigits(tick.toString()),
                    style = AppTypography.labelSmall,
                    color = if (isActive) DfThemeColors.primary() else DfThemeColors.textMuted(),
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        if (presets.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                presets.forEach { preset ->
                    val selected = value == preset
                    Surface(
                        onClick = { if (enabled) onValueChange(preset) },
                        enabled = enabled,
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) DfThemeColors.primary() else DfThemeColors.surface(),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) DfThemeColors.primary() else DfThemeColors.outline(),
                        ),
                    ) {
                        Text(
                            text = DateUtils.toPersianDigits(preset.toString()),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = AppTypography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else DfThemeColors.textSecondary(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountTrack(
    fraction: Float,
    enabled: Boolean,
    onFractionChange: (Float) -> Unit,
) {
    val density = LocalDensity.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val thumbSize = 24.dp
    val trackHeight = 8.dp
    val coercedFraction = fraction.coerceIn(0f, 1f)
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val displayFraction = dragFraction ?: coercedFraction

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .semantics { contentDescription = "اسلایدر تعداد آگهی" }
            .pointerInput(enabled, isRtl) {
                if (!enabled) return@pointerInput

                fun updateFromX(x: Float) {
                    val w = size.width.toFloat().coerceAtLeast(1f)
                    val raw = (x / w).coerceIn(0f, 1f)
                    val f = if (isRtl) 1f - raw else raw
                    dragFraction = f
                    onFractionChange(f)
                }

                detectHorizontalDragGestures(
                    onDragStart = { offset -> updateFromX(offset.x) },
                    onDragEnd = { dragFraction = null },
                    onDragCancel = { dragFraction = null },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        updateFromX(change.position.x)
                    },
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val thumbPx = with(density) { thumbSize.toPx() }
        val travel = (widthPx - thumbPx).coerceAtLeast(1f)
        val thumbOffset = displayFraction * travel
        val fillEndPx = thumbOffset + thumbPx / 2f

        val trackBg = DfThemeColors.primaryContainer().copy(alpha = 0.55f)
        val trackActiveStart = DfThemeColors.primary()
        val trackActiveEnd = DfThemeColors.primary().copy(alpha = 0.75f)
        val thumbRing = DfThemeColors.surface()
        val thumbCore = DfThemeColors.primary()

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(999.dp)),
        ) {
            val h = size.height
            val corner = CornerRadius(h / 2f, h / 2f)
            drawRoundRect(
                color = trackBg,
                size = size,
                cornerRadius = corner,
            )
            val fillWidth = fillEndPx.coerceIn(0f, size.width)
            if (fillWidth > 0f) {
                if (isRtl) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(trackActiveEnd, trackActiveStart),
                            startX = size.width - fillWidth,
                            endX = size.width,
                        ),
                        topLeft = Offset(size.width - fillWidth, 0f),
                        size = Size(fillWidth, h),
                        cornerRadius = corner,
                    )
                } else {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(trackActiveStart, trackActiveEnd),
                            startX = 0f,
                            endX = fillWidth,
                        ),
                        size = Size(fillWidth, h),
                        cornerRadius = corner,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                .size(thumbSize)
                .shadow(4.dp, CircleShape, clip = false, ambientColor = DfThemeColors.shadow())
                .clip(CircleShape)
                .background(thumbRing)
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(thumbCore),
            )
        }
    }
}
