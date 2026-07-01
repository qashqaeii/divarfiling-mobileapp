package ir.divarfiling.mobile.feature.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfAnimation
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfAnimatedCounter
import ir.divarfiling.mobile.core.design.components.DfShimmerBox
import ir.divarfiling.mobile.feature.home.DashboardStats
import java.util.Locale

@Composable
fun StatsSection(
    stats: DashboardStats,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val hoursSpent = formatHoursSpent(stats.todayTasksDone, stats.activeReminders)
    val progress = stats.dailyProgressPercent / 100f
    val progressAccent = when {
        stats.dailyProgressPercent >= 80 -> DfColors.Green
        stats.dailyProgressPercent >= 40 -> DfColors.Blue
        else -> DfColors.Amber
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        HomeSectionTitle(title = "نمای کلی امروز")

        if (isLoading) {
            DfShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            )
            return
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card,
            color = DfColors.Surface,
            shadowElevation = AppElevations.card,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TodayProgressHero(
                    progressPercent = stats.dailyProgressPercent,
                    progress = progress,
                    accent = progressAccent,
                    done = stats.todayTasksDone,
                    total = stats.todayTasksTotal,
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OverviewMiniTile(
                            value = hoursSpent,
                            label = "ساعت صرف‌شده",
                            footer = "تخمین امروز",
                            icon = DfIcons.Clock,
                            tint = DfColors.Amber,
                            background = DfColors.AmberLight,
                            modifier = Modifier.weight(1f),
                        )
                        OverviewMiniTile(
                            value = stats.todayTasksRemaining.toString(),
                            label = "کار باقی‌مانده",
                            footer = "تا پایان امروز",
                            icon = DfIcons.ListTodo,
                            tint = DfColors.Purple,
                            background = DfColors.PurpleContainer,
                            animateValue = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OverviewMiniTile(
                            value = stats.todayTasksDone.toString(),
                            label = "کار انجام‌شده",
                            footer = if (stats.tasksDoneDelta > 0) {
                                "+${stats.tasksDoneDelta} نسبت به دیروز"
                            } else {
                                "امروز"
                            },
                            icon = DfIcons.CircleCheck,
                            tint = DfColors.Green,
                            background = DfColors.GreenLight,
                            footerTint = if (stats.tasksDoneDelta > 0) DfColors.Green else DfColors.TextMuted,
                            animateValue = true,
                            modifier = Modifier.weight(1f),
                        )
                        OverviewMiniTile(
                            value = stats.activeReminders.toString(),
                            label = "یادآور فعال",
                            footer = "CRM",
                            icon = DfIcons.Bell,
                            tint = DfColors.Blue,
                            background = DfColors.BlueLight,
                            animateValue = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayProgressHero(
    progressPercent: Int,
    progress: Float,
    accent: Color,
    done: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = DfAnimation.springGentle(),
        label = "todayProgress",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        DfColors.PurpleGradientStart.copy(alpha = 0.92f),
                        DfColors.PurpleGradientEnd.copy(alpha = 0.88f),
                    ),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "پیشرفت روزانه",
                        style = AppTypography.labelSmall,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                    Text(
                        "$progressPercent٪",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.16f),
                ) {
                    Text(
                        text = if (total > 0) "$done از $total کار" else "بدون کار ثبت‌شده",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = AppTypography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.22f),
            )
        }
    }
}

@Composable
private fun OverviewMiniTile(
    value: String,
    label: String,
    footer: String,
    icon: ImageVector,
    tint: Color,
    background: Color,
    modifier: Modifier = Modifier,
    footerTint: Color = DfColors.TextMuted,
    animateValue: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.StatCard,
        color = background.copy(alpha = 0.45f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                }
                Text(
                    label,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (animateValue) {
                val numeric = value.toIntOrNull()
                if (numeric != null) {
                    DfAnimatedCounter(
                        target = numeric,
                        style = AppTypography.cardTitle,
                        color = DfColors.TextPrimary,
                    )
                } else {
                    Text(
                        value,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Text(
                    value,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                footer,
                style = AppTypography.labelSmall,
                color = footerTint,
                fontWeight = if (footerTint == DfColors.Green) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatHoursSpent(tasksDone: Int, activeReminders: Int): String {
    val hours = (tasksDone * 0.35f + activeReminders * 0.15f).coerceAtLeast(0.1f)
    return String.format(Locale.US, "%.1f", hours)
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StatsSectionPreview() {
    DivarFilingTheme {
        StatsSection(
            stats = DashboardStats(
                todayTasksDone = 12,
                todayTasksRemaining = 8,
                todayTasksTotal = 20,
                dailyProgressPercent = 60,
                tasksDoneDelta = 3,
                activeReminders = 4,
            ),
            isLoading = false,
        )
    }
}
