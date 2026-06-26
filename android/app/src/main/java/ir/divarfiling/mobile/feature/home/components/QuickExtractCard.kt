package ir.divarfiling.mobile.feature.home.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.DfSpacing
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
            .padding(horizontal = DfSpacing.screenHorizontal),
        shape = DfShapes.Hero,
        shadowElevation = 6.dp,
        onClick = { if (enabled) onStartClick() },
        enabled = enabled,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(DfColors.PurpleGradientStart, DfColors.PurpleGradientEnd),
                    ),
                )
                .padding(DfSpacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DfSpacing.xs),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.Sparkles,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "استخراج سریع",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Text(
                        text = "همین حالا از دیوار استخراج کن",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Text(
                        text = "حداکثر $maxItems آگهی در هر استخراج",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                    Surface(
                        onClick = onStartClick,
                        enabled = enabled,
                        shape = DfShapes.ButtonPill,
                        color = Color.White,
                        modifier = Modifier.padding(top = DfSpacing.sm),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "شروع استخراج",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = DfColors.PurpleDark,
                            )
                            Icon(
                                imageVector = DfIcons.ChevronLeft,
                                contentDescription = null,
                                tint = DfColors.PurpleDark,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(DfShapes.CardSmall)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = DfIcons.Smartphone,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
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
