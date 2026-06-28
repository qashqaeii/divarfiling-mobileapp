package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.network.DealPipelineColumnDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsPipelineBar(
    columns: List<DealPipelineColumnDto>,
    onStageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (columns.isEmpty()) return
    val total = columns.sumOf { it.count }.coerceAtLeast(1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp)),
        ) {
            columns.forEach { column ->
                if (column.count <= 0) return@forEach
                val weight = column.count.toFloat() / total
                Box(
                    modifier = Modifier
                        .weight(weight)
                        .height(10.dp)
                        .background(stageColor(column.stage)),
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            columns.filter { it.count > 0 }.forEach { column ->
                Surface(
                    onClick = { onStageClick(column.stage) },
                    shape = AppShapes.Chip,
                    color = DfColors.Surface,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(stageColor(column.stage)),
                        )
                        Text(
                            text = "${column.stage} ${column.count}",
                            style = AppTypography.labelSmall,
                            color = DfColors.TextSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

private fun stageColor(stage: String): Color = when {
    stage.contains("از دست") || stage.contains("سرد") -> DfColors.OverdueAccent
    stage.contains("بسته") -> DfColors.Green
    stage.contains("قرارداد") || stage.contains("پیش") -> Color(0xFFEC4899)
    stage.contains("بازدید") -> DfColors.Blue
    stage.contains("مذاکره") -> DfColors.Amber
    stage == "سرنخ" -> Color(0xFF1E3A8A)
    else -> DfColors.Purple
}
