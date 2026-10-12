package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import ir.divarfiling.mobile.core.network.AgencyAdvisorDetailPayload
import ir.divarfiling.mobile.core.network.AgencyAdvisorOverviewDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorPeriodMetricDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspaceDatasetDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspaceItemDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspaceListingDto
import ir.divarfiling.mobile.core.network.AgencyAdvisorWorkspacePayload

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
    workspaceQuery: String,
    periodDays: Int,
    filingDatasetId: String?,
    onQueryChange: (String) -> Unit,
    onPeriodChange: (Int) -> Unit,
    onOpenDataset: (Long) -> Unit,
    onBackFromDataset: () -> Unit,
    onLoadMore: () -> Unit,
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
            workspaceLoading && workspace == null -> DfCardListSkeleton()
            !detail.canViewWorkspace -> DfEmptyState(
                title = "داده مشاور در دسترس نیست",
                subtitle = "برای مشاهده Advisor 360، رضایت‌نامه دیداری داده و اشتراک آژانس لازم است.",
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
            tab == Advisor360Tab.FILING -> {
                val listings = workspace?.listings.orEmpty()
                if (listings.isEmpty() && !workspaceLoading) {
                    DfEmptyState(title = "آگهی‌ای نیست", subtitle = "", variant = DfEmptyVariant.Empty)
                } else {
                    listings.forEach { row ->
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
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            Advisor360KpiTile("مخاطب", overview.contacts, Modifier.weight(1f))
            Advisor360KpiTile("فایل شخصی", overview.properties, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            Advisor360KpiTile("فایلینگ", overview.datasets, Modifier.weight(1f))
            Advisor360StatTile(
                label = "معامله فعال",
                value = DateUtils.toPersianDigits(overview.dealsActive.toString()),
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs), modifier = Modifier.fillMaxWidth()) {
            Advisor360StatTile("قرارداد", DateUtils.toPersianDigits(overview.contracts.toString()), Modifier.weight(1f))
            Advisor360StatTile("پیگیری باز", DateUtils.toPersianDigits(overview.openTasks.toString()), Modifier.weight(1f))
        }
        if (overview.lastActivityDisplay.isNotBlank()) {
            Text(
                "آخرین فعالیت: ${overview.lastActivityDisplay}",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
        }
        DfCard {
            Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text("عملکرد ${DateUtils.toPersianDigits(overview.periodDays.toString())} روز", fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("تماس"); Text(DateUtils.toPersianDigits(performance.calls.toString()))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("بازدید"); Text(DateUtils.toPersianDigits(performance.visits.toString()))
                }
                LinearProgressIndicator(progress = { performance.scorePct / 100f }, modifier = Modifier.fillMaxWidth())
                Text("امتیاز ${DateUtils.toPersianDigits(performance.scorePct.toString())}٪", style = AppTypography.labelSmall)
            }
        }
    }
}

@Composable
private fun Advisor360KpiTile(label: String, metric: AgencyAdvisorPeriodMetricDto, modifier: Modifier = Modifier) {
    val accent = DfThemeColors.primary()
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(DfThemeColors.surface())
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        Text(
            DateUtils.toPersianDigits(metric.total.toString()),
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "+${DateUtils.toPersianDigits(metric.period.toString())} در بازه",
            style = AppTypography.labelSmall,
            color = accent,
        )
    }
}

@Composable
private fun Advisor360StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(AppShapes.CardSmall)
            .background(DfThemeColors.surface())
            .padding(AppSpacing.sm),
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        Text(value, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
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
    DfCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.internalNumber.ifBlank { item.title }.ifBlank { "قرارداد" }, fontWeight = FontWeight.Bold)
            if (item.status.isNotBlank()) DfBadge(text = item.status, color = DfThemeColors.infoContainer(), textColor = DfThemeColors.info())
            if (item.updatedAtDisplay.isNotBlank()) Text(item.updatedAtDisplay, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
        }
    }
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
