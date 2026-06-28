package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfColors

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
    val base = Modifier
        .shadow(AppElevations.floating + 6.dp, shape, ambientColor = DfColors.GlassShadow, spotColor = DfColors.GlassShadow)
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    DfColors.GlassHighlight.copy(alpha = 0.82f),
                    DfColors.GlassOverlay.copy(alpha = 0.58f),
                ),
            ),
        )
        .border(1.dp, DfColors.GlassBorder, shape)
    Box(
        modifier = modifier
            .then(base)
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
) {
    val shape = AppShapes.GlassSmall
    val bg = if (selected) {
        Brush.horizontalGradient(listOf(accent.copy(alpha = 0.35f), accent.copy(alpha = 0.18f)))
    } else {
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.45f)),
        )
    }
    Row(
        modifier = modifier
            .shadow(if (selected) 10.dp else 6.dp, shape, ambientColor = accent.copy(alpha = 0.2f))
            .clip(shape)
            .background(bg)
            .border(1.dp, if (selected) accent.copy(alpha = 0.45f) else DfColors.GlassBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = if (selected) accent else DfColors.TextSecondary)
        }
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) accent else DfColors.TextPrimary,
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
            .clip(shape)
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(DfColors.Purple.copy(alpha = 0.28f), DfColors.Blue.copy(alpha = 0.22f)),
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.4f)),
                    )
                },
            )
            .border(1.dp, if (selected) DfColors.Purple.copy(alpha = 0.4f) else DfColors.GlassBorder, shape)
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
            .clip(AppShapes.GlassSmall)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.88f), Color.White.copy(alpha = 0.62f)),
                ),
            )
            .border(1.dp, DfColors.GlassBorder, AppShapes.GlassSmall)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        DfGlassButton(text = "بازگشت", onClick = onBack, icon = null)
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
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
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(DfColors.Purple, DfColors.Blue, DfColors.Purple)),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
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
