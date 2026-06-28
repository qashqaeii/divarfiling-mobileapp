package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
import ir.divarfiling.mobile.core.network.TodayItemDto
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTaskCard(
    item: TodayItemDto,
    isOverdue: Boolean,
    isActionRunning: Boolean,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onViewContact: () -> Unit,
    onComplete: () -> Unit,
    onPostpone: (days: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val contactName = TodayTaskLabels.contactName(item)
    val taskTitle = TodayTaskLabels.titleLabel(item)
    val typeLabel = TodayTaskLabels.typeLabel(item.type)
    val dueLabel = TodayTaskLabels.dueLabel(item)
    val phone = item.contact?.phone
    val accent = taskAccentColor(contactName)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(if (isOverdue) DfColors.OverdueAccent else DfColors.Purple),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.7f)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = taskInitials(contactName),
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = contactName,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (taskTitle != contactName) {
                            Text(
                                text = taskTitle,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        phone?.let {
                            Text(
                                text = it,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextMuted,
                                maxLines = 1,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TaskTypeBadge(label = typeLabel, isOverdue = isOverdue)
                        dueLabel?.let {
                            Text(
                                text = it,
                                style = AppTypography.labelSmall,
                                color = if (isOverdue) DfColors.OverdueAccent else DfColors.Purple,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TodayActionButton(
                        label = "تماس",
                        icon = DfIcons.Phone,
                        tint = DfColors.Purple,
                        enabled = !isActionRunning,
                        onClick = onCall,
                        modifier = Modifier.weight(1f),
                    )
                    TodayActionButton(
                        label = "واتساپ",
                        icon = DfIcons.MessageCircle,
                        tint = DfColors.Green,
                        enabled = !isActionRunning,
                        onClick = onWhatsApp,
                        modifier = Modifier.weight(1f),
                    )
                    TodayActionButton(
                        label = "انجام",
                        icon = DfIcons.CircleCheck,
                        tint = DfColors.Blue,
                        enabled = !isActionRunning,
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                    )
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            enabled = !isActionRunning,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                imageVector = DfIcons.MoreVertical,
                                contentDescription = "بیشتر",
                                tint = DfColors.TextMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("تعویق ۱ روز") },
                                onClick = { showMenu = false; onPostpone(1) },
                            )
                            DropdownMenuItem(
                                text = { Text("تعویق ۳ روز") },
                                onClick = { showMenu = false; onPostpone(3) },
                            )
                            DropdownMenuItem(
                                text = { Text("تعویق ۱ هفته") },
                                onClick = { showMenu = false; onPostpone(7) },
                            )
                            DropdownMenuItem(
                                text = { Text("مشاهده مخاطب") },
                                onClick = { showMenu = false; onViewContact() },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskTypeBadge(label: String, isOverdue: Boolean) {
    val bg = when {
        isOverdue -> DfColors.RoseLight
        label == "یادآور" -> DfColors.BlueLight
        label == "پیگیری" -> DfColors.AmberLight
        else -> DfColors.PurpleContainer
    }
    val fg = when {
        isOverdue -> DfColors.OverdueAccent
        label == "یادآور" -> DfColors.Blue
        label == "پیگیری" -> DfColors.Amber
        else -> DfColors.Purple
    }
    Surface(shape = AppShapes.Chip, color = bg) {
        Text(
            text = if (isOverdue) "معوق · $label" else label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = fg,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun TodayActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .liquidGlassSurface(
                shape = AppShapes.Chip,
                variant = DfGlassButtonVariant.Accent,
                accent = tint,
                elevation = 4.dp,
                enabled = enabled,
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = tint,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

private fun taskInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1)
        else -> "${parts[0].take(1)}${parts[1].take(1)}"
    }
}

private fun taskAccentColor(name: String): Color {
    val palette = listOf(DfColors.Purple, DfColors.Blue, DfColors.Green, DfColors.Amber, DfColors.Rose)
    return palette[name.hashCode().absoluteValue % palette.size]
}
