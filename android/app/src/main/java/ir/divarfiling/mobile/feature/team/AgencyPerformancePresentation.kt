package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.PresentationLabels
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

    fun metricIcon(key: String): ImageVector = when (key) {
        "contacts" -> DfIcons.Users
        "calls" -> DfIcons.Phone
        "visits" -> DfIcons.MapPin
        "active_deals", "deals_closed" -> DfIcons.Handshake
        "score_pct" -> DfIcons.Trophy
        else -> DfIcons.BarChart
    }

    fun metricTint(key: String): Color = when (key) {
        "contacts" -> DfThemeColors.info()
        "calls" -> DfThemeColors.primary()
        "visits" -> Color(0xFF0F766E)
        "active_deals" -> DfThemeColors.warning()
        "deals_closed" -> DfThemeColors.success()
        else -> DfThemeColors.primary()
    }
}

@Composable
fun AgencyPerformanceHero(
    rangeLabel: String,
    totals: Map<String, Int>,
    memberCount: Int,
    modifier: Modifier = Modifier,
) {
    val primary = DfThemeColors.primary()
    val teamScore = (totals["score_pct"] ?: 0).coerceIn(0, 100)
    val calls = totals["calls"] ?: 0
    val visits = totals["visits"] ?: 0
    val activeDeals = totals["active_deals"] ?: 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(primary.copy(alpha = 0.16f), Color.Transparent),
                    ),
                )
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "پالس تیم · $rangeLabel",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                    Text(
                        "${DateUtils.toPersianDigits(memberCount.toString())} مشاور فعال",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(AppShapes.IconContainer)
                        .background(primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(DfIcons.Trophy, contentDescription = null, tint = primary, modifier = Modifier.size(26.dp))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                AgencyPerformanceHeroStat(
                    label = "تماس",
                    value = calls,
                    icon = DfIcons.Phone,
                    tint = primary,
                    modifier = Modifier.weight(1f),
                )
                AgencyPerformanceHeroStat(
                    label = "بازدید",
                    value = visits,
                    icon = DfIcons.MapPin,
                    tint = Color(0xFF0F766E),
                    modifier = Modifier.weight(1f),
                )
                AgencyPerformanceHeroStat(
                    label = "معامله",
                    value = activeDeals,
                    icon = DfIcons.Handshake,
                    tint = DfThemeColors.warning(),
                    modifier = Modifier.weight(1f),
                )
            }
            if (totals.containsKey("score_pct")) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("میانگین امتیاز تیم", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                        Text(
                            "${DateUtils.toPersianDigits(teamScore.toString())}٪",
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primary,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { teamScore / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(AppShapes.CardSmall),
                        color = primary,
                        trackColor = DfThemeColors.surfaceVariant(),
                    )
                }
            }
        }
    }
}

@Composable
private fun AgencyPerformanceHeroStat(
    label: String,
    value: Int,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(tint.copy(alpha = 0.1f))
            .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(
            DateUtils.toPersianDigits(value.toString()),
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
            color = DfThemeColors.textPrimary(),
        )
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), maxLines = 1)
    }
}

@Composable
fun AgencyPerformanceKpiStrip(
    totals: Map<String, Int>,
    excludeKeys: Set<String> = setOf("calls", "visits", "active_deals", "score_pct", "members"),
    modifier: Modifier = Modifier,
) {
    val entries = totals.filterKeys { it !in excludeKeys && totals[it] != null }
    if (entries.isEmpty()) return
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        items(entries.entries.toList(), key = { it.key }) { (key, value) ->
            AgencyPerformanceMetricCard(
                label = AgencyPerformancePresentation.totalLabel(key),
                value = DateUtils.toPersianDigits(value.toString()),
                icon = AgencyPerformancePresentation.metricIcon(key),
                tint = AgencyPerformancePresentation.metricTint(key),
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
    if (ranked.isEmpty()) return
    val maxScore = ranked.maxOf { it.scorePct }.coerceAtLeast(1)
    val primary = DfThemeColors.primary()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("رتبه‌بندی امتیاز", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                Icon(DfIcons.BarChart, contentDescription = null, tint = primary, modifier = Modifier.size(20.dp))
            }
            ranked.take(6).forEachIndexed { index, row ->
                val accent = teamAccentFor(row.name)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        row.rank?.let { DateUtils.toPersianDigits(it.toString()) }
                            ?: DateUtils.toPersianDigits((index + 1).toString()),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.size(24.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            row.name,
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        LinearProgressIndicator(
                            progress = { row.scorePct / maxScore.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(AppShapes.CardSmall),
                            color = accent,
                            trackColor = DfThemeColors.surfaceVariant(),
                        )
                    }
                    Text(
                        "${DateUtils.toPersianDigits(row.scorePct.toString())}٪",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    )
                }
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text("اهداف تیم", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            goals.forEach { goal ->
                val pct = goal.pct.coerceIn(0, 100)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${goal.memberName} · ${AgencyPerformancePresentation.goalMetricLabel(goal.metric)}",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "${DateUtils.toPersianDigits(pct.toString())}٪",
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.primary(),
                        )
                    }
                    LinearProgressIndicator(
                        progress = { pct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(AppShapes.CardSmall),
                        color = DfThemeColors.primary(),
                        trackColor = DfThemeColors.surfaceVariant(),
                    )
                    Text(
                        "${DateUtils.toPersianDigits(goal.actual.toString())} از ${DateUtils.toPersianDigits(goal.target.toString())}",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textMuted(),
                    )
                }
            }
        }
    }
}

@Composable
fun AgencyPerformanceMetricCard(
    label: String,
    value: String,
    icon: ImageVector = DfIcons.BarChart,
    tint: Color = DfThemeColors.primary(),
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(AppShapes.CardSmall)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(value, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold, color = DfThemeColors.textPrimary())
                Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), maxLines = 1)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyPerformanceMemberCard(
    rank: Int?,
    name: String,
    scorePct: Int,
    calls: Int,
    visits: Int,
    activeDeals: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val displayName = PresentationLabels.memberDisplayName(name)
    val accent = teamAccentFor(displayName)
    val rankLabel = rank?.let { DateUtils.toPersianDigits(it.toString()) }
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Hero,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.1f), Color.Transparent)))
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                TeamAvatar(name = displayName, size = 52.dp, accent = accent)
                if (rankLabel != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp),
                        shape = AppShapes.CardSmall,
                        color = accent.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                    ) {
                        Text(
                            rankLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = AppTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    displayName,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                LinearProgressIndicator(
                    progress = { scorePct.coerceIn(0, 100) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(AppShapes.CardSmall),
                    color = accent,
                    trackColor = DfThemeColors.surfaceVariant(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    AgencyPerformanceMemberChip("تماس", calls, DfIcons.Phone)
                    AgencyPerformanceMemberChip("بازدید", visits, DfIcons.MapPin)
                    AgencyPerformanceMemberChip("معامله", activeDeals, DfIcons.Handshake)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${DateUtils.toPersianDigits(scorePct.toString())}٪",
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
                Text("امتیاز", style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                Icon(DfIcons.ChevronLeft, contentDescription = null, tint = DfThemeColors.textMuted(), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AgencyPerformanceMemberChip(label: String, value: Int, icon: ImageVector) {
    Row(
        modifier = Modifier
            .clip(AppShapes.CardSmall)
            .background(DfThemeColors.surfaceVariant().copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = DfThemeColors.textMuted())
        Text(
            "${DateUtils.toPersianDigits(value.toString())}",
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
