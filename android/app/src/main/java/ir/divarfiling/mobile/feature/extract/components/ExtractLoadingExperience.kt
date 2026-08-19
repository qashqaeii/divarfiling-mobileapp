package ir.divarfiling.mobile.feature.extract.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import kotlinx.coroutines.delay

@Composable
fun ExtractLoadingExperience(
    phase: ExtractPhase,
    progressCurrent: Int,
    progressTotal: Int,
    modifier: Modifier = Modifier,
    batchJobLabel: String? = null,
    batchJobIndex: Int = 0,
    batchJobTotal: Int = 0,
) {
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val itemProgress = if (progressTotal > 0) progressCurrent.toFloat() / progressTotal else 0f
    val phaseProgress = phaseProgressValue(phase, itemProgress)
    val barProgress = if (progressTotal > 0) {
        itemProgress.coerceIn(0f, 1f)
    } else {
        (phaseProgress / 100f).coerceIn(0f, 1f)
    }
    val percentLabel = if (progressTotal > 0) {
        "${(itemProgress * 100).toInt().coerceIn(0, 100)}٪"
    } else {
        "${phaseProgress.toInt()}٪"
    }
    val remainingSeconds = estimateRemainingSeconds(phase, itemProgress, elapsedSeconds)

    val pulse by rememberInfiniteTransition(label = "extractPulse").animateFloat(
        initialValue = 0.96f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseScale",
    )

    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                    Text(
                        if (batchJobTotal > 1) "استخراج گروهی…" else "در حال استخراج…",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    if (!batchJobLabel.isNullOrBlank()) {
                        Text(
                            if (batchJobTotal > 1) {
                                "دسته ${DateUtils.toPersianDigits(batchJobIndex.toString())} از ${DateUtils.toPersianDigits(batchJobTotal.toString())} — $batchJobLabel"
                            } else {
                                batchJobLabel
                            },
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.primary(),
                        )
                    }
                    Text(
                        "$percentLabel · ${formatDuration(elapsedSeconds)}",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                Box(
                    modifier = Modifier
                        .scale(pulse)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(DfThemeColors.primary()),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp,
                        color = DfThemeColors.surface(),
                    )
                }
            }

            LinearProgressIndicator(
                progress = { barProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.Chip),
                color = DfThemeColors.primary(),
                trackColor = DfThemeColors.surfaceVariant(),
            )

            if (progressTotal > 0) {
                Text(
                    "$progressCurrent از $progressTotal آگهی",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfThemeColors.textPrimary(),
                )
            }

            Text(
                "زمان باقی‌مانده تقریبی: ${formatDuration(remainingSeconds)}",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                ExtractPhase.entries.forEach { step ->
                    ExtractPhaseRow(
                        step = step,
                        current = phase,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExtractPhaseRow(step: ExtractPhase, current: ExtractPhase) {
    val state = when {
        step.ordinal < current.ordinal -> PhaseRowState.Done
        step.ordinal == current.ordinal -> PhaseRowState.Active
        else -> PhaseRowState.Pending
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when (state) {
                        PhaseRowState.Done -> DfThemeColors.successContainer()
                        PhaseRowState.Active -> DfThemeColors.primaryContainer()
                        PhaseRowState.Pending -> DfThemeColors.surfaceVariant()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                PhaseRowState.Done -> Icon(
                    DfIcons.CircleCheck,
                    null,
                    tint = DfThemeColors.success(),
                    modifier = Modifier.size(18.dp),
                )
                PhaseRowState.Active -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = DfThemeColors.primary(),
                )
                PhaseRowState.Pending -> Icon(
                    step.icon,
                    null,
                    tint = DfThemeColors.textMuted(),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Column {
            Text(
                step.label,
                style = AppTypography.bodyDescription,
                fontWeight = if (state == PhaseRowState.Active) FontWeight.Bold else FontWeight.Normal,
                color = when (state) {
                    PhaseRowState.Done -> DfThemeColors.success()
                    PhaseRowState.Active -> DfThemeColors.onPrimaryContainer()
                    PhaseRowState.Pending -> DfThemeColors.textMuted()
                },
            )
            if (state == PhaseRowState.Active) {
                Text(
                    step.hint,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                )
            }
        }
    }
}

private enum class PhaseRowState { Done, Active, Pending }

enum class ExtractPhase(val label: String, val hint: String, val icon: ImageVector) {
    Connecting("اتصال", "برقراری ارتباط امن", DfIcons.Compass),
    Preparing("آماده‌سازی", "پیکربندی فیلترها", DfIcons.Search),
    LaunchingDivar("اجرای دیوار", "ورود به جستجوی دیوار", DfIcons.ExternalLink),
    Extracting("استخراج", "دریافت آگهی‌ها", DfIcons.Cloud),
    Downloading("دانلود", "بارگیری جزئیات", DfIcons.Download),
    Saving("ذخیره", "آپلود به فایلینگ", DfIcons.Upload),
    Completed("تکمیل", "عملیات با موفقیت انجام شد", DfIcons.CircleCheck),
}

fun extractPhaseFromProgress(current: Int, total: Int, isRunning: Boolean): ExtractPhase {
    if (!isRunning && current > 0 && total > 0 && current >= total) return ExtractPhase.Completed
    if (!isRunning) return ExtractPhase.Connecting
    if (total <= 0) {
        return when {
            current == 0 -> ExtractPhase.Connecting
            else -> ExtractPhase.Preparing
        }
    }
    val ratio = current.toFloat() / total
    return when {
        ratio < 0.02f -> ExtractPhase.LaunchingDivar
        ratio < 0.85f -> ExtractPhase.Extracting
        ratio < 0.95f -> ExtractPhase.Downloading
        else -> ExtractPhase.Saving
    }
}

private fun phaseProgressValue(phase: ExtractPhase, itemProgress: Float): Float {
    val base = phase.ordinal * 14.28f
    val inner = when (phase) {
        ExtractPhase.Extracting -> itemProgress * 40f
        ExtractPhase.Downloading -> itemProgress * 10f
        ExtractPhase.Saving -> itemProgress * 8f
        ExtractPhase.Completed -> 0f
        else -> 4f
    }
    return (base + inner).coerceIn(0f, 100f)
}

private fun estimateRemainingSeconds(phase: ExtractPhase, itemProgress: Float, elapsed: Long): Long {
    val overall = phaseProgressValue(phase, itemProgress) / 100f
    if (overall <= 0.05f) return 0
    val totalEstimate = (elapsed / overall).toLong()
    return (totalEstimate - elapsed).coerceAtLeast(0)
}

private fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}:${s.toString().padStart(2, '0')}" else "${s}ث"
}
