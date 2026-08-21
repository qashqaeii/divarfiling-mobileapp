package ir.divarfiling.mobile.feature.crm

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import ir.divarfiling.mobile.core.design.components.DfNbaAction
import ir.divarfiling.mobile.core.design.components.DfNbaCard
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.share.DossierShareActions
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfHapticPerformer
import ir.divarfiling.mobile.core.design.DfHaptics
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.ListingMessageFormatter
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.design.components.DfSnackbarHost
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.rememberDfSnackbarHostState
import ir.divarfiling.mobile.core.design.components.showDfMessage
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
import ir.divarfiling.mobile.feature.crm.components.ContactProfileDossier
import ir.divarfiling.mobile.feature.crm.components.ContactReminderCard
import ir.divarfiling.mobile.feature.crm.components.ContactReminderSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyListCard
import ir.divarfiling.mobile.feature.team.TeamMemberSelectList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    onBack: () -> Unit,
    onDealClick: (Long) -> Unit = {},
    onPropertyClick: (Long) -> Unit = {},
    onOpenAi: (Long) -> Unit = {},
    viewModel: ContactDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val contact = state.data?.contact
    val snackbarHostState = rememberDfSnackbarHostState()
    val haptics = DfHaptics.rememberPerformer()
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
            snackbarHostState.showDfMessage(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbarHostState.showDfMessage(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { DfSnackbarHost(snackbarHostState) },
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
                        onRetry = viewModel::refresh,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                state.data != null -> {
                    val detail = state.data!!
                    val contactInfo = detail.contact
                    val nbaAction = resolveContactNba(contactInfo, deals = detail.deals, linkedListings = detail.linkedListings)
                    val primaryActions = buildReachActions(contactInfo, context, viewModel, haptics)
                    val secondaryActions = buildSecondaryActions(
                        contact = contactInfo,
                        viewModel = viewModel,
                        pickDocument = documentPicker::launch,
                        onOpenAi = { onOpenAi(contactInfo.id) },
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
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
                            DfNbaCard(
                                action = DfNbaAction(
                                    title = nbaAction.title,
                                    subtitle = nbaAction.subtitle,
                                    cta = nbaAction.cta,
                                    tone = nbaAction.tone,
                                    onClick = {
                                        haptics.confirm()
                                        performContactNba(
                                            nba = nbaAction,
                                            contact = contactInfo,
                                            context = context,
                                            viewModel = viewModel,
                                            onDealClick = onDealClick,
                                        )
                                    },
                                ),
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
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

                        item {
                            ContactProfileDossier(contact = contactInfo)
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
                                        onEdit = { viewModel.openReminderEditor(reminder) },
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
                recurrence = state.reminderRecurrence,
                isSubmitting = state.isSubmitting,
                sheetTitle = if (state.editingReminderId == null) "یادآور جدید" else "ویرایش یادآور",
                primaryText = if (state.editingReminderId == null) "ثبت یادآور" else "ذخیره تغییرات",
                onDelete = state.editingReminderId?.let { reminderId ->
                    { viewModel.deleteReminder(reminderId) }
                },
                onTitleChange = viewModel::onReminderTitleChange,
                onNoteChange = viewModel::onReminderNoteChange,
                onDueChange = viewModel::onReminderDueChange,
                onRecurrenceChange = viewModel::onReminderRecurrenceChange,
                onDismiss = { viewModel.toggleReminderDialog(false) },
                onSubmit = viewModel::submitReminder,
            )
        }
    }

    if (state.showEditSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.requestDismissEdit() }) {
            ContactEditSheet(
                name = state.editName,
                phone = state.editPhone,
                status = state.editStatus,
                customerType = state.editCustomerType,
                priority = state.editPriority,
                money = state.editMoney,
                prefs = state.editPrefs,
                builder = state.editBuilder,
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
                onCityChange = viewModel::onEditCityChange,
                onYearMinChange = viewModel::onEditYearMinChange,
                onYearMaxChange = viewModel::onEditYearMaxChange,
                onFloorMinChange = viewModel::onEditFloorMinChange,
                onFloorMaxChange = viewModel::onEditFloorMaxChange,
                onWantParkingChange = viewModel::onEditWantParkingChange,
                onWantStorageChange = viewModel::onEditWantStorageChange,
                onWantElevatorChange = viewModel::onEditWantElevatorChange,
                onBuilderBuyBudgetMinChange = viewModel::onEditBuilderBuyBudgetMinChange,
                onBuilderBuyBudgetMaxChange = viewModel::onEditBuilderBuyBudgetMaxChange,
                onBuilderBuyMinAreaChange = viewModel::onEditBuilderBuyMinAreaChange,
                onBuilderBuyMaxAreaChange = viewModel::onEditBuilderBuyMaxAreaChange,
                onBuilderBuyAreasChange = viewModel::onEditBuilderBuyAreasChange,
                onBuilderBuyTypesChange = viewModel::onEditBuilderBuyTypesChange,
                onNotesChange = viewModel::onEditNotesChange,
                onSave = {
                    haptics.confirm()
                    viewModel.saveEdit()
                },
                onDismiss = { viewModel.requestDismissEdit() },
            )
        }
    }

    if (state.showDiscardEditDialog) {
        DfConfirmBottomSheet(
            title = "تغییرات ذخیره نشده",
            message = "ویرایش مخاطب ذخیره نشده است. از تغییرات صرف‌نظر می‌کنید؟",
            confirmText = "صرف‌نظر",
            cancelText = "ادامه ویرایش",
            destructive = true,
            onConfirm = viewModel::confirmDiscardEdit,
            onDismiss = viewModel::cancelDiscardEdit,
        )
    }

    if (state.showActivitySheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleActivitySheet(false) }) {
            ActivityLogSheet(
                activityType = state.selectedActivityType,
                content = state.activityContent,
                selectedStatus = state.selectedActivityStatus,
                isSubmitting = state.isSubmitting,
                onTypeChange = viewModel::onActivityTypeChange,
                onContentChange = viewModel::onActivityContentChange,
                onStatusChange = viewModel::onActivityStatusChange,
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
            templates = state.messageTemplates,
            templatesLoading = state.templatesLoading,
            showTemplatePicker = state.showTemplatePicker,
            onNoteChange = viewModel::onSendListingNoteChange,
            onToggleTemplatePicker = viewModel::toggleTemplatePicker,
            onApplyTemplate = viewModel::applyMessageTemplate,
            onDismiss = { viewModel.toggleSendFilingSheet(false) },
            onDatasetSelected = viewModel::selectFilingDataset,
            onBackToDatasets = viewModel::backToFilingDatasets,
            onListingSend = viewModel::sendListingFromFiling,
        )
    }

    if (state.showTeamAssignSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissTeamAssign) {
            DfSheetScaffold(
                title = if (state.teamAssignMode == "transfer") "انتقال پرونده" else "تخصیص به تیم",
                subtitle = if (state.teamAssignMode == "transfer") {
                    "مالکیت مخاطب به عضو دیگر منتقل می‌شود"
                } else {
                    "مخاطب با مشاور هم‌رسانی و تخصیص می‌شود"
                },
                icon = DfIcons.Users,
                onClose = viewModel::dismissTeamAssign,
                footer = {
                    DfSheetActions(
                        primaryText = if (state.isSubmitting) {
                            "در حال ثبت…"
                        } else if (state.teamAssignMode == "transfer") {
                            "انتقال"
                        } else {
                            "تخصیص"
                        },
                        onPrimary = viewModel::submitTeamAssign,
                        primaryEnabled = !state.isSubmitting &&
                            !state.teamMembersLoading &&
                            state.selectedTeamMemberId != null,
                        isSubmitting = state.isSubmitting,
                        onSecondary = viewModel::dismissTeamAssign,
                    )
                },
            ) {
                DfSheetSection(title = "عضو تیم") {
                    if (state.teamMembersLoading) {
                        Text(
                            "در حال بارگذاری اعضا…",
                            style = AppTypography.bodyDescription,
                            color = DfThemeColors.textSecondary(),
                        )
                    } else {
                        TeamMemberSelectList(
                            members = state.teamMembers,
                            selectedId = state.selectedTeamMemberId,
                            onSelect = viewModel::onTeamMemberSelect,
                            emptyLabel = "عضوی برای تخصیص یافت نشد",
                        )
                    }
                }
                if (state.teamAssignMode == "transfer") {
                    DfSheetSection(title = "یادداشت انتقال") {
                        DfTextField(
                            value = state.teamTransferNote,
                            onValueChange = viewModel::onTeamTransferNoteChange,
                            label = "توضیح اختیاری",
                            singleLine = false,
                            minLines = 2,
                        )
                    }
                }
            }
        }
    }

    ContactMatchesSheet(
        visible = state.showMatchesSheet,
        matches = state.matchesData,
        isLoading = state.matchesLoading,
        isSubmitting = state.isSubmitting,
        contactPhone = contact?.phone,
        note = state.matchSuggestNote,
        templates = state.messageTemplates,
        templatesLoading = state.templatesLoading,
        showTemplatePicker = state.showMatchTemplatePicker,
        onNoteChange = viewModel::onMatchSuggestNoteChange,
        onToggleTemplatePicker = viewModel::toggleMatchTemplatePicker,
        onApplyTemplate = viewModel::applyMatchMessageTemplate,
        onDismiss = { viewModel.toggleMatchesSheet(false) },
        onSuggest = { selected, viaWhatsApp ->
            viewModel.suggestMatches(selected, shareViaWhatsApp = viaWhatsApp)
        },
    )
}

