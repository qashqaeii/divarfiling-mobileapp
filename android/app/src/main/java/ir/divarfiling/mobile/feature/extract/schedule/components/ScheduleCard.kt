package ir.divarfiling.mobile.feature.extract.schedule.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfGlassButton
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.network.ExtractionRunDto
import ir.divarfiling.mobile.core.network.ExtractionScheduleDto
import ir.divarfiling.mobile.feature.extract.schedule.scheduleDateTimeLabel
import ir.divarfiling.mobile.feature.extract.schedule.scheduleIntervalAccent
import ir.divarfiling.mobile.feature.extract.schedule.scheduleIntervalIcon
import ir.divarfiling.mobile.feature.extract.schedule.scheduleIntervalLabel
import ir.divarfiling.mobile.feature.extract.schedule.scheduleNextRunLabel
import ir.divarfiling.mobile.feature.extract.schedule.scheduleNextRunLabel
import ir.divarfiling.mobile.feature.extract.schedule.scheduleRelativeLabel
import ir.divarfiling.mobile.feature.extract.schedule.scheduleStatusStyle

@Composable
fun ScheduleCard(
    schedule: ExtractionScheduleDto,
    runs: List<ExtractionRunDto>?,
    onToggle: () -> Unit,
    onRunNow: () -> Unit,
    onDelete: () -> Unit,
    onToggleRuns: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = scheduleIntervalAccent(schedule.intervalHours)
    val isExpanded = runs != null

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfColors.Surface,
        shadowElevation = AppElevations.card,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        brush = if (schedule.isEnabled) {
                            Brush.horizontalGradient(
                                colors = listOf(accent, DfColors.PurpleGradientEnd),
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    DfColors.TextMuted.copy(alpha = 0.35f),
                                    DfColors.Outline,
                                ),
                            )
                        },
                    ),
            )

            Column(
                modifier = Modifier.padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        shape = AppShapes.IconContainer,
                        color = accent.copy(alpha = 0.14f),
                        modifier = Modifier.size(48.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = scheduleIntervalIcon(schedule.intervalHours),
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = schedule.title,
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = scheduleIntervalLabel(schedule.intervalHours),
                            style = AppTypography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "حداکثر ${DateUtils.toPersianDigits(schedule.maxItems.toString())} آگهی",
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Switch(
                            checked = schedule.isEnabled,
                            onCheckedChange = { onToggle() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accent,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = DfColors.Outline,
                            ),
                        )
                        Text(
                            text = if (schedule.isEnabled) "فعال" else "متوقف",
                            style = AppTypography.labelSmall,
                            color = if (schedule.isEnabled) accent else DfColors.TextMuted,
                        )
                    }
                }

                ScheduleFilterChips(schedule = schedule)

                ScheduleTimelinePanel(schedule = schedule)

                ScheduleStatusRow(schedule = schedule)

                schedule.lastError?.takeIf { it.isNotBlank() }?.let { error ->
                    Surface(
                        shape = AppShapes.Chip,
                        color = DfColors.RoseLight,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                imageVector = DfIcons.X,
                                contentDescription = null,
                                tint = DfColors.Rose,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = error,
                                style = AppTypography.labelSmall,
                                color = DfColors.Rose,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    DfGlassButton(
                        text = "اجرا الان",
                        onClick = onRunNow,
                        icon = DfIcons.Play,
                        variant = DfGlassButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        DfGlassButton(
                            text = if (isExpanded) "بستن تاریخچه" else "تاریخچه اجرا",
                            onClick = onToggleRuns,
                            icon = if (isExpanded) DfIcons.ChevronUp else DfIcons.Clock,
                            modifier = Modifier.weight(1f),
                        )
                        DfGlassButton(
                            text = "حذف",
                            onClick = onDelete,
                            icon = DfIcons.X,
                            accent = DfColors.Rose,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    ScheduleRunHistory(runs = runs.orEmpty())
                }
            }
        }
    }
}

