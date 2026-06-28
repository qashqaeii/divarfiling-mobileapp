package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExtractCard(
    maxItems: Int,
    enabled: Boolean,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        shape = AppShapes.Hero,
        shadowElevation = AppElevations.floating,
        onClick = { if (enabled) onStartClick() },
        enabled = enabled,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF5B21B6),
                            Color(0xFF7C3AED),
                            Color(0xFF8B5CF6),
                        ),
                        start = Offset.Zero,
                        end = Offset(800f, 400f),
                    ),
                ),
        ) {
            HeroMeshPattern(modifier = Modifier.fillMaxSize())

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
                ) {
                    Text(
                        text = "استخراج خودکار از دیوار",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (enabled) {
                            "ربات فعال است و آماده استخراج آگهی‌های جدید"
                        } else {
                            "برای استخراج، لایسنس فعال لازم است"
                        },
                        style = AppTypography.bodyDescription,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        onClick = onStartClick,
                        enabled = enabled,
                        shape = AppShapes.ButtonPill,
                        color = Color.White,
                        shadowElevation = AppElevations.subtle,
                        modifier = Modifier.padding(top = AppSpacing.xs),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = AppSpacing.md,
                                vertical = 10.dp,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = DfIcons.Play,
                                contentDescription = null,
                                tint = DfColors.Purple,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "شروع استخراج",
                                style = AppTypography.bodyDescription,
                                fontWeight = FontWeight.SemiBold,
                                color = DfColors.Purple,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (enabled && maxItems > 0) {
                        Text(
                            text = "حداکثر $maxItems آگهی در هر استخراج",
                            style = AppTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                HomeRobotIllustration(
                    modifier = Modifier.padding(start = AppSpacing.xs),
                )
            }
        }
    }
}

@Composable
private fun HeroMeshPattern(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val waveColor = Color.White.copy(alpha = 0.06f)
        val path = Path().apply {
            moveTo(0f, size.height * 0.3f)
            cubicTo(
                size.width * 0.25f, size.height * 0.1f,
                size.width * 0.55f, size.height * 0.5f,
                size.width, size.height * 0.25f,
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, waveColor)
        drawCircle(
            color = Color.White.copy(alpha = 0.04f),
            radius = size.width * 0.35f,
            center = Offset(size.width * 0.85f, size.height * 0.15f),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Preview(showBackground = true, widthDp = 390)
@Preview(showBackground = true, widthDp = 412)
@Composable
private fun QuickExtractCardPreview() {
    DivarFilingTheme {
        QuickExtractCard(
            maxItems = 100,
            enabled = true,
            onStartClick = {},
        )
    }
}
