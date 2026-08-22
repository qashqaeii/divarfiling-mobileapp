package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfThemeColors

/**
 * قاب یکسان هدر — گرادیان نرم، حاشیه ظریف و المان تزئینی گوشه.
 */
@Composable
fun DfHeaderFrame(
    theme: DfHeaderTheme,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppSpacing.screenHorizontal,
                vertical = AppSpacing.sm,
            ),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Hero,
            color = Color.Transparent,
            border = BorderStroke(1.dp, theme.borderColor),
            shadowElevation = AppElevations.subtle,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    theme.gradientStart,
                                    theme.gradientEnd,
                                    DfThemeColors.surface().copy(alpha = if (DfThemeColors.isDark()) 0.72f else 0.35f),
                                ),
                            ),
                        ),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 28.dp, y = (-24).dp)
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(theme.orbColor),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-18).dp, y = 18.dp)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(theme.orbColor.copy(alpha = theme.orbColor.alpha * 0.65f)),
                )
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    content = content,
                )
            }
        }
    }
}
