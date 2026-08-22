package ir.divarfiling.mobile.feature.crm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfMoreAction
import ir.divarfiling.mobile.core.design.components.DfMoreActionsSheet
import ir.divarfiling.mobile.core.design.components.DfGlassIconButton
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.CustomerDocumentDto
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.LinkedListingDto
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.feature.crm.CrmConstants
import ir.divarfiling.mobile.feature.crm.CrmTypeProfiles
import kotlin.math.absoluteValue

data class ContactQuickActionItem(
    val label: String,
    val tint: Color,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
    val onClick: () -> Unit,
)

@Composable
fun ContactDetailHero(
    contact: ContactDto,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = contactAccentColor(contact.fullName)
    val initials = contactInitials(contact.fullName)

    val surface = DfThemeColors.surface()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(AppShapes.Hero)
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.16f),
                            DfThemeColors.primary().copy(alpha = 0.06f),
                            surface.copy(alpha = 0.96f),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 16.dp)
                .size(110.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.1f)),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DfGlassIconButton(
                    icon = DfIcons.ChevronLeft,
                    contentDescription = "بازگشت",
                    onClick = onBack,
                )
                DfGlassIconButton(
                    icon = DfIcons.Pencil,
                    contentDescription = "ویرایش",
                    onClick = onEdit,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.6f)))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = contact.fullName,
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    contact.phone?.let { phone ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = DfIcons.Phone,
                                contentDescription = null,
                                tint = DfThemeColors.textMuted(),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = phone,
                                style = AppTypography.bodyDescription,
                                color = DfThemeColors.textSecondary(),
                            )
                        }
                    }
                    ContactMetaFlow(contact)
                }
            }

            contact.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Surface(
                    shape = AppShapes.CardSmall,
                    color = DfThemeColors.surfaceVariant().copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
                ) {
                    Text(
                        text = notes,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContactMetaFlow(contact: ContactDto) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        contact.customerType?.takeIf { it.isNotBlank() }?.let {
            ContactMetaPill(it, DfColors.Purple, DfColors.PurpleContainer)
        }
        contact.status?.takeIf { it.isNotBlank() }?.let { status ->
            val colors = contactStatusColors(status)
            ContactMetaPill(status, colors.second, colors.third)
        }
        contact.priority?.takeIf { it.isNotBlank() }?.let {
            ContactMetaPill(it, DfColors.Amber, DfColors.AmberLight)
        }
        contact.source?.takeIf { it.isNotBlank() }?.let {
            ContactMetaPill(it, DfColors.Blue, DfColors.BlueLight)
        }
    }
}

