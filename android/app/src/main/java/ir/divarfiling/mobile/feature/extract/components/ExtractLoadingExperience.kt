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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.feature.extract.ExtractPhase
import kotlinx.coroutines.delay

@Composable
fun ExtractLoadingExperience(
    phase: ExtractPhase,
    progressCurrent: Int,
    progressTotal: Int,
    modifier: Modifier = Modifier,
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
    val remainingSeconds = estimateRemainingSeconds(phase, itemProgress, elapsedSeconds)

    val pulse by rememberInfiniteTransition(label = "extractPulse").animateFloat(
        initialValue = 0.96f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseScale",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DfColors.Surface,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "در حال استخراج…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${phaseProgress.toInt()}٪ · ${formatDuration(elapsedSeconds)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                    )
                }
                Box(
                    modifier = Modifier
                        .scale(pulse)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(DfColors.PurpleGradientStart, DfColors.PurpleGradientEnd),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.5.dp,
                        color = DfColors.Surface,
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (phaseProgress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = DfColors.Purple,
                trackColor = DfColors.SurfaceVariant,
            )

            if (progressTotal > 0) {
                Text(
                    "$progressCurrent از $progressTotal آگهی",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.PurpleDark,
                )
            }

            Text(
                "زمان باقی‌مانده تقریبی: ${formatDuration(remainingSeconds)}",
                style = MaterialTheme.typography.labelSmall,
                color = DfColors.TextMuted,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when (state) {
                        PhaseRowState.Done -> DfColors.GreenLight
                        PhaseRowState.Active -> DfColors.PurpleContainer
                        PhaseRowState.Pending -> DfColors.SurfaceVariant
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                PhaseRowState.Done -> Icon(Icons.Default.CheckCircle, null, tint = DfColors.Green, modifier = Modifier.size(18.dp))
                PhaseRowState.Active -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = DfColors.Purple)
                PhaseRowState.Pending -> Icon(step.icon, null, tint = DfColors.TextMuted, modifier = Modifier.size(16.dp))
            }
        }
        Column {
            Text(
                step.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (state == PhaseRowState.Active) FontWeight.Bold else FontWeight.Normal,
                color = when (state) {
                    PhaseRowState.Done -> DfColors.Green
                    PhaseRowState.Active -> DfColors.PurpleDark
                    PhaseRowState.Pending -> DfColors.TextMuted
                },
            )
            if (state == PhaseRowState.Active) {
                Text(step.hint, style = MaterialTheme.typography.labelSmall, color = DfColors.TextSecondary)
            }
        }
    }
}

private enum class PhaseRowState { Done, Active, Pending }

enum class ExtractPhase(val label: String, val hint: String, val icon: ImageVector) {
    Connecting("اتصال", "برقراری ارتباط امن", Icons.Default.Link),
    Preparing("آماده‌سازی", "پیکربندی فیلترها", Icons.Default.Search),
    LaunchingDivar("اجرای دیوار", "ورود به جستجوی دیوار", Icons.Default.Launch),
    Extracting("استخراج", "دریافت آگهی‌ها", Icons.Default.CloudDownload),
    Downloading("دانلود", "بارگیری جزئیات", Icons.Default.CloudDownload),
    Saving("ذخیره", "آپلود به فایلینگ", Icons.Default.Save),
    Completed("تکمیل", "عملیات با موفقیت انجام شد", Icons.Default.CheckCircle),
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
