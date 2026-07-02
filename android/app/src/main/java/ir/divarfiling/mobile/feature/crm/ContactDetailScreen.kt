package ir.divarfiling.mobile.feature.crm

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.ListingMessageFormatter
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.feature.crm.components.ActivityLogSheet
import ir.divarfiling.mobile.feature.crm.components.ContactActivityTimeline
import ir.divarfiling.mobile.feature.crm.components.ContactDealCard
import ir.divarfiling.mobile.feature.crm.components.ContactDetailHero
import ir.divarfiling.mobile.feature.crm.components.ContactDetailInsightStrip
import ir.divarfiling.mobile.feature.crm.components.ContactDetailQuickActionsPanel
import ir.divarfiling.mobile.feature.crm.components.ContactDetailSectionHeader
import ir.divarfiling.mobile.feature.crm.components.ContactDetailStatusBar
import ir.divarfiling.mobile.feature.crm.components.ContactDocumentCard
import ir.divarfiling.mobile.feature.crm.components.ContactEditSheet
import ir.divarfiling.mobile.feature.crm.components.ContactLinkedListingCard
import ir.divarfiling.mobile.feature.crm.components.ContactMatchesSheet
import ir.divarfiling.mobile.feature.crm.components.ContactNoteSheet
import ir.divarfiling.mobile.feature.crm.components.ContactQuickActionItem
import ir.divarfiling.mobile.feature.crm.components.ContactReminderCard
import ir.divarfiling.mobile.feature.crm.components.ContactReminderSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyListCard

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
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/98$wa?text=$text")))
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
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                state.error != null && state.data == null -> {
                    DfDetailPageHeader(
                        title = "جزئیات مخاطب",
                        onBack = onBack,
                        titleIconRes = DfDecorIcons.User,
                    )
                    DfErrorBanner(
                        state.error!!,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                state.data != null -> {
                    val detail = state.data!!
                    val contactInfo = detail.contact
                    val primaryActions = buildPrimaryActions(contactInfo, context, viewModel)
                    val secondaryActions = buildSecondaryActions(contactInfo, viewModel, documentPicker::launch)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        item {
                            ContactDetailHero(
                                contact = contactInfo,
                                onBack = onBack,
                                onEdit = { viewModel.toggleEditSheet(true) },
                            )
                        }
                        item { ContactDetailInsightStrip(contact = contactInfo) }
                        item {
                            ContactDetailQuickActionsPanel(
                                primary = primaryActions,
                                secondary = secondaryActions,
                            )
                        }
                        item {
                            ContactDetailStatusBar(
                                currentStatus = contactInfo.status,
                                onStatusChange = viewModel::changeStatus,
                            )
                        }

                        val reminders = detail.reminders
                        if (reminders.isNotEmpty()) {
                            item { ContactDetailSectionHeader("یادآورها", reminders.size) }
                            items(reminders, key = { it.id ?: it.hashCode().toLong() }) { reminder ->
                                reminder.id?.let { id ->
                                    ContactReminderCard(
                                        reminder = reminder,
                                        onComplete = { viewModel.completeReminder(id) },
                                        onPostpone = { viewModel.postponeReminder(id) },
                                    )
                                }
                            }
                        }

                        val deals = detail.deals
                        if (deals.isNotEmpty()) {
                            item { ContactDetailSectionHeader("معاملات", deals.size) }
                            items(deals, key = { it.id }) { deal ->
                                ContactDealCard(deal, onClick = { onDealClick(deal.id) })
                            }
                        }

                        val properties = detail.properties
                        if (properties.isNotEmpty()) {
                            item { ContactDetailSectionHeader("املاک مرتبط", properties.size) }
                            items(properties, key = { it.id }) { property ->
                                PropertyListCard(
                                    property = property,
                                    onClick = { onPropertyClick(property.id) },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                )
                            }
                        }

                        val documents = detail.documents
                        if (documents.isNotEmpty()) {
                            item { ContactDetailSectionHeader("مدارک", documents.size) }
                            items(documents, key = { it.id }) { doc ->
                                ContactDocumentCard(
                                    document = doc,
                                    onOpen = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                                    onDelete = { viewModel.deleteDocument(doc.id) },
                                )
                            }
                        }

                        val listings = detail.linkedListings
                        if (listings.isNotEmpty()) {
                            item { ContactDetailSectionHeader("فایل‌های ارسال‌شده", listings.size) }
                            items(listings, key = { it.id }) { listing ->
                                ContactLinkedListingCard(
                                    listing = listing,
                                    onShareWhatsApp = {
                                        contactInfo.phone?.let { phone ->
                                            val wa = phone.removePrefix("0")
                                            val text = Uri.encode(ListingMessageFormatter.fromLinked(listing))
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/98$wa?text=$text")),
                                            )
                                            viewModel.logActivity("واتساپ", "ارسال فایل: ${listing.title}")
                                        }
                                    },
                                    onOpenLink = { link ->
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                                    },
                                )
                            }
                        }

                        item {
                            ContactActivityTimeline(
                                activities = detail.activities,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showNoteDialog) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleNoteDialog(false) }) {
            ContactNoteSheet(
                note = state.noteText,
                isSubmitting = state.isSubmitting,
                onNoteChange = viewModel::onNoteChange,
                onSubmit = viewModel::submitNote,
                onDismiss = { viewModel.toggleNoteDialog(false) },
            )
        }
    }

    if (state.showReminderDialog) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleReminderDialog(false) }) {
            ContactReminderSheet(
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
    }

    if (state.showEditSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleEditSheet(false) }) {
            ContactEditSheet(
                name = state.editName,
                phone = state.editPhone,
                status = state.editStatus,
                customerType = state.editCustomerType,
                priority = state.editPriority,
                money = state.editMoney,
                prefs = state.editPrefs,
                notes = state.editNotes,
                isSubmitting = state.isSubmitting,
                onNameChange = viewModel::onEditNameChange,
                onPhoneChange = viewModel::onEditPhoneChange,
                onStatusChange = viewModel::onEditStatusChange,
                onCustomerTypeChange = viewModel::onEditCustomerTypeChange,
                onPriorityChange = viewModel::onEditPriorityChange,
                onBudgetMinChange = viewModel::onEditBudgetMinChange,
                onBudgetMaxChange = viewModel::onEditBudgetMaxChange,
                onDepositMinChange = viewModel::onEditDepositMinChange,
                onDepositMaxChange = viewModel::onEditDepositMaxChange,
                onRentMinChange = viewModel::onEditRentMinChange,
                onRentMaxChange = viewModel::onEditRentMaxChange,
                onPropertyTypeChange = viewModel::onEditPropertyTypeChange,
                onRoomsChange = viewModel::onEditRoomsChange,
                onMinAreaChange = viewModel::onEditMinAreaChange,
                onMaxAreaChange = viewModel::onEditMaxAreaChange,
                onAreasChange = viewModel::onEditAreasChange,
                onNotesChange = viewModel::onEditNotesChange,
                onSave = viewModel::saveEdit,
                onDismiss = { viewModel.toggleEditSheet(false) },
            )
        }
    }

    if (state.showActivitySheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleActivitySheet(false) }) {
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

    ContactMatchesSheet(
        visible = state.showMatchesSheet,
        matches = state.matchesData,
        isLoading = state.matchesLoading,
        isSubmitting = state.isSubmitting,
        contactPhone = contact?.phone,
        onDismiss = { viewModel.toggleMatchesSheet(false) },
        onSuggest = { selected ->
            viewModel.suggestMatches(selected, shareViaWhatsApp = contact?.phone != null)
        },
    )
}

