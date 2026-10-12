package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfJalaliDateField
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.AgencySpaceItemDto
import ir.divarfiling.mobile.core.network.TeamMemberDto

@Composable
fun AgencySpaceTabRow(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        items(AgencySpaceTab.labels, key = { it.first }) { (key, label) ->
            DfSoftChip(
                text = label,
                selected = selectedTab == key,
                onClick = { onTabSelected(key) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencySpaceItemCard(
    item: AgencySpaceItemDto,
    isSubmitting: Boolean,
    selectMode: Boolean = false,
    selected: Boolean = false,
    onOpen: () -> Unit,
    onCopy: () -> Unit,
    onViewCopied: (kind: String, targetId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (item.property != null) {
        AgencySpacePropertyCard(
            item = item,
            selectMode = selectMode,
            selected = selected,
            onOpen = onOpen,
            modifier = modifier,
        )
    } else {
        AgencySpaceContactCard(
            item = item,
            selectMode = selectMode,
            selected = selected,
            onOpen = onOpen,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgencySpacePropertyCard(
    item: AgencySpaceItemDto,
    selectMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prop = item.property ?: return
    val title = prop.title.ifBlank { "—" }
    val accent = DfThemeColors.info()
    val specs = listOfNotNull(
        prop.areaLabel.takeIf { it.isNotBlank() },
        prop.rooms.takeIf { it.isNotBlank() }?.let { "$it خواب" },
        prop.propertyType.takeIf { it.isNotBlank() },
    ).joinToString(" · ")
    DfCard(modifier = modifier, onClick = onOpen) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfListingImage(
                    thumbnailUrl = prop.thumbnailUrl,
                    modifier = Modifier.size(width = 72.dp, height = 64.dp),
                    shape = RoundedCornerShape(AppSpacing.sm),
                    contentDescription = title,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    AgencySpaceCardTitleRow(
                        selectMode = selectMode,
                        selected = selected,
                        onOpen = onOpen,
                        badge = "فایل",
                        badgeColor = accent,
                        title = title,
                    )
                    if (prop.priceLabel.isNotBlank()) {
                        Text(
                            prop.priceLabel,
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.textPrimary(),
                        )
                    }
                    if (specs.isNotBlank()) {
                        Text(
                            specs,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    val location = prop.region.ifBlank { prop.neighborhood }
                    if (location.isNotBlank()) {
                        Text(
                            location,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                        )
                    }
                    AgencySpaceCardFooter(item = item, isProperty = true)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgencySpaceContactCard(
    item: AgencySpaceItemDto,
    selectMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cust = item.customer ?: return
    val title = cust.name.ifBlank { "—" }
    val accent = DfThemeColors.primary()
    val accentName = teamAccentFor(title)
    DfCard(modifier = modifier, onClick = onOpen) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                if (cust.avatarUrl.isNotBlank()) {
                    DfAsyncImage(
                        url = cust.avatarUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(AppSpacing.sm)),
                    )
                } else {
                    TeamAvatar(name = title, size = 48.dp, accent = accentName)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    AgencySpaceCardTitleRow(
                        selectMode = selectMode,
                        selected = selected,
                        onOpen = onOpen,
                        badge = cust.customerType.ifBlank { "مخاطب" },
                        badgeColor = accent,
                        title = title,
                    )
                    if (cust.needSummary.isNotBlank()) {
                        Text(
                            cust.needSummary,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (cust.budgetLabel.isNotBlank()) {
                        Text(
                            cust.budgetLabel,
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = DfThemeColors.textPrimary(),
                        )
                    }
                    val statusLine = listOfNotNull(
                        cust.status.takeIf { it.isNotBlank() },
                        cust.priority.takeIf { it.isNotBlank() },
                    ).joinToString(" · ")
                    if (statusLine.isNotBlank()) {
                        Text(
                            statusLine,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                        )
                    }
                    if (cust.hasPhone && cust.phone.isNotBlank()) {
                        Text(
                            cust.phone,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                    if (cust.region.isNotBlank()) {
                        Text(
                            cust.region,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                        )
                    }
                    AgencySpaceCardFooter(item = item, isProperty = false)
                }
            }
        }
    }
}

@Composable
private fun AgencySpaceCardTitleRow(
    selectMode: Boolean,
    selected: Boolean,
    onOpen: () -> Unit,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    title: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectMode) {
            Checkbox(checked = selected, onCheckedChange = { onOpen() })
        }
        DfBadge(
            text = badge,
            color = badgeColor.copy(alpha = 0.12f),
            textColor = badgeColor,
        )
        Text(
            title,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AgencySpaceCardFooter(
    item: AgencySpaceItemDto,
    isProperty: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            item.publisher.name.ifBlank { "—" },
            style = AppTypography.labelSmall,
            color = DfThemeColors.textMuted(),
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        if (item.publishedAtDisplay.isNotBlank()) {
            Text(
                item.publishedAtDisplay,
                style = AppTypography.labelSmall,
                color = DfThemeColors.textMuted(),
            )
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AgencySpaceVisibilityBadge(
            visibility = item.visibility,
            label = item.visibilityLabel,
        )
        if (item.statusLabel.isNotBlank()) {
            DfBadge(
                text = item.statusLabel,
                color = DfThemeColors.surfaceVariant(),
                textColor = DfThemeColors.textSecondary(),
            )
        }
        if (item.alreadyCopied) {
            DfBadge(
                text = if (isProperty) "در فایل‌های من" else "در CRM من",
                color = DfThemeColors.successContainer(),
                textColor = DfThemeColors.success(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgencySpaceFilterSheet(
    tab: String,
    filters: AgencySpaceFilters,
    publishers: List<TeamMemberDto> = emptyList(),
    onDismiss: () -> Unit,
    onFiltersChange: (AgencySpaceFilters) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
) {
    val showProperty = tab == AgencySpaceTab.ALL || tab == AgencySpaceTab.PROPERTIES
    val showContact = tab == AgencySpaceTab.ALL || tab == AgencySpaceTab.CONTACTS
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "فیلتر پیشرفته",
            subtitle = "نوع ملک، منطقه، قیمت و دسترسی",
            onClose = onDismiss,
            footer = {
                DfSheetActions(
                    primaryText = "اعمال فیلتر",
                    onPrimary = onApply,
                    secondaryText = "پاک‌کردن",
                    onSecondary = onClear,
                )
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text("ترتیب نمایش", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    items(
                        listOf(
                            "newest" to "جدیدترین",
                            "oldest" to "قدیمی‌ترین",
                            "price_asc" to "ارزان‌تر",
                            "price_desc" to "گران‌تر",
                            "area_asc" to "کوچک‌تر",
                            "area_desc" to "بزرگ‌تر",
                            "most_copied" to "بیشترین کپی",
                        ),
                    ) { (key, label) ->
                        DfSoftChip(
                            text = label,
                            selected = filters.ordering == key,
                            onClick = { onFiltersChange(filters.copy(ordering = key)) },
                        )
                    }
                }
                if (showProperty) {
                    Text("فیلتر فایل", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
                    DfTextField(
                        value = filters.dealMode,
                        onValueChange = { onFiltersChange(filters.copy(dealMode = it)) },
                        label = "نوع معامله",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DfTextField(
                        value = filters.propertyType,
                        onValueChange = { onFiltersChange(filters.copy(propertyType = it)) },
                        label = "نوع ملک",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        DfTextField(
                            value = filters.city,
                            onValueChange = { onFiltersChange(filters.copy(city = it)) },
                            label = "شهر",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        DfTextField(
                            value = filters.district,
                            onValueChange = { onFiltersChange(filters.copy(district = it)) },
                            label = "منطقه",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        DfTextField(
                            value = filters.minPrice,
                            onValueChange = { onFiltersChange(filters.copy(minPrice = it)) },
                            label = "حداقل قیمت",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        DfTextField(
                            value = filters.maxPrice,
                            onValueChange = { onFiltersChange(filters.copy(maxPrice = it)) },
                            label = "حداکثر قیمت",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        DfTextField(
                            value = filters.minArea,
                            onValueChange = { onFiltersChange(filters.copy(minArea = it)) },
                            label = "حداقل متراژ",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        DfTextField(
                            value = filters.maxArea,
                            onValueChange = { onFiltersChange(filters.copy(maxArea = it)) },
                            label = "حداکثر متراژ",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    DfTextField(
                        value = filters.rooms,
                        onValueChange = { onFiltersChange(filters.copy(rooms = it)) },
                        label = "تعداد خواب",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (showContact) {
                    Text("فیلتر مخاطب", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
                    DfTextField(
                        value = filters.customerType,
                        onValueChange = { onFiltersChange(filters.copy(customerType = it)) },
                        label = "نوع مشتری",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DfTextField(
                        value = filters.region,
                        onValueChange = { onFiltersChange(filters.copy(region = it)) },
                        label = "محدوده",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        DfTextField(
                            value = filters.minBudget,
                            onValueChange = { onFiltersChange(filters.copy(minBudget = it)) },
                            label = "حداقل بودجه",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        DfTextField(
                            value = filters.maxBudget,
                            onValueChange = { onFiltersChange(filters.copy(maxBudget = it)) },
                            label = "حداکثر بودجه",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (publishers.isNotEmpty()) {
                    Text("منتشرکننده", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        item {
                            DfSoftChip(
                                text = "همه",
                                selected = filters.publisherId == null,
                                onClick = { onFiltersChange(filters.copy(publisherId = null)) },
                            )
                        }
                        items(publishers, key = { it.userId }) { pub ->
                            DfSoftChip(
                                text = pub.name.ifBlank { pub.phone },
                                selected = filters.publisherId == pub.userId,
                                onClick = {
                                    onFiltersChange(filters.copy(publisherId = pub.userId))
                                },
                            )
                        }
                    }
                }
                Text("تاریخ انتشار", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
                DfJalaliDateField(
                    value = filters.publishedAfter,
                    onValueChange = { onFiltersChange(filters.copy(publishedAfter = it)) },
                    label = "از تاریخ",
                )
                DfJalaliDateField(
                    value = filters.publishedBefore,
                    onValueChange = { onFiltersChange(filters.copy(publishedBefore = it)) },
                    label = "تا تاریخ",
                )
                Text("دسترسی و CRM", style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    items(
                        listOf(
                            "" to "همه سطوح",
                            "agency_public" to "عمومی آژانس",
                            "selected_members" to "اعضای منتخب",
                        ),
                    ) { (key, label) ->
                        DfSoftChip(
                            text = label,
                            selected = filters.visibility == key,
                            onClick = { onFiltersChange(filters.copy(visibility = key)) },
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    items(
                        listOf(
                            "" to "کپی CRM",
                            "copied" to "کپی شده",
                            "not_copied" to "کپی نشده",
                        ),
                    ) { (key, label) ->
                        DfSoftChip(
                            text = label,
                            selected = filters.crmCopy == key,
                            onClick = { onFiltersChange(filters.copy(crmCopy = key)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgencySpaceDetailSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(title, style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
            content()
        }
    }
}
