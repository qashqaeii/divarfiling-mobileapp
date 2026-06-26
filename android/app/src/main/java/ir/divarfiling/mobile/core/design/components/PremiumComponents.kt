package ir.divarfiling.mobile.core.design.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfAnimation
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfElevation
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.DfSpacing

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
            .shadow(DfElevation.navBar, DfShapes.BottomNav, ambientColor = DfColors.Shadow),
        shape = DfShapes.BottomNav,
        color = DfColors.Surface,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DfSpacing.sm, vertical = DfSpacing.sm),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom,
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
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = DfAnimation.springSnappy(),
        label = "centerNavScale",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = (-12).dp),
    ) {
        Box(
            modifier = Modifier
                .scale(scale)
                .size(56.dp)
                .shadow(
                    elevation = if (selected) DfElevation.floating else DfElevation.raised,
                    shape = CircleShape,
                    ambientColor = DfColors.Purple.copy(alpha = 0.3f),
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(DfColors.PurpleGradientStart, DfColors.PurpleGradientEnd),
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
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) DfColors.Purple else DfColors.TextMuted,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun DfSideNavItem(
    item: DfNavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val tint = if (selected) DfColors.Purple else DfColors.TextMuted
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(DfShapes.Chip)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = DfSpacing.xs, vertical = DfSpacing.xxs),
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
                        .background(DfColors.Rose),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (count > 9) "9+" else count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = tint,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
fun DfAnimatedCounter(
    target: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium,
    color: Color = DfColors.TextPrimary,
) {
    val animated by androidx.compose.animation.core.animateIntAsState(
        targetValue = target,
        animationSpec = DfAnimation.springSnappy(),
        label = "counter",
    )
    Text(
        text = animated.toString(),
        style = style,
        color = color,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DfPremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = DfColors.Surface,
    content: @Composable () -> Unit,
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .shadow(DfElevation.card, DfShapes.Card, ambientColor = DfColors.Shadow)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = DfShapes.Card,
            color = containerColor,
        ) {
            Box(Modifier.padding(DfSpacing.cardPadding)) { content() }
        }
    } else {
        Surface(
            modifier = cardModifier,
            shape = DfShapes.Card,
            color = containerColor,
        ) {
            Box(Modifier.padding(DfSpacing.cardPadding)) { content() }
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
            horizontalArrangement = Arrangement.spacedBy(DfSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.TextPrimary,
            )
            badge?.let {
                Surface(
                    shape = DfShapes.Chip,
                    color = DfColors.PurpleContainer,
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = DfColors.PurpleDark,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = DfColors.Purple,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onAction),
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
            DfColors.SurfaceVariant,
            DfColors.OutlineSubtle,
            DfColors.SurfaceVariant,
        ),
        start = Offset(offset - 300f, 0f),
        end = Offset(offset, 0f),
    )
    Box(
        modifier = modifier
            .clip(DfShapes.CardSmall)
            .background(brush),
    )
}
