package ir.divarfiling.mobile.feature.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExtractCard(
    maxItems: Int,
    enabled: Boolean,
    onStartClick: () -> Unit,
    onActivateLicense: () -> Unit = onStartClick,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        shape = AppShapes.Hero,
        shadowElevation = AppElevations.raised,
        onClick = { if (enabled) onStartClick() else onActivateLicense() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            DfColors.PurpleDark,
                            DfColors.Purple,
                            DfColors.PurpleGradientStart,
                        ),
                    ),
                ),
        ) {
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
                            "آماده استخراج آگهی‌های جدید"
                        } else {
                            "لایسنس را فعال کنید تا استخراج شروع شود"
                        },
                        style = AppTypography.bodyDescription,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        onClick = { if (enabled) onStartClick() else onActivateLicense() },
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
                                imageVector = if (enabled) DfIcons.Play else DfIcons.Sparkles,
                                contentDescription = null,
                                tint = DfColors.Purple,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = if (enabled) "شروع استخراج" else "فعال‌سازی لایسنس",
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
                            text = "حداکثر ${DateUtils.toPersianDigits(maxItems.toString())} آگهی در هر استخراج",
                            style = AppTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Image(
                    painter = painterResource(DfDecorIcons.Rocket),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = AppSpacing.xs)
                        .size(DfDecorSize.Hero),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun QuickExtractCardPreview() {
    DivarFilingTheme {
        QuickExtractCard(
            maxItems = 250,
            enabled = true,
            onStartClick = {},
        )
    }
}