private fun buildPrimaryActions(
    contact: ir.divarfiling.mobile.core.network.ContactDto,
    context: android.content.Context,
    viewModel: ContactDetailViewModel,
): List<ContactQuickActionItem> = buildList {
    add(
        ContactQuickActionItem("تماس", DfColors.Blue, icon = Icons.Default.Call) {
            contact.phone?.let { phone ->
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                viewModel.logActivity("تماس", "تماس تلفنی")
            }
        },
    )
    add(
        ContactQuickActionItem("واتساپ", DfColors.Green, iconRes = R.drawable.ic_whatsapp) {
            contact.phone?.let { phone ->
                val wa = phone.removePrefix("0")
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/98$wa")))
                viewModel.logActivity("واتساپ", "پیام واتساپ")
            }
        },
    )
    add(
        ContactQuickActionItem("پیامک", DfColors.Amber, icon = Icons.Default.Message) {
            contact.phone?.let { phone ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("smsto:$phone")))
                viewModel.logActivity("پیامک", "ارسال پیامک")
            }
        },
    )
    if (CrmConstants.isMatchEligible(contact.customerType)) {
        add(
            ContactQuickActionItem("پیشنهاد", DfColors.Purple, iconRes = DfDecorIcons.Sparkles) {
                viewModel.toggleMatchesSheet(true)
            },
        )
    } else {
        add(
            ContactQuickActionItem("یادآور", DfColors.Rose, icon = Icons.Default.Notifications) {
                viewModel.toggleReminderDialog(true)
            },
        )
    }
}

private fun buildSecondaryActions(
    contact: ir.divarfiling.mobile.core.network.ContactDto,
    viewModel: ContactDetailViewModel,
    pickDocument: (String) -> Unit,
): List<ContactQuickActionItem> = buildList {
    if (CrmConstants.isMatchEligible(contact.customerType)) {
        add(ContactQuickActionItem("یادآور", DfColors.Rose, icon = Icons.Default.Notifications) {
            viewModel.toggleReminderDialog(true)
        })
    }
    add(ContactQuickActionItem("یادداشت", DfColors.Purple, icon = Icons.Default.NoteAdd) {
        viewModel.toggleNoteDialog(true)
    })
    add(ContactQuickActionItem("فایل", DfColors.Blue, icon = Icons.Default.Share) {
        viewModel.toggleSendFilingSheet(true)
    })
    add(ContactQuickActionItem("مدرک", DfColors.TextSecondary, icon = Icons.Default.AttachFile) {
        pickDocument("*/*")
    })
    add(ContactQuickActionItem("فعالیت", DfColors.Blue, icon = Icons.Default.History) {
        viewModel.toggleActivitySheet(true)
    }
}
