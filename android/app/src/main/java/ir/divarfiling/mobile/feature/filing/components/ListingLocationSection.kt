package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

@Composable
fun ListingLocationSection(
    locationLabel: String,
    hasCoordinates: Boolean,
    onNavigate: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfDecorImage(
                    resId = DfDecorIcons.MapPin,
                    size = 18.dp,
                )
                Text(
                    text = "موقعیت ملک",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                if (hasCoordinates) {
                    LocationActionButton(
                        label = "مسیریابی",
                        icon = DfIcons.Compass,
                        tint = DfColors.Green,
                        background = DfColors.GreenLight,
                        onClick = onNavigate,
                        modifier = Modifier.weight(1f),
                    )
                }
                LocationActionButton(
                    label = "کپی لینک",
                    icon = DfIcons.Copy,
                    tint = DfColors.Purple,
                    background = DfColors.PurpleContainer,
                    onClick = onCopyLink,
                    modifier = Modifier.weight(1f),
                )
                onOpenDivar?.let { openDivar ->
                    LocationActionButton(
                        label = "دیوار",
                        icon = DfIcons.ExternalLink,
                        tint = DfColors.Blue,
                        background = DfColors.BlueLight,
                        onClick = openDivar,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 4.dp),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
