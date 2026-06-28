package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

@Composable
fun ListingMapCard(
    locationLabel: String,
    hasCoordinates: Boolean,
    onNavigate: () -> Unit,
    onCall: () -> Unit,
    onNote: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = DfIcons.MapPin,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "موقعیت روی نقشه",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                }
                if (locationLabel.isNotBlank()) {
                    Text(
                        text = locationLabel,
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(AppShapes.Card)
                    .background(DfColors.PurpleContainer.copy(alpha = 0.35f))
                    .clickable(enabled = hasCoordinates, onClick = onNavigate),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppSpacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = DfIcons.MapPin,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(44.dp),
                    )
                }
                Surface(
                    onClick = onNavigate,
                    enabled = hasCoordinates,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(AppSpacing.sm)
                        .size(36.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.92f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = DfIcons.LocateFixed,
                            contentDescription = "مسیریابی",
                            tint = DfColors.Purple,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                MapActionChip(
                    label = "مسیریابی",
                    icon = DfIcons.Compass,
                    background = DfColors.GreenLight,
                    iconTint = DfColors.Green,
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f),
                )
                MapActionChip(
                    label = "تماس",
                    icon = DfIcons.Phone,
                    background = DfColors.Purple,
                    iconTint = Color.White,
                    labelColor = Color.White,
                    onClick = onCall,
                    modifier = Modifier.weight(1.2f),
                    emphasized = true,
                )
                MapActionChip(
                    label = "یادداشت",
                    icon = DfIcons.StickyNote,
                    background = DfColors.SurfaceVariant,
                    iconTint = DfColors.TextSecondary,
                    onClick = onNote,
                    modifier = Modifier.weight(1f),
                )
                MapActionChip(
                    label = "گزارش آگهی",
                    icon = DfIcons.Flag,
                    background = DfColors.SurfaceVariant,
                    iconTint = DfColors.TextSecondary,
                    onClick = onReport,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = DfColors.TextPrimary,
    emphasized: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 4.dp,
                    vertical = if (emphasized) 12.dp else 10.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(if (emphasized) 18.dp else 16.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
