package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfSearchField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfPillChip
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.components.DealGridCard
import ir.divarfiling.mobile.feature.crm.components.DealListCard
import ir.divarfiling.mobile.feature.crm.components.DealsActionBar
import ir.divarfiling.mobile.feature.crm.components.DealsFilterBar
import ir.divarfiling.mobile.feature.crm.components.DealsFilters
import ir.divarfiling.mobile.feature.crm.components.DealsHeader
import ir.divarfiling.mobile.feature.crm.components.DealsNewFab
import ir.divarfiling.mobile.feature.crm.components.DealsPipelineBar
import ir.divarfiling.mobile.feature.crm.components.DealsSortOrder
import ir.divarfiling.mobile.feature.crm.components.DealsStatsRow
import ir.divarfiling.mobile.feature.crm.components.DealsViewMode
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    onBack: () -> Unit = {},
    onDealClick: (Long) -> Unit = {},
    onNavigateContacts: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    viewModel: DealsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var ownerFilter by remember { mutableStateOf(DealsFilters.ALL_OWNERS) }
    var sortLabel by remember { mutableStateOf(DealsFilters.NEWEST) }
    var viewMode by remember { mutableStateOf(DealsViewMode.List) }

    val sortOrder = if (sortLabel == DealsFilters.OLDEST) DealsSortOrder.Oldest else DealsSortOrder.Newest
    val displayedDeals = remember(state.deals, ownerFilter, sortOrder, state.query) {
        DealsFilters.filterAndSortDeals(
            deals = state.deals,
            ownerFilter = ownerFilter,
            sortOrder = sortOrder,
            localQuery = state.query,
        )
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        floatingActionButton = {
            DealsNewFab(onClick = { viewModel.toggleCreate(true) })
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance + AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DealsHeader(
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                        onBack = onBack,
                    )
                }
                item {
                    DealsActionBar(
                        onNewDeal = { viewModel.toggleCreate(true) },
                        onContactsClick = onNavigateContacts,
                        onSalesStagesClick = viewModel::clearStageFilter,
                    )
                }
                if (state.deals.isNotEmpty() || state.pipelineColumns.isNotEmpty()) {
                    item {
                        DealsStatsRow(
                            activeCount = DealsFilters.activeCount(state.deals),
                            pipelineValueLabel = DealsFilters.formatCompactToman(
                                DealsFilters.pipelineValue(state.deals, state.pipelineColumns),
                            ),
                            weightedForecastLabel = DealsFilters.formatCompactToman(
                                DealsFilters.weightedForecast(state.deals),
                            ),
                            closedCommissionLabel = DealsFilters.formatCompactToman(
                                DealsFilters.closedCommission(state.deals),
                            ),
                            closingRate = DealsFilters.closingRate(state.deals),
                        )
                    }
                }
                if (state.pipelineColumns.isNotEmpty()) {
                    item {
                        DealsPipelineBar(
                            columns = state.pipelineColumns,
                            onStageClick = viewModel::selectStage,
                        )
                    }
                }
                item {
                    DealsFilterBar(
                        owners = DealsFilters.uniqueOwners(state.deals),
                        selectedOwner = ownerFilter,
                        selectedSort = sortLabel,
                        viewMode = viewMode,
                        onOwnerChange = { ownerFilter = it },
                        onSortChange = { sortLabel = it },
                        onResetFilters = {
                            ownerFilter = DealsFilters.ALL_OWNERS
                            sortLabel = DealsFilters.NEWEST
                            viewModel.clearStageFilter()
                        },
                        onViewModeChange = { viewMode = it },
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = viewModel::search,
                    )
                }
                state.error?.let { error ->
                    item {
                        DfErrorBanner(
                            error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.isLoading && state.deals.isEmpty()) {
                    item {
                        DfCardListSkeleton(
                            count = 5,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (displayedDeals.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = if (state.deals.isEmpty()) "معامله‌ای نیست" else "نتیجه‌ای با این فیلتر نیست",
                            subtitle = if (state.deals.isEmpty()) {
                                "با «معامله جدید» یک فرصت فروش بسازید"
                            } else {
                                "فیلترها یا جستجو را تغییر دهید"
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    item {
                        ExtractSectionCard(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                if (viewMode == DealsViewMode.Grid) {
                                    displayedDeals.chunked(2).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                        ) {
                                            rowItems.forEach { deal ->
                                                DealGridCard(
                                                    deal = deal,
                                                    onClick = { onDealClick(deal.id) },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            if (rowItems.size == 1) {
                                                Box(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                } else {
                                    displayedDeals.forEach { deal ->
                                        DealListCard(
                                            deal = deal,
                                            onClick = { onDealClick(deal.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleCreate(false) },
            title = { Text("معامله جدید") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.createCustomerId,
                        onValueChange = viewModel::onCreateCustomerIdChange,
                        label = { Text("شناسه مخاطب") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.createTitle,
                        onValueChange = viewModel::onCreateTitleChange,
                        label = { Text("عنوان") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.createAmount,
                        onValueChange = viewModel::onCreateAmountChange,
                        label = { Text("مبلغ (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::submitCreate) { Text("ثبت") } },
            dismissButton = { TextButton(onClick = { viewModel.toggleCreate(false) }) { Text("انصراف") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailScreen(
    onBack: () -> Unit,
    onContactClick: (Long) -> Unit = {},
    viewModel: DealDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val deal = state.deal
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                deal != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfDetailPageHeader(
                                title = deal.title,
                                subtitle = deal.stage,
                                titleIcon = DfIcons.Handshake,
                                onBack = onBack,
                            )
                        }
                        item {
                            DfPremiumCard(
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                    deal.stage?.let { DfBadge(it) }
                                    deal.amount?.let {
                                        Text(
                                            FormatUtils.formatPriceToman(it),
                                            style = AppTypography.sectionTitle,
                                            color = DfColors.Purple,
                                        )
                                    }
                                    deal.commissionAmount?.let {
                                        Text(
                                            "کمیسیون: ${FormatUtils.formatPriceToman(it)}",
                                            style = AppTypography.bodyDescription,
                                        )
                                    }
                                    deal.customerName?.let {
                                        TextButton(onClick = { deal.customerId?.let(onContactClick) }) {
                                            Text("مخاطب: $it", style = AppTypography.bodyDescription)
                                        }
                                    }
                                    deal.propertyTitle?.let {
                                        Text("ملک: $it", style = AppTypography.bodyDescription)
                                    }
                                    deal.notes?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                                    }
                                }
                            }
                        }
                        item {
                            Text(
                                "تغییر مرحله",
                                style = AppTypography.labelSmall,
                                color = DfColors.TextMuted,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = AppSpacing.screenHorizontal),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                state.stages.forEach { stage ->
                                    DfPillChip(
                                        label = stage,
                                        selected = deal.stage == stage,
                                        onClick = { viewModel.changeStage(stage) },
                                    )
                                }
                            }
                        }
                        item {
                            DfPrimaryButton(
                                "ویرایش",
                                onClick = { viewModel.toggleEditSheet(true) },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { viewModel.toggleEditSheet(false) }, sheetState = sheetState) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(state.editTitle, viewModel::onEditTitleChange, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editAmount, viewModel::onEditAmountChange, label = { Text("مبلغ") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editNotes, viewModel::onEditNotesChange, label = { Text("یادداشت") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                DfPrimaryButton("ذخیره", onClick = viewModel::saveEdit, enabled = !state.isSubmitting)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(
    onBack: () -> Unit = {},
    onPropertyClick: (Long) -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    viewModel: PropertiesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DfScreenContainerColor,
        floatingActionButton = {
            DfExtendedFab(
                text = "فایل جدید",
                icon = DfIcons.Plus,
                onClick = { viewModel.toggleCreate(true) },
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance + AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "فایل‌های شخصی",
                        subtitle = "مدیریت فایل‌های ملکی و وضعیت معاملات",
                        titleIcon = DfIcons.Building,
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                        onBack = onBack,
                    )
                }
                item {
                    DfSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "جستجو در فایل‌های شخصی…",
                        onSearch = viewModel::search,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                item {
                    DfFilterChipRow(
                        options = buildList {
                            add(DfFilterOption<String?>(null, "همه وضعیت‌ها"))
                            addAll(PropertyConstants.TX_STATUSES.map { DfFilterOption(it, it) })
                        },
                        selected = state.transactionStatus,
                        onSelect = { viewModel.onTransactionStatusChange(it); viewModel.search() },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                item {
                    DfFilterChipRow(
                        options = buildList {
                            add(DfFilterOption<String?>(null, "همه معاملات"))
                            addAll(PropertyConstants.DEAL_MODES.map { DfFilterOption(it, it) })
                        },
                        selected = state.dealMode,
                        onSelect = { viewModel.onDealModeChange(it); viewModel.search() },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                item {
                    DfFilterChipRow(
                        options = buildList {
                            add(DfFilterOption<String?>(null, "همه انواع"))
                            addAll(PropertyConstants.PROPERTY_TYPES.map { DfFilterOption(it, it) })
                        },
                        selected = state.propertyType,
                        onSelect = { viewModel.onPropertyTypeChange(it); viewModel.search() },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                state.error?.let {
                    item {
                        DfErrorBanner(
                            it,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.isLoading && state.properties.isEmpty()) {
                    item {
                        DfCardListSkeleton(
                            count = 5,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (state.properties.isEmpty()) {
                    item {
                        DfEmptyState(
                            title = "فایل شخصی ثبت نشده",
                            subtitle = "با «فایل جدید» اضافه کنید یا از جزئیات آگهی تبدیل کنید",
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(state.properties, key = { it.id }) { prop ->
                        PropertyRow(
                            property = prop,
                            onClick = { onPropertyClick(prop.id) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleCreate(false) },
            title = { Text("فایل شخصی جدید") },
            text = {
                val isRent = state.createDealMode.contains("اجاره") || state.createDealMode.contains("رهن")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        state.createTitle,
                        viewModel::onCreateTitleChange,
                        label = { Text("عنوان") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        state.createCity,
                        viewModel::onCreateCityChange,
                        label = { Text("شهر") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        state.createDistrict,
                        viewModel::onCreateDistrictChange,
                        label = { Text("محله") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        state.createDealMode,
                        viewModel::onCreateDealModeChange,
                        label = { Text("نوع معامله (فروش / رهن و اجاره / …)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        state.createPropertyType,
                        viewModel::onCreatePropertyTypeChange,
                        label = { Text("نوع ملک") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        state.createArea,
                        viewModel::onCreateAreaChange,
                        label = { Text("متراژ") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (isRent) {
                        OutlinedTextField(
                            state.createDeposit,
                            viewModel::onCreateDepositChange,
                            label = { Text("رهن (تومان)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            state.createRent,
                            viewModel::onCreateRentChange,
                            label = { Text("اجاره ماهانه (تومان)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        OutlinedTextField(
                            state.createPrice,
                            viewModel::onCreatePriceChange,
                            label = { Text("قیمت فروش (تومان)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    OutlinedTextField(
                        state.createNotes,
                        viewModel::onCreateNotesChange,
                        label = { Text("یادداشت") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::submitCreate) { Text("ثبت") } },
            dismissButton = { TextButton(onClick = { viewModel.toggleCreate(false) }) { Text("انصراف") } },
        )
    }
}

@Composable
private fun PropertyRow(property: PropertyDto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DfPremiumCard(onClick = onClick, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(property.title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                property.dealMode?.let { DfBadge(it) }
                property.transactionStatus?.let {
                    DfBadge(it, color = DfColors.BlueLight, textColor = DfColors.Blue)
                }
            }
            val location = listOfNotNull(property.district, property.city).joinToString("، ")
            if (location.isNotBlank()) {
                Text(location, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            property.salePrice?.let {
                Text(FormatUtils.formatPriceToman(it), style = AppTypography.bodyDescription, color = DfColors.Purple)
            }
            property.rent?.let {
                Text("اجاره: ${FormatUtils.formatPriceToman(it)}", style = AppTypography.labelSmall, color = DfColors.Blue)
            }
            property.deposit?.let {
                Text("رهن: ${FormatUtils.formatPriceToman(it)}", style = AppTypography.labelSmall, color = DfColors.Blue)
            }
            property.updatedAt?.let { updated ->
                DateUtils.formatJalaliDateTime(updated)?.let { jalali ->
                    Text(jalali, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    onBack: () -> Unit,
    viewModel: PropertyDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val property = state.property
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                property != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfDetailPageHeader(
                                title = property.title,
                                subtitle = property.transactionStatus,
                                titleIcon = DfIcons.Building,
                                onBack = onBack,
                            )
                        }
                        item {
                            DfPremiumCard(
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                    property.transactionStatus?.let { DfBadge(it) }
                                    property.dealMode?.let { DfBadge(it) }
                                    property.salePrice?.let {
                                        Text(
                                            FormatUtils.formatPriceToman(it),
                                            style = AppTypography.sectionTitle,
                                            color = DfColors.Purple,
                                        )
                                    }
                                    property.area?.let {
                                        Text("$it متر", style = AppTypography.bodyDescription)
                                    }
                                    property.address?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                                    }
                                    property.notes?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
                                    }
                                }
                            }
                        }
                        item {
                            Text(
                                "تغییر وضعیت",
                                style = AppTypography.labelSmall,
                                color = DfColors.TextMuted,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = AppSpacing.screenHorizontal),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                            ) {
                                CrmConstants.PROPERTY_TX_STATUSES.forEach { status ->
                                    DfPillChip(
                                        label = status,
                                        selected = property.transactionStatus == status,
                                        onClick = { viewModel.changeStatus(status) },
                                    )
                                }
                            }
                        }
                        item {
                            DfPrimaryButton(
                                "ویرایش",
                                onClick = { viewModel.toggleEditSheet(true) },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { viewModel.toggleEditSheet(false) }, sheetState = sheetState) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(state.editTitle, viewModel::onEditTitleChange, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editCity, viewModel::onEditCityChange, label = { Text("شهر") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editPrice, viewModel::onEditPriceChange, label = { Text("قیمت") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editNotes, viewModel::onEditNotesChange, label = { Text("یادداشت") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                DfPrimaryButton("ذخیره", onClick = viewModel::saveEdit)
            }
        }
    }
}
