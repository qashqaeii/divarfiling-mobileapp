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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard

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

    DfCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = DfThemeColors.primaryContainer(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                    Text(
                        "در حال استخراج فایل…",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.onPrimaryContainer(),
                    )
                    Text(
                        steps[activeStep].label,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size((44 * pulse).dp)
                        .clip(CircleShape)
                        .background(DfThemeColors.primary()),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp,
                        color = DfThemeColors.surface(),
                    )
                }
            }

            if (total > 0) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.Chip),
                    color = DfThemeColors.primary(),
                    trackColor = DfThemeColors.surface(),
                )
                Text(
                    "$current از $total آگهی",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.onPrimaryContainer(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs - 2.dp)) {
                steps.forEachIndexed { index, step ->
                    val done = index < activeStep
                    val active = index == activeStep
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        done -> DfThemeColors.success()
                                        active -> DfThemeColors.primary()
                                        else -> DfThemeColors.outlineSubtle()
                                    },
                                ),
                        )
                        Text(
                            step.label,
                            style = AppTypography.labelSmall,
                            color = when {
                                done -> DfThemeColors.success()
                                active -> DfThemeColors.onPrimaryContainer()
                                else -> DfThemeColors.textMuted()
                            },
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
