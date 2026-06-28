package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilingTutorialBanner(
    onDismiss: () -> Unit,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtractSectionCard(
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = AppShapes.IconContainer,
                color = DfColors.PurpleContainer,
                modifier = Modifier.size(48.dp),
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = DfIcons.Play,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "آموزش فایلینگ، نقشه و تحلیل",
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                )
                Text(
                    text = "نحوه مدیریت فایل‌ها، فیلتر کردن و تحلیل روی نقشه را یاد بگیرید.",
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                )
                Surface(
                    onClick = onWatchClick,
                    shape = AppShapes.ButtonPill,
                    color = DfColors.Purple,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.Play,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "مشاهده آموزش",
                            style = AppTypography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = DfIcons.X,
                    contentDescription = "بستن",
                    tint = DfColors.TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
