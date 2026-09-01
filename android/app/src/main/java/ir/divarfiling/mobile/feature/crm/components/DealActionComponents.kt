package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.AppShapes
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfActionButton
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealNextActionDto
import ir.divarfiling.mobile.core.network.DealTimelineItemDto
import ir.divarfiling.mobile.feature.crm.CrmConstants

private val FOLLOWUP_TYPES = listOf(
    "call" to "تماس",
    "message" to "پیام",
    "visit" to "بازدید",
    "negotiation" to "مذاکره",
    "send_file" to "ارسال فایل",
    "contract_followup" to "پیگیری قرارداد",
    "note" to "یادداشت",
    "other" to "سایر",
)

private val NEXT_ACTION_TYPES = listOf(
    "call" to "تماس",
    "message" to "پیام",
    "send_file" to "ارسال فایل",
    "visit" to "بازدید",
    "negotiation" to "مذاکره",
    "contract_followup" to "پیگیری قرارداد",
    "documents" to "دریافت مدارک",
    "commission" to "پیگیری کمیسیون",
    "other" to "سایر",
)

@Composable
fun DealNextActionCard(
    nextAction: DealNextActionDto?,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onSet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = if (nextAction?.isOverdue == true) DfColors.AmberLight.copy(alpha = 0.35f) else DfThemeColors.surface(),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("اقدام بعدی", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
                if (nextAction != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        TextButton(onClick = onComplete) { Text("انجام شد") }
                        TextButton(onClick = onEdit) { Text("ویرایش") }
                    }
                } else {
                    TextButton(onClick = onSet) { Text("تعیین اقدام") }
                }
            }
            if (nextAction != null) {
                if (nextAction.isOverdue) {
                    Text("پیگیری عقب‌افتاده", style = AppTypography.labelSmall, color = DfColors.Amber, fontWeight = FontWeight.Bold)
                }
                Text(nextAction.label ?: "اقدام", style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
                nextAction.scheduleLabel?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                }
                nextAction.note?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                }
            } else {
                Text(
                    "اقدام بعدی تعیین نشده",
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextMuted,
                )
            }
        }
    }
}

@Composable
fun DealQuickActionBar(
    onFollowUp: () -> Unit,
    onEditStage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfActionButton(
            text = "ثبت پیگیری",
            icon = DfIcons.MessageCircle,
            onClick = onFollowUp,
            modifier = Modifier.weight(1f),
        )
        DfActionButton(
            text = "تغییر مرحله",
            icon = DfIcons.RefreshCw,
            onClick = onEditStage,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun DealTimelineSection(
    items: List<DealTimelineItemDto>,
    onLoadMore: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        DfSectionHeader(title = "تاریخچه معامله", count = items.size)
        if (items.isEmpty()) {
            Text(
                "هنوز پیگیری ثبت نشده",
                style = AppTypography.bodyDescription,
                color = DfColors.TextMuted,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        } else {
            items.forEach { item ->
                DealTimelineRow(item = item)
                HorizontalDivider(color = DfThemeColors.outlineSubtle())
            }
            onLoadMore?.let { load ->
                TextButton(onClick = load, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                    Text("نمایش بیشتر")
                }
            }
        }
    }
}

@Composable
private fun DealTimelineRow(item: DealTimelineItemDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(item.title.orEmpty(), style = AppTypography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                    listOfNotNull(item.dayLabel, item.timeLabel).joinToString(" · "),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            item.body?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
        }
    }
}

@Composable
fun DealFollowUpSheet(
    followUpType: String,
    note: String,
    nextActionType: String,
    nextActionAt: String,
    nextActionNote: String,
    isSubmitting: Boolean,
    onTypeChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onNextTypeChange: (String) -> Unit,
    onNextAtChange: (String) -> Unit,
    onNextNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "ثبت پیگیری",
        subtitle = "چه کاری انجام شد؟",
        icon = DfIcons.MessageCircle,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ثبت…" else "ثبت پیگیری",
                onPrimary = onSubmit,
                primaryEnabled = note.isNotBlank() && !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "نوع پیگیری") {
            DfDropdown(
                label = "نوع",
                value = FOLLOWUP_TYPES.firstOrNull { it.first == followUpType }?.second ?: "تماس",
                options = FOLLOWUP_TYPES.map { it.second },
                onSelect = { label ->
                    FOLLOWUP_TYPES.firstOrNull { it.second == label }?.first?.let(onTypeChange)
                },
            )
        }
        DfSheetSection(title = "نتیجه") {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("مثلاً: مشتری فایل را پسندید و برای فردا بازدید هماهنگ شد.") },
            )
        }
        DfSheetSection(title = "اقدام بعدی (اختیاری)") {
            DfDropdown(
                label = "نوع",
                value = NEXT_ACTION_TYPES.firstOrNull { it.first == nextActionType }?.second ?: "— بدون اقدام —",
                options = listOf("— بدون اقدام —") + NEXT_ACTION_TYPES.map { it.second },
                onSelect = { label ->
                    if (label == "— بدون اقدام —") onNextTypeChange("")
                    else NEXT_ACTION_TYPES.firstOrNull { it.second == label }?.first?.let(onNextTypeChange)
                },
            )
            OutlinedTextField(
                value = nextActionAt,
                onValueChange = onNextAtChange,
                label = { Text("تاریخ و ساعت") },
                placeholder = { Text("۱۴۰۴/۰۶/۱۵ ۱۸:۰۰") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = nextActionNote,
                onValueChange = onNextNoteChange,
                label = { Text("یادداشت") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

fun nextActionCompactLabel(deal: DealDto): String? =
    deal.nextAction?.compactLabel?.takeIf { it.isNotBlank() }
        ?: deal.nextAction?.label?.takeIf { it.isNotBlank() }

@Composable
fun DealNextActionSheet(
    actionType: String,
    actionAt: String,
    note: String,
    isSubmitting: Boolean,
    onTypeChange: (String) -> Unit,
    onAtChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "اقدام بعدی",
        subtitle = "چه کاری باید انجام شود؟",
        icon = DfIcons.Phone,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره",
                onPrimary = onSubmit,
                primaryEnabled = !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "نوع اقدام") {
            DfDropdown(
                label = "نوع",
                value = NEXT_ACTION_TYPES.firstOrNull { it.first == actionType }?.second ?: "— بدون اقدام —",
                options = listOf("— بدون اقدام —") + NEXT_ACTION_TYPES.map { it.second },
                onSelect = { label ->
                    if (label == "— بدون اقدام —") onTypeChange("")
                    else NEXT_ACTION_TYPES.firstOrNull { it.second == label }?.first?.let(onTypeChange)
                },
            )
        }
        DfSheetSection(title = "زمان") {
            OutlinedTextField(
                value = actionAt,
                onValueChange = onAtChange,
                label = { Text("تاریخ و ساعت") },
                placeholder = { Text("۱۴۰۴/۰۶/۱۵ ۱۸:۰۰") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        DfSheetSection(title = "یادداشت") {
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("یادداشت کوتاه") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}
