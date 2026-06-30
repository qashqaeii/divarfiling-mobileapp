package ir.divarfiling.mobile.feature.crm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfShimmerBox

data class CrmHubStatChip(
    val label: String,
    val value: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmHubFeatureCard(
    title: String,
    subtitle: String,
    tint: Color,
    background: Color,
    stats: List<CrmHubStatChip>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    illustration: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp),
        shape = AppShapes.Hero,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
        tonalElevation = AppElevations.none,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AppSpacing.cardPadding,
                    end = AppSpacing.cardPadding,
                    top = AppSpacing.sm,
                    bottom = AppSpacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = AppSpacing.xs),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(background, AppShapes.IconContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                iconRes != null -> DfDecorImage(resId = iconRes, size = 18.dp)
                                icon != null -> Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Text(
                            text = title,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = subtitle,
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (stats.isNotEmpty()) {
                    Surface(
                        shape = AppShapes.ButtonPill,
                        color = background,
                        shadowElevation = AppElevations.none,
                        modifier = Modifier.padding(top = AppSpacing.sm),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            stats.forEach { stat ->
                                CrmHubStatItem(stat = stat, tint = tint)
                            }
                        }
                    }
                }
            }

            Box(contentAlignment = Alignment.Center) {
                illustration()
            }
        }
    }
}

@Composable
private fun CrmHubStatItem(
    stat: CrmHubStatChip,
    tint: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            stat.iconRes != null -> DfDecorImage(resId = stat.iconRes, size = 12.dp)
            stat.icon != null -> Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = "${stat.label} ${stat.value}",
            style = AppTypography.labelSmall,
            color = DfColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CrmHubFeatureCardSkeleton(modifier: Modifier = Modifier) {
    DfShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CrmHubFeatureCardPreview() {
    DivarFilingTheme {
        CrmHubFeatureCard(
            title = "مخاطبین",
            subtitle = "لیست کامل مشتریان و سرنخ‌های جدید",
            iconRes = DfDecorIcons.Users,
            tint = DfColors.Purple,
            background = DfColors.PurpleContainer,
            stats = listOf(
                CrmHubStatChip("مخاطبین", "248", iconRes = DfDecorIcons.Users),
                CrmHubStatChip("سرنخ‌های جدید", "32", icon = DfIcons.UserPlus),
            ),
            onClick = {},
            illustration = {
                CrmContactsIllustration(
                    tint = DfColors.Purple,
                    background = DfColors.PurpleContainer,
                )
            },
        )
    }
}
