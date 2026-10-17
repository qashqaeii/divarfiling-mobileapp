package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallUiCopy

@Composable
fun PostCallKindBanner(
    metadata: PostCallCallMetadata,
    phone: String,
    modifier: Modifier = Modifier,
) {
    val visual = PostCallCallKindVisuals.forKind(metadata.callEndKind)
    val contextLine = PostCallUiCopy.formatCallContextLine(metadata)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = visual.container,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(visual.containerBrush)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(visual.containerBrush)
                    .padding(10.dp),
            ) {
                Icon(
                    visual.icon,
                    contentDescription = null,
                    tint = visual.accent,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    visual.kindLabel,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = visual.onContainer,
                )
                Text(
                    phone,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = visual.onContainer,
                    modifier = Modifier.padding(top = AppSpacing.xxs),
                )
                if (contextLine.isNotBlank()) {
                    Text(
                        contextLine,
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = visual.onContainer.copy(alpha = 0.78f),
                        modifier = Modifier.padding(top = AppSpacing.xxs),
                    )
                }
            }
        }
    }
}
