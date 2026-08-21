package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.network.PropertyContactMatchItemDto
import ir.divarfiling.mobile.core.network.PropertyContactMatchesData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyContactMatchesSheet(
    visible: Boolean,
    matches: PropertyContactMatchesData?,
    isLoading: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSuggest: (List<PropertyContactMatchItemDto>) -> Unit,
) {
    if (!visible) return

    val allMatches = remember(matches) {
        matches?.matches.orEmpty().sortedByDescending { it.score }
    }
    var selected by remember(allMatches) { mutableStateOf(emptySet<Long>()) }
    val selectedCount = selected.size
    val allSelected = allMatches.isNotEmpty() && selectedCount == allMatches.size
    val canSubmit = selectedCount > 0 && !isSubmitting

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "مخاطب‌های پیشنهادی",
            subtitle = "مشتریانی که با این فایل شخصی هم‌خوانی دارند",
            icon = DfIcons.Users,
            iconContainerColor = DfColors.BlueLight,
            iconTint = DfColors.Blue,
            onClose = onDismiss,
            footer = if (!isLoading && matches?.eligible != false && matches?.forbidden != true && allMatches.isNotEmpty()) {
                {
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
                                    "مشتریان مناسب را انتخاب کنید"
                                } else {
                                    "${DateUtils.toPersianDigits(selectedCount.toString())} از ${DateUtils.toPersianDigits(allMatches.size.toString())} انتخاب‌شده"
                                },
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                modifier = Modifier.weight(1f),
                            )
                            DfGlassTextButton(
                                text = if (allSelected) "لغو همه" else "انتخاب همه",
                                onClick = {
                                    selected = if (allSelected) {
                                        emptySet()
                                    } else {
                                        allMatches.map { it.customerId }.toSet()
                                    }
                                },
                                compact = true,
                            )
                        }
                        DfPrimaryButton(
                            text = if (selectedCount > 0) {
                                "ثبت پیشنهاد (${DateUtils.toPersianDigits(selectedCount.toString())})"
                            } else {
                                "ثبت پیشنهاد"
                            },
                            onClick = {
                                onSuggest(allMatches.filter { it.customerId in selected })
                            },
                            enabled = canSubmit,
                            loading = isSubmitting,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
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
private fun PropertyMatchInsightStrip(total: Int, topScore: Int) {
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
                            DfColors.Blue.copy(alpha = 0.12f),
                            DfColors.Purple.copy(alpha = 0.08f),
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
                        tint = DfColors.Blue,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        "تحلیل هوشمند مخاطبان",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Blue,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertySmartMatchCard(
    match: PropertyContactMatchItemDto,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) DfColors.Blue else DfColors.Outline.copy(alpha = 0.35f),
        label = "contactMatchBorder",
    )
    val bg by animateColorAsState(
        targetValue = if (selected) {
            DfColors.BlueLight.copy(alpha = 0.75f)
        } else {
            DfColors.Surface
        },
        label = "contactMatchBg",
    )
    val scoreColor = propertyScoreAccent(match.score)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = AppShapes.CardSmall,
        color = bg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    match.fullName.orEmpty().ifBlank { "بدون نام" },
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    match.customerType?.takeIf { it.isNotBlank() },
                    match.phone?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(meta, style = AppTypography.labelSmall, color = DfColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (match.reasons.isNotEmpty()) {
                    Text(
                        match.reasons.take(3).joinToString("  ·  "),
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                DateUtils.toPersianDigits(match.score.toString()),
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = scoreColor,
            )
        }
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

private fun propertyScoreAccent(score: Int): Color = when {
    score >= 75 -> DfColors.Green
    score >= 50 -> DfColors.Amber
    else -> DfColors.TextMuted
}
