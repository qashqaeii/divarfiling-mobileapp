package ir.divarfiling.mobile.feature.extract.schedule

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.network.ExtractionRunDto
import ir.divarfiling.mobile.core.network.ExtractionScheduleDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExtractSchedulesScreen(
    onBack: () -> Unit,
    viewModel: ExtractSchedulesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = { DfTopBar(title = "زمان‌بندی استخراج", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> DfCardListSkeleton(count = 4, itemHeight = 140.dp)
                state.schedules.isEmpty() -> DfEmptyState(
                    title = "زمان‌بندی فعالی ندارید",
                    subtitle = "از صفحه استخراج، فیلترها را ذخیره کنید تا هر چند ساعت یک‌بار خودکار اجرا شود",
                )
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text(
                                "استخراج روی همین دستگاه اجرا می‌شود. برای اجرای به‌موقع، اعلان‌ها را فعال نگه دارید.",
                                style = AppTypography.bodyDescription,
                                color = DfColors.TextMuted,
                            )
                        }
                        items(state.schedules, key = { it.id }) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                runs = state.expandedRuns[schedule.id],
                                onToggle = { viewModel.toggleSchedule(schedule.id) },
                                onRunNow = { viewModel.runNow(schedule.id) },
                                onDelete = { viewModel.deleteSchedule(schedule.id) },
                                onToggleRuns = { viewModel.loadRuns(schedule.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: ExtractionScheduleDto,
    runs: List<ExtractionRunDto>?,
    onToggle: () -> Unit,
    onRunNow: () -> Unit,
    onDelete: () -> Unit,
    onToggleRuns: () -> Unit,
) {
    DfPremiumCard {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(schedule.title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                    Text(
                        "هر ${formatInterval(schedule.intervalHours)} ساعت — حداکثر ${schedule.maxItems} آگهی",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                    )
                }
                Switch(checked = schedule.isEnabled, onCheckedChange = { onToggle() })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                schedule.lastStatus?.let { statusBadge(it) }
                if (schedule.consecutiveFailures > 0) {
                    DfBadge("${schedule.consecutiveFailures} خطا", color = DfColors.RoseLight, textColor = DfColors.Rose)
                }
            }
            schedule.nextRunAt?.takeIf { schedule.isEnabled }?.let {
                Text("اجرای بعدی: ${formatDateTime(it)}", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            schedule.lastRunAt?.let {
                Text("آخرین اجرا: ${formatDateTime(it)}", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            schedule.lastError?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.labelSmall, color = DfColors.Rose)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onRunNow) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("اجرا الان")
                }
                TextButton(onClick = onToggleRuns) {
                    Text(if (runs == null) "تاریخچه" else "بستن تاریخچه")
                }
                TextButton(onClick = onDelete) { Text("حذف") }
            }
            runs?.let { history ->
                if (history.isEmpty()) {
                    Text("اجرایی ثبت نشده", style = AppTypography.labelSmall)
                } else {
                    history.take(5).forEach { run ->
                        RunRow(run)
                    }
                }
            }
        }
    }
}

@Composable
private fun RunRow(run: ExtractionRunDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(run.status ?: "—", style = AppTypography.labelSmall)
        Text(
            "${run.ingestedCount} آگهی",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
        )
    }
    run.error?.takeIf { it.isNotBlank() }?.let {
        Text(it, style = AppTypography.labelSmall, color = DfColors.Rose)
    }
}

@Composable
private fun statusBadge(status: String) {
    val (label, color) = when (status) {
        "success" -> "موفق" to DfColors.Green
        "failed" -> "ناموفق" to DfColors.Rose
        "running" -> "در حال اجرا" to DfColors.Blue
        "queued" -> "در صف" to DfColors.Amber
        "skipped_limit" -> "سقف روزانه" to DfColors.Amber
        else -> status to DfColors.TextMuted
    }
    DfBadge(label, color = color.copy(alpha = 0.15f), textColor = color)
}

private fun formatInterval(hours: Double): String {
    return if (hours >= 1) {
        if (hours % 1.0 == 0.0) hours.toInt().toString() else hours.toString()
    } else {
        (hours * 60).toInt().toString() + " دقیقه‌ای"
    }
}

private fun formatDateTime(iso: String): String {
    return try {
        val dt = Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDateTime()
        dt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
    } catch (_: Exception) {
        iso
    }
}
