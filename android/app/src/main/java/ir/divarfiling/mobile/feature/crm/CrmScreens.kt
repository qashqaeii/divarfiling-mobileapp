package ir.divarfiling.mobile.feature.crm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfContactRow
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfFab
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfStatChip
import ir.divarfiling.mobile.core.design.components.DfTopBar
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
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.error?.let { DfErrorBanner(it) }

                state.data?.let { today ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DfStatChip(label = "امروز", value = "${today.stats?.total ?: 0}")
                        DfStatChip(label = "انجام‌شده", value = "${today.stats?.done ?: 0}")
                        DfStatChip(label = "معوق", value = "${today.overdue.size}")
                    }
                    today.date?.let {
                        Text("تاریخ: $it", color = DfColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }

                    if (today.overdue.isNotEmpty()) {
                        DfSectionHeader("معوق", today.overdue.size)
                        today.overdue.forEach { item ->
                            TodayTaskCard(item, isOverdue = true)
                        }
                    }
                    if (today.today.isNotEmpty()) {
                        DfSectionHeader("امروز", today.today.size)
                        today.today.forEach { item ->
                            TodayTaskCard(item, isOverdue = false)
                        }
                    }
                    if (today.overdue.isEmpty() && today.today.isEmpty()) {
                        DfEmptyState(
                            title = "کاری برای امروز نیست",
                            subtitle = "همه پیگیری‌ها انجام شده — عالی!",
                        )
                    }
                } ?: if (!state.isLoading && state.error == null) {
                    DfEmptyState(title = "داده‌ای نیست", subtitle = "با کشیدن صفحه به‌روزرسانی کنید")
                }
            }
        }
    }
}

@Composable
private fun TodayTaskCard(item: TodayItemDto, isOverdue: Boolean) {
    val title = item.contact?.fullName ?: item.reminder?.title ?: "—"
    val subtitle = item.reminder?.dueAt ?: item.contact?.phone.orEmpty()
    DfCard(
        containerColor = if (isOverdue) DfColors.Rose.copy(alpha = 0.08f) else DfColors.Surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                if (isOverdue) Icons.Default.Warning else Icons.Default.CalendarToday,
                contentDescription = null,
                tint = if (isOverdue) DfColors.Rose else DfColors.Purple,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = DfColors.TextSecondary)
                }
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
