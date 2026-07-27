package ir.divarfiling.mobile.feature.home.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage

@Composable
fun HomeDashboardCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    footerLabel: String,
    onFooterClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
    content: @Composable () -> Unit,
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(250),
        label = "homeCardChevron",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card,
            color = DfThemeColors.surface(),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            shadowElevation = AppElevations.subtle,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggle)
                        .padding(
                            horizontal = AppSpacing.cardPadding,
                            vertical = AppSpacing.sm,
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DfThemeColors.primaryContainer(), AppShapes.IconContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            when {
                                iconRes != null -> DfDecorImage(
                                    resId = iconRes,
                                    size = 20.dp,
                                )
                                icon != null -> Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = DfThemeColors.primary(),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        Text(
                            text = title,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.textPrimary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        imageVector = DfIcons.ChevronDown,
                        contentDescription = if (expanded) "بستن" else "باز کردن",
                        tint = DfThemeColors.textMuted(),
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(chevronRotation),
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(tween(250)),
                    exit = shrinkVertically() + fadeOut(tween(200)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = AppSpacing.cardPadding,
                                end = AppSpacing.cardPadding,
                                bottom = AppSpacing.xs,
                            ),
                    ) {
                        content()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onFooterClick)
                                .padding(vertical = AppSpacing.sm),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = footerLabel,
                                style = AppTypography.bodyDescription,
                                fontWeight = FontWeight.Medium,
                                color = DfThemeColors.primary(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = DfIcons.ChevronLeft,
                                contentDescription = null,
                                tint = DfThemeColors.primary(),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(DfThemeColors.primaryContainer(), AppShapes.IconContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DfThemeColors.primary(),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = title,
            style = AppTypography.sectionTitle,
            fontWeight = FontWeight.SemiBold,
            color = DfThemeColors.textPrimary(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(DfThemeColors.primary(), CircleShape),
        )
    }
}
