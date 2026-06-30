package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.ListingMessageFormatter

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.feature.crm.components.ActivityLogSheet
import ir.divarfiling.mobile.feature.crm.components.ContactActivityTimeline
import ir.divarfiling.mobile.feature.crm.components.ContactNoteSheet
import ir.divarfiling.mobile.feature.crm.components.ContactReminderSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfPillChip
import ir.divarfiling.mobile.feature.crm.components.ContactEditSheet
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.feature.crm.components.ContactsFilters
import ir.divarfiling.mobile.core.network.CustomerDocumentDto
import ir.divarfiling.mobile.core.network.LinkedListingDto
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.core.network.ReminderDto
import ir.divarfiling.mobile.feature.crm.components.PropertyListCard
import kotlin.math.absoluteValue

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
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                state.error != null && state.data == null -> {
                    Column {
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
                }
                state.data != null -> {
                    val detail = state.data!!
                    val contactInfo = detail.contact
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    ) {
                        item {
                            DfDetailPageHeader(
                                title = contactInfo.fullName,
                                subtitle = contactInfo.phone,
                                titleIconRes = DfDecorIcons.User,
                                onBack = onBack,
                                actions = {
                                    IconButton(onClick = { viewModel.toggleEditSheet(true) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "ویرایش")
                                    }
                                },
                            )
                        }
                        item {
                            ContactProfileCard(
                                name = contactInfo.fullName,
                                status = contactInfo.status,
                                customerType = contactInfo.customerType,
                                priority = contactInfo.priority,
                                budget = contactInfo.budget,
                                notes = contactInfo.notes,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }

                        item {
                            ContactStatusSection(
                                currentStatus = contactInfo.status,
                                onStatusChange = viewModel::changeStatus,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }

                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            ) {
                                item {
                                    ContactQuickAction(
                                        label = "تماس",
                                        icon = Icons.Default.Call,
                                        background = DfColors.BlueLight,
                                        iconTint = DfColors.Blue,
                                    ) {
                                        contactInfo.phone?.let { phone ->
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                                            viewModel.logActivity("تماس", "تماس تلفنی")
                                        }
                                    }
                                }
                                item {
                                    ContactQuickAction(
                                        label = "واتساپ",
                                        iconRes = R.drawable.ic_whatsapp,
                                        background = DfColors.GreenLight,
                                        iconTint = DfColors.Green,
                                    ) {
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
                                    ContactQuickAction(
                                        label = "پیامک",
                                        icon = Icons.Default.Message,
                                        background = DfColors.AmberLight,
                                        iconTint = DfColors.Amber,
                                    ) {
                                        contactInfo.phone?.let { phone ->
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("smsto:$phone")))
                                            viewModel.logActivity("پیامک", "ارسال پیامک")
                                        }
                                    }
                                }
                                item {
                                    ContactQuickAction(
                                        label = "فایل",
                                        icon = Icons.Default.Share,
                                        background = DfColors.PurpleContainer,
                                        iconTint = DfColors.Purple,
                                    ) {
                                        viewModel.toggleSendFilingSheet(true)
                                    }
                                }
                                item {
                                    ContactQuickAction(
                                        label = "مدرک",
                                        icon = Icons.Default.AttachFile,
                                        background = DfColors.SurfaceVariant,
                                        iconTint = DfColors.TextSecondary,
                                    ) {
                                        documentPicker.launch("*/*")
                                    }
                                }
                                item {
                                    ContactQuickAction(
                                        label = "یادداشت",
                                        icon = Icons.Default.NoteAdd,
                                        background = DfColors.PurpleContainer,
                                        iconTint = DfColors.Purple,
                                    ) {
                                        viewModel.toggleNoteDialog(true)
                                    }
                                }
                                item {
                                    ContactQuickAction(
                                        label = "یادآور",
                                        icon = Icons.Default.Notifications,
                                        background = DfColors.RoseLight,
                                        iconTint = DfColors.OverdueAccent,
                                    ) {
                                        viewModel.toggleReminderDialog(true)
                                    }
                                }
                                item {
                                    ContactQuickAction(
                                        label = "فعالیت",
                                        icon = Icons.Default.History,
                                        background = DfColors.BlueLight,
                                        iconTint = DfColors.Blue,
                                    ) {
                                        viewModel.toggleActivitySheet(true)
                                    }
                                }
                            }
                        }

                        val reminders = detail.reminders
                        if (reminders.isNotEmpty()) {
                            item { DfSectionHeader("یادآورها", reminders.size) }
                            items(reminders, key = { it.id ?: it.hashCode().toLong() }) { reminder ->
                                reminder.id?.let { id ->
                                    ReminderCard(
                                        reminder = reminder,
                                        onComplete = { viewModel.completeReminder(id) },
                                        onPostpone = { viewModel.postponeReminder(id) },
                                    )
                                }
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
                                PropertyListCard(
                                    property = property,
                                    onClick = { onPropertyClick(property.id) },
                                )
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
                                    onShareWhatsApp = {
                                        contactInfo.phone?.let { phone ->
                                            val wa = phone.removePrefix("0")
                                            val text = Uri.encode(
                                                ListingMessageFormatter.fromLinked(listing),
                                            )
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    Uri.parse("https://wa.me/98$wa?text=$text"),
                                                ),
                                            )
                                            viewModel.logActivity("واتساپ", "ارسال فایل: ${listing.title}")
                                        }
                                    },
                                )
                            }
                        }

                        val activities = detail.activities
                        item {
                            ContactActivityTimeline(
                                activities = activities,
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
}

@Composable
private fun ContactProfileCard(
    name: String,
    status: String?,
    customerType: String?,
    priority: String?,
    budget: Long?,
    notes: String?,
    modifier: Modifier = Modifier,
) {
    val accent = contactAccentColor(name)
    DfPremiumCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.45f)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = contactInitials(name),
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    status?.let { ContactDetailStatusBadge(it) }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        customerType?.let {
                            ContactDetailMetaChip(it, DfColors.Purple, DfColors.PurpleContainer)
                        }
                        priority?.let {
                            ContactDetailMetaChip(it, DfColors.Amber, DfColors.AmberLight)
                        }
                    }
                }
            }
            budget?.let {
                Surface(shape = AppShapes.Chip, color = DfColors.GreenLight) {
                    Text(
                        text = "بودجه: ${FormatUtils.formatPriceToman(it)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = AppTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DfColors.Green,
                    )
                }
            }
            notes?.takeIf { it.isNotBlank() }?.let {
                Surface(
                    shape = AppShapes.Card,
                    color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactStatusSection(
    currentStatus: String?,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = "وضعیت مخاطب",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            CrmConstants.STATUSES.forEach { status ->
                DfPillChip(
                    label = status,
                    selected = status == currentStatus,
                    onClick = { if (status != currentStatus) onStatusChange(status) },
                )
            }
        }
    }
}

