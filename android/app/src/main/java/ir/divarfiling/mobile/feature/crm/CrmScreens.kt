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
import androidx.compose.material.icons.filled.CalendarToday
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
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfContactRow
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DfSearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "جستجو نام یا تلفن…",
                    onSearch = viewModel::search,
                )

                state.error?.let { DfErrorBanner(it) }

                if (!state.isLoading && state.contacts.isEmpty() && state.error == null) {
                    DfEmptyState(
                        title = "مخاطبی ثبت نشده",
                        subtitle = "با دکمه + یک سرنخ سریع اضافه کنید",
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.contacts, key = { it.id }) { contact ->
                            DfContactRow(
                                name = contact.fullName,
                                phone = contact.phone,
                                status = contact.status,
                                customerType = contact.customerType,
                            )
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
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

                state.data?.let { today ->
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            DfStatChip(label = "امروز", value = "${today.stats?.total ?: 0}")
                            DfStatChip(label = "انجام‌شده", value = "${today.stats?.done ?: 0}")
                            DfStatChip(label = "معوق", value = "${today.overdue.size}")
                        }
                    }
                    today.date?.let { date ->
                        item {
                            Text(
                                text = "تاریخ: $date",
                                style = AppTypography.bodyDescription,
                                color = AppColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    if (today.overdue.isNotEmpty()) {
                        item { DfSectionHeader("معوق", today.overdue.size) }
                        items(today.overdue, key = { "overdue-${it.reminder?.id ?: it.contact?.id}") { item ->
                            TodayTaskCard(item, isOverdue = true)
                        }
                    }
                    if (today.today.isNotEmpty()) {
                        item { DfSectionHeader("امروز", today.today.size) }
                        items(today.today, key = { "today-${it.reminder?.id ?: it.contact?.id}") { item ->
                            TodayTaskCard(item, isOverdue = false)
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
private fun TodayTaskCard(item: TodayItemDto, isOverdue: Boolean) {
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
        containerColor = if (isOverdue) AppColors.OverdueBackground else AppColors.Surface,
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
                    color = AppColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = AppTypography.bodyDescription,
                        color = AppColors.TextSecondary,
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
                            color = AppColors.SurfaceVariant,
                            textColor = AppColors.TextSecondary,
                        )
                    }
                    dueAt?.let { due ->
                        Text(
                            text = due,
                            style = AppTypography.labelSmall,
                            color = AppColors.TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (isOverdue) {
                        DfBadge(
                            text = "معوق",
                            color = AppColors.RoseLight,
                            textColor = AppColors.OverdueAccent,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(AppShapes.IconContainer)
                    .background(
                        if (isOverdue) AppColors.RoseLight else AppColors.PurpleContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = if (isOverdue) AppColors.OverdueAccent else AppColors.Purple,
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
            DfCard(containerColor = DfColors.SurfaceVariant) {
                Text("معاملات و املاک", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Text(
                    "به‌زودی — همگام با میزکار وب",
                    style = MaterialTheme.typography.bodySmall,
                    color = DfColors.TextMuted,
                )
            }
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = DfColors.Purple)
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DfColors.TextSecondary)
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
