package ir.divarfiling.mobile.feature.contracts

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextButton
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.network.ContractAttachmentDto
import ir.divarfiling.mobile.core.network.ContractListItemDto
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractsHomeScreen(
    onBack: () -> Unit,
    onContractClick: (Long) -> Unit,
    onContinueWizard: (Long) -> Unit = onContractClick,
    onCreateContract: () -> Unit,
    onNavigatePlans: () -> Unit,
    viewModel: ContractsHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
    }
    if (state.showFilterSheet) {
        ContractFiltersBottomSheet(
            draft = state.draftFilters,
            onDraftChange = viewModel::onDraftFiltersChange,
            onDismiss = viewModel::closeFilterSheet,
            onClear = viewModel::clearDraftFilters,
            onApply = viewModel::applyFilters,
        )
    }
    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.locked) {
            Column(Modifier.padding(padding).padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text("برای استفاده از قراردادهای دیجیتال، لایسنس فعال لازم است.")
                DfPrimaryButton(text = "فعال‌سازی / خرید اشتراک", onClick = onNavigatePlans, modifier = Modifier.fillMaxWidth())
            }
            return@Scaffold
        }
        DfPullRefresh(isRefreshing = state.isRefreshing, onRefresh = { viewModel.load(refresh = true) }, modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                item {
                    ContractsPageHeader(
                        stats = state.stats,
                        onBack = onBack,
                        onCreateContract = onCreateContract,
                    )
                }
                item {
                    ContractsKpiDashboard(
                        stats = state.stats,
                        selectedTab = state.selectedTab,
                        onTabSelected = viewModel::onTabSelected,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                state.resumeContract?.let { resume ->
                    item {
                        ContractsResumeCard(
                            item = resume,
                            onContinue = { onContinueWizard(resume.id) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                item {
                    DfTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.screenHorizontal),
                        label = "جستجو",
                        placeholder = "عنوان، شماره یا طرف قرارداد…",
                        singleLine = true,
                    )
                }
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = AppSpacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        ContractSummaryTab.entries.forEach { tab ->
                            val count = when (tab) {
                                ContractSummaryTab.ALL -> state.stats.all
                                ContractSummaryTab.DRAFT -> state.stats.draft
                                ContractSummaryTab.NEEDS_ACTION -> state.stats.needsAction
                                ContractSummaryTab.AWAITING -> state.stats.awaitingAcceptance
                                ContractSummaryTab.FINALIZED -> state.stats.finalized
                            }
                            val label = when (tab) {
                                ContractSummaryTab.ALL -> "همه"
                                ContractSummaryTab.DRAFT -> "پیش‌نویس"
                                ContractSummaryTab.NEEDS_ACTION -> "نیازمند اقدام"
                                ContractSummaryTab.AWAITING -> "در انتظار"
                                ContractSummaryTab.FINALIZED -> "نهایی"
                            }
                            DfSoftChip(
                                text = "$label ${DateUtils.toPersianDigits(count.toString())}",
                                selected = state.selectedTab == tab,
                                onClick = { viewModel.onTabSelected(tab) },
                            )
                        }
                    }
                }
                item {
                    val count = state.appliedFilters.activeCount()
                    DfSecondaryButton(
                        text = if (count > 0) "فیلترها (${DateUtils.toPersianDigits(count.toString())})" else "فیلترها",
                        onClick = viewModel::openFilterSheet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                state.error?.let { item { DfErrorBanner(message = it) } }
                if (state.isLoading) {
                    item {
                        LinearProgressIndicator(
                            Modifier.fillMaxWidth(),
                            color = DfThemeColors.primary(),
                            trackColor = DfThemeColors.primaryContainer(),
                        )
                    }
                }
                if (!state.isLoading && state.items.isEmpty()) {
                    item {
                        DfEmptyState(
                            title = if (state.isFilteredEmpty) "قراردادی مطابق فیلترها پیدا نشد" else "قراردادی نیست",
                            subtitle = if (state.isFilteredEmpty) "فیلتر یا جستجو را تغییر دهید" else "اولین قرارداد را ایجاد کنید",
                            variant = if (state.isFilteredEmpty) DfEmptyVariant.NoResults else DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                items(state.items, key = { it.id }) { item ->
                    ContractListCard(
                        item = item,
                        onClick = { onContractClick(item.id) },
                        onPrimaryAction = {
                            when (item.nextAction.nativeTarget) {
                                "wizard" -> onContinueWizard(item.id)
                                else -> onContractClick(item.id)
                            }
                        },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContractListCard(
    item: ContractListItemDto,
    onClick: () -> Unit,
    onPrimaryAction: () -> Unit = onClick,
    modifier: Modifier = Modifier,
) {
    val statusColors = contractStatusColors(item.statusLabel)
    DfCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(statusColors.first, RoundedCornerShape(4.dp)),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.contractTypeLabel,
                    style = AppTypography.cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                DfBadge(
                    text = item.statusLabel,
                    color = statusColors.first,
                    textColor = statusColors.second,
                )
            }
            Text(
                item.title.ifBlank { item.propertyTitle }.ifBlank { item.contractTypeLabel },
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.primaryParty.isNotBlank()) {
                Text(
                    "طرف: ${item.primaryParty}",
                    style = AppTypography.bodyDescription,
                    color = DfThemeColors.textSecondary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (item.currentVersionNumber > 0) {
                    DfBadge(
                        text = "نسخه ${DateUtils.toPersianDigits(item.currentVersionNumber.toString())}",
                        color = DfThemeColors.primaryContainer(),
                        textColor = DfThemeColors.primary(),
                    )
                }
                if (item.signatureStatus.isNotBlank()) {
                    Text(item.signatureStatus, style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                }
                if (item.handoverStatus.isNotBlank()) {
                    Text("· ${item.handoverStatus}", style = AppTypography.labelSmall, color = DfThemeColors.textMuted())
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (item.internalNumber.isNotBlank()) {
                        Text(
                            "شماره: ${item.internalNumber}",
                            style = AppTypography.labelSmall,
                            color = DfThemeColors.textMuted(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    item.updatedAt?.let { iso ->
                        DateUtils.parseInstantMillis(iso)?.let { ms ->
                            Text(
                                "آخرین تغییر: ${DateUtils.formatJalaliDateTimeLatinFromMillis(ms)}",
                                style = AppTypography.labelSmall,
                                color = DfThemeColors.textMuted(),
                            )
                        }
                    }
                }
                if (item.nextAction.label.isNotBlank()) {
                    DfTextButton(
                        text = item.nextAction.label,
                        onClick = onPrimaryAction,
                    )
                }
            }
        }
    }
    }
}

private fun contractStatusColors(statusLabel: String): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    val label = statusLabel.trim()
    return when {
        label.contains("پیش") -> DfColors.AmberLight to DfColors.Amber
        label.contains("نهای") || label.contains("امض") -> DfColors.GreenLight to DfColors.Green
        label.contains("انتظار") -> DfColors.BlueLight to DfColors.Blue
        else -> DfColors.PurpleLight to DfColors.PurpleDark
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractDetailScreen(
    onBack: () -> Unit,
    onNavigateWebBridge: (String) -> Unit,
    onNavigateWizard: (Long) -> Unit = {},
    onNavigateHandover: (Long, Long) -> Unit = { _, _ -> },
    onOpenPdf: (Long, String) -> Unit,
    onNavigateDeal: (Long) -> Unit,
    hybridRefreshPending: Boolean = false,
    onHybridRefreshConsumed: () -> Unit = {},
    viewModel: ContractDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = state.detail
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.uploadAttachment(it) }
    }
    LaunchedEffect(hybridRefreshPending) {
        if (hybridRefreshPending) {
            viewModel.refreshInBackground()
            onHybridRefreshConsumed()
        }
    }
    LaunchedEffect(state.webBridgeUrl) {
        state.webBridgeUrl?.let { onNavigateWebBridge(it); viewModel.consumeWebBridge() }
    }
    LaunchedEffect(state.openNativeWizard, detail?.id) {
        if (state.openNativeWizard && detail != null) {
            onNavigateWizard(detail.id)
            viewModel.consumeOpenNativeWizard()
        }
    }
    LaunchedEffect(state.openPdf) {
        if (state.openPdf && detail != null) {
            onOpenPdf(detail.id, detail.title.ifBlank { detail.internalNumber })
            viewModel.consumeOpenPdf()
        }
    }
    LaunchedEffect(state.navigateCopiedContractId) {
        state.navigateCopiedContractId?.let { id ->
            onNavigateWizard(id)
            viewModel.consumeNavigateCopied()
        }
    }
    LaunchedEffect(state.openHandover) {
        state.openHandover?.let { nav ->
            onNavigateHandover(nav.contractId, nav.handoverId)
            viewModel.consumeOpenHandover()
        }
    }
    var cancelReason by remember { mutableStateOf("") }
    if (state.showCancelDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissCancelDialog,
            title = { Text("لغو قرارداد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    Text("دلیل لغو (اختیاری)")
                    DfTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = "دلیل",
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.cancelContract(cancelReason); cancelReason = "" }) {
                    Text("تأیید لغو")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissCancelDialog) { Text("انصراف") }
            },
        )
    }
    LaunchedEffect(state.snackbarMessage, state.snackbarError) {
        state.snackbarMessage?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
        state.snackbarError?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(isRefreshing = state.isRefreshing, onRefresh = { viewModel.refresh(initial = false) }, modifier = Modifier.padding(padding)) {
            when {
                state.isLoading -> LinearProgressIndicator(
                    Modifier.fillMaxWidth(),
                    color = DfThemeColors.primary(),
                    trackColor = DfThemeColors.primaryContainer(),
                )
                state.error != null && detail == null -> DfErrorBanner(message = state.error ?: "")
                detail != null -> {
                    var expandedParties by remember { mutableStateOf(true) }
                    var expandedFinancial by remember { mutableStateOf(true) }
                    var expandedProperty by remember { mutableStateOf(false) }
                    var expandedClauses by remember { mutableStateOf(false) }
                    var expandedSign by remember { mutableStateOf(true) }
                    var expandedAttach by remember { mutableStateOf(true) }
                    var expandedHandover by remember { mutableStateOf(false) }
                    var expandedTimeline by remember { mutableStateOf(false) }
                    var expandedVersions by remember { mutableStateOf(false) }
                    var expandedManage by remember { mutableStateOf(false) }
                    val finRows = remember(detail) { financialRows(detail) }
                    val propLine = remember(detail) { propertySummary(detail) }
                    val timelineItems = state.timeline.take(20)
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                    ) {
                        item { ContractDetailPageHeader(detail = detail, onBack = onBack) }
                        item {
                            ContractDetailHero(
                                detail = detail,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        item {
                            ContractDetailNextActionCard(
                                label = detail.nextAction.label,
                                onPrimary = viewModel::onPrimaryAction,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        item {
                            ContractDetailSummaryCard(
                                finHighlight = finRows.firstOrNull()?.let { "${it.label}: ${it.value}" },
                                propertyLine = propLine,
                                partyLine = detail.signatures.firstOrNull()?.name,
                                onOpenDeal = detail.dealId?.let { dealId -> { onNavigateDeal(dealId) } },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                        item {
                            ContractDetailExpandableSection(
                                title = "طرفین قرارداد",
                                icon = DfIcons.Users,
                                subtitle = "نقش، نام و وضعیت هر طرف",
                                badge = DateUtils.toPersianDigits(detail.signatures.size.toString()).takeIf { detail.signatures.isNotEmpty() },
                                accent = DfThemeColors.primary(),
                                expanded = expandedParties,
                                onToggle = { expandedParties = !expandedParties },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            ) {
                                detail.signatures.forEach { row ->
                                    ContractDetailPartyRow(row = row)
                                }
                            }
                        }
                        if (finRows.isNotEmpty()) {
                            item {
                                ContractDetailExpandableSection(
                                    title = "اطلاعات مالی",
                                    icon = DfIcons.Coins,
                                    subtitle = "مبالغ و شرایط پرداخت",
                                    badge = DateUtils.toPersianDigits(finRows.size.toString()),
                                    accent = DfThemeColors.warning(),
                                    expanded = expandedFinancial,
                                    onToggle = { expandedFinancial = !expandedFinancial },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                ) {
                                    finRows.forEach { row ->
                                        ContractDetailLabeledRow(label = row.label, value = row.value)
                                    }
                                }
                            }
                        }
                        if (propLine.isNotBlank()) {
                            item {
                                ContractDetailExpandableSection(
                                    title = "ملک",
                                    icon = DfIcons.Building,
                                    subtitle = "موقعیت و مشخصات ثبت‌شده",
                                    accent = DfThemeColors.info(),
                                    expanded = expandedProperty,
                                    onToggle = { expandedProperty = !expandedProperty },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                ) {
                                    Text(propLine, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
                                }
                            }
                        }
                        if (detail.clauses.isNotEmpty()) {
                            item {
                                ContractDetailExpandableSection(
                                    title = "بندهای قرارداد",
                                    icon = DfIcons.Scale,
                                    subtitle = "فهرست بندهای فعال",
                                    badge = DateUtils.toPersianDigits(detail.clauses.size.toString()),
                                    accent = DfThemeColors.primary(),
                                    expanded = expandedClauses,
                                    onToggle = { expandedClauses = !expandedClauses },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                ) {
                                    detail.clauses.take(12).forEachIndexed { index, clause ->
                                        ContractDetailClauseRow(clause = clause, index = index + 1)
                                    }
                                    if (detail.canEditClauses) {
                                        DfTextButton(text = "ادامه ویرایش بندها", onClick = viewModel::openClausesEditor, compact = true)
                                    }
                                }
                            }
                        }
                        item {
                            ContractDetailExpandableSection(
                                title = "امضا و تأیید",
                                icon = DfIcons.CircleCheck,
                                subtitle = "وضعیت دعوت‌نامه و امضای طرفین",
                                accent = DfThemeColors.success(),
                                expanded = expandedSign,
                                onToggle = { expandedSign = !expandedSign },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            ) {
                                detail.signatures.forEach { row ->
                                    ContractDetailSignatureRow(
                                        row = row,
                                        onResend = if (row.canResend && row.invitationId != null) {
                                            { viewModel.resendInvitation(row.invitationId) }
                                        } else {
                                            null
                                        },
                                    )
                                }
                            }
                        }
                        item {
                            ContractDetailExpandableSection(
                                title = "پیوست‌ها",
                                icon = DfIcons.Paperclip,
                                subtitle = "مدارک و فایل‌های مرتبط",
                                badge = DateUtils.toPersianDigits(state.attachments.size.toString()).takeIf { state.attachments.isNotEmpty() },
                                accent = DfThemeColors.primary(),
                                expanded = expandedAttach,
                                onToggle = { expandedAttach = !expandedAttach },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            ) {
                            if (state.attachments.isEmpty()) {
                                Text(
                                    "هنوز پیوستی ثبت نشده",
                                    style = AppTypography.bodyDescription,
                                    color = DfThemeColors.textMuted(),
                                )
                            }
                            state.attachments.forEach { att ->
                                ContractDetailAttachmentRow(
                                    att = att,
                                    busy = state.attachmentBusyId == att.id,
                                    onOpen = {
                                        viewModel.openAttachment(att) { file, mime ->
                                            if (!ContractFilesHelper.openFile(context, file, mime)) {
                                                ContractFilesHelper.shareFile(context, file, mime)
                                            }
                                        }
                                    },
                                    onDownload = {
                                        viewModel.openAttachment(att) { file, mime ->
                                            val name = att.title.ifBlank { "attachment_${att.id}" }
                                            if (ContractFilesHelper.saveToDownloads(context, file, name, mime)) {
                                                viewModel.showMessage("فایل ذخیره شد")
                                            }
                                        }
                                    },
                                    onShare = {
                                        viewModel.openAttachment(att) { file, mime ->
                                            ContractFilesHelper.shareFile(context, file, mime)
                                        }
                                    },
                                )
                            }
                            if (state.isUploadingAttachment) {
                                LinearProgressIndicator(
                                    Modifier.fillMaxWidth(),
                                    color = DfThemeColors.primary(),
                                    trackColor = DfThemeColors.primaryContainer(),
                                )
                            }
                            DfTextButton(
                                text = "افزودن پیوست",
                                onClick = { attachmentPicker.launch("*/*") },
                                compact = true,
                            )
                            }
                        }
                        item {
                            ContractDetailExpandableSection(
                                title = "تحویل",
                                icon = DfIcons.ClipboardList,
                                subtitle = "صورت‌جلسه و وضعیت تحویل ملک",
                                accent = DfThemeColors.info(),
                                expanded = expandedHandover,
                                onToggle = { expandedHandover = !expandedHandover },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            ) {
                            ContractDetailHandoverBlock(
                                exists = detail.handover.exists,
                                statusLabel = detail.handover.statusLabel,
                                summary = detail.handover.summary,
                                actionLabel = detail.handover.nextAction?.label,
                                onAction = viewModel::onPrimaryAction,
                            )
                            }
                        }
                        if (detail.versions.isNotEmpty()) {
                            item {
                                ContractDetailExpandableSection(
                                    title = "نسخه‌ها",
                                    icon = DfIcons.Layers,
                                    subtitle = "نسخه فعلی: ${DateUtils.toPersianDigits(detail.currentVersionNumber.toString())}",
                                    badge = DateUtils.toPersianDigits(detail.versions.size.toString()),
                                    accent = DfThemeColors.primary(),
                                    expanded = expandedVersions,
                                    onToggle = { expandedVersions = !expandedVersions },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                ) {
                                    detail.versions.forEach { ver ->
                                        ContractDetailVersionRow(ver = ver)
                                    }
                                    if (detail.canNewVersion) {
                                        DfSecondaryButton(
                                            text = "ایجاد نسخه جدید",
                                            onClick = viewModel::createNewVersion,
                                            enabled = !state.isContractActionLoading,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                        if (detail.canCopy || detail.canCancel || detail.canNewVersion) {
                            item {
                                ContractDetailExpandableSection(
                                    title = "مدیریت",
                                    icon = DfIcons.Settings,
                                    subtitle = "عملیات پیشرفته پرونده",
                                    accent = DfThemeColors.textSecondary(),
                                    expanded = expandedManage,
                                    onToggle = { expandedManage = !expandedManage },
                                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                                ) {
                                    if (state.isContractActionLoading) {
                                        LinearProgressIndicator(
                                            Modifier.fillMaxWidth(),
                                            color = DfThemeColors.primary(),
                                        )
                                    }
                                    if (detail.canNewVersion && detail.versions.isEmpty()) {
                                        ContractDetailManageAction(
                                            icon = DfIcons.Layers,
                                            title = "ایجاد نسخه جدید",
                                            subtitle = "شروع نسخه بعدی قرارداد",
                                            onClick = viewModel::createNewVersion,
                                            enabled = !state.isContractActionLoading,
                                        )
                                    }
                                    if (detail.canCopy) {
                                        ContractDetailManageAction(
                                            icon = DfIcons.Copy,
                                            title = "کپی قرارداد",
                                            subtitle = "ایجاد پرونده مشابه با داده‌های فعلی",
                                            onClick = viewModel::copyContract,
                                            enabled = !state.isContractActionLoading,
                                        )
                                    }
                                    if (detail.canCancel) {
                                        ContractDetailManageAction(
                                            icon = DfIcons.Trash,
                                            title = "لغو قرارداد",
                                            subtitle = "غیرفعال‌سازی پرونده با ثبت دلیل",
                                            onClick = viewModel::showCancelDialog,
                                            destructive = true,
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            ContractDetailExpandableSection(
                                title = "تاریخچه",
                                icon = DfIcons.Clock,
                                subtitle = "رویدادها و تغییرات اخیر",
                                badge = DateUtils.toPersianDigits(timelineItems.size.toString()).takeIf { timelineItems.isNotEmpty() },
                                accent = DfThemeColors.textSecondary(),
                                expanded = expandedTimeline,
                                onToggle = { expandedTimeline = !expandedTimeline },
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            ) {
                            if (timelineItems.isEmpty()) {
                                Text("رویدادی ثبت نشده", style = AppTypography.bodyDescription, color = DfThemeColors.textMuted())
                            } else {
                                timelineItems.forEachIndexed { index, ev ->
                                    ContractDetailTimelineItem(event = ev, isLast = index == timelineItems.lastIndex)
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContractCreateScreen(
    onBack: () -> Unit,
    onCreatedNavigateWizard: (Long) -> Unit,
    onNavigateWebBridge: (String) -> Unit,
    viewModel: ContractCreateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.webBridgeUrl) {
        state.webBridgeUrl?.let { onNavigateWebBridge(it); viewModel.consumeWebBridge() }
    }
    LaunchedEffect(state.navigateWizardContractId) {
        state.navigateWizardContractId?.let { id ->
            onCreatedNavigateWizard(id)
            viewModel.consumeNavigateWizard()
        }
    }
    Scaffold(containerColor = DfScreenContainerColor, snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                DfHubPageHeader(
                    title = "ایجاد قرارداد",
                    subtitle = "مرحله ۱ — نوع قرارداد را انتخاب کنید",
                    sectionLabel = DfHeaderSections.CRM,
                    titleIconRes = DfDecorIcons.FileText,
                    onBack = onBack,
                    showBottomDivider = true,
                )
            }
            item {
                ContractWizardStepRow(activeStep = 1)
            }
            item {
                DfStatusBanner(
                    message = "پس از انتخاب نوع، ویزارد تکمیل قرارداد در اپ باز می‌شود. در صورت نیاز می‌توانید از داخل ویزارد به نسخه وب بروید.",
                    tone = DfStatusTone.Info,
                    title = "تکمیل در اپ",
                    icon = DfIcons.File,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
            item {
                Column(
                    Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    data class ContractTypeOption(
                        val key: String,
                        val title: String,
                        val description: String,
                        @androidx.annotation.DrawableRes val iconRes: Int,
                    )
                    listOf(
                        ContractTypeOption("sale", "فروش / مبایعه‌نامه", "قرارداد خرید و فروش ملک", DfDecorIcons.Handshake),
                        ContractTypeOption("lease", "رهن و اجاره", "قرارداد رهن کامل یا اجاره", DfDecorIcons.House),
                        ContractTypeOption("jv", "مشارکت در ساخت", "قرارداد مشارکت سازنده و مالک", DfDecorIcons.Layers),
                    ).forEach { option ->
                        ContractTypePickCard(
                            title = option.title,
                            description = option.description,
                            iconRes = option.iconRes,
                            selected = state.contractType == option.key,
                            onClick = { viewModel.onTypeSelected(option.key) },
                        )
                    }
                    state.error?.let { DfErrorBanner(it) }
                    DfPrimaryButton(
                        text = if (state.isSubmitting) "در حال ایجاد…" else "ادامه تکمیل قرارداد",
                        onClick = viewModel::submit,
                        enabled = state.contractType.isNotBlank() && !state.isSubmitting,
                        loading = state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ContractWizardStepRow(activeStep: Int) {
    val steps = listOf("نوع قرارداد", "تکمیل جزئیات", "امضا و بایگانی")
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        steps.forEachIndexed { index, label ->
            val step = index + 1
            val reached = step <= activeStep
            val current = step == activeStep
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (reached) DfThemeColors.primary() else DfThemeColors.surfaceVariant(),
                    modifier = Modifier.size(if (current) 28.dp else 24.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = step.toString(),
                            style = AppTypography.labelSmall,
                            color = if (reached) Color.White else DfThemeColors.textMuted(),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Text(
                    text = label,
                    style = AppTypography.labelSmall,
                    color = if (current) DfThemeColors.primary() else DfThemeColors.textMuted(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContractTypePickCard(
    title: String,
    description: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) DfThemeColors.primary() else DfThemeColors.outlineSubtle()
    val bg = if (selected) DfThemeColors.primaryContainer().copy(alpha = 0.45f) else DfThemeColors.surface()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, border),
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                Text(description, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
            }
            if (selected) {
                Text("✓", color = DfThemeColors.primary(), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ContractPdfScreen(onBack: () -> Unit, viewModel: ContractPdfViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
    }
    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            DfDetailPageHeader(
                title = state.title,
                onBack = onBack,
                actions = {
                    IconButton(onClick = {
                        viewModel.pdfFile()?.let { ContractFilesHelper.shareFile(context, it, "application/pdf") }
                    }) { Icon(Icons.Default.Share, contentDescription = "اشتراک") }
                    TextButton(onClick = {
                        val file = viewModel.pdfFile() ?: return@TextButton
                        val name = "${state.title.ifBlank { "contract" }}.pdf"
                        if (ContractFilesHelper.savePdfToDownloads(context, file, name)) viewModel.onSaved()
                    }) { Text("ذخیره") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                state.isLoading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                state.error != null -> {
                    DfErrorBanner(state.error ?: "")
                    Button(onClick = viewModel::load) { Text("تلاش مجدد") }
                }
                state.filePath != null -> PdfPagesView(file = File(state.filePath!!), onPageCount = viewModel::setPageCount)
            }
        }
    }
}

@Composable
private fun PdfPagesView(file: File, onPageCount: (Int) -> Unit) {
    var pageIndex by remember { mutableIntStateOf(0) }
    var pageCount by remember { mutableIntStateOf(0) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(file) {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                pageCount = renderer.pageCount
                onPageCount(renderer.pageCount)
            }
        }
    }
    LaunchedEffect(file, pageIndex, pageCount) {
        if (pageCount <= 0) return@LaunchedEffect
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                val idx = pageIndex.coerceIn(0, renderer.pageCount - 1)
                renderer.openPage(idx).use { page ->
                    val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap = bmp
                }
            }
        }
    }
    bitmap?.let {
        Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(480.dp), contentScale = ContentScale.Fit)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = { if (pageIndex > 0) pageIndex-- }, enabled = pageIndex > 0) { Text("قبلی") }
        Text("${pageIndex + 1} / ${pageCount.coerceAtLeast(1)}")
        TextButton(onClick = { if (pageIndex < pageCount - 1) pageIndex++ }, enabled = pageIndex < pageCount - 1) { Text("بعدی") }
    }
}
