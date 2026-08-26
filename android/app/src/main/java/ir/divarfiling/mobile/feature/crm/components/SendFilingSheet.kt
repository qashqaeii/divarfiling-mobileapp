package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import androidx.compose.ui.graphics.Color
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.filing.DatasetDisplayUtils
import ir.divarfiling.mobile.core.filing.ListingPriceUtils
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.core.network.MessageTemplateDto
import ir.divarfiling.mobile.feature.filing.components.datasetCoverFallbackUrls

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SendFilingSheet(
    step: Int,
    datasets: List<DatasetDto>,
    listings: List<ListingDto>,
    selectedDatasetId: String?,
    selectedListingToken: String?,
    datasetQuery: String,
    listingQuery: String,
    note: String,
    isLoading: Boolean,
    isSubmitting: Boolean,
    templates: List<MessageTemplateDto>,
    templatesLoading: Boolean,
    showTemplatePicker: Boolean,
    onDatasetQueryChange: (String) -> Unit,
    onListingQueryChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onToggleTemplatePicker: (Boolean) -> Unit,
    onApplyTemplate: (MessageTemplateDto) -> Unit,
    onDismiss: () -> Unit,
    onDatasetSelected: (String) -> Unit,
    onBackToDatasets: () -> Unit,
    onListingSelect: (String) -> Unit,
    onSend: (Boolean) -> Unit,
) {
    val selectedDataset = remember(datasets, selectedDatasetId) {
        datasets.find { it.id == selectedDatasetId }
    }
    val filteredDatasets = remember(datasets, datasetQuery) {
        val query = datasetQuery.trim()
        datasets
            .filter { dataset ->
                if (query.isBlank()) return@filter true
                val title = DatasetDisplayUtils.displayTitle(dataset)
                title.contains(query, ignoreCase = true) ||
                    dataset.city?.contains(query, ignoreCase = true) == true ||
                    dataset.district?.contains(query, ignoreCase = true) == true ||
                    dataset.transactionType?.contains(query, ignoreCase = true) == true
            }
            .sortedWith(
                compareByDescending<DatasetDto> { it.updatedAt.orEmpty() }
                    .thenByDescending { it.createdAt.orEmpty() },
            )
    }
    val selectedListing = remember(listings, selectedListingToken) {
        listings.find { it.token == selectedListingToken }
    }
    var messageExpanded by remember(step) { mutableStateOf(false) }

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            sectionLabel = "قدم ${DateUtils.toPersianDigits((step + 1).toString())} از ۲",
            title = if (step == 0) "انتخاب فایلینگ" else "انتخاب آگهی",
            subtitle = if (step == 0) {
                "پوشه استخراج را برای ارسال به مخاطب انتخاب کنید"
            } else {
                "آگهی مناسب را انتخاب کنید — می‌توانید یادداشت هم اضافه کنید"
            },
            icon = if (step == 0) DfIcons.Folder else DfIcons.Home,
            onClose = onDismiss,
            bodyHeightFraction = 0.84f,
            scrollable = false,
            footer = if (step == 1 && selectedListing != null) {
                {
                    SendFilingFooter(
                        isSubmitting = isSubmitting,
                        listingTitle = selectedListing.title.orEmpty().ifBlank { "آگهی انتخاب‌شده" },
                        onSend = { onSend(false) },
                        onSendWhatsApp = { onSend(true) },
                    )
                }
            } else {
                null
            },
        ) {
            SendFilingStepBar(activeStep = step)

            when {
                isLoading -> SendFilingLoadingState(step = step)
                step == 0 -> SendFilingDatasetStep(
                    datasets = filteredDatasets,
                    totalCount = datasets.size,
                    query = datasetQuery,
                    onQueryChange = onDatasetQueryChange,
                    onDatasetSelected = onDatasetSelected,
                )
                else -> SendFilingListingStep(
                    dataset = selectedDataset,
                    listings = listings,
                    listingQuery = listingQuery,
                    selectedListingToken = selectedListingToken,
                    note = note,
                    templates = templates,
                    templatesLoading = templatesLoading,
                    showTemplatePicker = showTemplatePicker,
                    messageExpanded = messageExpanded,
                    isSubmitting = isSubmitting,
                    onListingQueryChange = onListingQueryChange,
                    onBackToDatasets = onBackToDatasets,
                    onListingSelect = onListingSelect,
                    onNoteChange = onNoteChange,
                    onToggleMessageExpanded = { messageExpanded = !messageExpanded },
                    onToggleTemplatePicker = onToggleTemplatePicker,
                    onApplyTemplate = onApplyTemplate,
                )
            }
        }
    }
}