@Composable
private fun ContactDetailStatusBadge(status: String) {
    val (dotColor, textColor, bgColor) = contactStatusColors(status)
    Surface(shape = AppShapes.Chip, color = bgColor) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Text(
                text = status,
                style = AppTypography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ContactDetailMetaChip(text: String, color: Color, background: Color) {
    Surface(shape = AppShapes.Chip, color = background) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = AppTypography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ContactQuickAction(
    label: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    background: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .size(width = 76.dp, height = 84.dp)
            .clip(AppShapes.Card)
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .liquidGlassSurface(
                    shape = CircleShape,
                    variant = DfGlassButtonVariant.Secondary,
                    elevation = 2.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            when {
                iconRes != null -> Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = iconTint,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 6.dp),
        )
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
                val (date, time) = ContactsFilters.splitUpdatedAt(due)
                val relative = ContactsFilters.relativeUpdatedLabel(due)
                Text(
                    text = listOfNotNull(relative ?: date, time.takeIf { it.isNotBlank() })
                        .joinToString(" · "),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
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
private fun LinkedListingCard(
    listing: LinkedListingDto,
    onShareWhatsApp: () -> Unit,
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
                TextButton(onClick = onShareWhatsApp) {
                    Icon(
                        painter = painterResource(R.drawable.ic_whatsapp),
                        contentDescription = null,
                        tint = DfColors.Green,
                        modifier = Modifier.size(18.dp),
                    )
                    Text("واتساپ", color = DfColors.Green)
                }
                listing.link?.takeIf { it.isNotBlank() }?.let { link ->
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
                val (date, time) = ContactsFilters.splitUpdatedAt(it)
                Text(
                    text = listOfNotNull(date.takeIf { d -> d != "—" }, time.takeIf { t -> t.isNotBlank() })
                        .joinToString(" · "),
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
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

private fun contactInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1)
        else -> "${parts[0].take(1)}${parts[1].take(1)}"
    }
}

private fun contactAccentColor(name: String): Color {
    val palette = listOf(
        DfColors.Purple,
        DfColors.Blue,
        DfColors.Green,
        DfColors.Amber,
        DfColors.Rose,
    )
    return palette[name.hashCode().absoluteValue % palette.size]
}

private fun contactStatusColors(status: String): Triple<Color, Color, Color> = when {
    status.contains("پیگیری") -> Triple(DfColors.Amber, DfColors.Amber, DfColors.AmberLight)
    status.contains("بازدید") -> Triple(DfColors.Green, DfColors.Green, DfColors.GreenLight)
    status == "جدید" -> Triple(DfColors.Blue, DfColors.Blue, DfColors.BlueLight)
    status.contains("قرارداد") -> Triple(DfColors.Purple, DfColors.Purple, DfColors.PurpleContainer)
    else -> Triple(DfColors.TextMuted, DfColors.TextSecondary, DfColors.SurfaceVariant)
}
