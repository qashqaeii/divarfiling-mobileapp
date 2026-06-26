package ir.divarfiling.mobile.feature.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مخاطبین") },
                actions = {
                    IconButton(onClick = viewModel::search) {
                        Icon(Icons.Default.Search, contentDescription = "جستجو")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.toggleQuickLead(true) }) {
                Icon(Icons.Default.Add, contentDescription = "سرنخ جدید")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جستجو") },
                singleLine = true,
            )
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.padding(16.dp))
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.contacts, key = { it.id }) { contact ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(contact.fullName, style = MaterialTheme.typography.titleMedium)
                            contact.phone?.let { Text(it) }
                            contact.status?.let {
                                Text(it, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showQuickLead) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleQuickLead(false) },
            title = { Text("ثبت سریع سرنخ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.leadName,
                        onValueChange = viewModel::onLeadNameChange,
                        label = { Text("نام") },
                    )
                    OutlinedTextField(
                        value = state.leadPhone,
                        onValueChange = viewModel::onLeadPhoneChange,
                        label = { Text("تلفن") },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::submitQuickLead) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleQuickLead(false) }) { Text("انصراف") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onRefresh: () -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("کارهای امروز") }) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            if (state.isLoading) CircularProgressIndicator()
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.data?.let { today ->
                Text("تاریخ: ${today.date ?: "—"}", style = MaterialTheme.typography.titleMedium)
                Text("کل: ${today.stats?.total ?: 0} — انجام‌شده: ${today.stats?.done ?: 0}")
                if (today.overdue.isNotEmpty()) {
                    Text("معوق", style = MaterialTheme.typography.titleSmall)
                    today.overdue.forEach { item ->
                        TaskRow(item.contact?.fullName ?: item.reminder?.title ?: "—")
                    }
                }
                if (today.today.isNotEmpty()) {
                    Text("امروز", style = MaterialTheme.typography.titleSmall)
                    today.today.forEach { item ->
                        TaskRow(item.contact?.fullName ?: item.reminder?.title ?: "—")
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(title: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f))
    }
}

@Composable
fun CrmHubScreen(
    onContacts: () -> Unit,
    onToday: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("CRM", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth().clickable(onClick = onContacts)) {
            Text("مخاطبین", Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
        }
        Card(Modifier.fillMaxWidth().clickable(onClick = onToday)) {
            Text("کارهای امروز", Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("معاملات و املاک", style = MaterialTheme.typography.titleMedium)
                Text("به‌زودی — sync با سرور", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
