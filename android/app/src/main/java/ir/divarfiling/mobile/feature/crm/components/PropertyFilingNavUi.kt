package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

private val FilingNavShape = RoundedCornerShape(12.dp)
private val CompactCardShape = RoundedCornerShape(10.dp)

@Composable
fun PropertyFilingNavSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = FilingNavShape,
        color = DfColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DfColors.OutlineSubtle),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            content()
        }
    }
}

@Composable
fun PropertyFilingNavHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "سازمان‌دهی فایل‌ها",
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
        )
        Text(
            text = "کمد برای گروه‌بندی · زونکن برای فیلتر سریع",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun FilingNavDashedDivider(modifier: Modifier = Modifier) {
    val color = DfColors.OutlineSubtle.copy(alpha = 0.85f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
        )
    }
}

@Composable
fun FilingTierHeader(
    label: String,
    icon: ImageVector,
    showManage: Boolean,
    onManage: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DfColors.Purple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(12.dp),
                )
            }
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextSecondary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (showManage) {
                FilingNavIconButton(
                    icon = DfIcons.SlidersHorizontal,
                    contentDescription = "مدیریت",
                    onClick = onManage,
                )
            }
            FilingNavIconButton(
                icon = DfIcons.Plus,
                contentDescription = "جدید",
                onClick = onAdd,
                primary = true,
            )
        }
    }
}

@Composable
fun FilingNavIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    primary: Boolean = false,
) {
    val borderColor = if (primary) {
        DfColors.Purple.copy(alpha = 0.35f)
    } else {
        DfColors.OutlineSubtle
    }
    val background = if (primary) {
        DfColors.PurpleContainer.copy(alpha = 0.55f)
    } else {
        DfColors.Surface
    }
    val tint = if (primary) DfColors.Purple else DfColors.TextSecondary

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = background,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.size(30.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
fun ZonkanCompactFolderCard(
    name: String,
    meta: String,
    color: Color,
    icon: String,
    selected: Boolean,
    pinned: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) color.copy(alpha = 0.55f) else DfColors.OutlineSubtle
    val background = if (selected) color.copy(alpha = 0.08f) else DfColors.Surface

    Box(
        modifier = modifier
            .widthIn(min = 100.dp, max = 118.dp)
            .clip(CompactCardShape)
            .background(background)
            .border(1.dp, borderColor, CompactCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PropertyFolderIconBadge(
                faIcon = icon,
                color = color,
                size = 26.dp,
                iconSize = 13.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = name,
                        style = AppTypography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selected) color.copy(alpha = 0.92f) else DfColors.TextPrimary,
                    )
                    if (pinned) {
                        Icon(
                            DfIcons.Pin,
                            contentDescription = "سنجاق‌شده",
                            tint = color.copy(alpha = 0.75f),
                            modifier = Modifier.size(10.dp),
                        )
                    }
                }
                Text(
                    text = meta,
                    style = AppTypography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (selected) color.copy(alpha = 0.72f) else DfColors.TextMuted,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
fun ZonkanCompactCreateCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = DfColors.Purple
    Box(
        modifier = modifier
            .widthIn(min = 100.dp, max = 118.dp)
            .clip(CompactCardShape)
            .background(accent.copy(alpha = 0.06f))
            .border(1.dp, accent.copy(alpha = 0.28f), CompactCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(AppShapes.Chip)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    DfIcons.Plus,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(14.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = AppTypography.labelSmall,
                    color = accent.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
