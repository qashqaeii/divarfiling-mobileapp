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
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import androidx.compose.foundation.layout.statusBarsPadding
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ir.divarfiling.mobile.feature.crm.components.ContactGridCard
import ir.divarfiling.mobile.feature.crm.components.ContactListCard
import ir.divarfiling.mobile.feature.crm.components.ContactsActionBar
import ir.divarfiling.mobile.feature.crm.components.ContactsFilterBar
import ir.divarfiling.mobile.feature.crm.components.ContactsFilters
import ir.divarfiling.mobile.feature.crm.components.ContactsHeader
import ir.divarfiling.mobile.feature.crm.components.ContactsSearchField
import ir.divarfiling.mobile.feature.crm.components.ContactsStatsRow
import ir.divarfiling.mobile.feature.crm.components.ContactsViewMode
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
import androidx.compose.material3.AlertDialog
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
import ir.divarfiling.mobile.core.design.components.DfSearchField
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
    onImportFromFile: () -> Unit = {},
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var priorityFilter by remember { mutableStateOf(ContactsFilters.ALL_PRIORITIES) }
    var statusFilter by remember { mutableStateOf(ContactsFilters.ALL_STATUSES) }
    var typeFilter by remember { mutableStateOf(ContactsFilters.ALL_TYPES) }
    var viewMode by remember { mutableStateOf(ContactsViewMode.List) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }

    val statusForFilter = remember(statusFilter) {
        if (statusFilter == ContactsFilters.ALL_STATUSES) null else statusFilter
    }

    val filteredContacts = remember(
        state.contacts,
        priorityFilter,
        statusFilter,
        typeFilter,
        state.query,
    ) {
        ContactsFilters.filterContacts(
            contacts = state.contacts,
            priorityFilter = priorityFilter,
            statusFilter = statusForFilter,
            typeFilter = typeFilter,
            localQuery = state.query,
        )
    }

    Scaffold(
        containerColor = DfColors.Background,
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DfColors.Background)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    ContactsHeader(
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                    )
                }
                item {
                    ContactsActionBar(
                        onNewContact = { viewModel.toggleQuickLead(true) },
                        onImportFromFile = onImportFromFile,
                        onQuickRefresh = viewModel::refresh,
                    )
                }
                item {
                    ContactsStatsRow(
                        todayCount = ContactsFilters.todayCount(state.contacts),
                        newCount = ContactsFilters.newCount(state.contacts),
                        followUpCount = ContactsFilters.followUpCount(state.contacts),
                        totalCount = ContactsFilters.totalCount(state.contacts),
                    )
                }
                item {
                    ContactsFilterBar(
                        priorities = ContactsFilters.uniquePriorities(state.contacts),
                        statuses = ContactsFilters.uniqueStatuses(state.contacts),
                        types = ContactsFilters.uniqueTypes(state.contacts),
                        selectedPriority = priorityFilter,
                        selectedStatus = statusFilter,
                        selectedType = typeFilter,
                        viewMode = viewMode,
                        onPriorityChange = { priorityFilter = it },
                        onStatusChange = { status ->
                            statusFilter = status
                            viewModel.onStatusFilterChange(
                                if (status == ContactsFilters.ALL_STATUSES) null else status,
                            )
                        },
                        onTypeChange = { typeFilter = it },
                        onViewModeChange = { viewMode = it },
                    )
                }
                item {
                    ContactsSearchField(
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
                                "سرنخ جدید اضافه کنید یا از میزکار وارد شوید"
                            } else {
                                "فیلترها یا جستجو را تغییر دهید"
                            },
                            actionLabel = "مخاطب جدید",
                            onAction = { viewModel.toggleQuickLead(true) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    item {
                        ExtractSectionCard(
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                                if (viewMode == ContactsViewMode.Grid) {
                                    filteredContacts.chunked(2).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                        ) {
                                            rowItems.forEach { contact ->
                                                ContactGridCard(
                                                    contact = contact,
                                                    onClick = { onContactClick(contact.id) },
                                                    onCallClick = {
                                                        contact.phone?.let { phone ->
                                                            context.startActivity(
                                                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                                                            )
                                                        }
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            if (rowItems.size == 1) {
                                                Box(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                } else {
                                    filteredContacts.forEach { contact ->
                                        ContactListCard(
                                            contact = contact,
                                            selected = selectedIds.contains(contact.id),
                                            onSelectedChange = { checked ->
                                                selectedIds = if (checked) {
                                                    selectedIds + contact.id
                                                } else {
                                                    selectedIds - contact.id
                                                }
                                            },
                                            onClick = { onContactClick(contact.id) },
                                            onCallClick = {
                                                contact.phone?.let { phone ->
                                                    context.startActivity(
                                                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                                                    )
                                                }
                                            },
                                            onWhatsAppClick = {
                                                contact.phone?.let { phone ->
                                                    val wa = phone.removePrefix("0")
                                                    context.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("https://wa.me/98$wa"),
                                                        ),
                                                    )
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (state.hasMore) {
                        item {
                            TextButton(
                                onClick = viewModel::loadMore,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    if (state.isLoadingMore) "در حال بارگذاری…" else "مشاهده بیشتر",
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
        QuickLeadDialog(
            name = state.leadName,
            phone = state.leadPhone,
            isSubmitting = state.isSubmitting,
            onNameChange = viewModel::onLeadNameChange,
            onPhoneChange = viewModel::onLeadPhoneChange,
            onDismiss = { viewModel.toggleQuickLead(false) },
            onSubmit = viewModel::submitQuickLead,
        )
    }
}

@Composable
private fun QuickLeadDialog(
    name: String,
    phone: String,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت سریع سرنخ", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("نام") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = { Text("تلفن") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = !isSubmitting) {
                Text(if (isSubmitting) "…" else "ثبت", color = DfColors.Purple)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onBack: () -> Unit = {},
    onContactClick: (Long) -> Unit = {},
    onNewTask: () -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(TodayFilterTab.All) }

    val filterChips = state.data?.let { today ->
        listOf(
            TodayFilterChip(TodayFilterTab.All, "همه", TodayFilters.todayCount(today), DfIcons.LayoutGrid),
            TodayFilterChip(TodayFilterTab.Overdue, "معوق", TodayFilters.overdueCount(today), DfIcons.Clock),
            TodayFilterChip(TodayFilterTab.Done, "انجام‌شده", TodayFilters.doneCount(today), DfIcons.CircleCheck),
            TodayFilterChip(TodayFilterTab.Reminders, "یادآورها", TodayFilters.remindersCount(today), DfIcons.Bell),
        )
    } ?: emptyList()

    val displayedEntries = state.data?.let { today ->
        TodayFilters.filterEntries(today, selectedTab)
    } ?: emptyList()

    Scaffold(
        containerColor = DfColors.Background,
        floatingActionButton = {
            TodayNewTaskFab(onClick = onNewTask)
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DfColors.Background)
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
                        onFilterClick = { selectedTab = TodayFilterTab.All },
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
                            onTodayClick = { selectedTab = TodayFilterTab.All },
                            onDoneClick = { selectedTab = TodayFilterTab.Done },
                            onOverdueClick = { selectedTab = TodayFilterTab.Overdue },
                        )
                    }
                    item {
                        TodayDateSection(
                            dateLabel = today.date ?: "امروز",
                            totalCount = TodayFilters.todayCount(today),
                            onDateClick = viewModel::refresh,
                        )
                    }
                    item {
                        TodayFilterTabsRow(
                            chips = filterChips,
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                        )
                    }
                    if (displayedEntries.isEmpty()) {
                        item {
                            DfEmptyState(
                                title = if (selectedTab == TodayFilterTab.Done) {
                                    "کار انجام‌شده‌ای برای نمایش نیست"
                                } else {
                                    "کاری برای امروز نیست"
                                },
                                subtitle = if (selectedTab == TodayFilterTab.Done) {
                                    "کارهای تکمیل‌شده در این لیست نمایش داده نمی‌شوند"
                                } else {
                                    "همه پیگیری‌ها انجام شده — عالی!"
                                },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    } else {
                        items(
                            items = displayedEntries,
                            key = { entry ->
                                val item = entry.item
                                "${entry.isOverdue}-${item.reminder?.id ?: item.contact?.id ?: item.hashCode()}"
                            },
                        ) { entry ->
                            TodayTaskCard(
                                item = entry.item,
                                isOverdue = entry.isOverdue,
                                onCall = {
                                    entry.item.contact?.phone?.let { phone ->
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")),
                                        )
                                    }
                                    entry.item.contact?.id?.let { viewModel.logCallActivity(it) }
                                },
                                onWhatsApp = {
                                    entry.item.contact?.phone?.let { phone ->
                                        val wa = phone.removePrefix("0")
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://wa.me/98$wa"),
                                            ),
                                        )
                                    }
                                },
                                onViewContact = { entry.item.contact?.id?.let(onContactClick) },
                                onComplete = {
                                    viewModel.completeTask(
                                        contactId = entry.item.contact?.id,
                                        reminderId = entry.item.reminder?.id,
                                    )
                                },
                                onPostpone = {
                                    viewModel.postponeTask(
                                        contactId = entry.item.contact?.id,
                                        reminderId = entry.item.reminder?.id,
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
            .background(DfColors.Background),
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
                        title = "املاک CRM",
                        subtitle = "ملک‌های شخصی و پرونده‌ها",
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
            .fillMaxSize()
            .background(DfColors.Background),
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
                .fillMaxSize()
                .background(DfColors.Background),
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
                onCall = {},
                onWhatsApp = {},
                onViewContact = {},
                onComplete = {},
                onPostpone = {},
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }
    }
}
