package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.feature.crm.CrmConstants
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactListCard(
    contact: ContactDto,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onSuggestClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val accent = contactAccentColor(contact.fullName)
    val statusStyle = statusColors(contact.status.orEmpty())
    val followUpLabel = contact.nextFollowUpAt?.let { next ->
        DateUtils.formatRelativeTimeUntil(next)
            ?: DateUtils.formatJalaliDateTime(next)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(statusStyle.first, statusStyle.first.copy(alpha = 0.35f)),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContactAvatar(name = contact.fullName, accent = accent)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = contact.fullName,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        contact.phone?.let { phone ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = DfIcons.Phone,
                                    contentDescription = null,
                                    tint = DfColors.TextMuted,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = phone,
                                    style = AppTypography.labelSmall,
                                    color = DfColors.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    ContactStatusBadge(status = contact.status)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    contact.customerType?.takeIf { it.isNotBlank() }?.let { type ->
                        ContactMetaChip(text = type, color = DfColors.Purple, background = DfColors.PurpleContainer)
                    }
                    contact.priority?.takeIf { it.isNotBlank() }?.let { priority ->
                        ContactMetaChip(text = priority, color = DfColors.Amber, background = DfColors.AmberLight)
                    }
                    contact.budget?.let { budget ->
                        ContactMetaChip(
                            text = FormatUtils.formatPriceShort(budget),
                            color = DfColors.Green,
                            background = DfColors.GreenLight,
                        )
                    }
                    contact.source?.takeIf { it.isNotBlank() }?.let { source ->
                        ContactMetaChip(text = source, color = DfColors.Blue, background = DfColors.BlueLight)
                    }
                }

                HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.12f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        followUpLabel?.let { label ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = DfIcons.Clock,
                                    contentDescription = null,
                                    tint = DfColors.Amber,
                                    modifier = Modifier.size(12.dp),
                                )
                                Text(
                                    text = "پیگیری: $label",
                                    style = AppTypography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = DfColors.Amber,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        ContactUpdatedLabel(updatedAt = contact.updatedAt)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (CrmConstants.isMatchEligible(contact.customerType) && onSuggestClick != null) {
                            ContactQuickAction(
                                icon = DfIcons.WandSparkles,
                                tint = DfColors.Green,
                                contentDescription = "پیشنهاد ملک",
                                onClick = onSuggestClick,
                            )
                        }
                        ContactQuickAction(
                            icon = DfIcons.Phone,
                            tint = DfColors.Purple,
                            contentDescription = "تماس",
                            onClick = onCallClick,
                        )
                        ContactQuickAction(
                            icon = DfIcons.MessageCircle,
                            tint = DfColors.Green,
                            contentDescription = "واتساپ",
                            onClick = onWhatsAppClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactAvatar(
    name: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(accent, accent.copy(alpha = 0.72f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = contactInitials(name),
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
private fun ContactUpdatedLabel(updatedAt: String?) {
    val (jalaliDate, jalaliTime) = ContactsFilters.splitUpdatedAt(updatedAt)
    if (jalaliDate == "—") return
    val relative = ContactsFilters.relativeUpdatedLabel(updatedAt)
    val display = when {
        relative != null && jalaliTime.isNotBlank() -> "$relative · $jalaliTime"
        relative != null -> relative
        jalaliTime.isNotBlank() -> "$jalaliDate · $jalaliTime"
        else -> jalaliDate
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = DfIcons.Calendar,
            contentDescription = null,
            tint = DfColors.TextMuted,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = "آخرین فعالیت: $display",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ContactMetaChip(
    text: String,
    color: Color,
    background: Color,
) {
    Surface(shape = AppShapes.Chip, color = background.copy(alpha = 0.85f)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ContactQuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .liquidGlassSurface(
                shape = CircleShape,
                variant = DfGlassButtonVariant.Secondary,
                elevation = 2.dp,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(17.dp),
        )
    }
}

@Composable
private fun ContactStatusBadge(status: String?) {
    if (status.isNullOrBlank()) return
    val (dotColor, textColor, bgColor) = statusColors(status)
    Surface(shape = AppShapes.Chip, color = bgColor) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Text(
                text = status,
                style = AppTypography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun contactInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1)
        else -> "${parts[0].take(1)}${parts[1].take(1)}"
    }
}

private fun contactAccentColor(name: String): Color {
    val palette = listOf(
        DfColors.Purple,
        DfColors.Blue,
        DfColors.Green,
        DfColors.Amber,
        DfColors.Rose,
    )
    val index = name.hashCode().absoluteValue % palette.size
    return palette[index]
}

private fun statusColors(status: String): Triple<Color, Color, Color> = when {
    status.contains("پیگیری") -> Triple(DfColors.Amber, DfColors.Amber, DfColors.AmberLight)
    status.contains("بازدید") -> Triple(DfColors.Green, DfColors.Green, DfColors.GreenLight)
    status == "جدید" -> Triple(DfColors.Blue, DfColors.Blue, DfColors.BlueLight)
    status.contains("قرارداد") -> Triple(DfColors.Purple, DfColors.Purple, DfColors.PurpleContainer)
    else -> Triple(DfColors.TextMuted, DfColors.TextSecondary, DfColors.SurfaceVariant)
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ContactListCardPreview() {
    DivarFilingTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ContactListCard(
                contact = ContactDto(
                    id = 1,
                    fullName = "علی رضایی",
                    phone = "۰۹۱۲۱۲۳۴۵۶۷",
                    customerType = "خریدار",
                    status = "در حال پیگیری",
                    priority = "بالا",
                    budget = 5_000_000_000,
                    source = "دیوار",
                    nextFollowUpAt = "2026-07-02T10:00:00Z",
                    updatedAt = "2026-06-28T10:00:00Z",
                ),
                onClick = {},
                onCallClick = {},
                onWhatsAppClick = {},
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
            ContactListCard(
                contact = ContactDto(
                    id = 2,
                    fullName = "مریم احمدی",
                    phone = "۰۹۳۵۱۱۱۲۲۳۳",
                    customerType = "فروشنده",
                    status = "جدید",
                    priority = "متوسط",
                    updatedAt = "2026-07-01T08:30:00Z",
                ),
                onClick = {},
                onCallClick = {},
                onWhatsAppClick = {},
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }
    }
}
