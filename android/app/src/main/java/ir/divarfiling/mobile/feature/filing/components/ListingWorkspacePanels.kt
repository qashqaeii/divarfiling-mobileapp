package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.filing.ListingOccupancyContacts
import ir.divarfiling.mobile.core.filing.ListingOccupancyPerson
import ir.divarfiling.mobile.core.network.ListingLinkedContactDto
import ir.divarfiling.mobile.core.network.ListingMetaDto
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

@Composable
fun ListingNotesSection(
    meta: ListingMetaDto?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val note = meta?.note.orEmpty()
    val tag = meta?.tag.orEmpty()
    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(DfIcons.StickyNote, contentDescription = null, tint = DfColors.Amber, modifier = Modifier.size(18.dp))
                    Text("یادداشت آگهی", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold, color = DfThemeColors.textPrimary())
                }
                TextButton(onClick = onEdit) {
                    Text(if (note.isBlank() && tag.isBlank()) "افزودن" else "ویرایش")
                }
            }
            if (tag.isNotBlank()) {
                DfBadge(text = tag)
            }
            if (note.isBlank()) {
                Text(
                    text = "یادداشت داخلی این آگهی فقط برای شماست؛ مثلاً نتیجه تماس یا زمان بازدید.",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textMuted(),
                )
            } else {
                Text(
                    text = note,
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListingNotesSheet(
    note: String,
    tag: String,
    tagOptions: List<String>,
    isSubmitting: Boolean,
    onNoteChange: (String) -> Unit,
    onTagChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "یادداشت آگهی",
        subtitle = "برچسب و یادداشت فقط برای فایلینگ شما ذخیره می‌شود",
        icon = DfIcons.StickyNote,
        iconContainerColor = DfColors.AmberLight,
        iconTint = DfColors.Amber,
        onClose = onDismiss,
        bodyHeightFraction = 0.62f,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره یادداشت",
                onPrimary = onSave,
                primaryEnabled = !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "برچسب") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                (listOf("") + tagOptions).distinct().forEach { option ->
                    val label = option.ifBlank { "بدون برچسب" }
                    Surface(
                        onClick = { onTagChange(option) },
                        shape = AppShapes.Chip,
                        color = if (tag == option) DfColors.PurpleContainer else DfColors.SurfaceVariant,
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                            style = AppTypography.labelSmall,
                            fontWeight = if (tag == option) FontWeight.Bold else FontWeight.Medium,
                            color = if (tag == option) DfColors.Purple else DfColors.TextSecondary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        DfSheetSection(title = "یادداشت") {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("یادداشت داخلی") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                enabled = !isSubmitting,
                placeholder = { Text("نتیجه تماس، زمان بازدید، نکته مالک…") },
            )
        }
    }
}

@Composable
fun ListingContactsSection(
    contacts: List<ListingLinkedContactDto>,
    ownerName: String,
    ownerPhone: String,
    tenantName: String,
    tenantPhone: String,
    isVacant: Boolean,
    onAdd: () -> Unit,
    onContactClick: (Long) -> Unit,
    onCall: (String) -> Unit,
    onSms: (String) -> Unit,
    onWhatsApp: (String) -> Unit,
    onBale: (String) -> Unit,
    onUnlink: (Long) -> Unit,
    onEditOwner: () -> Unit,
    onEditTenant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val presentation = remember(contacts, ownerName, ownerPhone, tenantName, tenantPhone, isVacant) {
        ListingOccupancyContacts.build(
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            tenantName = tenantName,
            tenantPhone = tenantPhone,
            isVacant = isVacant,
            contacts = contacts,
        )
    }
    ExtractSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(DfIcons.Users, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(18.dp))
                    Text("مخاطبین این ملک", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold, color = DfThemeColors.textPrimary())
                }
                TextButton(onClick = onAdd) { Text("ارسال") }
            }
            if (presentation.occupancy.isEmpty() && presentation.others.isEmpty()) {
                Text(
                    text = "مالک یا مستاجر ثبت نشده و هنوز مخاطبی به این آگهی پیوند نشده است.",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textMuted(),
                )
            } else {
                presentation.occupancy.forEach { person ->
                    ListingOccupancyRow(
                        person = person,
                        onClick = person.customerId?.let { id -> { onContactClick(id) } },
                        onCall = person.phone.takeIf { it.isNotBlank() }?.let { phone -> { onCall(phone) } },
                        onSms = person.phone.takeIf { it.isNotBlank() }?.let { phone -> { onSms(phone) } },
                        onWhatsApp = person.phone.takeIf { it.isNotBlank() }?.let { phone -> { onWhatsApp(phone) } },
                        onBale = person.phone.takeIf { it.isNotBlank() }?.let { phone -> { onBale(phone) } },
                        onEdit = if (person.role == ListingOccupancyContacts.ROLE_OWNER) onEditOwner else onEditTenant,
                    )
                }
                presentation.others.forEach { contact ->
                    ListingContactRow(
                        contact = contact,
                        onClick = { onContactClick(contact.customerId) },
                        onCall = contact.phone.takeIf { it.isNotBlank() }?.let { { onCall(it) } },
                        onUnlink = { onUnlink(contact.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingOccupancyRow(
    person: ListingOccupancyPerson,
    onClick: (() -> Unit)?,
    onCall: (() -> Unit)?,
    onSms: (() -> Unit)?,
    onWhatsApp: (() -> Unit)?,
    onBale: (() -> Unit)?,
    onEdit: () -> Unit,
) {
    val isOwner = person.role == ListingOccupancyContacts.ROLE_OWNER
    val badgeColor = if (isOwner) DfColors.GreenLight else DfColors.BlueLight
    val badgeText = if (isOwner) DfColors.Green else DfColors.Blue
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = person.name,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                DfBadge(text = person.role, color = badgeColor, textColor = badgeText)
                TextButton(onClick = onEdit) { Text("ویرایش") }
            }
            person.phone.takeIf { it.isNotBlank() }?.let { phone ->
                Text(phone, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
            }
            if (onCall != null || onSms != null || onWhatsApp != null || onBale != null) {
                ListingOwnerContactActions(
                    onCall = onCall ?: {},
                    onSms = onSms ?: {},
                    onWhatsApp = onWhatsApp ?: {},
                    onBale = onBale ?: {},
                )
            }
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = AppShapes.CardSmall,
            color = DfThemeColors.surfaceVariant().copy(alpha = 0.65f),
            content = { content() },
        )
    } else {
        Surface(
            shape = AppShapes.CardSmall,
            color = DfThemeColors.surfaceVariant().copy(alpha = 0.65f),
            content = { content() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingContactRow(
    contact: ListingLinkedContactDto,
    onClick: () -> Unit,
    onCall: (() -> Unit)?,
    onUnlink: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.CardSmall,
        color = DfColors.SurfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = contact.fullName.ifBlank { "مخاطب بدون نام" },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            contact.phone.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val roleColor = when (contact.role) {
                    ListingOccupancyContacts.ROLE_OWNER -> DfColors.GreenLight
                    ListingOccupancyContacts.ROLE_TENANT -> DfColors.BlueLight
                    else -> DfColors.PurpleContainer
                }
                val roleText = when (contact.role) {
                    ListingOccupancyContacts.ROLE_OWNER -> DfColors.Green
                    ListingOccupancyContacts.ROLE_TENANT -> DfColors.Blue
                    else -> DfColors.PurpleDark
                }
                if (contact.role.isNotBlank()) DfBadge(text = contact.role, color = roleColor, textColor = roleText)
                if (contact.dealType.isNotBlank()) DfBadge(text = contact.dealType)
                if (contact.status.isNotBlank()) DfBadge(text = contact.status)
            }
            contact.notes.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (onCall != null) {
                    TextButton(onClick = onCall) { Text("تماس") }
                }
                TextButton(onClick = onUnlink) {
                    Text("حذف پیوند", color = DfColors.Rose)
                }
            }
        }
    }
}

@Composable
fun ListingAiSummarySection(
    summary: String,
    isLoading: Boolean,
    isFallback: Boolean,
    onGenerate: () -> Unit,
    onCopy: () -> Unit,
    onOpenAssistant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(0.5.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        DfIcons.Sparkles,
                        contentDescription = null,
                        tint = DfThemeColors.textMuted(),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "خلاصه هوشمند",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = DfThemeColors.textPrimary(),
                    )
                }
                if (isFallback && summary.isNotBlank()) {
                    DfBadge(text = "محلی", color = DfColors.SurfaceVariant, textColor = DfThemeColors.textMuted())
                }
            }

            when {
                isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = DfThemeColors.textMuted(),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "در حال تهیه خلاصه…",
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                }
                summary.isBlank() -> {
                    Text(
                        text = "نکات کلیدی این آگهی را برای تماس و پیشنهاد به مشتری خلاصه کنید.",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textMuted(),
                    )
                    TextButton(onClick = onGenerate) { Text("تولید خلاصه") }
                }
                else -> {
                    Text(
                        text = summary,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                    ) {
                        TextButton(onClick = onCopy) { Text("کپی") }
                        TextButton(onClick = onGenerate) { Text("تولید مجدد") }
                        TextButton(onClick = onOpenAssistant) { Text("دستیار") }
                    }
                }
            }
        }
    }
}
