package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealStageOptionList(
    stages: List<String>,
    selectedStage: String,
    onStageSelect: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        stages.forEach { stage ->
            val probability = DealsFilters.stageProbability(stage)
            DfSheetOptionRow(
                label = stage,
                selected = stage == selectedStage,
                onClick = { if (enabled) onStageSelect(stage) },
                icon = DealUiUtils.stageIcon(stage),
                trailing = "$probability٪",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealStagePipeline(
    stages: List<String>,
    currentStage: String?,
    isSubmitting: Boolean,
    onStageSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedStage = currentStage ?: stages.firstOrNull().orEmpty()
    val currentIndex = stages.indexOf(resolvedStage).coerceAtLeast(0)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            stages.forEachIndexed { index, stage ->
                val isPast = index < currentIndex
                val isCurrent = index == currentIndex
                val colors = DealUiUtils.dealStageColors(stage)
                val probability = DealsFilters.stageProbability(stage)

                DealPipelineStepRow(
                    stage = stage,
                    description = DealUiUtils.stageDescription(stage),
                    probability = probability,
                    accent = colors.first,
                    background = colors.second,
                    isPast = isPast,
                    isCurrent = isCurrent,
                    isLast = index == stages.lastIndex,
                    isSubmitting = isSubmitting && isCurrent,
                    enabled = !isSubmitting,
                    onClick = {
                        if (stage != resolvedStage) onStageSelect(stage)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DealPipelineStepRow(
    stage: String,
    description: String,
    probability: Int,
    accent: Color,
    background: Color,
    isPast: Boolean,
    isCurrent: Boolean,
    isLast: Boolean,
    isSubmitting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = when {
            isCurrent -> background.copy(alpha = 0.85f)
            isPast -> DfColors.SurfaceVariant.copy(alpha = 0.35f)
            else -> Color.Transparent
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(28.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 22.dp else 18.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPast -> DfColors.Green
                                isCurrent -> accent
                                else -> DfColors.Outline.copy(alpha = 0.4f)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isSubmitting -> CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        isPast -> Icon(
                            imageVector = DfIcons.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                        isCurrent -> Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }
                if (!isLast) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(28.dp)
                            .background(
                                if (isPast || isCurrent) accent.copy(alpha = 0.35f)
                                else DfColors.Outline.copy(alpha = 0.2f),
                            ),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stage,
                    style = AppTypography.bodyDescription,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrent) accent else DfColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = description,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(shape = AppShapes.Chip, color = background) {
                Text(
                    text = "$probability٪",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
            }
        }
    }
}