@Composable
private fun ScheduleFilterChips(schedule: ExtractionScheduleDto) {
    val chips = buildList {
        schedule.filters.cityName?.takeIf { it.isNotBlank() }?.let { add(it to DfIcons.MapPin) }
        schedule.filters.districtNames
            ?.filter { it.isNotBlank() }
            ?.joinToString("، ")
            ?.takeIf { it.isNotBlank() }
            ?.let { add(it to DfIcons.Compass) }
        schedule.filters.categoryLabel?.takeIf { it.isNotBlank() }?.let { add(it to DfIcons.Tag) }
        schedule.filters.transactionTypeLabel?.takeIf { it.isNotBlank() }?.let { add(it to DfIcons.Filter) }
    }
    if (chips.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        chips.take(4).forEach { (label, icon) ->
            Surface(
                shape = AppShapes.Chip,
                color = DfColors.SurfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DfColors.TextMuted,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = label,
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleTimelinePanel(schedule: ExtractionScheduleDto) {
    val isRunning = schedule.lastStatus == "running"
    Surface(
        shape = AppShapes.Card,
        color = DfColors.PurpleContainer.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isRunning) {
                ScheduleTimelineRow(
                    icon = DfIcons.RefreshCw,
                    title = "وضعیت",
                    relative = "در حال اجرا…",
                    absolute = schedule.lastRunAt?.let(::scheduleDateTimeLabel) ?: "شروع شده",
                    accent = DfColors.Blue,
                )
            } else {
                schedule.nextRunAt?.takeIf { schedule.isEnabled }?.let { nextRun ->
                    ScheduleTimelineRow(
                        icon = DfIcons.AlarmClock,
                        title = "اجرای بعدی",
                        relative = scheduleNextRunLabel(nextRun),
                        absolute = scheduleDateTimeLabel(nextRun),
                        accent = DfColors.Purple,
                    )
                }
            }
            schedule.lastRunAt?.takeIf { !isRunning }?.let { lastRun ->
                ScheduleTimelineRow(
                    icon = DfIcons.RefreshCw,
                    title = "آخرین اجرا",
                    relative = scheduleRelativeLabel(lastRun),
                    absolute = scheduleDateTimeLabel(lastRun),
                    accent = DfColors.TextMuted,
                )
            }
            if (!isRunning && schedule.nextRunAt.isNullOrBlank() && schedule.lastRunAt.isNullOrBlank()) {
                Text(
                    text = "هنوز اجرایی ثبت نشده",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }
    }
}

@Composable
private fun ScheduleTimelineRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    relative: String?,
    absolute: String,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(16.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = AppTypography.labelSmall,
                color = DfColors.TextSecondary,
            )
            relative?.let {
                Text(
                    text = it,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }
            Text(
                text = absolute,
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
    }
}

@Composable
private fun ScheduleStatusRow(schedule: ExtractionScheduleDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        schedule.lastStatus?.let { status ->
            val style = scheduleStatusStyle(status)
            Surface(
                shape = AppShapes.Chip,
                color = style.background,
            ) {
                Text(
                    text = style.label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = AppTypography.labelSmall,
                    color = style.color,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Surface(
            shape = AppShapes.Chip,
            color = DfColors.SurfaceVariant,
        ) {
            Text(
                text = "${DateUtils.toPersianDigits(schedule.runCount.toString())} اجرا",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = AppTypography.labelSmall,
                color = DfColors.TextSecondary,
            )
        }
        if (schedule.consecutiveFailures > 0) {
            Surface(
                shape = AppShapes.Chip,
                color = DfColors.RoseLight,
            ) {
                Text(
                    text = "${DateUtils.toPersianDigits(schedule.consecutiveFailures.toString())} خطا",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = AppTypography.labelSmall,
                    color = DfColors.Rose,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ScheduleRunHistory(runs: List<ExtractionRunDto>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "تاریخچه اجرا",
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
        )
        if (runs.isEmpty()) {
            Text(
                text = "اجرایی ثبت نشده",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
            return
        }
        runs.take(5).forEachIndexed { index, run ->
            ScheduleRunRow(run = run, showConnector = index < runs.take(5).lastIndex)
        }
    }
}

@Composable
private fun ScheduleRunRow(
    run: ExtractionRunDto,
    showConnector: Boolean,
) {
    val style = scheduleStatusStyle(run.status.orEmpty())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(style.color),
            )
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(DfColors.Outline),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = style.label,
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = style.color,
                )
                Text(
                    text = "${DateUtils.toPersianDigits(run.ingestedCount.toString())} آگهی",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            run.startedAt?.let { started ->
                Text(
                    text = scheduleDateTimeLabel(started),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            run.error?.takeIf { it.isNotBlank() }?.let { error ->
                Text(
                    text = error,
                    style = AppTypography.labelSmall,
                    color = DfColors.Rose,
                )
            }
        }
    }
}
