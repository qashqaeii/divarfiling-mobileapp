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
        topBar = { DfDetailPageHeader(title = "قراردادها", onBack = onBack) },
    ) { padding ->
        if (state.locked) {
            Column(Modifier.padding(padding).padding(AppSpacing.lg), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text("برای استفاده از قراردادهای دیجیتال، لایسنس فعال لازم است.")
                Button(onClick = onNavigatePlans, modifier = Modifier.fillMaxWidth()) { Text("فعال‌سازی / خرید اشتراک") }
            }
            return@Scaffold
        }
        DfPullRefresh(isRefreshing = state.isRefreshing, onRefresh = { viewModel.load(refresh = true) }, modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("جستجو…") },
                        singleLine = true,
                        trailingIcon = { TextButton(onClick = viewModel::search) { Text("جستجو") } },
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContractSummaryTab.entries.forEach { tab ->
                            val label = when (tab) {
                                ContractSummaryTab.ALL -> "همه ${state.stats.all}"
                                ContractSummaryTab.DRAFT -> "پیش‌نویس ${state.stats.draft}"
                                ContractSummaryTab.NEEDS_ACTION -> "نیازمند اقدام ${state.stats.needsAction}"
                                ContractSummaryTab.AWAITING -> "در انتظار ${state.stats.awaitingAcceptance}"
                                ContractSummaryTab.FINALIZED -> "نهایی ${state.stats.finalized}"
                            }
                            FilterChip(selected = state.selectedTab == tab, onClick = { viewModel.onTabSelected(tab) }, label = { Text(label) })
                        }
                    }
                }
                item {
                    val count = state.appliedFilters.activeCount()
                    OutlinedButton(onClick = viewModel::openFilterSheet, modifier = Modifier.fillMaxWidth()) {
                        Text(if (count > 0) "فیلترها ($count)" else "فیلترها")
                    }
                }
                state.error?.let { item { DfErrorBanner(message = it) } }
                if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                if (!state.isLoading && state.items.isEmpty()) {
                    item {
                        DfEmptyState(
                            title = if (state.isFilteredEmpty) "نتیجه‌ای نیست" else "قراردادی نیست",
                            subtitle = if (state.isFilteredEmpty) "فیلتر یا جستجو را تغییر دهید" else "اولین قرارداد را ایجاد کنید",
                            variant = if (state.isFilteredEmpty) DfEmptyVariant.NoResults else DfEmptyVariant.Empty,
                        )
                    }
                }
                items(state.items, key = { it.id }) { item ->
                    ContractListCard(item = item, onClick = { onContractClick(item.id) })
                }
                item {
                    OutlinedButton(onClick = onCreateContract, modifier = Modifier.fillMaxWidth()) { Text("قرارداد جدید") }
                }
            }
        }
    }
}

@Composable
private fun ContractListCard(item: ContractListItemDto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.contractTypeLabel, style = MaterialTheme.typography.labelMedium)
                AssistChip(onClick = {}, label = { Text(item.statusLabel) })
            }
            Text(item.title.ifBlank { item.propertyTitle }, style = MaterialTheme.typography.titleMedium)
            if (item.primaryParty.isNotBlank()) Text(item.primaryParty, style = MaterialTheme.typography.bodyMedium)
            if (item.internalNumber.isNotBlank()) Text("شماره: ${item.internalNumber}", style = MaterialTheme.typography.bodySmall)
            item.updatedAt?.let { iso ->
                DateUtils.parseInstantMillis(iso)?.let { ms ->
                    Text("آخرین تغییر: ${DateUtils.formatJalaliDateTimeLatinFromMillis(ms)}", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (item.nextAction.label.isNotBlank()) {
                Text(item.nextAction.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
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
    Scaffold(containerColor = DfScreenContainerColor, topBar = { DfDetailPageHeader(title = "قرارداد جدید", onBack = onBack) }) { padding ->
        Column(Modifier.padding(padding).padding(AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            listOf("sale" to "فروش / مبایعه‌نامه", "lease" to "رهن و اجاره", "jv" to "مشارکت در ساخت").forEach { (key, label) ->
                FilterChip(selected = state.contractType == key, onClick = { viewModel.onTypeSelected(key) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth())
            }
            state.error?.let { DfErrorBanner(it) }
            Button(onClick = viewModel::submit, enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(if (state.isSubmitting) "در حال ایجاد…" else "ادامه در ویزارد")
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
