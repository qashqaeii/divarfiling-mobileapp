package ir.divarfiling.mobile.feature.crm.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.feature.crm.components.ContactReminderSheet
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmCalendarScreen(
    onBack: () -> Unit,
    onOpenContact: (Long) -> Unit = {},
    viewModel: CrmCalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.takeIf { state.showCreateSheet.not() }?.let {
            snackbar.showSnackbar(it); viewModel.clearMessage()
        }
    }

    if (state.showCreateSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleCreateSheet(false) }) {
            ContactReminderSheet(
                title = state.draftTitle,
                note = state.draftNote,
                dueMillis = state.draftDueMillis,
                recurrence = state.draftRecurrence,
                isSubmitting = state.isSubmitting,
                sheetTitle = "یادآور جدید",
                onTitleChange = viewModel::onDraftTitle,
                onNoteChange = viewModel::onDraftNote,
                onDueChange = viewModel::onDraftDue,
                onRecurrenceChange = viewModel::onDraftRecurrence,
                onDismiss = { viewModel.toggleCreateSheet(false) },
                onSubmit = viewModel::createReminder,
            )
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            DfExtendedFab(
                text = "یادآور",
                icon = DfIcons.Plus,
                onClick = { viewModel.toggleCreateSheet(true) },
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance + AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "تقویم",
                        subtitle = "یادآورها و پیگیری‌های روزانه",
                        titleIconRes = DfDecorIcons.Calendar,
                        onBack = onBack,
                    )
                }
                item {
                    ModeChips(
                        mode = state.mode,
                        onSelect = viewModel::setMode,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                state.error?.let { error ->
                    item {
                        DfErrorBanner(error, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                    }
                }
                item {
                    when (state.mode) {
                        CalendarMode.Month -> MonthGrid(
                            visibleMonth = state.visibleMonth,
                            selected = state.selectedDate,
                            counts = state.monthDayCounts,
                            onPrev = { viewModel.shiftMonth(-1) },
                            onNext = { viewModel.shiftMonth(1) },
                            onSelect = viewModel::selectDate,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                        CalendarMode.Week -> WeekStrip(
                            selected = state.selectedDate,
                            counts = state.monthDayCounts,
                            onPrev = { viewModel.shiftWeek(-1) },
                            onNext = { viewModel.shiftWeek(1) },
                            onSelect = viewModel::selectDate,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                        CalendarMode.Day -> DayHeader(
                            selected = state.selectedDate,
                            onPrev = { viewModel.selectDate(state.selectedDate.minusDays(1)) },
                            onNext = { viewModel.selectDate(state.selectedDate.plusDays(1)) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                item {
                    Text(
                        text = "رویدادهای ${state.selectedDate.toJalaliLabel()}",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                if (state.selectedDayReminders.isEmpty() && !state.isLoading) {
                    item {
                        DfEmptyState(
                            title = "یادآوری برای این روز نیست",
                            subtitle = "با دکمه یادآور، یک پیگیری جدید بسازید.",
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(state.selectedDayReminders, key = { it.id ?: it.title }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onComplete = { reminder.id?.let(viewModel::completeReminder) },
                            onSnooze = { reminder.id?.let { viewModel.snoozeReminder(it) } },
                            onOpenContact = { id -> onOpenContact(id) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeChips(
    mode: CalendarMode,
    onSelect: (CalendarMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            CalendarMode.Day to "روز",
            CalendarMode.Week to "هفته",
            CalendarMode.Month to "ماه",
        ).forEach { (value, label) ->
            FilterChip(
                selected = mode == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun MonthGrid(
    visibleMonth: LocalDate,
    selected: LocalDate,
    counts: Map<LocalDate, Int>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val first = visibleMonth.withDayOfMonth(1)
    val daysInMonth = first.lengthOfMonth()
    val lead = first.dayOfWeek.value % 7
    val cells = buildList {
        repeat(lead) { add(null) }
        for (d in 1..daysInMonth) add(first.withDayOfMonth(d))
    }
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfTextButton(text = "قبلی", onClick = onPrev, compact = true)
                Text(visibleMonth.toJalaliMonthTitle(), style = AppTypography.bodyDescription, fontWeight = FontWeight.Bold)
                DfTextButton(text = "بعدی", onClick = onNext, compact = true)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { d ->
                    Text(
                        d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        day == null -> DfColors.Surface
                                        day == selected -> DfColors.PurpleContainer
                                        else -> DfColors.SurfaceVariant.copy(alpha = 0.35f)
                                    },
                                )
                                .then(
                                    if (day != null) Modifier.clickable { onSelect(day) } else Modifier,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (day != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val (_, _, jd) = DateUtils.gregorianToJalali(
                                        day.year,
                                        day.monthValue,
                                        day.dayOfMonth,
                                    )
                                    Text(
                                        DateUtils.toPersianDigits(jd.toString()),
                                        style = AppTypography.labelSmall,
                                        fontWeight = if (day == selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (day == selected) DfColors.PurpleDark else DfColors.TextPrimary,
                                    )
                                    val count = counts[day] ?: 0
                                    if (count > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(DfColors.Purple),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    repeat(7 - week.size) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekStrip(
    selected: LocalDate,
    counts: Map<LocalDate, Int>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val start = selected.minusDays((selected.dayOfWeek.value % 7).toLong())
    val days = (0..6).map { start.plusDays(it.toLong()) }
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DfTextButton(text = "هفته قبل", onClick = onPrev, compact = true)
                DfTextButton(text = "هفته بعد", onClick = onNext, compact = true)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                days.forEach { day ->
                    val selectedDay = day == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedDay) DfColors.PurpleContainer else DfColors.SurfaceVariant.copy(alpha = 0.4f))
                            .clickable { onSelect(day) }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val (_, _, jd) = DateUtils.gregorianToJalali(day.year, day.monthValue, day.dayOfMonth)
                        Text(
                            DateUtils.toPersianDigits(jd.toString()),
                            style = AppTypography.bodyDescription,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedDay) DfColors.PurpleDark else DfColors.TextPrimary,
                        )
                        if ((counts[day] ?: 0) > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DfColors.Purple),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(
    selected: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DfTextButton(text = "روز قبل", onClick = onPrev, compact = true)
        Text(selected.toJalaliLabel(), style = AppTypography.bodyDescription, fontWeight = FontWeight.Bold)
        DfTextButton(text = "روز بعد", onClick = onNext, compact = true)
    }
}

@Composable
private fun ReminderCard(
    reminder: ReminderDto,
    onComplete: () -> Unit,
    onSnooze: () -> Unit,
    onOpenContact: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(reminder.title, style = AppTypography.cardTitle, fontWeight = FontWeight.SemiBold)
            if (reminder.contactName.isNotBlank()) {
                Text(
                    reminder.contactName,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    modifier = Modifier.clickable {
                        reminder.contactId?.let(onOpenContact)
                    },
                )
            }
            reminder.dueAt?.let {
                Text(DateUtils.formatForDisplay(it), style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            if (reminder.recurrence.isNotBlank()) {
                Text(
                    ReminderRecurrenceLabel(reminder.recurrence),
                    style = AppTypography.labelSmall,
                    color = DfColors.Purple,
                )
            }
            if (!reminder.note.isNullOrBlank()) {
                Text(reminder.note, style = AppTypography.labelSmall, color = DfColors.TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DfTextButton(text = "انجام شد", onClick = onComplete, compact = true)
                DfTextButton(text = "تعویق ۱ ساعته", onClick = onSnooze, compact = true)
            }
        }
    }
}

private fun ReminderRecurrenceLabel(value: String): String = when (value) {
    "daily" -> "تکرار روزانه"
    "weekly" -> "تکرار هفتگی"
    "biweekly" -> "تکرار دو هفته‌ای"
    "monthly" -> "تکرار ماهانه"
    else -> "یک‌بار"
}