private fun performContactNba(
    nba: ContactNba,
    contact: ir.divarfiling.mobile.core.network.ContactDto,
    context: android.content.Context,
    viewModel: ContactDetailViewModel,
    onDealClick: (Long) -> Unit,
) {
    when (nba.kind) {
        ContactNbaKind.Call -> contact.phone?.let { phone ->
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            viewModel.logActivity("تماس", "تماس تلفنی")
        }
        ContactNbaKind.RegisterNeed -> viewModel.toggleEditSheet(true)
        ContactNbaKind.ContinueDeal -> nba.dealId?.let(onDealClick)
        ContactNbaKind.ViewSuggestions -> viewModel.toggleMatchesSheet(true)
        ContactNbaKind.SendFile -> viewModel.toggleSendFilingSheet(true)
        ContactNbaKind.AddFollowUp -> viewModel.toggleReminderDialog(true)
    }
}

private fun buildReachActions(
    contact: ir.divarfiling.mobile.core.network.ContactDto,
    context: android.content.Context,
    viewModel: ContactDetailViewModel,
    haptics: DfHapticPerformer,
): List<ContactQuickActionItem> = buildList {
    add(
        ContactQuickActionItem("تماس", DfColors.Blue, icon = DfIcons.Phone) {
            contact.phone?.let { phone ->
                haptics.tick()
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                viewModel.logActivity("تماس", "تماس تلفنی")
            }
        },
    )
    add(
        ContactQuickActionItem("واتساپ", DfColors.Green, iconRes = R.drawable.ic_whatsapp) {
            contact.phone?.let { phone ->
                haptics.tick()
                DossierShareActions.openWhatsApp(context, "سلام", phone)
                viewModel.logActivity("واتساپ", "پیام واتساپ")
            }
        },
    )
    add(
        ContactQuickActionItem("بله", DfColors.Blue, icon = DfIcons.Share2) {
            contact.phone?.let {
                haptics.tick()
                DossierShareActions.openBale(context, "سلام")
                viewModel.logActivity("بله", "پیام بله")
            }
        },
    )
    add(
        ContactQuickActionItem("پیامک", DfColors.Amber, icon = DfIcons.MessageCircle) {
            contact.phone?.let { phone ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("smsto:$phone")))
                viewModel.logActivity("پیامک", "ارسال پیامک")
            }
        },
    )
}

