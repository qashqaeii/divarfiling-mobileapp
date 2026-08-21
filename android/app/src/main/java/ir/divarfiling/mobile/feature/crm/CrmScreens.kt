package ir.divarfiling.mobile.feature.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import androidx.compose.foundation.layout.statusBarsPadding
import android.content.Intent
import android.net.Uri
import ir.divarfiling.mobile.core.AppLinks
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ir.divarfiling.mobile.core.share.DossierShareActions
import ir.divarfiling.mobile.feature.crm.components.ContactListCard
import androidx.compose.foundation.lazy.rememberLazyListState
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import ir.divarfiling.mobile.feature.crm.components.ContactQuickLeadSheet
import ir.divarfiling.mobile.feature.crm.components.TodayNewTaskSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import ir.divarfiling.mobile.core.design.components.DfExportLinkButton
import ir.divarfiling.mobile.core.design.components.DfExportSheet
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.export.ExportFormat
import ir.divarfiling.mobile.feature.crm.components.ContactsHeader
import ir.divarfiling.mobile.feature.crm.components.ContactsSearchFilterPanel
import ir.divarfiling.mobile.feature.crm.components.ContactsFilters
import ir.divarfiling.mobile.feature.crm.components.ContactsStatsRow
import ir.divarfiling.mobile.feature.filing.components.SavedFiltersChipRow
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard
import ir.divarfiling.mobile.feature.crm.components.TodayFilterChip
import ir.divarfiling.mobile.feature.crm.components.TodayFilterTab
import ir.divarfiling.mobile.feature.crm.components.TodayFilters
import ir.divarfiling.mobile.feature.crm.components.TodayHeader
import ir.divarfiling.mobile.feature.crm.components.TodayNewTaskFab
import ir.divarfiling.mobile.feature.crm.components.TodaySearchFilterPanel
import ir.divarfiling.mobile.feature.crm.components.TodayStatsRow
import ir.divarfiling.mobile.feature.crm.components.TodayTaskCard
import ir.divarfiling.mobile.feature.crm.components.CrmDealsIllustration
import ir.divarfiling.mobile.feature.crm.components.CrmHubFeatureCard
import ir.divarfiling.mobile.feature.crm.components.CrmHubFeatureCardSkeleton
import ir.divarfiling.mobile.feature.crm.components.CrmHubHeader
import ir.divarfiling.mobile.feature.crm.components.CrmHubStatChip
import ir.divarfiling.mobile.feature.crm.components.CrmPropertiesIllustration
import ir.divarfiling.mobile.feature.crm.components.CrmQuickAction
import ir.divarfiling.mobile.feature.crm.components.CrmQuickActionsBar
import ir.divarfiling.mobile.feature.crm.components.CrmTodayIllustration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfHaptics
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.feature.crm.components.CrmContactsIllustration
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfContactListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatChip
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.core.network.TodayItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onBack: (() -> Unit)? = null,
    onContactClick: (Long) -> Unit = {},
    onContactSuggest: (Long) -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var priorityFilter by remember { mutableStateOf(ContactsFilters.ALL_PRIORITIES) }
    var statusFilter by remember { mutableStateOf(ContactsFilters.ALL_STATUSES) }
    var typeFilter by remember(state.customerTypeFilter) {
        mutableStateOf(state.customerTypeFilter ?: ContactsFilters.ALL_TYPES)
    }
    var quickFilter by remember { mutableStateOf(ContactsFilters.QuickFilter.ALL) }
    var showSaveFilterDialog by remember { mutableStateOf(false) }
    var saveFilterName by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val snackbar = remember { SnackbarHostState() }
    val haptics = DfHaptics.rememberPerformer()
    val ownerMode = state.customerTypeFilter == "مالک"
    val hasSavableCriteria = state.query.isNotBlank() ||
        (state.statusFilter?.isNotBlank() == true) ||
        (state.customerTypeFilter?.isNotBlank() == true)

    LaunchedEffect(state.exportMessage, state.error) {
        state.exportMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearExportMessage()
        }
        state.error?.let { snackbar.showSnackbar(it) }
    }

    LaunchedEffect(state.customerTypeFilter, state.statusFilter) {
        typeFilter = state.customerTypeFilter ?: ContactsFilters.ALL_TYPES
        statusFilter = state.statusFilter ?: ContactsFilters.ALL_STATUSES
    }

    if (showSaveFilterDialog) {
        AlertDialog(
            onDismissRequest = { showSaveFilterDialog = false },
            title = { Text("ذخیره فیلتر مخاطبین") },
            text = {
                OutlinedTextField(
                    value = saveFilterName,
                    onValueChange = { saveFilterName = it },
                    label = { Text("نام فیلتر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveCurrentAsFilter(saveFilterName)
                        showSaveFilterDialog = false
                        saveFilterName = ""
                    },
                ) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveFilterDialog = false
                        saveFilterName = ""
                    },
                ) { Text("انصراف") }
            },
        )
    }

    val statusForFilter = remember(statusFilter) {
        if (statusFilter == ContactsFilters.ALL_STATUSES) null else statusFilter
    }

    val filteredContacts = remember(
        state.contacts,
        priorityFilter,
        statusFilter,
        typeFilter,
        state.query,
        quickFilter,
    ) {
        ContactsFilters.filterContacts(
            contacts = state.contacts,
            priorityFilter = priorityFilter,
            statusFilter = statusForFilter,
            typeFilter = typeFilter,
            localQuery = state.query,
            quickFilter = quickFilter,
        )
    }

    LaunchedEffect(listState, state.hasMore, state.isLoadingMore, state.isLoading) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= info.totalItemsCount - 4
        }.collect { nearEnd ->
            if (nearEnd && state.hasMore && !state.isLoadingMore && !state.isLoading) {
                viewModel.loadMore()
            }
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            DfExtendedFab(
                text = if (ownerMode) "مالک جدید" else "مخاطب جدید",
                icon = DfIcons.UserPlus,
                onClick = { viewModel.toggleQuickLead(true) },
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
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl + 72.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    ContactsHeader(
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                        onBack = onBack,
                    )
                }
                if (ownerMode) {
                    item {
                        DfCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.screenHorizontal),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                Text(
                                    "نمای مالکین",
                                    style = AppTypography.cardTitle,
                                    color = DfColors.TextPrimary,
                                )
                                Text(
                                    "این بخش فقط مخاطب‌هایی را نشان می‌دهد که در CRM با نقش مالک ثبت شده‌اند تا پیگیری و لینک‌کردن آن‌ها سریع‌تر باشد.",
                                    style = AppTypography.bodyDescription,
                                    color = DfColors.TextMuted,
                                )
                            }
                        }
                    }
                }
                item {
                    ContactsStatsRow(
                        todayCount = ContactsFilters.todayCount(state.contacts),
                        newCount = ContactsFilters.newCount(state.contacts),
                        followUpCount = ContactsFilters.followUpCount(state.contacts),
                        totalCount = ContactsFilters.totalCount(state.contacts),
                        selectedFilter = quickFilter,
                        onFilterSelect = { quickFilter = it },
                    )
                }
                item {
                    DfExportLinkButton(
                        onClick = viewModel::openExportSheet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        DfSecondaryButton(
                            text = "قالب ورود",
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(AppLinks.WORKSPACE_CONTACT_IMPORT_TEMPLATE),
                                    ),
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DfSecondaryButton(
                            text = "ورود گروهی",
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(AppLinks.WORKSPACE_CONTACT_IMPORT),
                                    ),
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    ContactsSearchFilterPanel(
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = viewModel::search,
                        priorities = ContactsFilters.uniquePriorities(state.contacts),
                        statuses = ContactsFilters.uniqueStatuses(state.contacts),
                        types = ContactsFilters.uniqueTypes(state.contacts),
                        selectedPriority = priorityFilter,
                        selectedStatus = statusFilter,
                        selectedType = typeFilter,
                        onPriorityChange = { priorityFilter = it },
                        onStatusChange = { status ->
                            statusFilter = status
                            quickFilter = ContactsFilters.QuickFilter.ALL
                            viewModel.onStatusFilterChange(
                                if (status == ContactsFilters.ALL_STATUSES) null else status,
                            )
                        },
                        onTypeChange = { type ->
                            typeFilter = type
                            viewModel.onCustomerTypeFilterChange(
                                if (type == ContactsFilters.ALL_TYPES) null else type,
                            )
                        },
                    )
                }
                if (hasSavableCriteria) {
                    item {
                        TextButton(
                            onClick = { showSaveFilterDialog = true },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        ) {
                            Text("ذخیره فیلتر فعلی")
                        }
                    }
                }
                if (state.savedFilters.isNotEmpty()) {
                    item {
                        SavedFiltersChipRow(
                            filters = state.savedFilters,
                            activeId = state.activeSavedFilterId,
                            onSelect = { filter ->
                                viewModel.applySavedFilter(filter)
                                statusFilter = filter.resolvedParams["status"]
                                    ?: ContactsFilters.ALL_STATUSES
                                typeFilter = filter.resolvedParams["customer_type"]
                                    ?: ContactsFilters.ALL_TYPES
                                quickFilter = ContactsFilters.QuickFilter.ALL
                            },
                            onPin = viewModel::pinSavedFilter,
                            onDelete = viewModel::deleteSavedFilter,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                state.error?.let { error ->
                    item {
                        DfErrorBanner(
                            error,
                            onRetry = viewModel::refresh,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.isLoading && state.contacts.isEmpty()) {
                    item {
                        DfContactListSkeleton(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (!state.isLoading && filteredContacts.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = if (state.contacts.isEmpty()) {
                                if (ownerMode) "اولین مالک را ثبت کنید" else "اولین مخاطب را ثبت کنید"
                            } else {
                                "نتیجه‌ای با این فیلتر نیست"
                            },
                            subtitle = if (state.contacts.isEmpty()) {
                                if (ownerMode) {
                                    "مالک را برای اتصال به فایل شخصی اضافه کنید"
                                } else {
                                    "با دکمه پایین صفحه شروع کنید"
                                }
                            } else {
                                "فیلترها یا جستجو را تغییر دهید"
                            },
                            variant = if (state.contacts.isEmpty()) DfEmptyVariant.Empty else DfEmptyVariant.NoResults,
                            actionLabel = if (ownerMode) "مالک جدید" else "مخاطب جدید",
                            onAction = { viewModel.toggleQuickLead(true) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (filteredContacts.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            DfSectionHeader(
                                title = "لیست مخاطبین",
                                count = filteredContacts.size,
                            )
                        }
                    }
                    items(
                        items = filteredContacts,
                        key = { it.id },
                    ) { contact ->
                        ContactListCard(
                            contact = contact,
                            onClick = { onContactClick(contact.id) },
                            onSuggestClick = if (CrmConstants.isMatchEligible(contact.customerType)) {
                                { onContactSuggest(contact.id) }
                            } else {
                                null
                            },
                            onCallClick = {
                                contact.phone?.let { phone ->
                                    haptics.tick()
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                                        )
                                    }
                                }
                            },
                            onWhatsAppClick = {
                                contact.phone?.let { phone ->
                                    DossierShareActions.openWhatsApp(context, "سلام", phone)
                                }
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = AppSpacing.md),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "در حال بارگذاری…",
                                    style = AppTypography.labelSmall,
                                    color = DfColors.Purple,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showQuickLead) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleQuickLead(false) }) {
            ContactQuickLeadSheet(
                name = state.leadName,
                phone = state.leadPhone,
                customerType = state.leadCustomerType,
                isSubmitting = state.isSubmitting,
                onNameChange = viewModel::onLeadNameChange,
                onPhoneChange = viewModel::onLeadPhoneChange,
                onCustomerTypeChange = viewModel::onLeadCustomerTypeChange,
                onSubmit = viewModel::submitQuickLead,
                onDismiss = { viewModel.toggleQuickLead(false) },
            )
        }
    }

    if (state.showExportSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissExportSheet) {
            DfExportSheet(
                title = "خروجی مخاطبین",
                subtitle = "فایل CRM با فیلترهای فعلی",
                formats = listOf(ExportFormat.XLSX, ExportFormat.JSON, ExportFormat.CSV),
                isExporting = state.isExporting,
                onSelect = { format -> viewModel.exportContacts(context, format) },
                onDismiss = viewModel::dismissExportSheet,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onBack: (() -> Unit)? = null,
    onContactClick: (Long) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = DfHaptics.rememberPerformer()
    var selectedTab by remember { mutableStateOf(TodayFilterTab.All) }
    var showDoneSummary by remember { mutableStateOf(false) }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val filterChips = state.data?.let { today ->
        buildList {
            add(
                TodayFilterChip(
                    TodayFilterTab.All,
                    "همه",
                    TodayFilters.todayCount(today),
                    DfIcons.ListTodo,
                ),
            )
            add(
                TodayFilterChip(
                    TodayFilterTab.Overdue,
                    "معوق",
                    TodayFilters.overdueCount(today),
                    DfIcons.Clock,
                ),
            )
            if (TodayFilters.canFilterByDone(today)) {
                add(
                    TodayFilterChip(
                        TodayFilterTab.Done,
                        "انجام‌شده",
                        TodayFilters.doneCount(today),
                        DfIcons.CircleCheck,
                    ),
                )
            }
            add(
                TodayFilterChip(
                    TodayFilterTab.Reminders,
                    "یادآورها",
                    TodayFilters.remindersCount(today),
                    DfIcons.Bell,
                ),
            )
        }
    } ?: emptyList()

    val today = state.data
    val activeTab = when {
        today != null &&
            selectedTab == TodayFilterTab.Done &&
            !TodayFilters.canFilterByDone(today) -> TodayFilterTab.All
        else -> selectedTab
    }
    val implicitDoneSummary = today != null &&
        selectedTab == TodayFilterTab.Done &&
        !TodayFilters.canFilterByDone(today) &&
        TodayFilters.doneCount(today) > 0

    val displayedEntries = today?.let { data ->
        TodayFilters.filterEntries(data, activeTab, state.query)
    } ?: emptyList()

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            TodayNewTaskFab(onClick = { viewModel.toggleNewTaskSheet(true) })
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
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl + 72.dp),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    TodayHeader(onBack = onBack)
                }
                state.error?.let { error ->
                    item {
                        DfErrorBanner(
                            error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.isLoading && state.data == null) {
                    item {
                        DfCardListSkeleton(
                            count = 4,
                            itemHeight = 120.dp,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                state.data?.let { today ->
                    item {
                        TodayStatsRow(
                            todayCount = TodayFilters.todayCount(today),
                            doneCount = TodayFilters.doneCount(today),
                            overdueCount = TodayFilters.overdueCount(today),
                            selectedTab = activeTab,
                            onTodayClick = {
                                showDoneSummary = false
                                selectedTab = TodayFilterTab.All
                            },
                            onDoneClick = {
                                if (TodayFilters.canFilterByDone(today)) {
                                    showDoneSummary = false
                                    selectedTab = TodayFilterTab.Done
                                } else if (TodayFilters.doneCount(today) > 0) {
                                    showDoneSummary = true
                                    selectedTab = TodayFilterTab.All
                                }
                            },
                            onOverdueClick = {
                                showDoneSummary = false
                                selectedTab = TodayFilterTab.Overdue
                            },
                        )
                    }
                    item {
                        TodaySearchFilterPanel(
                            query = state.query,
                            onQueryChange = viewModel::onQueryChange,
                            chips = filterChips,
                            selectedTab = activeTab,
                            onTabSelected = {
                                showDoneSummary = false
                                selectedTab = it
                            },
                        )
                    }
                    if (showDoneSummary || implicitDoneSummary) {
                        item {
                            val doneCount = TodayFilters.doneCount(today)
                            DfEmptyState(
                                title = "$doneCount کار امروز انجام شده",
                                subtitle = "کارهای تکمیل‌شده از صف امروز حذف می‌شوند و در این لیست نمایش داده نمی‌شوند.",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    } else if (displayedEntries.isEmpty()) {
                        item {
                            DfEmptyState(
                                title = if (state.query.isNotBlank()) "نتیجه‌ای یافت نشد" else "امروز کاری ندارید",
                                subtitle = if (state.query.isNotBlank()) {
                                    "عبارت جستجو یا فیلتر را تغییر دهید"
                                } else {
                                    "پیگیری جدیدی برای امروز ثبت نشده"
                                },
                                variant = if (state.query.isNotBlank()) DfEmptyVariant.NoResults else DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = displayedEntries,
                            key = { index, entry -> TodayFilters.entryStableKey(entry, index) },
                        ) { _, entry ->
                            TodayTaskCard(
                                item = entry.item,
                                isOverdue = entry.isOverdue,
                                isActionRunning = state.isActionRunning,
                                onCall = {
                                    entry.item.contact?.phone?.let { phone ->
                                        haptics.tick()
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                                            )
                                        }
                                    }
                                    entry.item.contact?.id?.let { viewModel.logCallActivity(it) }
                                },
                                onWhatsApp = {
                                    entry.item.contact?.phone?.let { phone ->
                                        val wa = phone.removePrefix("0")
                                        runCatching {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://wa.me/98$wa"),
                                                ),
                                            )
                                        }
                                    }
                                    entry.item.contact?.id?.let { viewModel.logWhatsAppActivity(it) }
                                },
                                onViewContact = { entry.item.contact?.id?.let(onContactClick) },
                                onComplete = {
                                    viewModel.completeTask(
                                        contactId = entry.item.contact?.id,
                                        reminderId = entry.item.reminder?.id,
                                    )
                                },
                                onPostpone = { days ->
                                    viewModel.postponeTask(
                                        contactId = entry.item.contact?.id,
                                        reminderId = entry.item.reminder?.id,
                                        days = days,
                                    )
                                },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
                if (state.data == null && !state.isLoading && state.error == null) {
                    item {
                        DfEmptyState(
                            title = "داده‌ای نیست",
                            subtitle = "با کشیدن صفحه به‌روزرسانی کنید",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }

    if (state.showNewTaskSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleNewTaskSheet(false) }) {
            TodayNewTaskSheet(
                contacts = state.contactPicker,
                selectedContactId = state.newTaskContactId,
                title = state.newTaskTitle,
                dueMillis = state.newTaskDueMillis,
                isSubmitting = state.isSubmittingTask,
                onContactSelect = viewModel::onNewTaskContactSelect,
                onTitleChange = viewModel::onNewTaskTitleChange,
                onDueChange = viewModel::onNewTaskDueChange,
                onSubmit = viewModel::submitNewTask,
                onDismiss = { viewModel.toggleNewTaskSheet(false) },
            )
        }
    }
}

@Composable
fun CrmHubScreen(
    onBack: (() -> Unit)? = null,
    onContacts: () -> Unit,
    onToday: () -> Unit,
    onDeals: () -> Unit = {},
    onProperties: () -> Unit = {},
    onOwners: () -> Unit = onContacts,
    onTemplates: () -> Unit = onContacts,
    onCalendar: () -> Unit = onToday,
    onQuickContact: () -> Unit = onContacts,
    viewModel: CrmHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dealsValueLabel = if (state.dealsTotalValue > 0) {
        FormatUtils.formatPriceShort(state.dealsTotalValue) + " تومان"
    } else {
        "—"
    }

    DfPullRefresh(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                CrmHubHeader(userName = state.userName, onBack = onBack)
            }

            item {
                CrmQuickActionsBar(
                    actions = listOf(
                        CrmQuickAction(
                            title = "مالکین",
                            iconRes = DfDecorIcons.Building,
                            onClick = onOwners,
                            tint = DfColors.Purple,
                            background = DfColors.PurpleContainer,
                        ),
                        CrmQuickAction(
                            title = "قالب‌ها",
                            iconRes = DfDecorIcons.StickyNote,
                            onClick = onTemplates,
                            tint = DfColors.Blue,
                            background = DfColors.BlueLight,
                        ),
                        CrmQuickAction(
                            title = "تقویم",
                            iconRes = DfDecorIcons.Calendar,
                            onClick = onCalendar,
                            tint = DfColors.Amber,
                            background = DfColors.AmberLight,
                        ),
                        CrmQuickAction(
                            title = "مخاطب",
                            iconRes = DfDecorIcons.ClipboardList,
                            onClick = onQuickContact,
                            tint = DfColors.Green,
                            background = DfColors.GreenLight,
                        ),
                    ),
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            if (state.isLoading) {
                items(4) {
                    CrmHubFeatureCardSkeleton(
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            } else {
                item {
                    CrmHubFeatureCard(
                        title = "مخاطبین",
                        subtitle = "لیست کامل مشتریان و سرنخ‌های جدید",
                        iconRes = DfDecorIcons.Users,
                        tint = DfColors.Purple,
                        background = DfColors.PurpleContainer,
                        stats = listOf(
                            CrmHubStatChip("مخاطبین", state.contactsCount.toString(), iconRes = DfDecorIcons.Users),
                            CrmHubStatChip("سرنخ‌های جدید", state.newLeadsCount.toString(), icon = DfIcons.UserPlus),
                        ),
                        onClick = onContacts,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        illustration = {
                            CrmContactsIllustration(
                                tint = DfColors.Purple,
                                background = DfColors.PurpleContainer,
                            )
                        },
                    )
                }
                item {
                    CrmHubFeatureCard(
                        title = "کارهای امروز",
                        subtitle = "پیگیری‌های امروز و معوق",
                        iconRes = DfDecorIcons.ListTodo,
                        tint = DfColors.Blue,
                        background = DfColors.BlueLight,
                        stats = listOf(
                            CrmHubStatChip("کارهای امروز", state.todayTasksCount.toString(), iconRes = DfDecorIcons.ListTodo),
                            CrmHubStatChip("معوق", state.overdueCount.toString(), icon = DfIcons.Clock),
                        ),
                        onClick = onToday,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        illustration = {
                            CrmTodayIllustration(
                                tint = DfColors.Blue,
                                background = DfColors.BlueLight,
                            )
                        },
                    )
                }
                item {
                    CrmHubFeatureCard(
                        title = "معاملات",
                        subtitle = "پایپ‌لاین فروش و اجاره",
                        iconRes = DfDecorIcons.Handshake,
                        tint = DfColors.Green,
                        background = DfColors.GreenLight,
                        stats = listOf(
                            CrmHubStatChip("معاملات فعال", state.activeDealsCount.toString(), iconRes = DfDecorIcons.Handshake),
                            CrmHubStatChip("ارزش کل", dealsValueLabel, iconRes = DfDecorIcons.BarChart),
                        ),
                        onClick = onDeals,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        illustration = {
                            CrmDealsIllustration(
                                tint = DfColors.Green,
                                background = DfColors.GreenLight,
                            )
                        },
                    )
                }
                item {
                    CrmHubFeatureCard(
                        title = "فایل‌های شخصی",
                        subtitle = "مدیریت فایل‌های ملکی و پرونده‌ها",
                        iconRes = DfDecorIcons.Building,
                        tint = DfColors.Amber,
                        background = DfColors.AmberLight,
                        stats = listOf(
                            CrmHubStatChip("ملک‌های ثبت‌شده", state.propertiesCount.toString(), iconRes = DfDecorIcons.Building),
                            CrmHubStatChip("پرونده‌های باز", state.openCasesCount.toString(), iconRes = DfDecorIcons.Folder),
                        ),
                        onClick = onProperties,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        illustration = {
                            CrmPropertiesIllustration(
                                tint = DfColors.Amber,
                                background = DfColors.AmberLight,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "CRM Hub 360×800")
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "CRM Hub 390×844")
@Preview(showBackground = true, widthDp = 412, heightDp = 915, name = "CRM Hub 412×915")
@Composable
private fun CrmHubScreenPreview() {
    DivarFilingTheme {
        CrmHubScreenContentPreview()
    }
}

@Composable
internal fun CrmHubScreenContentPreview() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = AppSpacing.screenHorizontal,
            vertical = AppSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item { CrmHubHeader(userName = "حسین") }
        item {
            CrmQuickActionsBar(
                actions = listOf(
                    CrmQuickAction("فیلتر", iconRes = DfDecorIcons.Filter, onClick = {}, tint = DfColors.Purple, background = DfColors.PurpleContainer),
                    CrmQuickAction("یادداشت", iconRes = DfDecorIcons.StickyNote, onClick = {}, tint = DfColors.Blue, background = DfColors.BlueLight),
                    CrmQuickAction("یادآور", iconRes = DfDecorIcons.Upload, onClick = {}, tint = DfColors.Amber, background = DfColors.AmberLight),
                    CrmQuickAction("مخاطب", iconRes = DfDecorIcons.ClipboardList, onClick = {}, tint = DfColors.Green, background = DfColors.GreenLight),
                ),
            )
        }
        item {
            CrmHubFeatureCard(
                title = "مخاطبین",
                subtitle = "لیست کامل مشتریان و سرنخ‌های جدید",
                iconRes = DfDecorIcons.Users,
                tint = DfColors.Purple,
                background = DfColors.PurpleContainer,
                stats = listOf(
                    CrmHubStatChip("مخاطبین", "248", iconRes = DfDecorIcons.Users),
                    CrmHubStatChip("سرنخ‌های جدید", "32", icon = DfIcons.UserPlus),
                ),
                onClick = {},
                illustration = {
                    CrmContactsIllustration(
                        tint = DfColors.Purple,
                        background = DfColors.PurpleContainer,
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Today 360×800")
@Composable
private fun TodayScreenPreview() {
    DivarFilingTheme {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            TodayHeader(onBack = {})
            TodayStatsRow(
                todayCount = 33,
                doneCount = 0,
                overdueCount = 32,
                selectedTab = TodayFilterTab.All,
                onTodayClick = {},
                onDoneClick = {},
                onOverdueClick = {},
            )
            TodayTaskCard(
                item = TodayItemDto(
                    type = "follow_up",
                    contact = ContactDto(id = 1, fullName = "آرش ستوده", phone = "09121110010"),
                    reminder = ReminderDto(id = 1, title = "پیگیری", dueAt = "09:00"),
                ),
                isOverdue = true,
                isActionRunning = false,
                onCall = {},
                onWhatsApp = {},
                onViewContact = {},
                onComplete = {},
                onPostpone = { _ -> },
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }
    }
}
