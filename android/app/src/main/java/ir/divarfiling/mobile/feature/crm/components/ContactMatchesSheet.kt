package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfGlassButton
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.ContactMatchGroupDto
import ir.divarfiling.mobile.core.network.ContactMatchesData
import ir.divarfiling.mobile.core.network.MessageTemplateDto
import ir.divarfiling.mobile.core.network.PropertyMatchDto

private enum class MatchFilter(val label: String) {
    ALL("همه"),
    CRM("شخصی"),
    DIVAR("دیوار"),
    TOP("برتر"),
}

private const val TOP_SCORE_THRESHOLD = 70

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
    var activeFilter by remember { mutableStateOf(MatchFilter.ALL) }
    var messageExpanded by remember { mutableStateOf(true) }

    fun matchKey(m: PropertyMatchDto): String =
        if (m.source == "crm") "crm:${m.propertyId}" else "divar:${m.token}"

    val filteredMatches = remember(allMatches, activeFilter) {
        when (activeFilter) {
            MatchFilter.ALL -> allMatches
            MatchFilter.CRM -> allMatches.filter { it.source == "crm" }
            MatchFilter.DIVAR -> allMatches.filter { it.source == "divar" }
            MatchFilter.TOP -> allMatches.filter { it.score >= TOP_SCORE_THRESHOLD }
        }
    }

    val hasPhone = !contactPhone.isNullOrBlank()
    val selectedCount = selected.size
    val allSelected = filteredMatches.isNotEmpty() && filteredMatches.all { matchKey(it) in selected }
    val canSubmit = selectedCount > 0 && !isSubmitting
    val topMatches = remember(allMatches) { allMatches.take(3) }

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "فایل‌های پیشنهادی",
            subtitle = when {
                matches?.isBuilder == true -> "پروژه و آپارتمان مناسب این مخاطب — انتخاب کنید و ارسال کنید"
                else -> "ملک‌های هم‌خوان با بودجه، محله و متراژ مخاطب"
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
                        filteredCount = filteredMatches.size,
                        allSelected = allSelected,
                        canSubmit = canSubmit,
                        isSubmitting = isSubmitting,
                        hasPhone = hasPhone,
                        onToggleSelectAll = {
                            selected = if (allSelected) {
                                selected - filteredMatches.map { matchKey(it) }.toSet()
                            } else {
                                selected + filteredMatches.map { matchKey(it) }.toSet()
                            }
                        },
                        onSelectTop = {
                            selected = topMatches.map { matchKey(it) }.toSet()
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
                isLoading -> MatchLoadingState()
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
                    MatchInsightHero(
                        total = allMatches.size,
                        crmCount = allMatches.count { it.source == "crm" },
                        divarCount = allMatches.count { it.source == "divar" },
                        topScore = allMatches.maxOfOrNull { it.score } ?: 0,
                        selectedCount = selectedCount,
                        isBuilder = matches?.isBuilder == true,
                    )

                    if (matches?.divarLocked == true && allMatches.none { it.source == "divar" }) {
                        DivarLockedBanner()
                    }

                    MatchFilterBar(
                        activeFilter = activeFilter,
                        crmCount = allMatches.count { it.source == "crm" },
                        divarCount = allMatches.count { it.source == "divar" },
                        topCount = allMatches.count { it.score >= TOP_SCORE_THRESHOLD },
                        onFilterChange = { activeFilter = it },
                    )

                    if (filteredMatches.isEmpty()) {
                        DfEmptyState(
                            title = "در این فیلتر نتیجه‌ای نیست",
                            subtitle = "فیلتر دیگری را امتحان کنید یا معیارهای مخاطب را به‌روز کنید.",
                            variant = DfEmptyVariant.NoResults,
                        )
                    } else if (groups.isNotEmpty() && activeFilter == MatchFilter.ALL) {
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
                        filteredMatches.forEach { match ->
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

                    MatchMessageSection(
                        expanded = messageExpanded,
                        note = note,
                        templates = templates,
                        templatesLoading = templatesLoading,
                        showTemplatePicker = showTemplatePicker,
                        isSubmitting = isSubmitting,
                        selectedCount = selectedCount,
                        onNoteChange = onNoteChange,
                        onToggleExpanded = { messageExpanded = !messageExpanded },
                        onToggleTemplatePicker = onToggleTemplatePicker,
                        onApplyTemplate = onApplyTemplate,
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        CircularProgressIndicator(color = DfColors.Purple, strokeWidth = 3.dp)
        Text(
            "در حال تحلیل بودجه، محله و نیاز مخاطب…",
            style = AppTypography.bodyDescription,
            color = DfColors.TextSecondary,
            fontWeight = FontWeight.Medium,
        )
        Text(
            "چند لحظه — بهترین فایل‌ها انتخاب می‌شوند",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
        )
    }
}

@Composable
private fun DivarLockedBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfColors.AmberLight.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, DfColors.Amber.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(DfIcons.Lock, contentDescription = null, tint = DfColors.Amber, modifier = Modifier.size(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "فایلینگ دیوار قفل است",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.OnWarning,
                )
                Text(
                    "با ارتقای لایسنس، آگهی‌های دیوار هم در پیشنهادها نمایش داده می‌شوند.",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun MatchFilterBar(
    activeFilter: MatchFilter,
    crmCount: Int,
    divarCount: Int,
    topCount: Int,
    onFilterChange: (MatchFilter) -> Unit,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MatchFilterChip(
            label = MatchFilter.ALL.label,
            count = crmCount + divarCount,
            selected = activeFilter == MatchFilter.ALL,
            accent = DfColors.Purple,
            onClick = { onFilterChange(MatchFilter.ALL) },
        )
        if (crmCount > 0) {
            MatchFilterChip(
                label = MatchFilter.CRM.label,
                count = crmCount,
                selected = activeFilter == MatchFilter.CRM,
                accent = DfColors.Purple,
                onClick = { onFilterChange(MatchFilter.CRM) },
            )
        }
        if (divarCount > 0) {
            MatchFilterChip(
                label = MatchFilter.DIVAR.label,
                count = divarCount,
                selected = activeFilter == MatchFilter.DIVAR,
                accent = DfColors.Blue,
                onClick = { onFilterChange(MatchFilter.DIVAR) },
            )
        }
        if (topCount > 0) {
            MatchFilterChip(
                label = MatchFilter.TOP.label,
                count = topCount,
                selected = activeFilter == MatchFilter.TOP,
                accent = DfColors.Green,
                onClick = { onFilterChange(MatchFilter.TOP) },
            )
        }
    }
}

@Composable
private fun MatchFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.Chip,
        color = if (selected) accent.copy(alpha = 0.14f) else DfColors.SurfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            1.dp,
            if (selected) accent.copy(alpha = 0.55f) else DfColors.Outline.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                style = AppTypography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accent else DfColors.TextSecondary,
            )
            Surface(shape = CircleShape, color = if (selected) accent else DfColors.TextMuted.copy(alpha = 0.2f)) {
                Text(
                    DateUtils.toPersianDigits(count.toString()),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else DfColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun MatchSuggestFooter(
    selectedCount: Int,
    totalCount: Int,
    filteredCount: Int,
    allSelected: Boolean,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    hasPhone: Boolean,
    onToggleSelectAll: () -> Unit,
    onSelectTop: () -> Unit,
    onSuggest: () -> Unit,
    onSuggestWhatsApp: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        if (selectedCount > 0) {
            LinearProgressIndicator(
                progress = { selectedCount.toFloat() / totalCount.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(AppShapes.Chip),
                color = DfColors.Purple,
                trackColor = DfColors.PurpleContainer,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedCount == 0) {
                        "ملک‌های مناسب را انتخاب کنید"
                    } else {
                        "${DateUtils.toPersianDigits(selectedCount.toString())} فایل انتخاب‌شده"
                    },
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedCount > 0) DfColors.Purple else DfColors.TextSecondary,
                )
                if (filteredCount < totalCount && selectedCount == 0) {
                    Text(
                        "${DateUtils.toPersianDigits(filteredCount.toString())} در فیلتر فعلی",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DfGlassTextButton(text = "۳ برتر", onClick = onSelectTop, compact = true)
                DfGlassTextButton(
                    text = if (allSelected) "لغو" else "همه",
                    onClick = onToggleSelectAll,
                    compact = true,
                )
            }
        }

        DfPrimaryButton(
            text = if (selectedCount > 0) {
                "ثبت پیشنهاد (${DateUtils.toPersianDigits(selectedCount.toString())})"
            } else {
                "ثبت پیشنهاد برای مخاطب"
            },
            onClick = onSuggest,
            enabled = canSubmit,
            loading = isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )

        if (hasPhone) {
            DfGlassButton(
                text = "ثبت + ارسال واتساپ",
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
private fun MatchInsightHero(
    total: Int,
    crmCount: Int,
    divarCount: Int,
    topScore: Int,
    selectedCount: Int,
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
                    Brush.linearGradient(
                        listOf(
                            DfColors.Purple.copy(alpha = 0.14f),
                            DfColors.Blue.copy(alpha = 0.09f),
                            DfColors.Green.copy(alpha = 0.05f),
                        ),
                    ),
                )
                .padding(AppSpacing.md),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        Icon(
                            DfIcons.WandSparkles,
                            contentDescription = null,
                            tint = DfColors.Purple,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            if (isBuilder) "پیشنهاد دوگانه سازنده" else "تحلیل هوشمند",
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.Purple,
                        )
                    }
                    if (selectedCount > 0) {
                        Surface(shape = AppShapes.Chip, color = DfColors.Purple) {
                            Text(
                                "${DateUtils.toPersianDigits(selectedCount.toString())} انتخاب",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
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
            .background(DfColors.Surface.copy(alpha = 0.82f))
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
            .padding(top = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 18.dp)
                    .clip(AppShapes.Chip)
                    .background(DfColors.Purple),
            )
            Text(
                group.title.ifBlank { "گروه تطبیق" },
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Surface(shape = AppShapes.Chip, color = DfColors.PurpleContainer) {
                Text(
                    DateUtils.toPersianDigits(count.toString()),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        group.hint?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                style = AppTypography.labelSmall,
                color = DfColors.TextSecondary,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
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
        targetValue = if (selected) DfColors.Purple else DfColors.Outline.copy(alpha = 0.3f),
        label = "matchBorder",
    )
    val bg by animateColorAsState(
        targetValue = if (selected) DfColors.PurpleContainer.copy(alpha = 0.45f) else DfColors.Surface,
        label = "matchBg",
    )
    val scoreColor = scoreAccent(match.score)
    val isCrm = match.source == "crm"
    val sourceAccent = if (isCrm) DfColors.Purple else DfColors.Blue

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onToggle),
        shape = AppShapes.Card,
        color = bg,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (selected) AppElevations.subtle else AppElevations.none,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (selected) DfColors.Purple else sourceAccent.copy(alpha = 0.35f)),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SelectionMark(selected = selected)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            match.title.orEmpty().ifBlank { "بدون عنوان" },
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        SourceBadge(isCrm = isCrm)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        match.priceLabel?.takeIf { it.isNotBlank() }?.let { price ->
                            Text(
                                price,
                                style = AppTypography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = DfColors.Green,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        matchIntentChip(match)
                    }

                    val location = listOfNotNull(
                        match.neighborhood?.takeIf { it.isNotBlank() },
                        match.city?.takeIf { it.isNotBlank() },
                    ).joinToString("، ")
                    if (location.isNotBlank()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                DfIcons.MapPin,
                                contentDescription = null,
                                tint = DfColors.TextMuted,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                location,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        match.area?.takeIf { it > 0 }?.let { area ->
                            MatchChip(
                                text = FormatUtils.formatArea(area.toInt()),
                                accent = DfColors.Blue,
                            )
                        }
                        match.rooms?.takeIf { it.isNotBlank() }?.let { rooms ->
                            MatchChip(text = rooms, accent = DfColors.Purple)
                        }
                        match.propertyType?.takeIf { it.isNotBlank() }?.let { type ->
                            MatchChip(text = type, accent = DfColors.TextSecondary)
                        }
                    }

                    if (match.reasons.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            match.reasons.take(4).forEach { reason ->
                                MatchChip(text = reason, accent = scoreColor, filled = false)
                            }
                        }
                    }
                }
                MatchScoreRing(score = match.score, accent = scoreColor)
            }
        }
    }
}

@Composable
private fun SourceBadge(isCrm: Boolean) {
    val label = if (isCrm) "شخصی" else "دیوار"
    val accent = if (isCrm) DfColors.Purple else DfColors.Blue
    Surface(shape = AppShapes.Chip, color = accent.copy(alpha = 0.12f)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

@Composable
private fun matchIntentChip(match: PropertyMatchDto) {
    val label = match.intentLabel?.takeIf { it.isNotBlank() }
        ?: match.matchIntent?.takeIf { it.isNotBlank() }
        ?: return
    Surface(shape = AppShapes.Chip, color = DfColors.AmberLight) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DfColors.Amber,
        )
    }
}

@Composable
private fun MatchScoreRing(score: Int, accent: Color) {
    val progress by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100) / 100f),
        label = "scoreProgress",
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(52.dp)) {
                val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(color = accent.copy(alpha = 0.15f), style = stroke)
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
        Text(
            scoreLabel(score),
            style = AppTypography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SelectionMark(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (selected) DfColors.Purple else DfColors.SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(DfIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun MatchChip(text: String, accent: Color, filled: Boolean = true) {
    Surface(
        shape = AppShapes.Chip,
        color = if (filled) accent.copy(alpha = 0.12f) else Color.Transparent,
        border = if (filled) null else BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MatchMessageSection(
    expanded: Boolean,
    note: String,
    templates: List<MessageTemplateDto>,
    templatesLoading: Boolean,
    showTemplatePicker: Boolean,
    isSubmitting: Boolean,
    selectedCount: Int,
    onNoteChange: (String) -> Unit,
    onToggleExpanded: () -> Unit,
    onToggleTemplatePicker: (Boolean) -> Unit,
    onApplyTemplate: (MessageTemplateDto) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.35f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .padding(vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(DfIcons.MessageCircle, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(18.dp))
                Column {
                    Text(
                        "پیام همراه پیشنهاد",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        if (selectedCount > 0) {
                            "همراه ${DateUtils.toPersianDigits(selectedCount.toString())} فایل انتخاب‌شده"
                        } else {
                            "اختیاری — برای شخصی‌سازی پیام"
                        },
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }
            Icon(
                if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                contentDescription = null,
                tint = DfColors.TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                DfTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = "متن پیام",
                    placeholder = "مثلاً: چند فایل نزدیک به بودجه شما انتخاب کردم — برای بازدید هماهنگ کنید",
                    singleLine = false,
                    minLines = 3,
                    enabled = !isSubmitting,
                )
                TemplateQuickPicker(
                    templates = templates,
                    templatesLoading = templatesLoading,
                    showAll = showTemplatePicker,
                    isSubmitting = isSubmitting,
                    onToggleShowAll = { onToggleTemplatePicker(!showTemplatePicker) },
                    onApplyTemplate = onApplyTemplate,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.TemplateQuickPicker(
    templates: List<MessageTemplateDto>,
    templatesLoading: Boolean,
    showAll: Boolean,
    isSubmitting: Boolean,
    onToggleShowAll: () -> Unit,
    onApplyTemplate: (MessageTemplateDto) -> Unit,
) {
    when {
        templatesLoading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = DfColors.Purple,
                    strokeWidth = 2.dp,
                )
            }
        }
        templates.isEmpty() -> {
            Text(
                "قالب پیامی ثبت نشده — متن را دستی بنویسید",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
        else -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "قالب‌های آماده",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextMuted,
                )
                DfGlassTextButton(
                    text = if (showAll) "کمتر" else "همه قالب‌ها",
                    onClick = onToggleShowAll,
                    compact = true,
                )
            }
            val visibleTemplates = if (showAll) templates else templates.take(4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visibleTemplates.forEach { template ->
                    Surface(
                        onClick = { if (!isSubmitting) onApplyTemplate(template) },
                        shape = AppShapes.CardSmall,
                        color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, DfColors.Outline.copy(alpha = 0.35f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .width(160.dp)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                template.title,
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DfColors.Purple,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                template.body,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
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

private fun scoreLabel(score: Int): String = when {
    score >= 85 -> "عالی"
    score >= 70 -> "خوب"
    score >= 50 -> "متوسط"
    else -> "ضعیف"
}
