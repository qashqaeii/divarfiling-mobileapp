package ir.divarfiling.mobile.feature.crm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.DfGlassChip
import ir.divarfiling.mobile.core.design.components.DfGlassIconButton
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
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
                            DfColors.Purple.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.94f),
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
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    onClick = onBack,
                )
                DfGlassIconButton(
                    icon = Icons.Default.Edit,
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
                        color = DfColors.TextPrimary,
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
                                tint = DfColors.TextMuted,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = phone,
                                style = AppTypography.bodyDescription,
                                color = DfColors.TextSecondary,
                            )
                        }
                    }
                    ContactMetaFlow(contact)
                }
            }

            contact.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Surface(shape = AppShapes.CardSmall, color = Color.White.copy(alpha = 0.6f)) {
                    Text(
                        text = notes,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
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
        if (CrmTypeProfiles.showsBudget(profile.moneyMode)) {
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
        color = DfColors.Surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
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
    primary: List<ContactQuickActionItem>,
    secondary: List<ContactQuickActionItem>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                "دسترسی سریع",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                primary.forEach { action ->
                    ContactQuickActionTile(action, emphasized = true, modifier = Modifier.weight(1f))
                }
            }
            if (secondary.isNotEmpty()) {
                HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    secondary.forEach { action ->
                        ContactQuickActionTile(action, emphasized = false, modifier = Modifier.width(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactQuickActionTile(
    action: ContactQuickActionItem,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .then(
                if (emphasized) Modifier.background(action.tint.copy(alpha = 0.07f)) else Modifier,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = action.onClick,
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(if (emphasized) 44.dp else 38.dp)
                .liquidGlassSurface(
                    shape = CircleShape,
                    variant = if (emphasized) DfGlassButtonVariant.Accent else DfGlassButtonVariant.Secondary,
                    accent = action.tint,
                    elevation = if (emphasized) 4.dp else 2.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                action.iconRes != null -> Icon(
                    painter = painterResource(action.iconRes),
                    contentDescription = action.label,
                    tint = action.tint,
                    modifier = Modifier.size(if (emphasized) 20.dp else 18.dp),
                )
                action.icon != null -> Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = action.tint,
                    modifier = Modifier.size(if (emphasized) 20.dp else 18.dp),
                )
            }
        }
        Text(
            text = action.label,
            style = AppTypography.labelSmall,
            color = if (emphasized) action.tint else DfColors.TextSecondary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

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
            color = DfColors.TextMuted,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CrmConstants.STATUSES.forEach { status ->
                DfGlassChip(
                    text = status,
                    selected = status == currentStatus,
                    onClick = { if (status != currentStatus) onStatusChange(status) },
                )
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(AppShapes.Chip)
                .background(DfColors.Purple),
        )
        Text(
            text = title,
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        count?.takeIf { it > 0 }?.let {
            Surface(shape = AppShapes.Chip, color = DfColors.PurpleContainer) {
                Text(
                    text = DateUtils.toPersianDigits(it.toString()),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun ContactReminderCard(
    reminder: ReminderDto,
    onComplete: () -> Unit,
    onPostpone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfPremiumCard(modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
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
                Text(reminder.title.orEmpty(), style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
                reminder.dueAt?.let { due ->
                    val label = DateUtils.formatJalaliDateTime(due)
                        ?: DateUtils.formatRelativeTimeUntil(due)
                        ?: due
                    Text(label, style = AppTypography.labelSmall, color = DfColors.Amber)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onComplete) { Text("انجام شد", color = DfColors.Green) }
                    TextButton(onClick = onPostpone) { Text("فردا", color = DfColors.TextMuted) }
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
    DfPremiumCard(onClick = onClick, modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(deal.title, style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
                deal.stage?.let { DfBadge(it) }
            }
            deal.amount?.let {
                Text(
                    FormatUtils.formatPriceToman(it),
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.Purple,
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
    DfPremiumCard(modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                listing.title ?: listing.token,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
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
                    TextButton(onClick = { onOpenLink(link) }) { Text("مشاهده") }
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
    DfPremiumCard(modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(AppShapes.IconContainer)
                    .background(DfColors.SurfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(DfIcons.File, contentDescription = null, tint = DfColors.TextSecondary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(document.title, style = AppTypography.cardTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                document.uploadedAt?.let {
                    Text(
                        DateUtils.formatForDisplay(it),
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }
            document.fileUrl?.takeIf { it.isNotBlank() }?.let { url ->
                TextButton(onClick = { onOpen(url) }) { Text("باز") }
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
