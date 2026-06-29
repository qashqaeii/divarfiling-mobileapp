package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface

@Composable
fun ExtractStartButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassSurface(
                shape = AppShapes.Hero,
                variant = DfGlassButtonVariant.Primary,
                elevation = 10.dp,
                enabled = enabled,
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .liquidGlassSurface(
                    shape = AppShapes.IconContainer,
                    variant = DfGlassButtonVariant.Secondary,
                    elevation = 4.dp,
                    enabled = enabled,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(DfDecorIcons.Rocket),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "شروع استخراج",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Icon(
                    imageVector = DfIcons.ArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = "استخراج آگهی‌ها را با تنظیمات فوق آغاز کنید",
                style = AppTypography.labelSmall,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ExtractStartButtonPreview() {
    DivarFilingTheme {
        ExtractStartButton(
            enabled = true,
            onClick = {},
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
        )
    }
}
