package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.feature.crm.ContactTypeVisuals
import ir.divarfiling.mobile.feature.crm.CrmConstants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactListCard(
    contact: ContactDto,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onSmsClick: (() -> Unit)? = null,
    onSuggestClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val typeVisual = ContactTypeVisuals.visualFor(contact.customerType)
    val statusStyle = statusColors(contact.status.orEmpty())
    val followUpLabel = contact.nextFollowUpAt?.let { next ->
        DateUtils.formatRelativeTimeUntil(next)
            ?: DateUtils.formatJalaliDateTime(next)
    }
    val canSuggest = CrmConstants.isMatchEligible(contact.customerType) && onSuggestClick != null

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(statusStyle.first, statusStyle.first.copy(alpha = 0.28f)),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContactTypeIcon(
                        icon = typeVisual.icon,
                        accent = typeVisual.accent,
                        container = typeVisual.container,
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = contact.fullName,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfThemeColors.textPrimary(),
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
                                    tint = DfThemeColors.textMuted(),
                                    modifier = Modifier.size(13.dp),
                                )
                                Text(
                                    text = phone,
                                    style = AppTypography.labelSmall,
                                    color = DfThemeColors.textSecondary(),
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
                        ContactMetaChip(
                            text = type,
                            color = typeVisual.accent,
                            background = typeVisual.container,
                        )
                    }
                    contact.budget?.let { budget ->
                        ContactMetaChip(
                            text = FormatUtils.formatPriceShort(budget),
                            color = DfThemeColors.textSecondary(),
                            background = DfThemeColors.surfaceVariant(),
                        )
                    }
                    contact.source?.takeIf { it.isNotBlank() }?.let { source ->
                        ContactMetaChip(
                            text = source,
                            color = DfThemeColors.textSecondary(),
                            background = DfThemeColors.surfaceVariant(),
                        )
                    }
                }

                HorizontalDivider(color = DfThemeColors.outlineSubtle())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ContactActionButton(
                        label = "تماس",
                        icon = DfIcons.Phone,
                        tint = DfThemeColors.primary(),
                        container = DfThemeColors.primaryContainer(),
                        onClick = onCallClick,
                        modifier = Modifier.weight(1f),
                    )
                    if (onSmsClick != null) {
                        ContactActionButton(
                            label = "پیامک",
                            icon = DfIcons.MessageSquare,
                            tint = DfThemeColors.info(),
                            container = DfThemeColors.infoContainer(),
                            onClick = onSmsClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ContactActionButton(
                        label = "واتساپ",
                        icon = DfIcons.MessageCircle,
                        tint = DfThemeColors.success(),
                        container = DfThemeColors.successContainer(),
                        onClick = onWhatsAppClick,
                        modifier = Modifier.weight(1f),
                    )
                    if (canSuggest && onSuggestClick != null) {
                        ContactActionButton(
                            label = "پیشنهاد",
                            icon = DfIcons.WandSparkles,
                            tint = DfThemeColors.warning(),
                            container = DfThemeColors.warningContainer(),
                            onClick = onSuggestClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    followUpLabel?.let { label ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = DfIcons.Clock,
                                contentDescription = null,
                                tint = DfThemeColors.warning(),
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = "پیگیری: $label",
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = DfThemeColors.warning(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    ContactUpdatedLabel(updatedAt = contact.updatedAt)
                }
            }
        }
    }
}

@Composable
private fun ContactTypeIcon(
    icon: ImageVector,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(AppShapes.CardSmall)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(22.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactActionButton(
    label: String,
    icon: ImageVector,
    tint: Color,
    container: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = AppShapes.CardSmall,
        color = container,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.16f)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
            tint = DfThemeColors.textMuted(),
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = "آخرین فعالیت: $display",
            style = AppTypography.labelSmall,
            color = DfThemeColors.textMuted(),
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
    Surface(shape = AppShapes.Chip, color = background.copy(alpha = 0.9f)) {
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
                onSmsClick = {},
                onSuggestClick = {},
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
            ContactListCard(
                contact = ContactDto(
                    id = 2,
                    fullName = "مریم احمدی",
                    phone = "۰۹۳۵۱۱۱۲۲۳۳",
                    customerType = "مالک",
                    status = "جدید",
                    priority = "متوسط",
                    updatedAt = "2026-07-01T08:30:00Z",
                ),
                onClick = {},
                onCallClick = {},
                onWhatsAppClick = {},
                onSmsClick = {},
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }
    }
}
