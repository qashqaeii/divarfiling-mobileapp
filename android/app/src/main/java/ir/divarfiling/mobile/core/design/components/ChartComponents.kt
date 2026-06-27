package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.network.ChartDatasetDto
import ir.divarfiling.mobile.core.network.ChartDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private fun parseColor(raw: String?, fallback: Color): Color {
    if (raw.isNullOrBlank()) return fallback
    val cleaned = raw.removePrefix("rgba(").removePrefix("rgb(").removeSuffix(")")
    val parts = cleaned.split(',').map { it.trim() }
    return try {
        when (parts.size) {
            4 -> Color(
                red = parts[0].toFloat() / 255f,
                green = parts[1].toFloat() / 255f,
                blue = parts[2].toFloat() / 255f,
                alpha = parts[3].toFloat(),
            )
            3 -> Color(
                red = parts[0].toFloat() / 255f,
                green = parts[1].toFloat() / 255f,
                blue = parts[2].toFloat() / 255f,
            )
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

private fun parseColorElement(element: JsonElement?, fallback: Color): Color? {
    val raw = element?.jsonPrimitive?.contentOrNull ?: return null
    return parseColor(raw, fallback)
}

private fun ChartDatasetDto.resolvedColors(count: Int): List<Color> {
    val bg = backgroundColor
    if (bg is JsonArray) {
        val parsed = bg.mapIndexed { index, item ->
            parseColorElement(item, palette[index % palette.size]) ?: palette[index % palette.size]
        }
        if (parsed.isNotEmpty()) {
            return List(count) { parsed[it % parsed.size] }
        }
    }
    val single = parseColorElement(bg, DfColors.Purple) ?: DfColors.Purple
    return List(count) { single }
}

@Composable
fun DfChartCard(chart: ChartDto, modifier: Modifier = Modifier) {
    if (chart.labels.isEmpty() || chart.datasets.isEmpty()) return
    DfGlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(chart.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            when (chart.type) {
                "doughnut" -> DfDoughnutChart(chart)
                else -> {
                    val horizontal = chart.title.contains("محله")
                    if (horizontal) DfHorizontalBarChart(chart) else DfBarChart(chart)
                }
            }
            chart.unit?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
            }
        }
    }
}

@Composable
private fun DfBarChart(chart: ChartDto) {
    val dataset = chart.datasets.firstOrNull() ?: return
    val values = dataset.numericValues()
    val max = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val barColors = remember(chart) { dataset.resolvedColors(values.size) }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        val barCount = values.size.coerceAtLeast(1)
        val gap = size.width * 0.04f
        val barWidth = (size.width - gap * (barCount + 1)) / barCount
        values.forEachIndexed { index, value ->
            val barHeight = (value / max) * size.height * 0.85f
            val barColor = barColors.getOrElse(index) { DfColors.Purple }
            drawRoundRect(
                color = barColor.copy(alpha = 0.85f),
                topLeft = Offset(gap + index * (barWidth + gap), size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8f, 8f),
            )
        }
    }
    ChartLabelRow(chart.labels)
}

@Composable
private fun DfHorizontalBarChart(chart: ChartDto) {
    val dataset = chart.datasets.firstOrNull() ?: return
    val values = dataset.numericValues()
    val max = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chart.labels.zip(values).take(8).forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    label,
                    modifier = Modifier.width(72.dp),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Canvas(Modifier.weight(1f).height(14.dp)) {
                    val width = (value / max) * size.width
                    drawRoundRect(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(DfColors.Purple, DfColors.Blue),
                        ),
                        size = Size(width, size.height),
                        cornerRadius = CornerRadius(8f, 8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DfDoughnutChart(chart: ChartDto) {
    val dataset = chart.datasets.firstOrNull() ?: return
    val values = dataset.numericValues()
    val total = values.sum().coerceAtLeast(1f)
    val segmentColors = remember(chart) { dataset.resolvedColors(values.size) }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        val radius = size.minDimension * 0.38f
        val center = Offset(size.width / 2f, size.height / 2f)
        var start = -90f
        values.forEachIndexed { index, value ->
            val sweep = (value / total) * 360f
            drawArc(
                color = segmentColors.getOrElse(index) { palette[index % palette.size] },
                startAngle = start,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
            )
            start += sweep
        }
        drawCircle(color = Color.White.copy(alpha = 0.92f), radius = radius * 0.55f, center = center)
    }
    ChartLabelRow(chart.labels)
}

@Composable
private fun ChartLabelRow(labels: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        labels.take(4).forEach { label ->
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = DfColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

@Composable
fun DfMarketDepthBar(
    underPct: Float,
    fairPct: Float,
    overPct: Float,
    modifier: Modifier = Modifier,
) {
    DfGlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("عمق بازار", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Canvas(Modifier.fillMaxWidth().height(18.dp)) {
                var x = 0f
                val segments = listOf(
                    underPct to DfColors.Green,
                    fairPct to DfColors.Amber,
                    overPct to DfColors.Rose,
                )
                segments.forEach { (pct, color) ->
                    val w = size.width * (pct / 100f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, 0f),
                        size = Size(w, size.height),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                    x += w
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DepthLegend("زیر بازار", underPct, DfColors.Green)
                DepthLegend("منصفانه", fairPct, DfColors.Amber)
                DepthLegend("بالای بازار", overPct, DfColors.Rose)
            }
        }
    }
}

@Composable
private fun DepthLegend(label: String, pct: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
        Text("${pct.toInt()}%", fontWeight = FontWeight.Bold, color = color)
    }
}

private val palette = listOf(
    DfColors.Purple,
    DfColors.Blue,
    DfColors.Green,
    DfColors.Amber,
    DfColors.Rose,
)

fun ChartDatasetDto.numericValues(): List<Float> =
    data.mapNotNull { element ->
        element.jsonPrimitive.floatOrNull ?: element.jsonPrimitive.intOrNull?.toFloat()
    }
