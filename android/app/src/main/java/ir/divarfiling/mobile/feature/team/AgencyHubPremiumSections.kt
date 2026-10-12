package ir.divarfiling.mobile.feature.team

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.PresentationLabels
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.network.AgencyAttentionDto
import ir.divarfiling.mobile.core.network.AgencySeatsSummaryDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyHubSeatsSummaryCard(
    seats: AgencySeatsSummaryDto,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (seats.total <= 0) return
    val primary = DfThemeColors.primary()
    val assigned = seats.assigned.coerceAtLeast(0)
    val total = seats.total.coerceAtLeast(1)
    val available = seats.available.coerceAtLeast(0)
    val fill = (assigned.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val expiry = formatAgencySeatExpiry(seats.expiresAt)

    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(primary.copy(alpha = 0.1f), Color.Transparent),
                    ),
                )
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(AppShapes.IconContainer)
                            .background(primary.copy(alpha = 0.14f))
                            .border(BorderStroke(1.dp, primary.copy(alpha = 0.22f)), AppShapes.IconContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(DfIcons.Lock, contentDescription = null, tint = primary, modifier = Modifier.size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("خلاصه صندلی‌ها", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                        Text(
                            "لایسنس فعال آژانس",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
                DfBadge(
                    text = "${DateUtils.toPersianDigits(assigned.toString())}/${DateUtils.toPersianDigits(total.toString())}",
                    color = DfThemeColors.primaryContainer(),
                    textColor = primary,
                )
            }
            LinearProgressIndicator(
                progress = { fill },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(AppShapes.CardSmall),
                color = primary,
                trackColor = DfThemeColors.surfaceVariant(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                AgencyHubSeatStatChip(
                    label = "تخصیص",
                    value = assigned,
                    tint = DfThemeColors.success(),
                    modifier = Modifier.weight(1f),
                )
                AgencyHubSeatStatChip(
                    label = "آزاد",
                    value = available,
                    tint = DfThemeColors.info(),
                    modifier = Modifier.weight(1f),
                )
                AgencyHubSeatStatChip(
                    label = "کل",
                    value = total,
                    tint = primary,
                    modifier = Modifier.weight(1f),
                )
            }
            if (!expiry.isNullOrBlank()) {
                Text(expiry, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
            }
            if (onClick != null) {
                Text(
                    "مدیریت صندلی‌ها",
                    style = AppTypography.labelSmall,
                    color = primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

@Composable
private fun AgencyHubSeatStatChip(
    label: String,
    value: Int,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(tint.copy(alpha = 0.08f))
            .padding(vertical = AppSpacing.xs, horizontal = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            DateUtils.toPersianDigits(value.toString()),
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = tint,
        )
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
    }
}

@Composable
private fun attentionVisual(kind: String, level: String): Pair<ImageVector, Color> {
    val warning = level == "warning" || level == "danger"
    val tint = if (warning) DfThemeColors.warning() else DfThemeColors.info()
    val icon = when (kind) {
        "pending_invites" -> DfIcons.UserPlus
        "members_without_seat" -> DfIcons.Lock
        "inbox_leads" -> DfIcons.ListTodo
        else -> DfIcons.Bell
    }
    return icon to tint
}

@Composable
fun AgencyHubAttentionPanel(
    items: List<AgencyAttentionDto>,
    onItemClick: (AgencyAttentionDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgencySectionHeader(
                    title = "نیازمند توجه",
                    subtitle = "اقدام‌های پیشنهادی برای امروز",
                    icon = DfIcons.Sparkles,
                    accent = AgencyHeaderAccent.Management,
                )
            }
            items.forEach { item ->
                val (icon, tint) = attentionVisual(item.kind, item.level)
                Surface(
                    onClick = { onItemClick(item) },
                    shape = AppShapes.CardSmall,
                    color = tint.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, tint.copy(alpha = 0.18f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.sm),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(AppShapes.IconContainer)
                                .background(tint.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                PresentationLabels.attentionTitle(item.title),
                                style = AppTypography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val subtitle = PresentationLabels.attentionSubtitle(item.subtitle)
                            if (subtitle.isNotBlank()) {
                                Text(
                                    subtitle,
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.textMuted(),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (item.count > 0) {
                            DfBadge(
                                text = DateUtils.toPersianDigits(item.count.toString()),
                                color = tint.copy(alpha = 0.14f),
                                textColor = tint,
                            )
                        }
                        Icon(
                            DfIcons.ChevronLeft,
                            contentDescription = null,
                            tint = DfThemeColors.textMuted(),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyHubQuickActionsRow(
    actions: List<TeamQuickAction>,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        AgencySectionHeader(
            title = "اقدام سریع",
            subtitle = "کارهای پرتکرار مدیر",
            icon = DfIcons.Sparkles,
            accent = AgencyHeaderAccent.Conversation,
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items(actions, key = { it.label }) { action ->
                Surface(
                    onClick = action.onClick,
                    shape = AppShapes.Card,
                    color = DfThemeColors.surface(),
                    border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
                    shadowElevation = AppElevations.subtle,
                    modifier = Modifier.width(132.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(AppShapes.IconContainer)
                                .background(DfThemeColors.primaryContainer()),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                action.icon,
                                contentDescription = null,
                                tint = DfThemeColors.primary(),
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Text(
                            action.label,
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgencyHubExpandableSection(
    title: String,
    subtitle: String?,
    badge: String?,
    initiallyExpanded: Boolean,
    modifier: Modifier = Modifier,
    headerAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "hubExpand")

    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            subtitle,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                badge?.let {
                    DfBadge(
                        text = it,
                        color = DfThemeColors.primaryContainer(),
                        textColor = DfThemeColors.primary(),
                    )
                }
                headerAction?.invoke()
                Icon(
                    DfIcons.ChevronDown,
                    contentDescription = if (expanded) "بستن" else "باز کردن",
                    tint = DfThemeColors.textMuted(),
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotation),
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md)
                        .padding(bottom = AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    content()
                }
            }
        }
    }
}
