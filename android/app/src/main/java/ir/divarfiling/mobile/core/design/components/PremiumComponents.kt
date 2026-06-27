package ir.divarfiling.mobile.core.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.DfAnimation
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme

data class DfNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val isCenter: Boolean = false,
    val badge: Int? = null,
)

@Composable
fun DfBottomNavigation(
    items: List<DfNavItem>,
    selectedRoute: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(AppElevations.navBar, AppShapes.BottomNav, ambientColor = AppColors.Shadow),
        shape = AppShapes.BottomNav,
        color = AppColors.Surface,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSpacing.bottomNavHeight)
                .padding(horizontal = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                if (item.isCenter) {
                    DfCenterNavItem(
                        item = item,
                        selected = selectedRoute == item.route,
                        onClick = { onItemClick(item.route) },
                    )
                } else {
                    DfSideNavItem(
                        item = item,
                        selected = selectedRoute == item.route,
                        onClick = { onItemClick(item.route) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DfCenterNavItem(
    item: DfNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = DfAnimation.springSnappy(),
        label = "centerNavScale",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .widthIn(min = 56.dp, max = 72.dp)
            .defaultMinSize(minHeight = 56.dp),
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(46.dp)
                .shadow(
                    elevation = if (selected) AppElevations.floating else AppElevations.raised,
                    shape = CircleShape,
                    ambientColor = AppColors.Purple.copy(alpha = 0.25f),
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(AppColors.PurpleGradientStart, AppColors.PurpleGradientEnd),
                    ),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.White),
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = item.label,
            style = AppTypography.bottomNav,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) AppColors.NavActive else AppColors.NavInactive,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = AppSpacing.xxs)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun DfSideNavItem(
    item: DfNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (selected) AppColors.NavActive else AppColors.NavInactive
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 52.dp, max = 72.dp)
            .defaultMinSize(minHeight = 48.dp)
            .clip(AppShapes.Chip)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = AppSpacing.xxs, vertical = AppSpacing.xxs),
    ) {
        Box {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
            item.badge?.takeIf { it > 0 }?.let { count ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AppColors.Rose),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (count > 9) "9+" else count.toString(),
                        style = AppTypography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
        Text(
            text = item.label,
            style = AppTypography.bottomNav,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = AppSpacing.xxs)
                .fillMaxWidth(),
        )
    }
}

@Composable
fun DfAnimatedCounter(
    target: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = AppTypography.statNumber,
    color: Color = AppColors.TextPrimary,
) {
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = DfAnimation.springSnappy(),
        label = "counter",
    )
    Text(
        text = animated.toString(),
        style = style,
        color = color,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfPremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = AppColors.Surface,
    content: @Composable () -> Unit,
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .shadow(AppElevations.card, AppShapes.Card, ambientColor = AppColors.Shadow)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = AppShapes.Card,
            color = containerColor,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                content()
            }
        }
    } else {
        Surface(
            modifier = cardModifier,
            shape = AppShapes.Card,
            color = containerColor,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
fun DfSectionTitle(
    title: String,
    badge: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = AppTypography.sectionTitle,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            badge?.let {
                Surface(
                    shape = AppShapes.Chip,
                    color = AppColors.PurpleContainer,
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = 2.dp),
                        style = AppTypography.labelSmall,
                        color = AppColors.PurpleDark,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = AppTypography.bodyDescription,
                color = AppColors.Purple,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = AppSpacing.sm)
                    .clickable(onClick = onAction),
            )
        }
    }
}

@Composable
fun DfShimmerBox(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerOffset",
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            AppColors.SurfaceVariant,
            AppColors.OutlineSubtle,
            AppColors.SurfaceVariant,
        ),
        start = Offset(offset - 300f, 0f),
        end = Offset(offset, 0f),
    )
    Box(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(brush),
    )
}

@Preview(showBackground = true, widthDp = 360, name = "BottomNav 360")
@Preview(showBackground = true, widthDp = 390, name = "BottomNav 390")
@Preview(showBackground = true, widthDp = 412, name = "BottomNav 412")
@Composable
private fun DfBottomNavigationPreview() {
    DivarFilingTheme {
        DfBottomNavigation(
            items = listOf(
                DfNavItem("filing", "فایلینگ", DfIcons.Folder),
                DfNavItem("crm", "CRM", DfIcons.Users),
                DfNavItem("home", "میزکار", DfIcons.Home, isCenter = true),
                DfNavItem("today", "امروز", DfIcons.Handshake),
                DfNavItem("settings", "تنظیمات", DfIcons.Settings),
            ),
            selectedRoute = "home",
            onItemClick = {},
        )
    }
}
