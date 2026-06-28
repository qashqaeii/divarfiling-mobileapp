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
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import androidx.compose.foundation.layout.statusBarsPadding
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ir.divarfiling.mobile.feature.crm.components.ContactListCard
import androidx.compose.foundation.lazy.rememberLazyListState
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import ir.divarfiling.mobile.feature.crm.components.ContactQuickLeadSheet
import ir.divarfiling.mobile.feature.crm.components.TodayFilterSheet
import ir.divarfiling.mobile.feature.crm.components.TodayNewTaskSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.feature.crm.components.ContactsHeader
import ir.divarfiling.mobile.feature.crm.components.ContactsSearchFilterPanel
import ir.divarfiling.mobile.feature.crm.components.ContactsFilters
import ir.divarfiling.mobile.feature.crm.components.ContactsStatsRow
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard
import ir.divarfiling.mobile.feature.crm.components.TodayDateSection
import ir.divarfiling.mobile.feature.crm.components.TodayFilterChip
import ir.divarfiling.mobile.feature.crm.components.TodayFilterTab
import ir.divarfiling.mobile.feature.crm.components.TodayFilterTabsRow
import ir.divarfiling.mobile.feature.crm.components.TodayFilters
import ir.divarfiling.mobile.feature.crm.components.TodayHeader
import ir.divarfiling.mobile.feature.crm.components.TodayNewTaskFab
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.feature.crm.components.CrmContactsIllustration
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfContactListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfStatChip
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.core.network.TodayItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onBack: () -> Unit = {},
    onContactClick: (Long) -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var priorityFilter by remember { mutableStateOf(ContactsFilters.ALL_PRIORITIES) }
    var statusFilter by remember { mutableStateOf(ContactsFilters.ALL_STATUSES) }
    var typeFilter by remember { mutableStateOf(ContactsFilters.ALL_TYPES) }
    var quickFilter by remember { mutableStateOf(ContactsFilters.QuickFilter.ALL) }
    val listState = rememberLazyListState()

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
        floatingActionButton = {
            DfExtendedFab(
                text = "مخاطب جدید",
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
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
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
                        onTypeChange = { typeFilter = it },
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
                if (state.isLoading && state.contacts.isEmpty()) {
                    item {
                        DfContactListSkeleton(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (!state.isLoading && filteredContacts.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = if (state.contacts.isEmpty()) "مخاطبی ثبت نشده" else "نتیجه‌ای با این فیلتر نیست",
                            subtitle = if (state.contacts.isEmpty()) {
                                "با دکمه پایین صفحه، اولین مخاطب را اضافه کنید"
                            } else {
                                "فیلترها یا جستجو را تغییر دهید"
                            },
                            actionLabel = "مخاطب جدید",
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
                            onCallClick = {
                                contact.phone?.let { phone ->
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                                        )
                                    }
                                }
                            },
                            onWhatsAppClick = {
                                contact.phone?.let { phone ->
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onBack: () -> Unit = {},
    onContactClick: (Long) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
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
        TodayFilters.filterEntries(data, activeTab)
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
                    TodayHeader(
                        onBack = onBack,
                        onFilterClick = { viewModel.toggleFilterSheet(true) },
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
                        TodayDateSection(
                            dateLabel = today.date ?: "",
                            totalCount = TodayFilters.todayCount(today),
                            onDateClick = viewModel::refresh,
                        )
                    }
                    item {
                        TodayFilterTabsRow(
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
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    } else if (displayedEntries.isEmpty()) {
                        item {
                            DfEmptyState(
                                title = "کاری برای امروز نیست",
                                subtitle = "همه پیگیری‌ها انجام شده — عالی!",
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
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }

    if (state.showFilterSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleFilterSheet(false) }) {
            TodayFilterSheet(
                chips = filterChips,
                selectedTab = activeTab,
                onSelect = {
                    showDoneSummary = false
                    selectedTab = it
                },
                onDismiss = { viewModel.toggleFilterSheet(false) },
            )
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
    onContacts: () -> Unit,
    onToday: () -> Unit,
    onDeals: () -> Unit = {},
    onProperties: () -> Unit = {},
    onQuickFilter: () -> Unit = onContacts,
    onQuickNote: () -> Unit = onContacts,
    onQuickReminder: () -> Unit = onToday,
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
                CrmHubHeader(userName = state.userName)
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
                        icon = DfIcons.Users,
                        tint = DfColors.Purple,
                        background = DfColors.PurpleContainer,
                        stats = listOf(
                            CrmHubStatChip("مخاطبین", state.contactsCount.toString(), DfIcons.Users),
                            CrmHubStatChip("سرنخ‌های جدید", state.newLeadsCount.toString(), DfIcons.UserPlus),
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
                        icon = DfIcons.Calendar,
                        tint = DfColors.Blue,
                        background = DfColors.BlueLight,
                        stats = listOf(
                            CrmHubStatChip("کارهای امروز", state.todayTasksCount.toString(), DfIcons.ListTodo),
                            CrmHubStatChip("معوق", state.overdueCount.toString(), DfIcons.Clock),
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
                        icon = DfIcons.Handshake,
                        tint = DfColors.Green,
                        background = DfColors.GreenLight,
                        stats = listOf(
                            CrmHubStatChip("معاملات فعال", state.activeDealsCount.toString(), DfIcons.Handshake),
                            CrmHubStatChip("ارزش کل", dealsValueLabel, DfIcons.BarChart),
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
                        icon = DfIcons.Building,
                        tint = DfColors.Amber,
                        background = DfColors.AmberLight,
                        stats = listOf(
                            CrmHubStatChip("ملک‌های ثبت‌شده", state.propertiesCount.toString(), DfIcons.Building),
                            CrmHubStatChip("پرونده‌های باز", state.openCasesCount.toString(), DfIcons.Folder),
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

            item {
                CrmQuickActionsBar(
                    actions = listOf(
                        CrmQuickAction("فیلتر پیشرفته", "جستجوی دقیق", DfIcons.Filter, onQuickFilter),
                        CrmQuickAction("یادداشت سریع", "ثبت یادداشت", DfIcons.File, onQuickNote),
                        CrmQuickAction("یادآور جدید", "تنظیم یادآور", DfIcons.AlarmClock, onQuickReminder),
                        CrmQuickAction("مخاطب جدید", "افزودن سریع", DfIcons.UserPlus, onQuickContact),
                    ),
                    modifier = Modifier
                        .padding(horizontal = AppSpacing.screenHorizontal)
                        .padding(top = AppSpacing.xs),
                )
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
            CrmHubFeatureCard(
                title = "مخاطبین",
                subtitle = "لیست کامل مشتریان و سرنخ‌های جدید",
                icon = DfIcons.Users,
                tint = DfColors.Purple,
                background = DfColors.PurpleContainer,
                stats = listOf(
                    CrmHubStatChip("مخاطبین", "248", DfIcons.Users),
                    CrmHubStatChip("سرنخ‌های جدید", "32", DfIcons.UserPlus),
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
        item {
            CrmQuickActionsBar(
                actions = listOf(
                    CrmQuickAction("فیلتر پیشرفته", "جستجوی دقیق", DfIcons.Filter) {},
                    CrmQuickAction("یادآور جدید", "تنظیم یادآور", DfIcons.AlarmClock) {},
                    CrmQuickAction("مخاطب جدید", "افزودن سریع", DfIcons.UserPlus) {},
                    CrmQuickAction("یادداشت سریع", "ثبت یادداشت", DfIcons.File) {},
                ),
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
            TodayHeader(onBack = {}, onFilterClick = {})
            TodayStatsRow(
                todayCount = 33,
                doneCount = 0,
                overdueCount = 32,
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
