package ir.divarfiling.mobile.feature.filing.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
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
    onAdd: () -> Unit,
    onContactClick: (Long) -> Unit,
    onCall: (String) -> Unit,
    onUnlink: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            if (contacts.isEmpty()) {
                Text(
                    text = "هنوز مخاطبی به این آگهی پیوند نشده است. با ارسال فایل، مخاطب به آگهی وصل می‌شود.",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textMuted(),
                )
            } else {
                contacts.forEach { contact ->
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
                if (contact.role.isNotBlank()) DfBadge(text = contact.role)
            }
            contact.phone.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
    DfPremiumCard(modifier = modifier, containerColor = DfColors.PurpleContainer.copy(alpha = 0.35f)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(DfIcons.Sparkles, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("خلاصه هوشمند آگهی", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold, color = DfColors.Purple)
                    Text("نکات کلیدی برای تماس و پیشنهاد به مشتری", style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                }
                if (isFallback && summary.isNotBlank()) {
                    DfBadge(text = "نسخه محلی")
                }
            }

            when {
                isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.sm),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = DfColors.Purple, strokeWidth = 2.dp)
                        Text(
                            text = "در حال تهیه خلاصه…",
                            style = AppTypography.bodyDescription,
                            color = DfColors.Purple,
                            modifier = Modifier.padding(start = AppSpacing.sm),
                        )
                    }
                }
                summary.isBlank() -> {
                    Text(
                        text = "یک خلاصه حرفه‌ای از مشخصات، قیمت و نکات فروش این آگهی بسازید.",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                    DfPrimaryButton(text = "تولید خلاصه", onClick = onGenerate)
                }
                else -> {
                    Text(
                        text = summary,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textPrimary(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
                        CompactAction(
                            label = "کپی",
                            icon = DfIcons.Copy,
                            tint = DfColors.Purple,
                            background = DfColors.PurpleContainer,
                            onClick = onCopy,
                            modifier = Modifier.weight(1f),
                        )
                        CompactAction(
                            label = "تولید مجدد",
                            icon = DfIcons.RefreshCw,
                            tint = DfColors.Blue,
                            background = DfColors.BlueLight,
                            onClick = onGenerate,
                            modifier = Modifier.weight(1f),
                        )
                        CompactAction(
                            label = "دستیار",
                            icon = DfIcons.Bot,
                            tint = DfColors.Amber,
                            background = DfColors.AmberLight,
                            onClick = onOpenAssistant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick = onClick, modifier = modifier, shape = AppShapes.CardSmall, color = background) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
            Text(
                text = label,
                modifier = Modifier.padding(start = 4.dp),
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1,
            )
        }
    }
}