private fun buildSecondaryActions(
    contact: ir.divarfiling.mobile.core.network.ContactDto,
    viewModel: ContactDetailViewModel,
    pickDocument: (String) -> Unit,
    onOpenAi: () -> Unit,
): List<ContactQuickActionItem> = buildList {
    if (CrmConstants.isMatchEligible(contact.customerType)) {
        add(ContactQuickActionItem("پیشنهادها", DfColors.Purple, icon = DfIcons.Sparkles) {
            viewModel.toggleMatchesSheet(true)
        })
        add(ContactQuickActionItem("یادآور", DfColors.Rose, icon = DfIcons.Bell) {
            viewModel.toggleReminderDialog(true)
        })
    } else {
        add(ContactQuickActionItem("یادآور", DfColors.Rose, icon = DfIcons.Bell) {
            viewModel.toggleReminderDialog(true)
        })
    }
    add(ContactQuickActionItem("AI", DfColors.Purple, icon = DfIcons.Sparkles) {
        onOpenAi()
    })
    add(ContactQuickActionItem("تخصیص", DfColors.Blue, icon = DfIcons.UserPlus) {
        viewModel.openTeamAssign("assign")
    })
    add(ContactQuickActionItem("انتقال", DfColors.Amber, icon = DfIcons.Share2) {
        viewModel.openTeamAssign("transfer")
    })
    add(ContactQuickActionItem("یادداشت", DfColors.Purple, icon = DfIcons.StickyNote) {
        viewModel.toggleNoteDialog(true)
    })
    add(ContactQuickActionItem("فایل", DfColors.Blue, icon = DfIcons.Share2) {
        viewModel.toggleSendFilingSheet(true)
    })
    add(ContactQuickActionItem("مدرک", DfColors.TextSecondary, icon = DfIcons.Paperclip) {
        pickDocument("*/*")
    })
    add(ContactQuickActionItem("بازدید", DfColors.Green, icon = DfIcons.MapPin) {
        viewModel.openActivitySheet(type = "بازدید")
    })
    add(ContactQuickActionItem("فعالیت", DfColors.Blue, icon = DfIcons.ClipboardList) {
        viewModel.openActivitySheet()
    })
}
