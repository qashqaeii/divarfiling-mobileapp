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
import ir.divarfiling.mobile.core.design.components.DfContactRow
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfContactListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfFab
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
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { DfTopBar(title = "مخاطبین CRM", onBack = onBack) },
        floatingActionButton = {
            DfFab(
                onClick = { viewModel.toggleQuickLead(true) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                contentDescription = "سرنخ جدید",
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DfSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "جستجو نام یا تلفن…",
                        onSearch = viewModel::search,
                    )
                }
                state.error?.let { error ->
                    item { DfErrorBanner(error) }
                }
                if (state.isLoading && state.contacts.isEmpty()) {
                    item { DfContactListSkeleton() }
                } else if (!state.isLoading && state.contacts.isEmpty() && state.error == null) {
                    item {
                        DfEmptyState(
                            title = "مخاطبی ثبت نشده",
                            subtitle = "سرنخ جدید اضافه کنید یا از میزکار وارد شوید",
                            actionLabel = "سرنخ سریع",
                            onAction = { viewModel.toggleQuickLead(true) },
                        )
                    }
                } else {
                    items(state.contacts, key = { it.id }) { contact ->
                        DfContactRow(
                            name = contact.fullName,
                            phone = contact.phone,
                            status = contact.status,
                            customerType = contact.customerType,
                            onClick = { onContactClick(contact.id) },
                        )
                    }
                    if (state.hasMore) {
                        item {
                            TextButton(
                                onClick = viewModel::loadMore,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(if (state.isLoadingMore) "در حال بارگذاری…" else "بارگذاری بیشتر")
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
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(topBar = { DfTopBar(title = "کارهای امروز", onBack = onBack) }) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = AppSpacing.screenHorizontal,
                    vertical = AppSpacing.md,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
            ) {
                state.error?.let { error ->
                    item {
                        DfErrorBanner(error)
                    }
                }

                if (state.isLoading && state.data == null) {
                    item { DfCardListSkeleton(count = 4, itemHeight = 100.dp) }
                }

                state.data?.let { today ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            DfStatChip(
                                label = "امروز",
                                value = "${today.stats?.total ?: 0}",
                                modifier = Modifier.weight(1f),
                            )
                            DfStatChip(
                                label = "انجام‌شده",
                                value = "${today.stats?.done ?: 0}",
                                modifier = Modifier.weight(1f),
                            )
                            DfStatChip(
                                label = "معوق",
                                value = "${today.overdue.size}",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    today.date?.let { date ->
                        item {
                            Text(
                                text = "تاریخ: $date",
                                style = AppTypography.bodyDescription,
                                color = DfColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    if (today.overdue.isNotEmpty()) {
                        item { DfSectionHeader("معوق", today.overdue.size) }
                        items(
                            items = today.overdue,
                            key = { "overdue-${it.reminder?.id ?: it.contact?.id}" },
                        ) { item ->
                            TodayTaskCard(
                                item = item,
                                isOverdue = true,
                                onCall = {
                                    item.contact?.phone?.let { phone ->
                                        context.startActivity(
                                            android.content.Intent(
                                                android.content.Intent.ACTION_DIAL,
                                                android.net.Uri.parse("tel:$phone"),
                                            ),
                                        )
                                    }
                                    item.contact?.id?.let { viewModel.logCallActivity(it) }
                                },
                                onWhatsApp = {
                                    item.contact?.phone?.let { phone ->
                                        val wa = phone.removePrefix("0")
                                        context.startActivity(
                                            android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse("https://wa.me/98$wa"),
                                            ),
                                        )
                                    }
                                },
                                onViewContact = { item.contact?.id?.let(onContactClick) },
                                onComplete = {
                                    viewModel.completeTask(
                                        contactId = item.contact?.id,
                                        reminderId = item.reminder?.id,
                                    )
                                },
                                onPostpone = {
                                    viewModel.postponeTask(
                                        contactId = item.contact?.id,
                                        reminderId = item.reminder?.id,
                                    )
                                },
                            )
                        }
                    }
                    if (today.today.isNotEmpty()) {
                        item { DfSectionHeader("امروز", today.today.size) }
                        items(
                            items = today.today,
                            key = { "today-${it.reminder?.id ?: it.contact?.id}" },
                        ) { item ->
                            TodayTaskCard(
                                item = item,
                                isOverdue = false,
                                onCall = {
                                    item.contact?.phone?.let { phone ->
                                        context.startActivity(
                                            android.content.Intent(
                                                android.content.Intent.ACTION_DIAL,
                                                android.net.Uri.parse("tel:$phone"),
                                            ),
                                        )
                                    }
                                    item.contact?.id?.let { viewModel.logCallActivity(it) }
                                },
                                onWhatsApp = {
                                    item.contact?.phone?.let { phone ->
                                        val wa = phone.removePrefix("0")
                                        context.startActivity(
                                            android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                android.net.Uri.parse("https://wa.me/98$wa"),
                                            ),
                                        )
                                    }
                                },
                                onViewContact = { item.contact?.id?.let(onContactClick) },
                                onComplete = {
                                    viewModel.completeTask(
                                        contactId = item.contact?.id,
                                        reminderId = item.reminder?.id,
                                    )
                                },
                                onPostpone = {
                                    viewModel.postponeTask(
                                        contactId = item.contact?.id,
                                        reminderId = item.reminder?.id,
                                    )
                                },
                            )
                        }
                    }
                    if (today.overdue.isEmpty() && today.today.isEmpty()) {
                        item {
                            DfEmptyState(
                                title = "کاری برای امروز نیست",
                                subtitle = "همه پیگیری‌ها انجام شده — عالی!",
                            )
                        }
                    }
                }

                if (state.data == null && !state.isLoading && state.error == null) {
                    item {
                        DfEmptyState(
                            title = "داده‌ای نیست",
                            subtitle = "با کشیدن صفحه به‌روزرسانی کنید",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayTaskCard(
    item: TodayItemDto,
    isOverdue: Boolean,
    onCall: () -> Unit = {},
    onWhatsApp: () -> Unit = {},
    onViewContact: () -> Unit = {},
    onComplete: () -> Unit = {},
    onPostpone: () -> Unit = {},
) {
    val contactName = item.contact?.fullName
    val phone = item.contact?.phone
    val reminderTitle = item.reminder?.title
    val dueAt = item.reminder?.dueAt
    val reminderType = item.type?.takeIf { it.isNotBlank() }

    val title = contactName ?: reminderTitle ?: "—"
    val subtitle = when {
        !reminderTitle.isNullOrBlank() && contactName != null -> reminderTitle
        !phone.isNullOrBlank() -> phone
        else -> ""
    }

    DfPremiumCard(
        containerColor = if (isOverdue) DfColors.OverdueBackground else DfColors.Surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = AppSpacing.listRowMinHeight),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.iconTextGap),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
            ) {
                Text(
                    text = title,
                    style = AppTypography.cardTitle,
                    color = DfColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    reminderType?.let { type ->
                        DfBadge(
                            text = type,
                            color = DfColors.SurfaceVariant,
                            textColor = DfColors.TextSecondary,
                        )
                    }
                    dueAt?.let { due ->
                        Text(
                            text = due,
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (isOverdue) {
                        DfBadge(
                            text = "معوق",
                            color = DfColors.RoseLight,
                            textColor = DfColors.OverdueAccent,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(onClick = onCall) { Text("تماس", style = AppTypography.labelSmall) }
                    TextButton(onClick = onWhatsApp) { Text("واتساپ", style = AppTypography.labelSmall) }
                    TextButton(onClick = onViewContact) { Text("مخاطب", style = AppTypography.labelSmall) }
                    TextButton(onClick = onComplete) { Text("انجام شد", style = AppTypography.labelSmall) }
                    TextButton(onClick = onPostpone) { Text("تعویق", style = AppTypography.labelSmall) }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(AppShapes.IconContainer)
                    .background(
                        if (isOverdue) DfColors.RoseLight else DfColors.PurpleContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (isOverdue) DfColors.OverdueAccent else DfColors.Purple,
                    modifier = Modifier.size(20.dp),
                )
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
) {
    Scaffold(topBar = { DfTopBar(title = "مدیریت مشتری", showLogo = true) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "پیگیری مشتریان، یادآورها و سرنخ‌های جدید",
                style = MaterialTheme.typography.bodyMedium,
                color = DfColors.TextSecondary,
            )

            CrmHubCard(
                title = "مخاطبین",
                subtitle = "لیست کامل مشتریان و سرنخ‌ها",
                icon = Icons.Default.People,
                onClick = onContacts,
            )
            CrmHubCard(
                title = "کارهای امروز",
                subtitle = "پیگیری‌های امروز و معوق",
                icon = Icons.Default.CalendarToday,
                onClick = onToday,
            )
            CrmHubCard(
                title = "معاملات",
                subtitle = "پایپ‌لاین فروش و اجاره",
                icon = Icons.Default.Handshake,
                onClick = onDeals,
            )
            CrmHubCard(
                title = "املاک CRM",
                subtitle = "ملک‌های شخصی و پرونده‌ها",
                icon = Icons.Default.Apartment,
                onClick = onProperties,
            )
        }
    }
}

@Composable
private fun CrmHubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    DfCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = DfColors.Purple)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = DfColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DfColors.TextMuted)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Today 360×800")
@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Today 390×844")
@Preview(showBackground = true, widthDp = 412, heightDp = 915, name = "Today 412×915")
@Composable
private fun TodayScreenPreview() {
    DivarFilingTheme {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = AppSpacing.screenHorizontal,
                vertical = AppSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionGap),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    DfStatChip(label = "امروز", value = "5")
                    DfStatChip(label = "انجام‌شده", value = "2")
                    DfStatChip(label = "معوق", value = "1")
                }
            }
            item { DfSectionHeader("معوق", 1) }
            item {
                TodayTaskCard(
                    item = TodayItemDto(
                        type = "تماس",
                        contact = ContactDto(id = 1, fullName = "رضا احمدی", phone = "09121234567"),
                        reminder = ReminderDto(id = 1, title = "پیگیری خرید", dueAt = "09:00"),
                    ),
                    isOverdue = true,
                )
            }
            item { DfSectionHeader("امروز", 2) }
            item {
                TodayTaskCard(
                    item = TodayItemDto(
                        type = "بازدید",
                        contact = ContactDto(id = 2, fullName = "مریم کریمی", phone = "09129876543"),
                        reminder = ReminderDto(id = 2, title = "بازدید ملک", dueAt = "14:30"),
                    ),
                    isOverdue = false,
                )
            }
        }
    }
}
