package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors

enum class DfGlassButtonVariant {
    Secondary,
    Primary,
    Accent,
}

fun liquidGlassBrush(
    variant: DfGlassButtonVariant = DfGlassButtonVariant.Secondary,
    accent: Color = DfColors.Purple,
): Brush = when (variant) {
    DfGlassButtonVariant.Secondary -> Brush.verticalGradient(
        colors = listOf(
            DfColors.GlassHighlight.copy(alpha = 0.88f),
            Color.White.copy(alpha = 0.52f),
            Color.White.copy(alpha = 0.38f),
        ),
    )
    DfGlassButtonVariant.Primary -> Brush.linearGradient(
        colors = listOf(
            DfColors.PurpleGradientStart.copy(alpha = 0.72f),
            DfColors.Purple.copy(alpha = 0.58f),
            DfColors.PurpleGradientEnd.copy(alpha = 0.48f),
            Color.White.copy(alpha = 0.16f),
        ),
    )
    DfGlassButtonVariant.Accent -> Brush.horizontalGradient(
        colors = listOf(
            accent.copy(alpha = 0.34f),
            accent.copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.42f),
        ),
    )
}

fun liquidGlassBorderColor(
    variant: DfGlassButtonVariant = DfGlassButtonVariant.Secondary,
    accent: Color = DfColors.Purple,
): Color = when (variant) {
    DfGlassButtonVariant.Secondary -> DfColors.GlassBorder
    DfGlassButtonVariant.Primary -> Color.White.copy(alpha = 0.52f)
    DfGlassButtonVariant.Accent -> accent.copy(alpha = 0.42f)
}

fun Modifier.liquidGlassSurface(
    shape: Shape,
    variant: DfGlassButtonVariant = DfGlassButtonVariant.Secondary,
    accent: Color = DfColors.Purple,
    elevation: Dp = AppElevations.subtle + 4.dp,
    enabled: Boolean = true,
): Modifier {
    val shadowColor = when (variant) {
        DfGlassButtonVariant.Primary -> DfColors.Purple.copy(alpha = 0.28f)
        DfGlassButtonVariant.Accent -> accent.copy(alpha = 0.2f)
        DfGlassButtonVariant.Secondary -> DfColors.GlassShadow
    }
    return this
        .shadow(
            elevation = if (enabled) elevation else AppElevations.none,
            shape = shape,
            ambientColor = shadowColor,
            spotColor = shadowColor,
        )
        .clip(shape)
        .background(
            if (enabled) {
                liquidGlassBrush(variant = variant, accent = accent)
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.22f),
                    ),
                )
            },
        )
        .border(
            width = 1.dp,
            color = if (enabled) {
                liquidGlassBorderColor(variant = variant, accent = accent)
            } else {
                DfColors.GlassBorder.copy(alpha = 0.35f)
            },
            shape = shape,
        )
}

@Composable
fun DfLiquidBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DfColors.Background),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(DfColors.LiquidPurple, Color.Transparent),
                        radius = 900f,
                        center = androidx.compose.ui.geometry.Offset(0.15f, 0.1f),
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(DfColors.LiquidBlue, Color.Transparent),
                        radius = 800f,
                        center = androidx.compose.ui.geometry.Offset(0.85f, 0.25f),
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(DfColors.LiquidPink, Color.Transparent),
                        radius = 700f,
                        center = androidx.compose.ui.geometry.Offset(0.5f, 0.95f),
                    ),
                ),
        )
    }
}

@Composable
fun DfGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .liquidGlassSurface(shape = shape, elevation = AppElevations.floating + 6.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(AppSpacing.cardPadding),
    ) {
        content()
    }
}

@Composable
fun DfGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
    accent: Color = DfColors.Purple,
    variant: DfGlassButtonVariant = DfGlassButtonVariant.Secondary,
    enabled: Boolean = true,
) {
    val resolvedVariant = when {
        variant == DfGlassButtonVariant.Primary -> DfGlassButtonVariant.Primary
        selected -> DfGlassButtonVariant.Accent
        else -> variant
    }
    val shape = AppShapes.GlassSmall
    val contentColor = when (resolvedVariant) {
        DfGlassButtonVariant.Primary -> Color.White
        DfGlassButtonVariant.Accent -> accent
        DfGlassButtonVariant.Secondary -> DfColors.TextPrimary
    }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 42.dp)
            .liquidGlassSurface(
                shape = shape,
                variant = resolvedVariant,
                accent = accent,
                enabled = enabled,
            )
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (selected && resolvedVariant != DfGlassButtonVariant.Primary) accent else contentColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected || resolvedVariant == DfGlassButtonVariant.Primary) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = contentColor,
        )
    }
}

@Composable
fun DfGlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DfGlassButtonVariant = DfGlassButtonVariant.Secondary,
    accent: Color = DfColors.Purple,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
) {
    val tint = when (variant) {
        DfGlassButtonVariant.Primary -> Color.White
        DfGlassButtonVariant.Accent -> accent
        DfGlassButtonVariant.Secondary -> DfColors.TextSecondary
    }
    Box(
        modifier = modifier
            .size(size)
            .liquidGlassSurface(shape = AppShapes.IconContainer, variant = variant, accent = accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun DfGlassTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = DfColors.Purple,
    compact: Boolean = false,
) {
    val shape = if (compact) AppShapes.Chip else AppShapes.GlassSmall
    val vertical = if (compact) 6.dp else 8.dp
    val horizontal = if (compact) 10.dp else 12.dp
    Box(
        modifier = modifier
            .liquidGlassSurface(shape = shape, variant = DfGlassButtonVariant.Accent, accent = accent)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontal, vertical = vertical),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = if (compact) AppTypography.labelSmall else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
    }
}

@Composable
fun DfGlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .liquidGlassSurface(
                shape = shape,
                variant = if (selected) DfGlassButtonVariant.Accent else DfGlassButtonVariant.Secondary,
                accent = DfColors.Purple,
                elevation = if (selected) 8.dp else 5.dp,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) DfColors.PurpleDark else DfColors.TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
fun DfGlassTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .liquidGlassSurface(shape = AppShapes.GlassSmall, elevation = 8.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DfGlassButton(text = "بازگشت", onClick = onBack, icon = null)
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            maxLines = 1,
        )
        if (action != null) {
            action()
        } else {
            Box(Modifier.padding(end = 8.dp))
        }
    }
}

@Composable
fun DfConfidenceRing(
    score: Int,
    label: String,
    hint: String?,
    modifier: Modifier = Modifier,
) {
    val progress = (score.coerceIn(0, 100)) / 100f
    DfGlassCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .height(88.dp)
                    .width(88.dp),
            ) {
                androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
                    val stroke = 10.dp.toPx()
                    drawArc(
                        color = DfColors.SurfaceVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = stroke,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        ),
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(DfColors.Purple, DfColors.Blue, DfColors.Purple)),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = stroke,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        ),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score%", fontWeight = FontWeight.Bold, color = DfColors.PurpleDark)
                    Text("امتیاز", style = MaterialTheme.typography.labelSmall, color = DfColors.TextMuted)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, fontWeight = FontWeight.SemiBold)
                hint?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = DfColors.TextSecondary)
                }
            }
        }
    }
}
