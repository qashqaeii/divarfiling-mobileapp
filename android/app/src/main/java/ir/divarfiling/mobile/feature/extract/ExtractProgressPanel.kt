package ir.divarfiling.mobile.feature.extract

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors

private data class ExtractStep(val label: String, val threshold: Float)

private val steps = listOf(
    ExtractStep("اتصال به دیوار", 0.05f),
    ExtractStep("دریافت آگهی‌ها", 0.35f),
    ExtractStep("پردازش و فیلتر", 0.7f),
    ExtractStep("آپلود به فایلینگ", 1f),
)

@Composable
fun ExtractProgressPanel(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val progress = if (total > 0) current.toFloat() / total else 0f
    val activeStep = steps.indexOfLast { progress >= it.threshold }.coerceAtLeast(0)
    val pulse by rememberInfiniteTransition(label = "extractPulse").animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DfColors.PurpleContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "در حال استخراج فایل…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.PurpleDark,
                    )
                    Text(
                        steps[activeStep].label,
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                    )
                }
                Box(
                    modifier = Modifier
                        .size((44 * pulse).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(DfColors.PurpleGradientStart, DfColors.PurpleGradientEnd),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = DfColors.Surface,
                    )
                }
            }

            if (total > 0) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = DfColors.Purple,
                    trackColor = DfColors.Surface,
                )
                Text(
                    "$current از $total آگهی",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.PurpleDark,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.forEachIndexed { index, step ->
                    val done = index < activeStep
                    val active = index == activeStep
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        done -> DfColors.Green
                                        active -> DfColors.Purple
                                        else -> DfColors.OutlineSubtle
                                    },
                                ),
                        )
                        Text(
                            step.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                done -> DfColors.Green
                                active -> DfColors.PurpleDark
                                else -> DfColors.TextMuted
                            },
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
