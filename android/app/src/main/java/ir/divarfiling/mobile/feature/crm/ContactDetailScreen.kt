package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.design.DfColors

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfDropdown
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.network.ActivityDto
import ir.divarfiling.mobile.core.network.CustomerDocumentDto
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.LinkedListingDto
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.ReminderDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    onBack: () -> Unit,
    onDealClick: (Long) -> Unit = {},
    onPropertyClick: (Long) -> Unit = {},
    viewModel: ContactDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val contact = state.data?.contact
    val snackbarHostState = remember { SnackbarHostState() }
    val documentPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadDocument(it) }
    }

    LaunchedEffect(state.pendingWhatsAppShare) {
        val message = state.pendingWhatsAppShare ?: return@LaunchedEffect
        contact?.phone?.let { phone ->
            val wa = phone.removePrefix("0")
            val text = Uri.encode(message)
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/98$wa?text=$text")),
            )
        }
        viewModel.clearPendingWhatsAppShare()
    }

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

    Scaffold(
        topBar = {
            DfTopBar(
                title = contact?.fullName ?: "جزئیات مخاطب",
                onBack = onBack,
                actions = {
                    TextButton(onClick = { viewModel.toggleEditSheet(true) }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                state.error != null && state.data == null -> {
                    Column(Modifier.padding(16.dp)) { DfErrorBanner(state.error!!) }
                }
                state.data != null -> {
                    val detail = state.data!!
                    val contactInfo = detail.contact
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            ContactProfileCard(
                                name = contactInfo.fullName,
                                phone = contactInfo.phone,
                                status = contactInfo.status,
                                customerType = contactInfo.customerType,
                                priority = contactInfo.priority,
                                budget = contactInfo.budget,
                                notes = contactInfo.notes,
                                onStatusChange = viewModel::changeStatus,
                            )
                        }

                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    ContactActionChip("تماس", Icons.Default.Call) {
                                        contactInfo.phone?.let { phone ->
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                            viewModel.logActivity("تماس", "تماس تلفنی")
                                        }
                                    }
                                }
                                item {
                                    ContactActionChip("واتساپ", Icons.Default.Message) {
                                        contactInfo.phone?.let { phone ->
                                            val wa = phone.removePrefix("0")
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/98$wa")),
                                            )
                                            viewModel.logActivity("واتساپ", "پیام واتساپ")
                                        }
                                    }
                                }
                                item {
                                    ContactActionChip("پیامک", Icons.Default.Message) {
                                        contactInfo.phone?.let { phone ->
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("smsto:$phone")))
                                            viewModel.logActivity("پیامک", "ارسال پیامک")
                                        }
                                    }
                                }
                                item {
                                    ContactActionChip("ارسال فایل", Icons.Default.Share) {
                                        viewModel.toggleSendFilingSheet(true)
                                    }
                                }
                                item {
                                    ContactActionChip("مدرک", Icons.Default.AttachFile) {
                                        documentPicker.launch("*/*")
                                    }
                                }
                                item {
                                    ContactActionChip("یادداشت", Icons.Default.NoteAdd) {
                                        viewModel.toggleNoteDialog(true)
                                    }
                                }
                                item {
                                    ContactActionChip("یادآور", Icons.Default.Notifications) {
                                        viewModel.toggleReminderDialog(true)
                                    }
                                }
                                item {
                                    ContactActionChip("فعالیت", Icons.Default.History) {
                                        viewModel.toggleActivitySheet(true)
                                    }
                                }
                            }
                        }

                        val reminders = detail.reminders
                        if (reminders.isNotEmpty()) {
                            item { DfSectionHeader("یادآورها", reminders.size) }
                            items(reminders, key = { it.id }) { reminder ->
                                ReminderCard(
                                    reminder = reminder,
                                    onComplete = { viewModel.completeReminder(reminder.id) },
                                    onPostpone = { viewModel.postponeReminder(reminder.id) },
                                )
                            }
                        }

                        val deals = detail.deals
                        if (deals.isNotEmpty()) {
                            item { DfSectionHeader("معاملات", deals.size) }
                            items(deals, key = { it.id }) { deal ->
                                DealCard(deal, onClick = { onDealClick(deal.id) })
                            }
                        }

                        val properties = detail.properties
                        if (properties.isNotEmpty()) {
                            item { DfSectionHeader("املاک مرتبط", properties.size) }
                            items(properties, key = { it.id }) { property ->
                                PropertyCard(property, onClick = { onPropertyClick(property.id) })
                            }
                        }

                        val documents = detail.documents
                        if (documents.isNotEmpty()) {
                            item { DfSectionHeader("مدارک مخاطب", documents.size) }
                            items(documents, key = { it.id }) { doc ->
                                DocumentCard(
                                    document = doc,
                                    onOpen = { url ->
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                    },
                                    onDelete = { viewModel.deleteDocument(doc.id) },
                                )
                            }
                        }

                        val listings = detail.linkedListings
                        if (listings.isNotEmpty()) {
                            item { DfSectionHeader("آگهی‌های ارسال‌شده", listings.size) }
                            items(listings, key = { it.id }) { listing ->
                                LinkedListingCard(
                                    listing = listing,
                                    onShareWhatsApp = { link ->
                                        contactInfo.phone?.let { phone ->
                                            val wa = phone.removePrefix("0")
                                            val text = Uri.encode("${listing.title ?: ""}\n$link")
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://wa.me/98$wa?text=$text"),
                                                ),
                                            )
                                            viewModel.logActivity("واتساپ", "ارسال آگهی: ${listing.title}")
                                        }
                                    },
                                )
                            }
                        }

                        val activities = detail.activities
                        item { DfSectionHeader("تایم‌لاین فعالیت‌ها", activities.size) }
                        if (activities.isEmpty()) {
                            item {
                                DfEmptyState(
                                    title = "فعالیتی ثبت نشده",
                                    subtitle = "با تماس، یادداشت یا ثبت فعالیت، تایم‌لاین را شروع کنید",
                                )
                            }
                        } else {
                            items(activities, key = { it.id }) { act ->
                                ActivityTimelineItem(act)
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
                TextButton(onClick = viewModel::submitNote, enabled = !state.isSubmitting) {
                    Text("ثبت")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleNoteDialog(false) }) { Text("انصراف") }
            },
        )
    }

    if (state.showReminderDialog) {
        ReminderDialog(
            title = state.reminderTitle,
            note = state.reminderNote,
            dueMillis = state.reminderDueMillis,
            isSubmitting = state.isSubmitting,
            onTitleChange = viewModel::onReminderTitleChange,
            onNoteChange = viewModel::onReminderNoteChange,
            onDueChange = viewModel::onReminderDueChange,
            onDismiss = { viewModel.toggleReminderDialog(false) },
            onSubmit = viewModel::submitReminder,
        )
    }

    if (state.showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleEditSheet(false) },
            sheetState = sheetState,
        ) {
            ContactEditSheet(
                name = state.editName,
                phone = state.editPhone,
                status = state.editStatus,
                customerType = state.editCustomerType,
                priority = state.editPriority,
                budget = state.editBudget,
                notes = state.editNotes,
                isSubmitting = state.isSubmitting,
                onNameChange = viewModel::onEditNameChange,
                onPhoneChange = viewModel::onEditPhoneChange,
                onStatusChange = viewModel::onEditStatusChange,
                onCustomerTypeChange = viewModel::onEditCustomerTypeChange,
                onPriorityChange = viewModel::onEditPriorityChange,
                onBudgetChange = viewModel::onEditBudgetChange,
                onNotesChange = viewModel::onEditNotesChange,
                onSave = viewModel::saveEdit,
                onDismiss = { viewModel.toggleEditSheet(false) },
            )
        }
    }

    if (state.showActivitySheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleActivitySheet(false) },
            sheetState = sheetState,
        ) {
            ActivityLogSheet(
                activityType = state.selectedActivityType,
                content = state.activityContent,
                isSubmitting = state.isSubmitting,
                onTypeChange = viewModel::onActivityTypeChange,
                onContentChange = viewModel::onActivityContentChange,
                onSubmit = {
                    viewModel.logActivity(
                        state.selectedActivityType,
                        state.activityContent,
                        state.selectedActivityType,
                    )
                },
                onDismiss = { viewModel.toggleActivitySheet(false) },
            )
        }
    }

    if (state.showSendFilingSheet) {
        SendFilingSheet(
            step = state.filingPickerStep,
            datasets = state.filingDatasets,
            listings = state.filingListings,
            note = state.sendListingNote,
            isLoading = state.isFilingLoading,
            isSubmitting = state.isSubmitting,
            onNoteChange = viewModel::onSendListingNoteChange,
            onDismiss = { viewModel.toggleSendFilingSheet(false) },
            onDatasetSelected = viewModel::selectFilingDataset,
            onBackToDatasets = viewModel::backToFilingDatasets,
            onListingSend = viewModel::sendListingFromFiling,
        )
    }
}

