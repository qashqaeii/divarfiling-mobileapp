package ir.divarfiling.mobile.feature.contracts

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
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
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
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
                    DfDetailPageHeader(
                        title = "قراردادها",
                        subtitle = "${DateUtils.toPersianDigits(state.stats.all.toString())} قرارداد ثبت‌شده",
                        sectionLabel = DfHeaderSections.CRM,
                        titleIconRes = DfDecorIcons.FileText,
                        onBack = onBack,
                        showBottomDivider = true,
                    )
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
                if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
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
                    ContractListCard(item = item, onClick = { onContractClick(item.id) }, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal))
                }
                item {
                    DfPrimaryButton(
                        text = "قرارداد جدید",
                        onClick = onCreateContract,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.screenHorizontal),
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
    modifier: Modifier = Modifier,
) {
    val statusColors = contractStatusColors(item.statusLabel)
    DfCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.contractTypeLabel,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
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
                    Text(
                        item.nextAction.label,
                        style = AppTypography.labelLarge,
                        color = DfThemeColors.primary(),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
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
    LaunchedEffect(state.openPdf) {
        if (state.openPdf && detail != null) {
            onOpenPdf(detail.id, detail.title.ifBlank { detail.internalNumber })
            viewModel.consumeOpenPdf()
        }
    }
    LaunchedEffect(state.snackbarMessage, state.snackbarError) {
        state.snackbarMessage?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
        state.snackbarError?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { DfDetailPageHeader(title = detail?.title ?: "قرارداد", onBack = onBack) },
    ) { padding ->
        DfPullRefresh(isRefreshing = state.isRefreshing, onRefresh = { viewModel.refresh(initial = false) }, modifier = Modifier.padding(padding)) {
            when {
                state.isLoading -> LinearProgressIndicator(Modifier.fillMaxWidth())
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
                    val finRows = remember(detail) { financialRows(detail) }
                    val propLine = remember(detail) { propertySummary(detail) }
                    LazyColumn(contentPadding = PaddingValues(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AssistChip(onClick = {}, label = { Text(detail.contractTypeLabel) })
                                    AssistChip(onClick = {}, label = { Text(detail.statusLabel) })
                                }
                                if (detail.internalNumber.isNotBlank()) Text("شماره: ${detail.internalNumber}")
                                detail.updatedAt?.let { iso ->
                                    DateUtils.parseInstantMillis(iso)?.let { ms ->
                                        Text("آخرین بروزرسانی: ${DateUtils.formatJalaliDateTimeLatinFromMillis(ms)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        item {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("اقدام بعدی", style = MaterialTheme.typography.titleSmall)
                                    Text(detail.nextAction.label.ifBlank { "ادامه فرایند" })
                                    Button(onClick = viewModel::onPrimaryAction, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                        Text(detail.nextAction.label.ifBlank { "ادامه" })
                                    }
                                }
                            }
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("خلاصه", style = MaterialTheme.typography.titleSmall)
                                finRows.firstOrNull()?.let { Text("${it.label}: ${it.value}", style = MaterialTheme.typography.bodyLarge) }
                                if (propLine.isNotBlank()) Text("ملک: $propLine")
                                detail.signatures.firstOrNull()?.let { Text("طرف: ${it.name}") }
                                detail.dealId?.let { TextButton(onClick = { onNavigateDeal(it) }) { Text("مشاهده معامله") } }
                            }
                        }
                        item { ExpandableSection("طرفین", expandedParties, { expandedParties = !expandedParties }) {
                            detail.signatures.forEach { row ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(row.name, style = MaterialTheme.typography.bodyLarge)
                                        Text(row.roleLabel, style = MaterialTheme.typography.bodySmall)
                                    }
                                    AssistChip(onClick = {}, label = { Text(row.statusLabel) })
                                }
                            }
                        } }
                        if (finRows.isNotEmpty()) {
                            item { ExpandableSection("اطلاعات مالی", expandedFinancial, { expandedFinancial = !expandedFinancial }) {
                                finRows.forEach { row ->
                                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(row.label, style = MaterialTheme.typography.bodyMedium)
                                        Text(row.value, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            } }
                        }
                        if (propLine.isNotBlank()) {
                            item { ExpandableSection("ملک", expandedProperty, { expandedProperty = !expandedProperty }) { Text(propLine) } }
                        }
                        if (detail.clauses.isNotEmpty()) {
                            item { ExpandableSection("بندهای قرارداد", expandedClauses, { expandedClauses = !expandedClauses }) {
                                detail.clauses.take(12).forEach { Text("• ${it.title}") }
                                if (detail.canEditClauses) TextButton(onClick = viewModel::openClausesEditor) { Text("ادامه ویرایش") }
                            } }
                        }
                        item { ExpandableSection("امضا و تأیید", expandedSign, { expandedSign = !expandedSign }) {
                            detail.signatures.forEach { row ->
                                Text("${row.roleLabel} · ${row.name} · ${row.statusLabel}")
                            }
                        } }
                        item { ExpandableSection("پیوست‌ها", expandedAttach, { expandedAttach = !expandedAttach }) {
                            if (state.attachments.isEmpty()) Text("پیوستی نیست")
                            state.attachments.forEach { att ->
                                AttachmentRow(
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
                            if (state.isUploadingAttachment) LinearProgressIndicator(Modifier.fillMaxWidth())
                            TextButton(onClick = { attachmentPicker.launch("*/*") }, modifier = Modifier.heightIn(min = 48.dp)) { Text("افزودن پیوست") }
                        } }
                        item { ExpandableSection("تحویل", expandedHandover, { expandedHandover = !expandedHandover }) {
                            if (detail.handover.exists) Text("${detail.handover.statusLabel} · ${detail.handover.summary}")
                            else Text("صورت‌جلسه ثبت نشده")
                            detail.handover.nextAction?.let { action ->
                                if (action.label.isNotBlank()) {
                                    TextButton(onClick = { viewModel.openBridge(action.webPath) }) { Text(action.label) }
                                }
                            }
                        } }
                        item { ExpandableSection("تاریخچه", expandedTimeline, { expandedTimeline = !expandedTimeline }) {
                            state.timeline.take(20).forEach { ev ->
                                val whenText = ev.createdAt?.let { iso ->
                                    DateUtils.parseInstantMillis(iso)?.let(DateUtils::formatJalaliDateTimeLatinFromMillis)
                                } ?: ""
                                Text("${ev.label} · $whenText")
                                if (ev.detail.isNotBlank()) Text(ev.detail, style = MaterialTheme.typography.bodySmall)
                            }
                        } }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableSection(title: String, expanded: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(if (expanded) "▲" else "▼", style = MaterialTheme.typography.labelSmall)
        }
        if (expanded) {
            content()
            HorizontalDivider(Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun AttachmentRow(att: ContractAttachmentDto, busy: Boolean, onOpen: () -> Unit, onDownload: () -> Unit, onShare: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clickable(enabled = !busy, onClick = onOpen).padding(vertical = 8.dp).heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(att.title.ifBlank { "پیوست" }, style = MaterialTheme.typography.bodyLarge)
            Text("${att.mimeType} · ${att.fileSize} بایت", style = MaterialTheme.typography.bodySmall)
            att.createdAt?.let { iso ->
                DateUtils.parseInstantMillis(iso)?.let { ms ->
                    Text(DateUtils.formatJalaliDateTimeLatinFromMillis(ms), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Box {
            IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("باز کردن") }, onClick = { menu = false; onOpen() })
                DropdownMenuItem(text = { Text("دانلود") }, onClick = { menu = false; onDownload() })
                DropdownMenuItem(text = { Text("اشتراک") }, onClick = { menu = false; onShare() })
            }
        }
    }
    if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
fun ContractCreateScreen(
    onBack: () -> Unit,
    onCreatedNavigateDetail: (Long) -> Unit,
    onNavigateWebBridge: (String) -> Unit,
    viewModel: ContractCreateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.webBridgeUrl) {
        state.webBridgeUrl?.let { onNavigateWebBridge(it); viewModel.consumeWebBridge() }
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
                DfDetailPageHeader(
                    title = "ایجاد قرارداد",
                    subtitle = "نوع قرارداد را انتخاب کنید",
                    sectionLabel = DfHeaderSections.CRM,
                    titleIconRes = DfDecorIcons.FileText,
                    onBack = onBack,
                    showBottomDivider = true,
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
    val bg = if (selected) DfColors.PurpleLight.copy(alpha = 0.35f) else DfThemeColors.surface()
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
