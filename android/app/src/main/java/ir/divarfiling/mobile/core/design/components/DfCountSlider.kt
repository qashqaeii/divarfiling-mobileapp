package ir.divarfiling.mobile.core.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.license.ExtractLightLimits
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfCountSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "تعداد آگهی",
    valueRange: ClosedFloatingPointRange<Float> = 0f..ExtractLightLimits.MAX_ITEMS.toFloat(),
    enabled: Boolean = true,
) {
    val min = valueRange.start.roundToInt()
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
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
        label = "countFraction",
    )
    val persianValue = DateUtils.toPersianDigits(value.toString())

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
                    color = DfColors.TextSecondary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "سقف ${DateUtils.toPersianDigits(max.toString())} آگهی در هر دور",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DfColors.PurpleContainer,
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
                        color = DfColors.PurpleDark,
                    )
                    Text(
                        text = "آگهی",
                        style = AppTypography.labelSmall,
                        color = DfColors.Purple,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }
        }

        CountTrack(
            fraction = animatedFraction,
            enabled = enabled,
            onFractionChange = { f ->
                val raw = min + (f * (max - min)).roundToInt()
                val snapped = ((raw.toFloat() / stepSize).roundToInt() * stepSize)
                    .coerceIn(min, max)
                if (snapped != value) onValueChange(snapped)
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf(min, (min + max) / 2, max).forEach { tick ->
                Text(
                    text = DateUtils.toPersianDigits(tick.toString()),
                    style = AppTypography.labelSmall,
                    color = if (tick == value) DfColors.Purple else DfColors.TextMuted,
                    fontWeight = if (tick == value) FontWeight.Bold else FontWeight.Normal,
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
                        color = if (selected) DfColors.Purple else DfColors.Surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) DfColors.Purple else DfColors.Outline,
                        ),
                    ) {
                        Text(
                            text = DateUtils.toPersianDigits(preset.toString()),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            style = AppTypography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else DfColors.TextSecondary,
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
    val thumbSize = 22.dp
    val trackHeight = 10.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .semantics { contentDescription = "اسلایدر تعداد آگهی" }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        onFractionChange((offset.x / w).coerceIn(0f, 1f))
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val w = size.width.toFloat().coerceAtLeast(1f)
                        onFractionChange((change.position.x / w).coerceIn(0f, 1f))
                    },
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val thumbPx = with(density) { thumbSize.toPx() }
        val travel = (widthPx - thumbPx).coerceAtLeast(1f)
        val thumbOffset = (fraction.coerceIn(0f, 1f) * travel)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(999.dp)),
        ) {
            val h = size.height
            drawRoundRect(
                color = DfColors.PurpleContainer.copy(alpha = 0.55f),
                size = size,
                cornerRadius = CornerRadius(h / 2, h / 2),
            )
            val activeW = (size.width * fraction).coerceIn(0f, size.width)
            if (activeW > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7C3AED),
                            Color(0xFFA78BFA),
                            Color(0xFFC4B5FD),
                        ),
                    ),
                    size = Size(activeW, h),
                    cornerRadius = CornerRadius(h / 2, h / 2),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = h * 0.18f,
                    center = Offset(activeW * 0.72f, h * 0.35f),
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                .size(thumbSize)
                .shadow(6.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White, Color(0xFFF3E8FF)),
                    ),
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(DfColors.Purple),
            )
        }
    }
}