@Composable
private fun ContactProfileCard(
    name: String,
    phone: String?,
    status: String?,
    customerType: String?,
    priority: String?,
    budget: Long?,
    notes: String?,
    onStatusChange: (String) -> Unit,
) {
    DfPremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(name, style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
            phone?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                status?.let { DfBadge(it) }
                customerType?.let {
                    DfBadge(it, color = DfColors.PurpleContainer, textColor = DfColors.Purple)
                }
                priority?.let {
                    DfBadge(it, color = DfColors.AmberLight, textColor = DfColors.Amber)
                }
            }
            budget?.let {
                Text(
                    "بودجه: ${FormatUtils.formatPriceToman(it)}",
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                )
            }
            notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription)
            }
            Text("تغییر سریع وضعیت", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CrmConstants.STATUSES.forEach { s ->
                    val selected = s == status
                    TextButton(
                        onClick = { if (!selected) onStatusChange(s) },
                        enabled = !selected,
                    ) {
                        Text(
                            s,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) DfColors.Purple else DfColors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    DfPremiumCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null, tint = DfColors.Purple)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: ReminderDto,
    onComplete: () -> Unit,
    onPostpone: () -> Unit,
) {
    DfPremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(reminder.title, style = AppTypography.cardTitle)
            reminder.dueAt?.let { due ->
                Text(formatDateTime(due), style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onComplete) { Text("انجام شد") }
                TextButton(onClick = onPostpone) { Text("تعویق ۱ روز") }
            }
        }
    }
}

