package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.network.AgencyPerformanceMemberDto
import ir.divarfiling.mobile.core.network.AgencyTeamGoalDto

object AgencyPerformancePresentation {
    fun totalLabel(key: String): String = when (key) {
        "contacts" -> "مخاطب"
        "calls" -> "تماس"
        "visits" -> "بازدید"
        "active_deals" -> "معامله فعال"
        "deals_closed" -> "معامله بسته"
        "score_pct" -> "میانگین امتیاز"
        "members" -> "اعضا"
        else -> key.replace('_', ' ')
    }

    fun goalMetricLabel(metric: String): String = when (metric) {
        "calls" -> "تماس"
        "visits" -> "بازدید"
        "contacts" -> "مخاطب"
        "deals_closed" -> "معاملات بسته"
        "active_deals" -> "معاملات فعال"
        else -> metric.replace('_', ' ')
    }
}

@Composable
fun AgencyPerformanceHero(
    rangeLabel: String,
    totals: Map<String, Int>,
    memberCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DfThemeColors.primary().copy(alpha = 0.12f))
            .border(1.dp, DfThemeColors.primary().copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text("خلاصه $rangeLabel", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
        Text(
            "${DateUtils.toPersianDigits(memberCount.toString())} مشاور در این بازه",
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.primary(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            listOf("calls" to "تماس", "visits" to "بازدید", "active_deals" to "معامله").forEach { (key, label) ->
                val value = totals[key] ?: 0
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DfThemeColors.surface())
                        .padding(vertical = AppSpacing.xs, horizontal = AppSpacing.sm),
                ) {
                    Text(
                        DateUtils.toPersianDigits(value.toString()),
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                }
            }
        }
        if (totals.containsKey("score_pct")) {
            val pct = (totals["score_pct"] ?: 0).coerceIn(0, 100)
            Text("میانگین امتیاز تیم: ${DateUtils.toPersianDigits(pct.toString())}٪", style = AppTypography.labelSmall)
            LinearProgressIndicator(
                progress = { pct / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
        }
    }
}

@Composable
fun AgencyPerformanceScoreChart(
    members: List<AgencyPerformanceMemberDto>,
    modifier: Modifier = Modifier,
) {
    val ranked = members.filter { it.scorePct > 0 }.sortedByDescending { it.scorePct }
    if (ranked.size < 2) return
    val maxScore = ranked.maxOf { it.scorePct }.coerceAtLeast(1)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text("مقایسه امتیاز مشاوران", fontWeight = FontWeight.SemiBold)
        ranked.take(6).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    row.name,
                    modifier = Modifier.weight(0.35f),
                    style = AppTypography.labelSmall,
                    maxLines = 1,
                )
                LinearProgressIndicator(
                    progress = { row.scorePct / maxScore.toFloat() },
                    modifier = Modifier
                        .weight(0.5f)
                        .height(8.dp),
                )
                Text(
                    "${DateUtils.toPersianDigits(row.scorePct.toString())}٪",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.primary(),
                )
            }
        }
    }
}

@Composable
fun AgencyPerformanceGoalsPanel(
    goals: List<AgencyTeamGoalDto>,
    modifier: Modifier = Modifier,
) {
    if (goals.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DfThemeColors.surface())
            .border(1.dp, DfThemeColors.outlineSubtle(), RoundedCornerShape(14.dp))
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text("اهداف تیم", fontWeight = FontWeight.Bold)
        goals.forEach { goal ->
            Text(
                "${goal.memberName} · ${goalMetricLabel(goal.metric)}",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
            )
            LinearProgressIndicator(
                progress = { (goal.pct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
            )
            Text(
                "${DateUtils.toPersianDigits(goal.actual.toString())} / ${DateUtils.toPersianDigits(goal.target.toString())}",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
        }
    }
}

@Composable
fun AgencyPerformanceMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DfThemeColors.surface())
            .border(1.dp, DfThemeColors.outlineSubtle(), RoundedCornerShape(14.dp))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.md),
    ) {
        Text(value, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold, color = DfThemeColors.primary())
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
    }
}
