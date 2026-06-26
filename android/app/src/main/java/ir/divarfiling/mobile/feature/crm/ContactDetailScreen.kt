package ir.divarfiling.mobile.feature.crm

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppColors
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.network.ActivityDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    onBack: () -> Unit,
    viewModel: ContactDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val contact = state.data?.contact

    Scaffold(
        topBar = {
            DfTopBar(
                title = contact?.fullName ?: "جزئیات مخاطب",
                onBack = onBack,
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
            when {
                state.isLoading -> {
                    DfEmptyState(title = "در حال بارگذاری…", subtitle = "")
                }
                state.error != null && state.data == null -> {
                    Column(Modifier.padding(16.dp)) {
                        DfErrorBanner(state.error!!)
                    }
                }
                state.data != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            DfPremiumCard {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        contact!!.fullName,
                                        style = AppTypography.sectionTitle,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    contact.phone?.let {
                                        Text(it, style = AppTypography.bodyDescription, color = AppColors.TextSecondary)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        contact.status?.let { DfBadge(it) }
                                        contact.customerType?.let {
                                            DfBadge(it, color = AppColors.PurpleContainer, textColor = AppColors.Purple)
                                        }
                                    }
                                    contact.notes?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = AppTypography.bodyDescription)
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ActionChip("تماس", Icons.Default.Call) {
                                    contact.phone?.let { phone ->
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                        viewModel.logActivity("call", "تماس تلفنی")
                                    }
                                }
                                ActionChip("واتساپ", Icons.Default.Call) {
                                    contact.phone?.let { phone ->
                                        val wa = phone.removePrefix("0")
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/98$wa")),
                                        )
                                        viewModel.logActivity("whatsapp", "پیام واتساپ")
                                    }
                                }
                                ActionChip("یادداشت", Icons.Default.NoteAdd) {
                                    viewModel.toggleNoteDialog(true)
                                }
                                ActionChip("یادآور", Icons.Default.Notifications) {
                                    viewModel.toggleReminderDialog(true)
                                }
                                ActionChip("ویرایش", Icons.Default.Edit) {
                                    viewModel.toggleEditDialog(true)
                                }
                            }
                        }

                        state.successMessage?.let { msg ->
                            item { DfBadge(msg, color = AppColors.PurpleContainer, textColor = AppColors.Purple) }
                        }

                        val reminders = state.data!!.reminders
                        if (reminders.isNotEmpty()) {
                            item { DfSectionHeader("یادآورهای فعال", reminders.size) }
                            items(reminders, key = { it.id }) { r ->
                                DfPremiumCard {
                                    Text(r.title, style = AppTypography.cardTitle)
                                    r.dueAt?.let {
                                        Text(it, style = AppTypography.labelSmall, color = AppColors.TextMuted)
                                    }
                                }
                            }
                        }

                        val activities = state.data!!.activities
                        item { DfSectionHeader("تایم‌لاین فعالیت‌ها", activities.size) }
                        if (activities.isEmpty()) {
                            item {
                                DfEmptyState(
                                    title = "فعالیتی ثبت نشده",
                                    subtitle = "با تماس یا یادداشت، تایم‌لاین را شروع کنید",
                                )
                            }
                        } else {
                            items(activities, key = { it.id }) { act ->
                                ActivityTimelineItem(act)
                            }
                        }

                        val listings = state.data!!.linkedListings
                        if (listings.isNotEmpty()) {
                            item { DfSectionHeader("آگهی‌های لینک‌شده", listings.size) }
                            items(listings, key = { it.id }) { lst ->
                                DfPremiumCard {
                                    Text(lst.title ?: lst.token, style = AppTypography.cardTitle)
                                    lst.price?.let {
                                        Text(it, style = AppTypography.bodyDescription, color = AppColors.TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showNoteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleNoteDialog(false) },
            title = { Text("یادداشت جدید") },
            text = {
                OutlinedTextField(
                    value = state.noteText,
                    onValueChange = viewModel::onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("متن یادداشت…") },
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::submitNote) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleNoteDialog(false) }) { Text("انصراف") }
            },
        )
    }

    if (state.showReminderDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleReminderDialog(false) },
            title = { Text("یادآور جدید") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.reminderTitle,
                        onValueChange = viewModel::onReminderTitleChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("عنوان") },
                    )
                    OutlinedTextField(
                        value = state.reminderDueAt,
                        onValueChange = viewModel::onReminderDueChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("زمان (ISO: 2026-06-26T10:00:00)") },
                        placeholder = { Text("2026-06-26T10:00:00") },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::submitReminder) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleReminderDialog(false) }) { Text("انصراف") }
            },
        )
    }

    if (state.showEditDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleEditDialog(false) },
            title = { Text("ویرایش مخاطب") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.editName,
                        onValueChange = viewModel::onEditNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نام") },
                    )
                    OutlinedTextField(
                        value = state.editPhone,
                        onValueChange = viewModel::onEditPhoneChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("تلفن") },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveEdit) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleEditDialog(false) }) { Text("انصراف") }
            },
        )
    }
}

@Composable
private fun ActionChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Icon(icon, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ActivityTimelineItem(activity: ActivityDto) {
    DfPremiumCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.title ?: activity.activityTypeLabel ?: "فعالیت",
                    style = AppTypography.cardTitle,
                )
                activity.content?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = AppTypography.bodyDescription, color = AppColors.TextSecondary)
                }
            }
            activity.createdAt?.take(16)?.let {
                Text(it, style = AppTypography.labelSmall, color = AppColors.TextMuted)
            }
        }
    }
}
