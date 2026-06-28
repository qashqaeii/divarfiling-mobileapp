package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.network.TodayItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTaskCard(
    item: TodayItemDto,
    isOverdue: Boolean,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onViewContact: () -> Unit,
    onComplete: () -> Unit,
    onPostpone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    val contactName = item.contact?.fullName ?: item.reminder?.title ?: "—"
    val phone = item.contact?.phone
    val reminderType = item.type?.takeIf { it.isNotBlank() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (isOverdue) DfColors.OverdueAccent else DfColors.Purple),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    if (isOverdue) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = DfIcons.TriangleAlert,
                                contentDescription = null,
                                tint = DfColors.OverdueAccent,
                                modifier = Modifier.size(16.dp),
                            )
                            Surface(shape = AppShapes.Chip, color = DfColors.RoseLight) {
                                Text(
                                    text = "معوق",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = AppTypography.labelSmall,
                                    color = DfColors.OverdueAccent,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = contactName,
                                style = AppTypography.cardTitle,
                                fontWeight = FontWeight.Bold,
                                color = DfColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            phone?.let {
                                Text(
                                    text = it,
                                    style = AppTypography.labelSmall,
                                    color = DfColors.TextMuted,
                                    maxLines = 1,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                reminderType?.let { type ->
                                    Surface(shape = AppShapes.Chip, color = DfColors.SurfaceVariant) {
                                        Text(
                                            text = type,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = AppTypography.labelSmall,
                                            color = DfColors.TextSecondary,
                                        )
                                    }
                                }
                                if (isOverdue) {
                                    Surface(shape = AppShapes.Chip, color = DfColors.RoseLight) {
                                        Text(
                                            text = "معوق",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = AppTypography.labelSmall,
                                            color = DfColors.OverdueAccent,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DfColors.PurpleContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = DfIcons.User,
                                contentDescription = null,
                                tint = DfColors.Purple,
                                modifier = Modifier.size(18.dp),
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
                        onClick = onCall,
                        modifier = Modifier.weight(1f),
                    )
                    TodayActionButton(
                        label = "واتساپ",
                        icon = DfIcons.MessageCircle,
                        tint = DfColors.Green,
                        onClick = onWhatsApp,
                        modifier = Modifier.weight(1f),
                    )
                    TodayActionButton(
                        label = "مخاطب",
                        icon = DfIcons.UserPlus,
                        tint = DfColors.Purple,
                        onClick = onViewContact,
                        modifier = Modifier.weight(1f),
                    )
                    TodayActionButton(
                        label = "انجام شد",
                        icon = DfIcons.CircleCheck,
                        tint = DfColors.Blue,
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                    )
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = DfIcons.MoreVertical,
                                contentDescription = "بیشتر",
                                tint = DfColors.TextMuted,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("تعویق") },
                                onClick = {
                                    showMenu = false
                                    onPostpone()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Chip,
        color = DfColors.Surface,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = tint,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}