@Composable
private fun ContactMetaPill(text: String, color: Color, background: Color) {
    Surface(shape = AppShapes.Chip, color = background.copy(alpha = 0.92f)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
fun ContactDetailInsightStrip(
    contact: ContactDto,
    modifier: Modifier = Modifier,
) {
    val followUp = contact.nextFollowUpAt?.let { due ->
        DateUtils.formatRelativeTimeUntil(due) ?: DateUtils.formatJalaliDateTime(due)
    }
    val profile = CrmTypeProfiles.profileFor(contact.customerType)
    val financialTiles = buildList {
        if (contact.isBuilder || contact.customerType == "سازنده") {
            formatMoneyRange(contact.budgetMin, contact.budgetMax)?.let { range ->
                add(FinancialInsightTile("فروش آپارتمان", range, DfColors.Green))
            }
            formatMoneyRange(contact.builderBuyBudgetMin, contact.builderBuyBudgetMax)?.let { range ->
                add(FinancialInsightTile("خرید پروژه", range, DfColors.Blue))
            }
        } else if (CrmTypeProfiles.showsBudget(profile.moneyMode)) {
            formatMoneyRange(contact.budgetMin, contact.budgetMax)?.let { range ->
                add(FinancialInsightTile(profile.budgetLabels.first.substringBefore(' '), range, DfColors.Green))
            }
        }
        if (CrmTypeProfiles.showsRent(profile.moneyMode)) {
            formatMoneyRange(contact.depositMin, contact.depositMax)?.let { range ->
                add(FinancialInsightTile("رهن", range, DfColors.Blue))
            }
            formatMoneyRange(contact.rentMin, contact.rentMax)?.let { range ->
                add(FinancialInsightTile("اجاره", range, DfColors.Amber))
            }
        }
    }
    if (financialTiles.isEmpty() && followUp.isNullOrBlank()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        financialTiles.forEach { tile ->
            ContactInsightTile(
                label = tile.label,
                value = tile.value,
                accent = tile.accent,
                modifier = Modifier.width(148.dp),
            )
        }
        if (!followUp.isNullOrBlank()) {
            ContactInsightTile(
                label = "پیگیری",
                value = followUp,
                accent = DfColors.Amber,
                modifier = Modifier.width(148.dp),
            )
        }
    }
}

private data class FinancialInsightTile(
    val label: String,
    val value: String,
    val accent: Color,
)

private fun formatMoneyRange(min: Long?, max: Long?): String? {
    val lo = min?.takeIf { it > 0 }
    val hi = max?.takeIf { it > 0 }
    return when {
        lo != null && hi != null -> "${FormatUtils.formatPriceShort(lo)} — ${FormatUtils.formatPriceShort(hi)}"
        lo != null -> "از ${FormatUtils.formatPriceShort(lo)}"
        hi != null -> "تا ${FormatUtils.formatPriceShort(hi)}"
        else -> null
    }
}

@Composable
private fun ContactInsightTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            Text(
                value,
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ContactDetailQuickActionsPanel(
    visibleActions: List<ContactQuickActionItem>,
    moreActions: List<ContactQuickActionItem>,
    modifier: Modifier = Modifier,
) {
    if (visibleActions.isEmpty() && moreActions.isEmpty()) return

    var showMore by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = "اقدامات سریع",
            style = AppTypography.labelSmall,
            color = DfThemeColors.textMuted(),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 2.dp),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card,
            color = DfThemeColors.surface(),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            shadowElevation = AppElevations.subtle,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top,
            ) {
                visibleActions.forEach { action ->
                    ContactQuickActionButton(
                        action = action,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (moreActions.isNotEmpty()) {
                    ContactQuickActionButton(
                        action = ContactQuickActionItem(
                            label = "بیشتر",
                            tint = DfThemeColors.textSecondary(),
                            icon = DfIcons.MoreVertical,
                            onClick = { showMore = true },
                        ),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    if (moreActions.isNotEmpty()) {
        DfMoreActionsSheet(
            visible = showMore,
            onDismiss = { showMore = false },
            actions = moreActions.map { action ->
                DfMoreAction(label = action.label, onClick = action.onClick, icon = action.icon)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactQuickActionButton(
    action: ContactQuickActionItem,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.padding(horizontal = 2.dp),
    ) {
        Surface(
            onClick = action.onClick,
            shape = CircleShape,
            color = action.tint.copy(alpha = 0.12f),
            shadowElevation = 0.dp,
            modifier = Modifier.size(48.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ContactQuickActionIcon(action = action, size = 22.dp)
            }
        }
        Text(
            text = action.label,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DfThemeColors.textPrimary(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ContactQuickActionIcon(
    action: ContactQuickActionItem,
    size: androidx.compose.ui.unit.Dp,
) {
    when {
        action.iconRes != null -> Icon(
            painter = painterResource(action.iconRes),
            contentDescription = action.label,
            tint = action.tint,
            modifier = Modifier.size(size),
        )
        action.icon != null -> Icon(
            imageVector = action.icon,
            contentDescription = action.label,
            tint = action.tint,
            modifier = Modifier.size(size),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailStatusBar(
    currentStatus: String?,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            "مرحله پیگیری",
            style = AppTypography.labelSmall,
            color = DfThemeColors.textMuted(),
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CrmConstants.STATUSES.forEach { status ->
                val selected = status == currentStatus
                Surface(
                    onClick = { if (!selected) onStatusChange(status) },
                    shape = AppShapes.Chip,
                    color = if (selected) DfThemeColors.primary() else DfThemeColors.surface(),
                    border = BorderStroke(
                        1.dp,
                        if (selected) DfThemeColors.primary() else DfThemeColors.outlineSubtle(),
                    ),
                    shadowElevation = AppElevations.none,
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = AppTypography.labelSmall,
                        color = if (selected) Color.White else DfThemeColors.textSecondary(),
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun ContactDetailSectionHeader(
    title: String,
    count: Int? = null,
    modifier: Modifier = Modifier,
) {
    DfSectionHeader(
        title = title,
        count = count,
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
    )
}

@Composable
fun ContactReminderCard(
    reminder: ReminderDto,
    onComplete: () -> Unit,
    onPostpone: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
        containerColor = DfThemeColors.surface(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DfColors.RoseLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = DfIcons.AlarmClock,
                    contentDescription = null,
                    tint = DfColors.OverdueAccent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    reminder.title.orEmpty(),
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.textPrimary(),
                )
                reminder.dueAt?.let { due ->
                    val label = DateUtils.formatJalaliDateTime(due)
                        ?: DateUtils.formatRelativeTimeUntil(due)
                        ?: due
                    Text(label, style = AppTypography.labelSmall, color = DfColors.Amber)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onComplete) { Text("انجام شد", color = DfColors.Green) }
                    TextButton(onClick = onPostpone) { Text("فردا", color = DfThemeColors.textMuted()) }
                    TextButton(onClick = onEdit) { Text("ویرایش", color = DfColors.Purple) }
                }
            }
        }
    }
}

@Composable
fun ContactDealCard(
    deal: DealDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(
        onClick = onClick,
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
        containerColor = DfThemeColors.surface(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    deal.title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.textPrimary(),
                )
                deal.stage?.let { DfBadge(it) }
            }
            deal.amount?.let {
                Text(
                    FormatUtils.formatPriceToman(it),
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.primary(),
                )
            }
        }
    }
}

@Composable
fun ContactLinkedListingCard(
    listing: LinkedListingDto,
    onShareWhatsApp: () -> Unit,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
        containerColor = DfThemeColors.surface(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                listing.title ?: listing.token,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listing.price?.let {
                    Text(it, style = AppTypography.bodyDescription, color = DfColors.Green, fontWeight = FontWeight.Medium)
                }
                listing.role?.let { DfBadge(it) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onShareWhatsApp) {
                    Icon(
                        painter = painterResource(R.drawable.ic_whatsapp),
                        contentDescription = null,
                        tint = DfColors.Green,
                        modifier = Modifier.size(18.dp),
                    )
                    Text("واتساپ", color = DfColors.Green)
                }
                listing.link?.takeIf { it.isNotBlank() }?.let { link ->
                    TextButton(onClick = { onOpenLink(link) }) {
                        Text("مشاهده", color = DfThemeColors.textSecondary())
                    }
                }
            }
        }
    }
}

@Composable
fun ContactDocumentCard(
    document: CustomerDocumentDto,
    onOpen: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
        containerColor = DfThemeColors.surface(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(AppShapes.IconContainer)
                    .background(DfThemeColors.surfaceVariant()),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    DfIcons.Paperclip,
                    contentDescription = null,
                    tint = DfThemeColors.textSecondary(),
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    document.title,
                    style = AppTypography.cardTitle,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                document.uploadedAt?.let {
                    Text(
                        DateUtils.formatForDisplay(it),
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                }
            }
            document.fileUrl?.takeIf { it.isNotBlank() }?.let { url ->
                TextButton(onClick = { onOpen(url) }) {
                    Text("باز", color = DfThemeColors.textSecondary())
                }
            }
            TextButton(onClick = onDelete) { Text("حذف", color = DfColors.OverdueAccent) }
        }
    }
}

fun contactInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1)
        else -> "${parts[0].take(1)}${parts[1].take(1)}"
    }
}

fun contactAccentColor(name: String): Color {
    val palette = listOf(DfColors.Purple, DfColors.Blue, DfColors.Green, DfColors.Amber, DfColors.Rose)
    return palette[name.hashCode().absoluteValue % palette.size]
}

fun contactStatusColors(status: String): Triple<Color, Color, Color> = when {
    status.contains("پیگیری") -> Triple(DfColors.Amber, DfColors.Amber, DfColors.AmberLight)
    status.contains("بازدید") -> Triple(DfColors.Green, DfColors.Green, DfColors.GreenLight)
    status == "جدید" -> Triple(DfColors.Blue, DfColors.Blue, DfColors.BlueLight)
    status.contains("قرارداد") -> Triple(DfColors.Purple, DfColors.Purple, DfColors.PurpleContainer)
    else -> Triple(DfColors.TextMuted, DfColors.TextSecondary, DfColors.SurfaceVariant)
}
