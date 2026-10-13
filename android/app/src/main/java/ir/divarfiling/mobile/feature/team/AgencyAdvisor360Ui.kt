package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.CircularProgressIndicator
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.network.AgencyAdvisorSectionCountsDto
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.PresentationLabels
import ir.divarfiling.mobile.core.design.AppShapes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfTextField
import androidx.compose.material3.Surface
import ir.divarfiling.mobile.core.network.AgencyAdvisorDetailPayload
import ir.divarfiling.mobile.core.network.AgencyAdvisorOverviewDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorPeriodMetricDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspaceDatasetDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspaceItemDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspaceListingDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspacePayload

/** عنوان صفحهٔ میزکار مشاور در اپ (معادل فارسی Advisor 360 در میزکار وب). */
const val AGENCY_ADVISOR_WORKSPACE_TITLE = "پروفایل کامل مشاور"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgencyAdvisorProfileHero(
    name: String,
    phone: String,
    roleLabel: String,
    isActive: Boolean,
    seatSummary: String,
    seatExpiry: String?,
    scorePct: Int = 0,
    rank: Int? = null,
    activeLeads: Int = 0,
    activeDeals: Int = 0,
    modifier: Modifier = Modifier,
) {
    val displayName = PresentationLabels.memberDisplayName(name, phone)
    val accent = teamAccentFor(displayName)
    val score = scorePct.coerceIn(0, 100)
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
                        listOf(accent.copy(alpha = 0.14f), Color.Transparent),
                    ),
                )
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (score > 0) {
                        CircularProgressIndicator(
                            progress = { score / 100f },
                            modifier = Modifier.size(76.dp),
                            color = accent,
                            trackColor = DfThemeColors.surfaceVariant(),
                            strokeWidth = 4.dp,
                        )
                    }
                    TeamAvatar(name = displayName, size = 64.dp, accent = accent)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        displayName,
                        style = AppTypography.pageTitle,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (phone.isNotBlank()) {
                        Text(
                            phone,
                            style = AppTypography.labelSmall.copy(textDirection = TextDirection.Ltr),
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                        )
                    }
                    Text(
                        roleLabel.ifBlank { "مشاور" },
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textMuted(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (score > 0) {
                        Text(
                            "${DateUtils.toPersianDigits(score.toString())}٪",
                            style = AppTypography.sectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                        )
                        Text("امتیاز", style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                    }
                    rank?.let {
                        DfBadge(
                            text = "رتبه ${DateUtils.toPersianDigits(it.toString())}",
                            color = accent.copy(alpha = 0.15f),
                            textColor = accent,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Advisor360HeroMiniStat("سرنخ فعال", activeLeads, DfIcons.Users, Modifier.weight(1f))
                Advisor360HeroMiniStat("معامله فعال", activeDeals, DfIcons.Handshake, Modifier.weight(1f))
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                DfBadge(
                    text = PresentationLabels.memberActiveLabel(isActive),
                    color = if (isActive) DfThemeColors.successContainer() else DfThemeColors.errorContainer(),
                    textColor = if (isActive) DfThemeColors.success() else DfThemeColors.error(),
                )
                if (seatSummary.isNotBlank()) {
                    DfBadge(
                        text = seatSummary,
                        color = DfThemeColors.primaryContainer(),
                        textColor = DfThemeColors.primary(),
                    )
                }
                seatExpiry?.let {
                    DfBadge(
                        text = it,
                        color = DfThemeColors.surfaceVariant(),
                        textColor = DfThemeColors.textSecondary(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Advisor360HeroMiniStat(
    label: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(DfThemeColors.surfaceVariant().copy(alpha = 0.55f))
            .padding(vertical = AppSpacing.sm, horizontal = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = DfThemeColors.primary(), modifier = Modifier.size(16.dp))
        Text(
            DateUtils.toPersianDigits(value.toString()),
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
        )
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), maxLines = 1)
    }
}

@Composable
fun AgencyAdvisorWorkspaceSnapshot(
    sectionCounts: AgencyAdvisorSectionCountsDto,
    calls: Int,
    visits: Int,
    modifier: Modifier = Modifier,
) {
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
            Text("نمای پرونده", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                item {
                    Advisor360SnapshotChip("مخاطب", sectionCounts.contacts, DfIcons.Users, DfThemeColors.info())
                }
                item {
                    Advisor360SnapshotChip("فایل", sectionCounts.properties, DfIcons.Building, Color(0xFF0F766E))
                }
                item {
                    Advisor360SnapshotChip("فایلینگ", sectionCounts.datasets, DfIcons.Layers, DfThemeColors.primary())
                }
                item {
                    Advisor360SnapshotChip("معامله", sectionCounts.deals, DfIcons.Handshake, DfThemeColors.warning())
                }
                item {
                    Advisor360SnapshotChip("قرارداد", sectionCounts.contracts, DfIcons.File, DfThemeColors.success())
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Advisor360HeroMiniStat("تماس", calls, DfIcons.Phone, Modifier.weight(1f))
                Advisor360HeroMiniStat("بازدید", visits, DfIcons.MapPin, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Advisor360SnapshotChip(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
) {
    Row(
        modifier = Modifier
            .clip(AppShapes.CardSmall)
            .background(tint.copy(alpha = 0.1f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
        Text(
            DateUtils.toPersianDigits(count.toString()),
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = tint,
        )
    }
}

enum class Advisor360Tab(val apiValue: String, val label: String) {
    OVERVIEW("overview", "نمای کلی"),
    CONTACTS("contacts", "مخاطبین"),
    PROPERTIES("properties", "فایل شخصی"),
    FILING("filing", "فایلینگ"),
    DEALS("deals", "معاملات"),
    CONTRACTS("contracts", "قراردادها"),
    ACTIVITIES("activities", "فعالیت"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencyAdvisor360Tabs(
    selected: Advisor360Tab,
    counts: Map<Advisor360Tab, Int>,
    onSelect: (Advisor360Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScrollableTabRow(
        selectedTabIndex = Advisor360Tab.entries.indexOf(selected),
        edgePadding = AppSpacing.sm,
        containerColor = DfThemeColors.background(),
        modifier = modifier,
    ) {
        Advisor360Tab.entries.forEach { tab ->
            val count = counts[tab]
            val label = if (count != null && count > 0 && tab != Advisor360Tab.OVERVIEW) {
                "${tab.label} (${DateUtils.toPersianDigits(count.toString())})"
            } else {
                tab.label
            }
            Tab(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                text = {
                    Text(
                        label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = AppTypography.labelSmall,
                    )
                },
            )
        }
    }
}

@Composable
fun AgencyAdvisor360WorkspaceBody(
    tab: Advisor360Tab,
    detail: AgencyAdvisorDetailPayload,
    workspace: AgencyAdvisorWorkspacePayload?,
    workspaceLoading: Boolean,
    workspaceError: String? = null,
    workspaceForbidden: Boolean = false,
    workspaceQuery: String,
    periodDays: Int,
    filingDatasetId: String?,
    onQueryChange: (String) -> Unit,
    onPeriodChange: (Int) -> Unit,
    onOpenDataset: (String) -> Unit,
    onBackFromDataset: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryWorkspace: () -> Unit = {},
    onOpenContact: (Long) -> Unit = {},
    onOpenProperty: (Long) -> Unit = {},
    onOpenDeal: (Long) -> Unit = {},
    onOpenContract: (Long) -> Unit = {},
    onOpenListing: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)) {
        if (tab != Advisor360Tab.OVERVIEW && tab != Advisor360Tab.ACTIVITIES) {
            DfTextField(
                value = workspaceQuery,
                onValueChange = onQueryChange,
                label = "جستجو",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (tab == Advisor360Tab.OVERVIEW) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                items(listOf(7, 30, 90)) { days ->
                    DfSoftChip(
                        text = when (days) {
                            7 -> "۷ روز"
                            90 -> "۹۰ روز"
                            else -> "۳۰ روز"
                        },
                        selected = periodDays == days,
                        onClick = { onPeriodChange(days) },
                    )
                }
            }
        }
        if (tab == Advisor360Tab.FILING && filingDatasetId != null) {
            DfSecondaryButton(text = "بازگشت به دیتاست‌ها", onClick = onBackFromDataset)
        }
        when {
            workspaceError != null && workspace == null -> DfEmptyState(
                title = if (workspaceForbidden) "دسترسی محدود" else "بارگذاری ناموفق",
                subtitle = workspaceError,
                variant = if (workspaceForbidden) DfEmptyVariant.Empty else DfEmptyVariant.Error,
                actionLabel = if (workspaceForbidden) null else "تلاش مجدد",
                onAction = if (workspaceForbidden) null else onRetryWorkspace,
            )
            workspaceLoading && workspace == null -> DfCardListSkeleton()
            !detail.canViewWorkspace -> DfEmptyState(
                title = "داده مشاور در دسترس نیست",
                subtitle = "برای مشاهده $AGENCY_ADVISOR_WORKSPACE_TITLE، رضایت‌نامه دیداری داده و اشتراک آژانس لازم است.",
                variant = DfEmptyVariant.Empty,
            )
            tab == Advisor360Tab.OVERVIEW -> {
                val overview = workspace?.overview
                if (overview != null) {
                    Advisor360OverviewPanel(overview = overview, performance = detail.performance)
                } else if (!workspaceLoading) {
                    DfEmptyState(title = "خلاصه‌ای نیست", subtitle = "", variant = DfEmptyVariant.Empty)
                }
            }
            tab == Advisor360Tab.FILING && filingDatasetId == null -> {
                val datasets = workspace?.datasets.orEmpty()
                if (datasets.isEmpty() && !workspaceLoading) {
                    DfEmptyState(title = "دیتاستی نیست", subtitle = "فایلینگ این مشاور خالی است.", variant = DfEmptyVariant.Empty)
                } else {
                    datasets.forEach { ds ->
                        Advisor360DatasetCard(dataset = ds, onClick = { onOpenDataset(ds.id) })
                    }
                }
            }
            tab == Advisor360Tab.FILING && filingDatasetId != null -> {
                val listings = workspace?.listings.orEmpty()
                when {
                    workspaceLoading -> DfCardListSkeleton()
                    listings.isEmpty() -> DfEmptyState(
                        title = "آگهی‌ای نیست",
                        subtitle = "در این دیتاست هنوز آگهی ثبت نشده.",
                        variant = DfEmptyVariant.Empty,
                    )
                    else -> listings.forEach { row ->
                        Advisor360ListingCard(
                            row = row,
                            onClick = {
                                val token = row.token.ifBlank { null }
                                if (token != null) onOpenListing(token)
                            },
                        )
                    }
                }
            }
            tab == Advisor360Tab.ACTIVITIES -> {
                val items = workspace?.items.orEmpty()
                if (items.isEmpty() && !workspaceLoading) {
                    DfEmptyState(title = "فعالیتی نیست", subtitle = "", variant = DfEmptyVariant.Empty)
                } else {
                    DfCard {
                        Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            items.forEach { act ->
                                TeamActivityTimelineRow(
                                    title = act.label.ifBlank { act.title },
                                    actorName = act.detail.ifBlank { act.title },
                                    timeLabel = act.createdAtDisplay.ifBlank { act.updatedAtDisplay },
                                )
                            }
                        }
                    }
                }
            }
            else -> {
                val items = workspace?.items.orEmpty()
                if (items.isEmpty() && !workspaceLoading) {
                    DfEmptyState(title = "موردی نیست", subtitle = "در این بخش داده‌ای ثبت نشده.", variant = DfEmptyVariant.Empty)
                } else {
                    items.forEach { item ->
                        when (tab) {
                            Advisor360Tab.CONTACTS -> Advisor360ContactCard(
                                item = item,
                                onClick = { if (item.id > 0) onOpenContact(item.id) },
                            )
                            Advisor360Tab.PROPERTIES -> Advisor360PropertyCard(
                                item = item,
                                onClick = { if (item.id > 0) onOpenProperty(item.id) },
                            )
                            Advisor360Tab.DEALS -> Advisor360DealCard(
                                item = item,
                                onClick = { if (item.id > 0) onOpenDeal(item.id) },
                            )
                            Advisor360Tab.CONTRACTS -> Advisor360ContractCard(
                                item = item,
                                onClick = { if (item.id > 0) onOpenContract(item.id) },
                            )
                            else -> Unit
                        }
                    }
                }
                if ((workspace?.page ?: 1) < (workspace?.numPages ?: 1)) {
                    DfSecondaryButton(text = "نمایش بیشتر", onClick = onLoadMore, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun Advisor360OverviewPanel(
    overview: AgencyAdvisorOverviewDto,
    performance: ir.divarfiling.mobile.core.network.AgencyMemberPerformanceDto,
) {
    val periodLabel = DateUtils.toPersianDigits(overview.periodDays.toString())
    val accent = DfThemeColors.primary()
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            Advisor360KpiTile("مخاطب", overview.contacts, DfIcons.Users, DfThemeColors.info(), Modifier.weight(1f))
            Advisor360KpiTile("فایل شخصی", overview.properties, DfIcons.Building, Color(0xFF0F766E), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            Advisor360KpiTile("فایلینگ", overview.datasets, DfIcons.Layers, accent, Modifier.weight(1f))
            Advisor360StatTile(
                label = "معامله فعال",
                value = DateUtils.toPersianDigits(overview.dealsActive.toString()),
                icon = DfIcons.Handshake,
                tint = DfThemeColors.warning(),
                modifier = Modifier.weight(1f),
            )
        }
        if (overview.lastActivityDisplay.isNotBlank()) {
            Text(
                "آخرین فعالیت: ${overview.lastActivityDisplay}",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Hero,
            color = DfThemeColors.surface(),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            shadowElevation = AppElevations.subtle,
        ) {
            Column(
                Modifier
                    .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.1f), Color.Transparent)))
                    .padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("عملکرد $periodLabel روز", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                        Text(
                            "تماس و بازدید در بازه انتخاب‌شده",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                        )
                    }
                    DfBadge(
                        text = "${DateUtils.toPersianDigits(performance.scorePct.toString())}٪",
                        color = accent.copy(alpha = 0.14f),
                        textColor = accent,
                    )
                }
                LinearProgressIndicator(
                    progress = { performance.scorePct.coerceIn(0, 100) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(AppShapes.CardSmall),
                    color = DfThemeColors.primary(),
                    trackColor = DfThemeColors.surfaceVariant(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Advisor360PerfMetric(
                        label = "تماس",
                        value = performance.calls,
                        icon = DfIcons.Phone,
                        modifier = Modifier.weight(1f),
                    )
                    Advisor360PerfMetric(
                        label = "بازدید",
                        value = performance.visits,
                        icon = DfIcons.MapPin,
                        modifier = Modifier.weight(1f),
                    )
                    Advisor360PerfMetric(
                        label = "معامله بسته",
                        value = performance.dealsClosed,
                        icon = DfIcons.Check,
                        modifier = Modifier.weight(1f),
                    )
                }
                performance.rank?.let { rank ->
                    Text(
                        "رتبه تیم: ${DateUtils.toPersianDigits(rank.toString())}",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Advisor360PerfMetric(
    label: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(DfThemeColors.surfaceVariant().copy(alpha = 0.45f))
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = DfThemeColors.primary(), modifier = Modifier.size(16.dp))
        Text(
            DateUtils.toPersianDigits(value.toString()),
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
        )
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
    }
}

@Composable
private fun Advisor360KpiTile(
    label: String,
    metric: AgencyAdvisorPeriodMetricDto,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier
                .background(tint.copy(alpha = 0.08f))
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(AppShapes.CardSmall)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                }
                Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
            Text(
                DateUtils.toPersianDigits(metric.total.toString()),
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "+${DateUtils.toPersianDigits(metric.period.toString())} در بازه",
                style = AppTypography.labelSmall,
                color = tint,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun Advisor360StatTile(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = DfThemeColors.info(),
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Column(
            modifier = Modifier
                .background(tint.copy(alpha = 0.08f))
                .padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(AppShapes.CardSmall)
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                }
                Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
            }
            Text(value, style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Advisor360ContactCard(
    item: AgencyAdvisorWorkspaceItemDto,
    onClick: () -> Unit,
) {
    DfCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(AppSpacing.cardPadding), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            TeamAvatar(name = item.name, size = 44.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(item.name.ifBlank { "—" }, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.phone.isNotBlank()) Text(item.phone, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    if (item.status.isNotBlank()) DfBadge(text = item.status, color = DfThemeColors.infoContainer(), textColor = DfThemeColors.info())
                    if (item.nextFollowUpDisplay.isNotBlank()) {
                        DfBadge(text = "پیگیری ${item.nextFollowUpDisplay}", color = DfThemeColors.warningContainer(), textColor = DfThemeColors.warning())
                    }
                }
            }
        }
    }
}

@Composable
private fun Advisor360PropertyCard(
    item: AgencyAdvisorWorkspaceItemDto,
    onClick: () -> Unit,
) {
    DfCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title.ifBlank { "—" }, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (item.region.isNotBlank()) Text(item.region, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                if (item.dealMode.isNotBlank()) DfBadge(text = item.dealMode, color = DfThemeColors.primaryContainer(), textColor = DfThemeColors.primary())
                if (item.transactionStatus.isNotBlank()) DfBadge(text = item.transactionStatus, color = DfThemeColors.surfaceVariant(), textColor = DfThemeColors.textSecondary())
            }
        }
    }
}

@Composable
private fun Advisor360DealCard(
    item: AgencyAdvisorWorkspaceItemDto,
    onClick: () -> Unit,
) {
    DfCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title.ifBlank { "—" }, fontWeight = FontWeight.Bold)
            if (item.stage.isNotBlank()) DfBadge(text = item.stage, color = DfThemeColors.primaryContainer(), textColor = DfThemeColors.primary())
            if (item.customerName.isNotBlank()) Text("مشتری: ${item.customerName}", style = AppTypography.labelSmall)
            if (item.propertyTitle.isNotBlank()) Text("ملک: ${item.propertyTitle}", style = AppTypography.labelSmall)
        }
    }
}

@Composable
private fun Advisor360ContractCard(
    item: AgencyAdvisorWorkspaceItemDto,
    onClick: () -> Unit,
) {
    AgencyContractListCard(
        item = ir.divarfiling.mobile.core.network.AgencyContractListItemDto(
            id = item.id,
            title = item.title,
            internalNumber = item.internalNumber,
            statusLabel = item.status,
            contractTypeLabel = "قرارداد",
            dealTitle = item.dealTitle,
        ),
        onClick = onClick,
        compact = true,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Advisor360DatasetCard(dataset: AgencyAdvisorWorkspaceDatasetDto, onClick: () -> Unit) {
    DfCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(AppSpacing.cardPadding), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(dataset.name.ifBlank { "دیتاست" }, fontWeight = FontWeight.Bold)
                Text(
                    "${DateUtils.toPersianDigits(dataset.listingCount.toString())} آگهی",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            Icon(DfIcons.ChevronLeft, contentDescription = null, tint = DfThemeColors.textMuted(), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun Advisor360ListingCard(
    row: AgencyAdvisorWorkspaceListingDto,
    onClick: () -> Unit,
) {
    DfCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(row.title.ifBlank { row.token }.ifBlank { "آگهی" }, fontWeight = FontWeight.SemiBold, maxLines = 2)
            if (row.priceLabel.isNotBlank()) Text(row.priceLabel, color = DfThemeColors.primary(), fontWeight = FontWeight.Medium)
            if (row.district.isNotBlank()) Text(row.district, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        }
    }
}