@Composable
private fun SendFilingStepBar(activeStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        SendFilingStepChip(
            label = "فایلینگ",
            active = activeStep == 0,
            done = activeStep > 0,
            modifier = Modifier.weight(1f),
        )
        SendFilingStepChip(
            label = "آگهی",
            active = activeStep == 1,
            done = false,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SendFilingStepChip(
    label: String,
    active: Boolean,
    done: Boolean,
    modifier: Modifier = Modifier,
) {
    val bg = when {
        active -> DfColors.PurpleContainer.copy(alpha = 0.85f)
        done -> DfColors.GreenLight.copy(alpha = 0.75f)
        else -> DfColors.SurfaceVariant.copy(alpha = 0.45f)
    }
    val fg = when {
        active -> DfColors.Purple
        done -> DfColors.Green
        else -> DfColors.TextMuted
    }
    Surface(
        modifier = modifier,
        shape = AppShapes.Chip,
        color = bg,
        border = BorderStroke(
            1.dp,
            if (active) DfColors.Purple.copy(alpha = 0.35f) else DfColors.Outline.copy(alpha = 0.25f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (done) {
                Icon(
                    DfIcons.CircleCheck,
                    contentDescription = null,
                    tint = fg,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(end = 4.dp),
                )
            }
            Text(
                label,
                style = AppTypography.labelSmall,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                color = fg,
            )
        }
    }
}

@Composable
private fun SendFilingLoadingState(step: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        CircularProgressIndicator(color = DfColors.Purple, strokeWidth = 3.dp)
        Text(
            if (step == 0) "در حال بارگذاری پوشه‌های استخراج…" else "در حال بارگذاری آگهی‌ها…",
            style = AppTypography.bodyDescription,
            color = DfColors.TextSecondary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SendFilingDatasetStep(
    datasets: List<DatasetDto>,
    totalCount: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    onDatasetSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        DfSearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "جستجوی نام، شهر یا نوع معامله…",
        )
        SendFilingSummaryRow(
            primary = "${DateUtils.toPersianDigits(datasets.size.toString())} پوشه",
            secondary = if (query.isNotBlank() && datasets.size != totalCount) {
                "از ${DateUtils.toPersianDigits(totalCount.toString())} پوشه"
            } else {
                "مرتب‌شده بر اساس آخرین بروزرسانی"
            },
        )
        when {
            datasets.isEmpty() -> DfEmptyState(
                title = if (query.isBlank()) "فایلینگی یافت نشد" else "نتیجه‌ای با این جستجو نیست",
                subtitle = if (query.isBlank()) {
                    "ابتدا از بخش استخراج، یک پوشه بسازید"
                } else {
                    "عبارت جستجو را تغییر دهید"
                },
                variant = if (query.isBlank()) DfEmptyVariant.Empty else DfEmptyVariant.NoResults,
            )
            else -> {
                datasets.forEach { dataset ->
                    SendFilingDatasetRow(
                        dataset = dataset,
                        onClick = { onDatasetSelected(dataset.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SendFilingListingStep(
    dataset: DatasetDto?,
    listings: List<ListingDto>,
    listingQuery: String,
    selectedListingToken: String?,
    note: String,
    templates: List<MessageTemplateDto>,
    templatesLoading: Boolean,
    showTemplatePicker: Boolean,
    messageExpanded: Boolean,
    isSubmitting: Boolean,
    onListingQueryChange: (String) -> Unit,
    onBackToDatasets: () -> Unit,
    onListingSelect: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onToggleMessageExpanded: () -> Unit,
    onToggleTemplatePicker: (Boolean) -> Unit,
    onApplyTemplate: (MessageTemplateDto) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        SendFilingDatasetBreadcrumb(
            dataset = dataset,
            listingCount = listings.size,
            onBack = onBackToDatasets,
        )
        DfSearchField(
            value = listingQuery,
            onValueChange = onListingQueryChange,
            placeholder = "جستجوی عنوان، محله یا قیمت…",
        )
        when {
            listings.isEmpty() -> DfEmptyState(
                title = if (listingQuery.isBlank()) "آگهی‌ای در این پوشه نیست" else "آگهی‌ای با این جستجو نیست",
                subtitle = if (listingQuery.isBlank()) {
                    "پوشه دیگری انتخاب کنید یا استخراج را تکمیل کنید"
                } else {
                    "فیلتر جستجو را تغییر دهید"
                },
                variant = DfEmptyVariant.NoResults,
            )
            else -> {
                listings.forEach { listing ->
                    SendFilingListingRow(
                        listing = listing,
                        selected = listing.token == selectedListingToken,
                        onClick = { onListingSelect(listing.token) },
                    )
                }
            }
        }

        SendFilingMessageSection(
            expanded = messageExpanded,
            note = note,
            templates = templates,
            templatesLoading = templatesLoading,
            showTemplatePicker = showTemplatePicker,
            isSubmitting = isSubmitting,
            hasSelection = selectedListingToken != null,
            onNoteChange = onNoteChange,
            onToggleExpanded = onToggleMessageExpanded,
            onToggleTemplatePicker = onToggleTemplatePicker,
            onApplyTemplate = onApplyTemplate,
        )
    }
}

@Composable
private fun SendFilingSummaryRow(
    primary: String,
    secondary: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            primary,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
        )
        Text(
            secondary,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SendFilingDatasetBreadcrumb(
    dataset: DatasetDto?,
    listingCount: Int,
    onBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = DfColors.PurpleContainer.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, DfColors.Purple.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DfGlassTextButton(text = "تغییر پوشه", onClick = onBack, compact = true)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dataset?.let { DatasetDisplayUtils.displayTitle(it) } ?: "پوشه انتخاب‌شده",
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${DateUtils.toPersianDigits(listingCount.toString())} آگهی در این پوشه",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
            DfDecorImage(
                resId = DfDecorIcons.Folder,
                size = 28.dp,
                contentDescription = null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SendFilingDatasetRow(
    dataset: DatasetDto,
    onClick: () -> Unit,
) {
    val title = DatasetDisplayUtils.displayTitle(dataset)
    val location = listOfNotNull(dataset.district, dataset.city).joinToString("، ")
    val format = dataset.fileFormat?.uppercase() ?: "JSON"
    val updatedLabel = DateUtils.formatRelativeTimeAgo(dataset.updatedAt)
        ?: DateUtils.formatJalaliDate(dataset.updatedAt.orEmpty())
        ?: "—"
    val coverUrls = remember(dataset.id, dataset.thumbnailUrl, dataset.thumbnailUrls) {
        datasetCoverFallbackUrls(dataset)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .width(88.dp)
                    .height(88.dp)
                    .clip(AppShapes.CardSmall),
            ) {
                if (coverUrls.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(DfColors.PurpleContainer.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        DfDecorImage(
                            resId = DfDecorIcons.Folder,
                            size = 32.dp,
                            contentDescription = null,
                        )
                    }
                } else {
                    DfListingImage(
                        thumbnailUrl = dataset.thumbnailUrl,
                        images = coverUrls,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = AppSpacing.sm)
                    .padding(end = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    title,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (location.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            DfIcons.MapPin,
                            contentDescription = null,
                            tint = DfColors.Purple,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            location,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SendFilingMetaChip(format)
                    SendFilingMetaChip("${DateUtils.toPersianDigits(dataset.itemCount.toString())} آگهی")
                    SendFilingMetaChip("بروزرسانی $updatedLabel")
                }
            }
            Icon(
                DfIcons.ChevronLeft,
                contentDescription = null,
                tint = DfColors.TextMuted,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = AppSpacing.sm)
                    .size(18.dp),
            )
        }
    }
}

@Composable
private fun SendFilingMetaChip(text: String) {
    Surface(
        shape = AppShapes.Chip,
        color = DfColors.SurfaceVariant.copy(alpha = 0.65f),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = AppTypography.labelSmall,
            color = DfColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SendFilingListingRow(
    listing: ListingDto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) DfColors.Purple else DfColors.Outline.copy(alpha = 0.3f),
        label = "listingBorder",
    )
    val bg by animateColorAsState(
        targetValue = if (selected) DfColors.PurpleContainer.copy(alpha = 0.42f) else DfThemeColors.surface(),
        label = "listingBg",
    )
    val priceLine = ListingPriceUtils.primaryPriceLine(listing)
    val location = listOfNotNull(listing.district, listing.city).filter { it.isNotBlank() }.joinToString("، ")

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = bg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(AppShapes.CardSmall),
            ) {
                DfListingImage(
                    thumbnailUrl = listing.thumbnailUrl,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    listing.title.orEmpty().ifBlank { "آگهی بدون عنوان" },
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                priceLine?.let { line ->
                    Text(
                        text = line.value,
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.Purple,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    listing.area?.takeIf { it > 0 }?.let {
                        Text(
                            "${DateUtils.toPersianDigits(it.toString())} متر",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textSecondary(),
                        )
                    }
                    if (location.isNotBlank()) {
                        Text(
                            location,
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) DfColors.Purple else DfColors.SurfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        DfIcons.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SendFilingMessageSection(
    expanded: Boolean,
    note: String,
    templates: List<MessageTemplateDto>,
    templatesLoading: Boolean,
    showTemplatePicker: Boolean,
    isSubmitting: Boolean,
    hasSelection: Boolean,
    onNoteChange: (String) -> Unit,
    onToggleExpanded: () -> Unit,
    onToggleTemplatePicker: (Boolean) -> Unit,
    onApplyTemplate: (MessageTemplateDto) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        HorizontalDivider(color = DfColors.Outline.copy(alpha = 0.35f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .padding(vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    DfIcons.MessageCircle,
                    contentDescription = null,
                    tint = DfColors.Purple,
                    modifier = Modifier.size(18.dp),
                )
                Column {
                    Text(
                        "یادداشت همراه ارسال",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        if (hasSelection) "اختیاری — برای شخصی‌سازی پیام مخاطب" else "ابتدا یک آگهی انتخاب کنید",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                }
            }
            Icon(
                if (expanded) DfIcons.ChevronUp else DfIcons.ChevronDown,
                contentDescription = null,
                tint = DfColors.TextMuted,
                modifier = Modifier.size(18.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                DfTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = "متن یادداشت",
                    placeholder = "مثلاً: این ملک نزدیک به بودجه شماست — برای بازدید هماهنگ کنید",
                    singleLine = false,
                    minLines = 3,
                    enabled = !isSubmitting && hasSelection,
                )
                SendFilingTemplatePicker(
                    templates = templates,
                    templatesLoading = templatesLoading,
                    showAll = showTemplatePicker,
                    isSubmitting = isSubmitting || !hasSelection,
                    onToggleShowAll = { onToggleTemplatePicker(!showTemplatePicker) },
                    onApplyTemplate = onApplyTemplate,
                )
            }
        }
    }
}

@Composable
private fun SendFilingTemplatePicker(
    templates: List<MessageTemplateDto>,
    templatesLoading: Boolean,
    showAll: Boolean,
    isSubmitting: Boolean,
    onToggleShowAll: () -> Unit,
    onApplyTemplate: (MessageTemplateDto) -> Unit,
) {
    when {
        templatesLoading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = DfColors.Purple,
                    strokeWidth = 2.dp,
                )
            }
        }
        templates.isEmpty() -> {
            Text(
                "قالب پیامی ثبت نشده — متن را دستی بنویسید",
                style = AppTypography.labelSmall,
                color = DfColors.TextMuted,
            )
        }
        else -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "قالب‌های آماده",
                    style = AppTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DfColors.TextMuted,
                )
                DfGlassTextButton(
                    text = if (showAll) "کمتر" else "همه قالب‌ها",
                    onClick = onToggleShowAll,
                    compact = true,
                )
            }
            val visibleTemplates = if (showAll) templates else templates.take(4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visibleTemplates.forEach { template ->
                    Surface(
                        onClick = { if (!isSubmitting) onApplyTemplate(template) },
                        shape = AppShapes.CardSmall,
                        color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, DfColors.Outline.copy(alpha = 0.35f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .width(160.dp)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                template.title,
                                style = AppTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DfColors.Purple,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                template.body,
                                style = AppTypography.labelSmall,
                                color = DfColors.TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SendFilingFooter(
    isSubmitting: Boolean,
    listingTitle: String,
    onSend: () -> Unit,
    onSendWhatsApp: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            "ارسال: $listingTitle",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DfPrimaryButton(
                text = if (isSubmitting) "در حال ارسال…" else "ارسال به مخاطب",
                onClick = onSend,
                enabled = !isSubmitting,
                loading = isSubmitting,
                modifier = Modifier.weight(1f),
            )
            DfGlassTextButton(
                text = "واتساپ",
                onClick = onSendWhatsApp,
            )
        }
    }
}
