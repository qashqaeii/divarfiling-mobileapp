package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfGlassButton
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.ContactMatchGroupDto
import ir.divarfiling.mobile.core.network.ContactMatchesData
import ir.divarfiling.mobile.core.network.MessageTemplateDto
import ir.divarfiling.mobile.core.network.PropertyMatchDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactMatchesSheet(
    visible: Boolean,
    matches: ContactMatchesData?,
    isLoading: Boolean,
    isSubmitting: Boolean,
    contactPhone: String?,
    note: String,
    templates: List<MessageTemplateDto>,
    templatesLoading: Boolean,
    showTemplatePicker: Boolean,
    onNoteChange: (String) -> Unit,
    onToggleTemplatePicker: (Boolean) -> Unit,
    onApplyTemplate: (MessageTemplateDto) -> Unit,
    onDismiss: () -> Unit,
    onSuggest: (List<PropertyMatchDto>, shareViaWhatsApp: Boolean) -> Unit,
) {
    if (!visible) return

    val groups = remember(matches) { matches?.matchGroups.orEmpty() }
    val allMatches = remember(matches, groups) {
        if (groups.isNotEmpty()) {
            groups.flatMap { it.crmMatches + it.divarMatches }
        } else {
            matches?.crmMatches.orEmpty() + matches?.divarMatches.orEmpty()
        }.sortedByDescending { it.score }
    }
    var selected by remember(allMatches) { mutableStateOf(emptySet<String>()) }

    fun matchKey(m: PropertyMatchDto): String =
        if (m.source == "crm") "crm:${m.propertyId}" else "divar:${m.token}"

    val hasPhone = !contactPhone.isNullOrBlank()
    val selectedCount = selected.size
    val allSelected = allMatches.isNotEmpty() && selectedCount == allMatches.size
    val canSubmit = selectedCount > 0 && !isSubmitting

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "پیشنهاد هوشمند",
            subtitle = when {
                matches?.isBuilder == true -> "تأمین پروژه و بازار آپارتمان"
                else -> "ملک‌های هم‌خوان با بودجه، محله و متراژ"
            },
            icon = DfIcons.Sparkles,
            iconContainerColor = DfColors.PurpleContainer,
            iconTint = DfColors.Purple,
            onClose = onDismiss,
            footer = if (!isLoading && matches?.eligible != false && allMatches.isNotEmpty()) {
                {
                    MatchSuggestFooter(
                        selectedCount = selectedCount,
                        totalCount = allMatches.size,
                        allSelected = allSelected,
                        canSubmit = canSubmit,
                        isSubmitting = isSubmitting,
                        hasPhone = hasPhone,
                        onToggleSelectAll = {
                            selected = if (allSelected) {
                                emptySet()
                            } else {
                                allMatches.map { matchKey(it) }.toSet()
                            }
                        },
                        onSuggest = {
                            onSuggest(allMatches.filter { matchKey(it) in selected }, false)
                        },
                        onSuggestWhatsApp = {
                            onSuggest(allMatches.filter { matchKey(it) in selected }, true)
                        },
                    )
                }
            } else {
                null
            },
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        CircularProgressIndicator(color = DfColors.Purple, strokeWidth = 3.dp)
                        Text(
                            "در حال تحلیل تطبیق‌ها…",
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextSecondary,
                        )
                    }
                }
                matches?.eligible == false -> {
                    DfEmptyState(
                        title = "تطبیق فعال نیست",
                        subtitle = matches.message ?: "برای این نوع مخاطب پیشنهاد هوشمند در دسترس نیست.",
                        variant = DfEmptyVariant.Locked,
                    )
                }
                allMatches.isEmpty() -> {
                    DfEmptyState(
                        title = "ملک مناسبی پیدا نشد",
                        subtitle = if (matches?.divarLocked == true) {
                            "فایل شخصی مناسبی نبود. برای تطبیق فایلینگ دیوار به لایسنس نیاز است."
                        } else {
                            "بودجه، محله یا متراژ مخاطب را تکمیل کنید تا پیشنهادهای دقیق‌تری ببینید."
                        },
                        variant = if (matches?.divarLocked == true) {
                            DfEmptyVariant.Locked
                        } else {
                            DfEmptyVariant.NoResults
                        },
                    )
                }
                else -> {
                    MatchInsightStrip(
                        total = allMatches.size,
                        crmCount = allMatches.count { it.source == "crm" },
                        divarCount = allMatches.count { it.source == "divar" },
                        topScore = allMatches.maxOfOrNull { it.score } ?: 0,
                        isBuilder = matches?.isBuilder == true,
                    )

                    if (groups.isNotEmpty()) {
                        groups.forEach { group ->
                            val groupMatches = (group.crmMatches + group.divarMatches)
                                .sortedByDescending { it.score }
                            if (groupMatches.isEmpty()) return@forEach
                            MatchGroupHeader(group = group, count = groupMatches.size)
                            groupMatches.forEach { match ->
                                SmartMatchCard(
                                    match = match,
                                    selected = matchKey(match) in selected,
                                    onToggle = {
                                        val key = matchKey(match)
                                        selected = if (key in selected) selected - key else selected + key
                                    },
                                )
                            }
                        }
                    } else {
                        val crm = allMatches.filter { it.source == "crm" }
                        val divar = allMatches.filter { it.source == "divar" }
                        if (crm.isNotEmpty()) {
                            MatchSourceHeader(
                                title = "فایل‌های شخصی",
                                count = crm.size,
                                accent = DfColors.Purple,
                            )
                            crm.forEach { match ->
                                SmartMatchCard(
                                    match = match,
                                    selected = matchKey(match) in selected,
                                    onToggle = {
                                        val key = matchKey(match)
                                        selected = if (key in selected) selected - key else selected + key
                                    },
                                )
                            }
                        }
                        if (divar.isNotEmpty()) {
                            MatchSourceHeader(
                                title = "فایلینگ دیوار",
                                count = divar.size,
                                accent = DfColors.Blue,
                            )
                            divar.forEach { match ->
                                SmartMatchCard(
                                    match = match,
                                    selected = matchKey(match) in selected,
                                    onToggle = {
                                        val key = matchKey(match)
                                        selected = if (key in selected) selected - key else selected + key
                                    },
                                )
                            }
                        }
                    }

                    DfSheetSection(title = "پیام همراه پیشنهاد") {
                        DfTextField(
                            value = note,
                            onValueChange = onNoteChange,
                            label = "متن پیام",
                            placeholder = "مثلاً چند فایل نزدیک به بودجه شما انتخاب کردم",
                            singleLine = false,
                            minLines = 3,
                            enabled = !isSubmitting,
                        )
                        DfGlassTextButton(
                            text = if (showTemplatePicker) "بستن قالب‌ها" else "انتخاب از قالب پیام",
                            onClick = { onToggleTemplatePicker(!showTemplatePicker) },
                        )
                        if (showTemplatePicker) {
                            TemplatePickerBlock(
                                templates = templates,
                                templatesLoading = templatesLoading,
                                isSubmitting = isSubmitting,
                                onApplyTemplate = onApplyTemplate,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchSuggestFooter(
    selectedCount: Int,
    totalCount: Int,
    allSelected: Boolean,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    hasPhone: Boolean,
    onToggleSelectAll: () -> Unit,
    onSuggest: () -> Unit,
    onSuggestWhatsApp: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (selectedCount == 0) {
                    "ملک‌های مناسب را انتخاب کنید"
                } else {
                    "${DateUtils.toPersianDigits(selectedCount.toString())} از ${DateUtils.toPersianDigits(totalCount.toString())} انتخاب‌شده"
                },
                style = AppTypography.labelSmall,
                color = DfColors.TextSecondary,
                modifier = Modifier.weight(1f),
            )
            DfGlassTextButton(
                text = if (allSelected) "لغو همه" else "انتخاب همه",
                onClick = onToggleSelectAll,
                compact = true,
            )
        }
        DfPrimaryButton(
            text = if (selectedCount > 0) {
                "ثبت پیشنهاد (${DateUtils.toPersianDigits(selectedCount.toString())})"
            } else {
                "ثبت پیشنهاد"
            },
            onClick = onSuggest,
            enabled = canSubmit,
            loading = isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )
        if (hasPhone) {
            DfGlassButton(
                text = "ثبت و ارسال واتساپ",
                onClick = onSuggestWhatsApp,
                modifier = Modifier.fillMaxWidth(),
                icon = DfIcons.MessageCircle,
                accent = DfColors.Green,
                variant = DfGlassButtonVariant.Accent,
                enabled = canSubmit,
            )
        }
    }
}

@Composable
private fun MatchInsightStrip(
    total: Int,
    crmCount: Int,
    divarCount: Int,
    topScore: Int,
    isBuilder: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = Color.Transparent,
        shadowElevation = AppElevations.subtle,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.Card)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            DfColors.Purple.copy(alpha = 0.12f),
                            DfColors.Blue.copy(alpha = 0.08f),
                            DfColors.Green.copy(alpha = 0.06f),
                        ),
                    ),
                )
                .padding(AppSpacing.md),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Icon(
                        DfIcons.WandSparkles,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        if (isBuilder) "پیشنهاد دوگانه سازنده" else "تحلیل هوشمند تطبیق",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Purple,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    InsightStat(
                        label = "پیشنهاد",
                        value = DateUtils.toPersianDigits(total.toString()),
                        accent = DfColors.Purple,
                        modifier = Modifier.weight(1f),
                    )
                    if (crmCount > 0) {
                        InsightStat(
                            label = "شخصی",
                            value = DateUtils.toPersianDigits(crmCount.toString()),
                            accent = DfColors.Purple,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (divarCount > 0) {
                        InsightStat(
                            label = "دیوار",
                            value = DateUtils.toPersianDigits(divarCount.toString()),
                            accent = DfColors.Blue,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    InsightStat(
                        label = "بهترین",
                        value = DateUtils.toPersianDigits(topScore.toString()),
                        accent = scoreAccent(topScore),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(DfColors.Surface.copy(alpha = 0.75f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            value,
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
        Text(
            label,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MatchGroupHeader(group: ContactMatchGroupDto, count: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                group.title.ifBlank { "گروه تطبیق" },
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                DateUtils.toPersianDigits(count.toString()),
                style = AppTypography.labelSmall,
                color = DfColors.Purple,
                fontWeight = FontWeight.Bold,
            )
        }
        group.hint?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
        }
    }
}

@Composable
private fun MatchSourceHeader(title: String, count: Int, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            title,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = DfColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            DateUtils.toPersianDigits(count.toString()),
            style = AppTypography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SmartMatchCard(
    match: PropertyMatchDto,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) DfColors.Purple else DfColors.Outline.copy(alpha = 0.35f),
        label = "matchBorder",
    )
    val bg by animateColorAsState(
        targetValue = if (selected) {
            DfColors.PurpleContainer.copy(alpha = 0.55f)
        } else {
            DfColors.Surface
        },
        label = "matchBg",
    )
    val scoreColor = scoreAccent(match.score)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = AppShapes.Card,
        color = bg,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = if (selected) AppElevations.raised else AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            MatchScoreRing(score = match.score, accent = scoreColor)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(
                        match.title.orEmpty().ifBlank { "بدون عنوان" },
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    SelectionMark(selected = selected)
                }
                val meta = listOfNotNull(
                    match.priceLabel?.takeIf { it.isNotBlank() },
                    match.area?.let { "${DateUtils.toPersianDigits(it.toInt().toString())} متر" },
                    match.neighborhood?.takeIf { it.isNotBlank() },
                    match.rooms?.takeIf { it.isNotBlank() }?.let { "$it خواب" },
                ).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(meta, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MatchChip(
                        text = if (match.source == "crm") "فایل شخصی" else "دیوار",
                        accent = if (match.source == "crm") DfColors.Purple else DfColors.Blue,
                    )
                    match.intentLabel?.takeIf { it.isNotBlank() }?.let {
                        MatchChip(text = it, accent = DfColors.Amber)
                    }
                    match.reasons.take(3).forEach { reason ->
                        MatchChip(text = reason, accent = DfColors.Green)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchScoreRing(score: Int, accent: Color) {
    val progress by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100) / 100f),
        label = "scoreProgress",
    )
    Box(
        modifier = Modifier.size(52.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(52.dp)) {
            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(
                color = accent.copy(alpha = 0.15f),
                style = stroke,
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = stroke,
            )
        }
        Text(
            DateUtils.toPersianDigits(score.toString()),
            style = AppTypography.labelLarge.copy(fontSize = 14.sp),
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

@Composable
private fun SelectionMark(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
                if (selected) DfColors.Purple else DfColors.SurfaceVariant,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                DfIcons.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun MatchChip(text: String, accent: Color) {
    Surface(
        shape = AppShapes.Chip,
        color = accent.copy(alpha = 0.12f),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ColumnScope.TemplatePickerBlock(
    templates: List<MessageTemplateDto>,
    templatesLoading: Boolean,
    isSubmitting: Boolean,
    onApplyTemplate: (MessageTemplateDto) -> Unit,
) {
    when {
        templatesLoading -> DfCardListSkeleton(count = 2, itemHeight = 72.dp)
        templates.isEmpty() -> DfEmptyState(
            title = "قالبی آماده نیست",
            subtitle = "قالب‌های پیام از workspace همگام می‌شوند.",
            variant = DfEmptyVariant.Empty,
        )
        else -> {
            templates.take(8).forEach { template ->
                Surface(
                    shape = AppShapes.Card,
                    color = DfColors.SurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Text(template.title, style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
                        template.category.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                        }
                        Text(
                            template.body,
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        DfGlassButton(
                            text = "جایگذاری در پیام",
                            onClick = { onApplyTemplate(template) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSubmitting,
                            variant = DfGlassButtonVariant.Secondary,
                        )
                    }
                }
            }
        }
    }
}

private fun scoreAccent(score: Int): Color = when {
    score >= 75 -> DfColors.Green
    score >= 50 -> DfColors.Amber
    else -> DfColors.TextMuted
}