@Composable
private fun DealCard(deal: DealDto, onClick: () -> Unit = {}) {
    DfPremiumCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(deal.title, style = AppTypography.cardTitle)
            deal.stage?.let { DfBadge(it) }
            deal.amount?.let {
                Text(
                    FormatUtils.formatPriceToman(it),
                    style = AppTypography.bodyDescription,
                    color = DfColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun PropertyCard(property: PropertyDto, onClick: () -> Unit = {}) {
    DfPremiumCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(property.title, style = AppTypography.cardTitle)
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
                Text(FormatUtils.formatPriceToman(it), style = AppTypography.bodyDescription)
            }
        }
    }
}

@Composable
private fun LinkedListingCard(
    listing: LinkedListingDto,
    onShareWhatsApp: (String) -> Unit,
) {
    val context = LocalContext.current
    DfPremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(listing.title ?: listing.token, style = AppTypography.cardTitle)
            listing.price?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
            listing.role?.let { DfBadge(it) }
            listing.notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listing.link?.takeIf { it.isNotBlank() }?.let { link ->
                    TextButton(onClick = { onShareWhatsApp(link) }) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Text("واتساپ")
                    }
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }) {
                        Text("دیوار")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: CustomerDocumentDto,
    onOpen: (String) -> Unit,
    onDelete: () -> Unit,
) {
    DfPremiumCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(document.title, style = AppTypography.cardTitle)
            document.docType?.takeIf { it.isNotBlank() }?.let { DfBadge(it) }
            document.uploadedAt?.let {
                Text(formatDateTime(it), style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                document.fileUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    TextButton(onClick = { onOpen(url) }) { Text("مشاهده") }
                }
                TextButton(onClick = onDelete) { Text("حذف") }
            }
        }
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
                    activity.title ?: activity.activityTypeLabel ?: activity.activityType ?: "فعالیت",
                    style = AppTypography.cardTitle,
                )
                activity.content?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                }
            }
            activity.createdAt?.take(16)?.let {
                Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactEditSheet(
    name: String,
    phone: String,
    status: String,
    customerType: String,
    priority: String,
    budget: String,
    notes: String,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onCustomerTypeChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("ویرایش مخاطب", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("نام") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("تلفن") }, modifier = Modifier.fillMaxWidth())
        DfDropdown("وضعیت", status, CrmConstants.STATUSES, enabled = true, onStatusChange)
        DfDropdown("نوع مخاطب", customerType, CrmConstants.CUSTOMER_TYPES, enabled = true, onCustomerTypeChange)
        DfDropdown("اولویت", priority, CrmConstants.PRIORITIES, enabled = true, onPriorityChange)
        OutlinedTextField(
            value = budget,
            onValueChange = onBudgetChange,
            label = { Text("بودجه (تومان)") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("یادداشت") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DfPrimaryButton("ذخیره", onClick = onSave, enabled = !isSubmitting, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    }
}

@Composable
private fun ActivityLogSheet(
    activityType: String,
    content: String,
    isSubmitting: Boolean,
    onTypeChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("ثبت فعالیت", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        DfDropdown(
            "نوع فعالیت",
            activityType,
            CrmConstants.QUICK_ACTIVITY_TYPES.map { it.first },
            enabled = true,
            onTypeChange,
        )
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("توضیحات") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DfPrimaryButton("ثبت", onClick = onSubmit, enabled = !isSubmitting, modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDialog(
    title: String,
    note: String,
    dueMillis: Long,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDueChange: (Long) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()
    val localDateTime = Instant.ofEpochMilli(dueMillis).atZone(zone).toLocalDateTime()
    val displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale("fa", "IR"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("یادآور جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("عنوان") },
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("یادداشت (اختیاری)") },
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text("زمان: ${localDateTime.format(displayFormatter)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = !isSubmitting) { Text("ثبت") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )

    if (showDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = dueMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { selected ->
                        val current = Instant.ofEpochMilli(dueMillis).atZone(zone).toLocalDateTime()
                        val newDate = Instant.ofEpochMilli(selected).atZone(zone).toLocalDate()
                        val updated = newDate.atTime(current.hour, current.minute)
                        onDueChange(updated.atZone(zone).toInstant().toEpochMilli())
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("بعدی") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("انصراف") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = localDateTime.hour,
            initialMinute = localDateTime.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("انتخاب ساعت") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val current = Instant.ofEpochMilli(dueMillis).atZone(zone).toLocalDateTime()
                    val updated = current.toLocalDate().atTime(timeState.hour, timeState.minute)
                    onDueChange(updated.atZone(zone).toInstant().toEpochMilli())
                    showTimePicker = false
                }) { Text("تأیید") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("انصراف") }
            },
        )
    }
}

private fun formatDateTime(iso: String): String {
    return runCatching {
        val dt = Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDateTime()
        dt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale("fa", "IR")))
    }.getOrElse { iso.take(16) }
}
