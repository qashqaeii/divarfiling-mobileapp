package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.network.PropertyContactMatchItemDto
import ir.divarfiling.mobile.core.network.PropertyContactMatchesData
import ir.divarfiling.mobile.feature.crm.ContactShareChannel
import ir.divarfiling.mobile.feature.crm.ContactSocialShare

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyContactMatchesSheet(
    visible: Boolean,
    matches: PropertyContactMatchesData?,
    isLoading: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSuggest: (List<PropertyContactMatchItemDto>, shareChannel: String?) -> Unit,
) {
    if (!visible) return

    val allMatches = remember(matches) {
        matches?.matches.orEmpty().sortedByDescending { it.score }
    }
    var selected by remember(allMatches) { mutableStateOf(emptySet<Long>()) }
    val selectedMatches = remember(allMatches, selected) {
        allMatches.filter { it.customerId in selected }
    }
    val selectedCount = selected.size
    val allSelected = allMatches.isNotEmpty() && selectedCount == allMatches.size
    val canSubmit = selectedCount > 0 && !isSubmitting
    val topMatches = remember(allMatches) { allMatches.take(3) }

    val shareChannels = remember(selectedMatches) {
        if (selectedMatches.size == 1) {
            val match = selectedMatches.first()
            ContactSocialShare.resolveMessagingChannels(
                match.phone,
                match.activeChannels,
                match.socialLinks,
            )
        } else {
            emptyList()
        }
    }

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "مخاطب‌های پیشنهادی",
            subtitle = "مشتریانی که با این فایل شخصی هم‌خوانی دارند — انتخاب کنید و ارسال کنید",
            icon = DfIcons.Users,
            iconContainerColor = DfColors.BlueLight,
            iconTint = DfColors.Blue,
            onClose = onDismiss,
            bodyHeightFraction = 0.84f,
            footer = if (!isLoading && matches?.eligible != false && matches?.forbidden != true && allMatches.isNotEmpty()) {
                {
                    PropertyMatchSuggestFooter(
                        selectedCount = selectedCount,
                        totalCount = allMatches.size,
                        allSelected = allSelected,
                        canSubmit = canSubmit,
                        isSubmitting = isSubmitting,
                        shareChannels = shareChannels,
                        onToggleSelectAll = {
                            selected = if (allSelected) emptySet() else allMatches.map { it.customerId }.toSet()
                        },
                        onSelectTop = {
                            selected = topMatches.map { it.customerId }.toSet()
                        },
                        onSuggest = { onSuggest(selectedMatches, null) },
                        onSuggestViaChannel = { channel -> onSuggest(selectedMatches, channel) },
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
                            "در حال یافتن مشتریان مناسب…",
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextSecondary,
                        )
                    }
                }
                matches?.eligible == false || matches?.forbidden == true -> {
                    DfEmptyState(
                        title = "تطبیق فعال نیست",
                        subtitle = matches?.message ?: "برای این فایل پیشنهاد هوشمند در دسترس نیست.",
                        variant = DfEmptyVariant.Locked,
                    )
                }
                allMatches.isEmpty() -> {
                    DfEmptyState(
                        title = "مشتری مناسبی پیدا نشد",
                        subtitle = "هنوز مخاطبی با بودجه و نیاز هم‌خوان با این فایل در CRM نیست.",
                        variant = DfEmptyVariant.NoResults,
                    )
                }
                else -> {
                    PropertyMatchInsightStrip(
                        total = allMatches.size,
                        topScore = allMatches.maxOfOrNull { it.score } ?: 0,
                        selectedCount = selectedCount,
                    )
                    allMatches.forEach { match ->
                        PropertySmartMatchCard(
                            match = match,
                            selected = match.customerId in selected,
                            onToggle = {
                                selected = if (match.customerId in selected) {
                                    selected - match.customerId
                                } else {
                                    selected + match.customerId
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyMatchSuggestFooter(
    selectedCount: Int,
    totalCount: Int,
    allSelected: Boolean,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    shareChannels: List<ContactShareChannel>,
    onToggleSelectAll: () -> Unit,
    onSelectTop: () -> Unit,
    onSuggest: () -> Unit,
    onSuggestViaChannel: (String) -> Unit,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (selectedCount == 0) {
                        "مشتریان مناسب را انتخاب کنید"
                    } else {
                        "${DateUtils.toPersianDigits(selectedCount.toString())} مشتری از ${DateUtils.toPersianDigits(totalCount.toString())}"
                    },
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedCount > 0) DfColors.Blue else DfColors.TextSecondary,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                DfGlassTextButton(text = "۳ برتر", onClick = onSelectTop, compact = true)
                DfGlassTextButton(
                    text = if (allSelected) "لغو انتخاب" else "انتخاب همه",
                    onClick = onToggleSelectAll,
                    compact = true,
                )
            }
        }

        if (selectedCount > 0) {
            LinearProgressIndicator(
                progress = { selectedCount.toFloat() / totalCount.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(AppShapes.Chip),
                color = DfColors.Blue,
                trackColor = DfColors.BlueLight.copy(alpha = 0.45f),
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

        if (shareChannels.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    "ثبت و ارسال در",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextMuted,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    shareChannels.forEach { channel ->
                        PropertySocialShareButton(
                            channel = channel,
                            enabled = canSubmit,
                            onClick = { onSuggestViaChannel(channel.key) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyMatchInsightStrip(total: Int, topScore: Int, selectedCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Card)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        DfColors.Blue.copy(alpha = 0.06f),
                        DfColors.Purple.copy(alpha = 0.04f),
                        Color.Transparent,
                    ),
                ),
            )
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DfColors.Blue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        DfIcons.WandSparkles,
                        contentDescription = null,
                        tint = DfColors.Blue,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "تحلیل هوشمند مخاطبان",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        "بر اساس بودجه، محله و نیاز مشتری",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }
            if (selectedCount > 0) {
                Surface(shape = AppShapes.Chip, color = DfColors.Blue.copy(alpha = 0.12f)) {
                    Text(
                        "${DateUtils.toPersianDigits(selectedCount.toString())} انتخاب",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Blue,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PropertyInsightStat(
                label = "مشتری",
                value = DateUtils.toPersianDigits(total.toString()),
                accent = DfColors.Blue,
                modifier = Modifier.weight(1f),
            )
            PropertyInsightStat(
                label = "بهترین امتیاز",
                value = DateUtils.toPersianDigits(topScore.toString()),
                accent = propertyScoreAccent(topScore),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PropertyInsightStat(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(DfColors.SurfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = AppSpacing.xs, vertical = 8.dp),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertySmartMatchCard(
    match: PropertyContactMatchItemDto,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) DfColors.Blue else DfColors.Outline.copy(alpha = 0.25f),
        label = "contactMatchBorder",
    )
    val bg by animateColorAsState(
        targetValue = if (selected) DfColors.BlueLight.copy(alpha = 0.55f) else DfColors.Surface,
        label = "contactMatchBg",
    )
    val scoreColor = propertyScoreAccent(match.score)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onToggle),
        shape = AppShapes.Card,
        color = bg,
        border = BorderStroke(if (selected) 1.dp else 0.5.dp, borderColor),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(if (selected) DfColors.Blue else scoreColor.copy(alpha = 0.45f)),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PropertySelectionMark(selected = selected)
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
                            match.displayName(),
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        match.customerType?.takeIf { it.isNotBlank() }?.let { type ->
                            PropertyMatchChip(text = type, accent = DfColors.Blue)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        match.budgetLabel?.takeIf { it.isNotBlank() }?.let { budget ->
                            Text(
                                budget,
                                style = AppTypography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = DfColors.Green,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        match.intentLabel?.takeIf { it.isNotBlank() }?.let { intent ->
                            PropertyMatchChip(text = intent, accent = DfColors.Purple, filled = false)
                        }
                    }

                    val meta = listOfNotNull(
                        match.phone?.takeIf { it.isNotBlank() },
                        match.areas?.takeIf { it.isNotBlank() },
                    ).joinToString(" · ")
                    if (meta.isNotBlank()) {
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
                                meta,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        match.propertyType?.takeIf { it.isNotBlank() }?.let { type ->
                            PropertyMatchChip(text = type, accent = DfColors.TextSecondary)
                        }
                        match.status?.takeIf { it.isNotBlank() }?.let { status ->
                            PropertyMatchChip(text = status, accent = DfColors.Amber, filled = false)
                        }
                    }

                    if (match.reasons.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            match.reasons.take(4).forEach { reason ->
                                PropertyMatchChip(text = reason, accent = scoreColor, filled = false)
                            }
                        }
                    }
                }
                PropertyMatchScoreRing(score = match.score, accent = scoreColor)
            }
        }
    }
}

@Composable
private fun PropertySelectionMark(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) DfColors.Blue else DfColors.SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(DfIcons.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun PropertyMatchChip(
    text: String,
    accent: Color,
    filled: Boolean = true,
) {
    Surface(
        shape = AppShapes.Chip,
        color = if (filled) accent.copy(alpha = 0.12f) else Color.Transparent,
        border = if (filled) null else BorderStroke(0.5.dp, accent.copy(alpha = 0.35f)),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = AppTypography.labelSmall,
            color = if (filled) accent else DfColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PropertyMatchScoreRing(score: Int, accent: Color) {
    val progress by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100) / 100f),
        label = "contactScoreProgress",
    )
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
}

@Composable
internal fun PropertySocialShareButton(
    channel: ContactShareChannel,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            color = channel.accent.copy(alpha = if (enabled) 0.14f else 0.07f),
            border = BorderStroke(0.5.dp, channel.accent.copy(alpha = if (enabled) 0.35f else 0.18f)),
            modifier = Modifier.size(52.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    channel.iconRes != null -> {
                        Icon(
                            painter = painterResource(channel.iconRes),
                            contentDescription = channel.label,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    channel.emoji != null -> {
                        Text(channel.emoji, style = AppTypography.sectionTitle)
                    }
                    channel.vectorIcon != null -> {
                        Icon(
                            channel.vectorIcon,
                            contentDescription = channel.label,
                            tint = channel.accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
        Text(
            channel.label,
            style = AppTypography.labelSmall,
            color = if (enabled) DfColors.TextSecondary else DfColors.TextMuted,
            maxLines = 1,
        )
    }
}

private fun propertyScoreAccent(score: Int): Color = when {
    score >= 75 -> DfColors.Green
    score >= 50 -> DfColors.Amber
    else -> DfColors.TextMuted
}
