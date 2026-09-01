package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfJalaliDateTimeField
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfActionButton
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetExpandableSection
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.DealNextActionDto
import ir.divarfiling.mobile.core.network.DealTimelineItemDto

@Composable
fun DealNextActionCard(
    nextAction: DealNextActionDto?,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onSet: () -> Unit,
    modifier: Modifier = Modifier,
    isSubmitting: Boolean = false,
) {
    val overdue = nextAction?.isOverdue == true
    val accent = when {
        overdue -> DfColors.Amber
        nextAction != null -> DfColors.Purple
        else -> DfColors.TextMuted
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = when {
            overdue -> DfColors.AmberLight.copy(alpha = 0.45f)
            nextAction != null -> DfColors.PurpleContainer.copy(alpha = 0.35f)
            else -> DfThemeColors.surface()
        },
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
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
                        .clip(AppShapes.IconContainer)
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = DealUiUtils.nextActionIcon(nextAction?.type, nextAction?.icon),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("اقدام بعدی", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                    if (nextAction != null) {
                        Text(
                            text = nextAction.label ?: "اقدام",
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                        )
                    } else {
                        Text(
                            text = "هنوز اقدامی تعیین نشده",
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextMuted,
                        )
                    }
                }
            }
            if (nextAction != null) {
                if (overdue) {
                    Surface(shape = AppShapes.Chip, color = DfColors.AmberLight) {
                        Text(
                            text = "پیگیری عقب‌افتاده",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.Amber,
                        )
                    }
                }
                nextAction.scheduleLabel?.takeIf { it.isNotBlank() }?.let { schedule ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.Clock,
                            contentDescription = null,
                            tint = if (overdue) DfColors.Amber else DfColors.TextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = schedule,
                            style = AppTypography.bodyDescription,
                            color = if (overdue) DfColors.Amber else DfColors.TextSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                nextAction.note?.takeIf { it.isNotBlank() }?.let { note ->
                    Surface(
                        shape = AppShapes.CardSmall,
                        color = DfThemeColors.surface().copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = note,
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                            style = AppTypography.labelSmall,
                            color = DfColors.TextSecondary,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    DfPrimaryButton(
                        text = "انجام شد",
                        onClick = onComplete,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                    DfSecondaryButton(
                        text = "ویرایش",
                        onClick = onEdit,
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                DfPrimaryButton(
                    text = "تعیین اقدام بعدی",
                    onClick = onSet,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
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
        DfSectionHeader(
            title = "تاریخچه معامله",
            count = items.size.takeIf { items.isNotEmpty() },
        )
        if (items.isEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal),
                shape = AppShapes.Card,
                color = DfThemeColors.surface(),
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "هنوز رویدادی ثبت نشده",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "با ثبت پیگیری یا تغییر مرحله، تاریخچه اینجا ساخته می‌شود.",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextMuted,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal),
            ) {
                items.forEachIndexed { index, item ->
                    DealTimelineRow(
                        item = item,
                        isLast = index == items.lastIndex,
                    )
                }
            }
            onLoadMore?.let { load ->
                DfSecondaryButton(
                    text = "نمایش بیشتر",
                    onClick = load,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }
    }
}

@Composable
private fun DealTimelineRow(
    item: DealTimelineItemDto,
    isLast: Boolean,
) {
    val accent = DealUiUtils.timelineAccent(item.kind)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = DealUiUtils.timelineIcon(item.kind, item.icon),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(2.dp)
                        .height(28.dp)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(DfThemeColors.outlineSubtle()),
                )
            }
        }
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else AppSpacing.sm),
            shape = AppShapes.CardSmall,
            color = DfThemeColors.surface(),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.title.orEmpty(),
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        text = listOfNotNull(item.dayLabel, item.timeLabel).joinToString(" · "),
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
                item.typeLabel?.takeIf { it.isNotBlank() && it != item.title }?.let { type ->
                    Text(type, style = AppTypography.labelSmall, color = accent, fontWeight = FontWeight.SemiBold)
                }
                item.body?.takeIf { it.isNotBlank() }?.let { body ->
                    Text(body, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                }
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
    var nextExpanded by remember {
        mutableStateOf(nextActionType.isNotBlank() || nextActionAt.isNotBlank())
    }
    DfSheetScaffold(
        title = "ثبت پیگیری",
        subtitle = "نتیجه کار انجام‌شده و اقدام بعدی را ثبت کنید",
        icon = DfIcons.MessageCircle,
        onClose = onDismiss,
        bodyHeightFraction = 0.92f,
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
            DealFollowUpTypePicker(
                selectedKey = followUpType,
                enabled = !isSubmitting,
                onSelect = onTypeChange,
            )
        }
        DfSheetSection(title = "نتیجه") {
            DfTextField(
                value = note,
                onValueChange = onNoteChange,
                label = "نتیجه پیگیری",
                placeholder = "مثلاً: مشتری فایل را پسندید و برای فردا بازدید هماهنگ شد.",
                singleLine = false,
                minLines = 3,
                enabled = !isSubmitting,
            )
        }
        DfSheetExpandableSection(
            title = "اقدام بعدی (اختیاری)",
            subtitle = if (nextActionType.isBlank()) "برای ادامه پرونده زمان بگذارید" else ir.divarfiling.mobile.feature.crm.CrmConstants.dealNextActionLabel(nextActionType),
            expanded = nextExpanded,
            onToggle = { nextExpanded = !nextExpanded },
        ) {
            DealNextActionTypePicker(
                selectedKey = nextActionType,
                enabled = !isSubmitting,
                onSelect = onNextTypeChange,
            )
            DfJalaliDateTimeField(
                value = nextActionAt,
                onValueChange = onNextAtChange,
                label = "زمان اقدام بعدی",
                enabled = !isSubmitting,
            )
            DfTextField(
                value = nextActionNote,
                onValueChange = onNextNoteChange,
                label = "یادداشت کوتاه",
                enabled = !isSubmitting,
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
        bodyHeightFraction = 0.88f,
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
            DealNextActionTypePicker(
                selectedKey = actionType,
                enabled = !isSubmitting,
                onSelect = onTypeChange,
            )
        }
        DfSheetSection(title = "زمان") {
            DfJalaliDateTimeField(
                value = actionAt,
                onValueChange = onAtChange,
                label = "تاریخ و ساعت",
                enabled = !isSubmitting,
            )
        }
        DfSheetSection(title = "یادداشت") {
            DfTextField(
                value = note,
                onValueChange = onNoteChange,
                label = "یادداشت کوتاه",
                enabled = !isSubmitting,
            )
        }
    }
}

@Composable
fun DealLostReasonSheet(
    selectedReason: String,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isSubmitting: Boolean = false,
) {
    DfSheetScaffold(
        title = "دلیل از دست رفتن",
        subtitle = "چرا این معامله بسته نشد؟",
        icon = DfIcons.CircleAlert,
        iconContainerColor = DfColors.RoseLight,
        iconTint = DfColors.OverdueAccent,
        onClose = onDismiss,
        bodyHeightFraction = 0.72f,
        footer = {
            DfSheetActions(
                primaryText = "ثبت و تغییر مرحله",
                onPrimary = onConfirm,
                primaryEnabled = selectedReason.isNotBlank() && !isSubmitting,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "علت") {
            ir.divarfiling.mobile.feature.crm.CrmConstants.DEAL_LOST_REASONS.forEach { reason ->
                ir.divarfiling.mobile.core.design.components.DfSheetOptionRow(
                    label = reason,
                    selected = selectedReason == reason,
                    onClick = { onReasonChange(reason) },
                )
            }
        }
    }
}
