package ir.divarfiling.mobile.feature.contentstudio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfThemeColors

@Composable
fun PcsStudioCanvasStage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Card,
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.35f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.sm)
                .clip(AppShapes.CardSmall)
                .background(DfThemeColors.background()),
            content = { content() },
        )
    }
}
