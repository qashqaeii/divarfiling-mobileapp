package ir.divarfiling.mobile.feature.crm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DossierShareKind
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DossierShareSheet
import ir.divarfiling.mobile.core.share.DossierShareActions
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfExportLinkButton
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfExportSheet
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.export.ExportFormat
import ir.divarfiling.mobile.feature.export.ExportSecurityGate
import ir.divarfiling.mobile.feature.crm.components.PropertiesHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.feature.share.PublicShareSettingsSheet
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.feature.crm.components.PropertiesSearchFilterPanel
import ir.divarfiling.mobile.feature.crm.components.PropertiesStatsRow
import ir.divarfiling.mobile.feature.crm.components.PropertyContactMatchesSheet
import ir.divarfiling.mobile.feature.crm.components.PropertySuggestionResultSheet
import ir.divarfiling.mobile.feature.crm.components.suggestionMessage
import ir.divarfiling.mobile.feature.crm.components.PropertyDetailTabbedContent
import ir.divarfiling.mobile.feature.crm.components.PropertyLocationMapPickerSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyFormMode
import ir.divarfiling.mobile.feature.crm.components.PropertyFormSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyLinkContactSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyFilters
import ir.divarfiling.mobile.feature.crm.components.PropertyCabinetFormSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyCabinetsManageSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyCabinetsRail
import ir.divarfiling.mobile.feature.crm.components.PropertyFolderFormSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyFoldersManageSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyFoldersRail
import ir.divarfiling.mobile.feature.crm.components.PropertyListCard
import ir.divarfiling.mobile.feature.filing.components.SavedFiltersChipRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(
    onBack: () -> Unit = {},
    onPropertyClick: (Long) -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onNavigateSettings: () -> Unit = {},
    viewModel: PropertiesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val hasSavableCriteria = state.query.isNotBlank() ||
        state.transactionStatus != null ||
        state.dealMode != null ||
        state.propertyType != null ||
        state.cityQuery.isNotBlank() ||
        state.selectedFolderId != null

    if (state.showDeleteFolderDialog) {
        DfConfirmBottomSheet(
            title = "حذف زونکن",
            message = "«${state.folderPendingDelete?.name.orEmpty()}» حذف شود؟ فایل‌های داخل آن حذف نمی‌شوند.",
            confirmText = "حذف",
            destructive = true,
            isSubmitting = state.isSubmittingFolder,
            onConfirm = viewModel::confirmDeleteFolder,
            onDismiss = viewModel::dismissDeleteFolderDialog,
        )
    }

    if (state.showDeleteCabinetDialog) {
        DfConfirmBottomSheet(
            title = "حذف کمد",
            message = "کمد حذف می‌شود اما زونکن‌ها و فایل‌های داخل آن حذف نمی‌شوند.",
            confirmText = "حذف",
            destructive = true,
            isSubmitting = state.isSubmittingCabinet,
            onConfirm = viewModel::confirmDeleteCabinet,
            onDismiss = viewModel::dismissDeleteCabinetDialog,
        )
    }

    if (state.showSaveFilterDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSaveFilterDialog,
            title = { Text("ذخیره فیلتر فایل‌های شخصی") },
            text = {
                OutlinedTextField(
                    value = state.saveFilterName,
                    onValueChange = viewModel::onSaveFilterNameChange,
                    label = { Text("نام فیلتر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveCurrentFilter) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSaveFilterDialog) { Text("انصراف") }
            },
        )
    }

    LaunchedEffect(listState, state.hasMore, state.isLoadingMore, state.isLoading) {
        val layoutInfo = listState.layoutInfo
        val total = layoutInfo.totalItemsCount
        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val nearEnd = total > 0 && lastVisible >= total - 3
        if (nearEnd && state.hasMore && !state.isLoadingMore && !state.isLoading) {
            viewModel.loadMore()
        }
    }

    LaunchedEffect(state.error, state.exportMessage) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
        state.exportMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            DfExtendedFab(
                text = "فایل جدید",
                icon = DfIcons.Plus,
                onClick = { viewModel.toggleCreate(true) },
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance + AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    PropertiesHeader(
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                        onBack = onBack,
                    )
                }
                item {
                    PropertyCabinetsRail(
                        cabinets = state.propertyCabinets,
                        selectedCabinetId = state.selectedCabinetId,
                        unassignedFolderCount = state.unassignedFolderCount,
                        onSelectAll = viewModel::clearPropertyCabinetFilter,
                        onSelectCabinet = viewModel::selectPropertyCabinet,
                        onSelectUnassigned = viewModel::selectUnassignedCabinet,
                        onCreateCabinet = viewModel::openCreateCabinetDialog,
                        onManageCabinets = viewModel::openManageCabinetsSheet,
                    )
                }
                item {
                    PropertyFoldersRail(
                        folders = state.propertyFolders,
                        selectedFolderId = state.selectedFolderId,
                        totalCount = state.propertiesTotal.takeIf { it > 0 }
                            ?: state.propertyFolders.sumOf { it.propertyCount },
                        onSelectAll = viewModel::clearPropertyFolderFilter,
                        onSelectFolder = viewModel::selectPropertyFolder,
                        onCreateFolder = viewModel::openCreateFolderDialog,
                        onManageFolders = viewModel::openManageFoldersSheet,
                        folderMetaFor = { folder ->
                            folder.cabinet?.name?.let { "· $it" }.orEmpty()
                        },
                    )
                }
                if (state.properties.isNotEmpty()) {
                    item {
                        PropertiesStatsRow(
                            totalCount = PropertyFilters.totalCount(state.properties),
                            saleCount = PropertyFilters.saleCount(state.properties),
                            rentCount = PropertyFilters.rentCount(state.properties),
                            activeCount = PropertyFilters.activeCount(state.properties),
                        )
                    }
                    item {
                        DfExportLinkButton(
                            onClick = viewModel::openExportSheet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                item {
                    PropertiesSearchFilterPanel(
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = viewModel::search,
                        cityQuery = state.cityQuery,
                        onCityQueryChange = viewModel::onCityQueryChange,
                        transactionStatus = state.transactionStatus,
                        dealMode = state.dealMode,
                        propertyType = state.propertyType,
                        onTransactionStatusChange = viewModel::onTransactionStatusChange,
                        onDealModeChange = viewModel::onDealModeChange,
                        onPropertyTypeChange = viewModel::onPropertyTypeChange,
                        onResetFilters = viewModel::clearFilters,
                    )
                }
                if (hasSavableCriteria) {
                    item {
                        TextButton(
                            onClick = viewModel::openSaveFilterDialog,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        ) {
                            Text("ذخیره فیلتر فعلی")
                        }
                    }
                }
                if (state.savedFilters.isNotEmpty()) {
                    item {
                        SavedFiltersChipRow(
                            filters = state.savedFilters,
                            activeId = state.activeSavedFilterId,
                            onSelect = viewModel::applySavedFilter,
                            onPin = viewModel::pinSavedFilter,
                            onDelete = viewModel::deleteSavedFilter,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                state.error?.let {
                    item {
                        DfErrorBanner(
                            it,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.isLoading && state.properties.isEmpty()) {
                    item {
                        DfCardListSkeleton(
                            count = 5,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (state.properties.isEmpty()) {
                    val hasActiveFilters = state.query.isNotBlank() ||
                        state.transactionStatus != null ||
                        state.dealMode != null ||
                        state.propertyType != null ||
                        state.selectedFolderId != null
                    item {
                        DfEmptyState(
                            title = if (hasActiveFilters) "نتیجه‌ای با این فیلتر نیست" else "اولین فایل شخصی را ثبت کنید",
                            subtitle = if (hasActiveFilters) {
                                "فیلترها یا جستجو را تغییر دهید"
                            } else {
                                "با «فایل جدید» اضافه کنید یا از جزئیات آگهی تبدیل کنید"
                            },
                            variant = if (hasActiveFilters) DfEmptyVariant.NoResults else DfEmptyVariant.Empty,
                            actionLabel = if (hasActiveFilters) null else "فایل جدید",
                            onAction = if (hasActiveFilters) null else {
                                { viewModel.toggleCreate(true) }
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    item {
                        Box(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            DfSectionHeader(title = "فایل‌ها", count = state.properties.size)
                        }
                    }
                    items(state.properties, key = { it.id }) { prop ->
                        PropertyListCard(
                            property = prop,
                            onClick = { onPropertyClick(prop.id) },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    if (state.isLoadingMore) {
                        item {
                            DfCardListSkeleton(
                                count = 2,
                                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showManageCabinetsSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissManageCabinetsSheet) {
            PropertyCabinetsManageSheet(
                cabinets = state.manageCabinetsOrder,
                isSavingOrder = state.isSavingCabinetOrder,
                onMoveUp = viewModel::moveManageCabinetUp,
                onMoveDown = viewModel::moveManageCabinetDown,
                onPin = viewModel::toggleCabinetPin,
                onEdit = viewModel::openEditCabinet,
                onDelete = viewModel::requestDeleteCabinet,
                onSaveOrder = viewModel::saveManageCabinetOrder,
                onCreateNew = {
                    viewModel.dismissManageCabinetsSheet()
                    viewModel.openCreateCabinetDialog()
                },
            )
        }
    }

    if (state.showCabinetFormSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissCabinetFormSheet) {
            PropertyCabinetFormSheet(
                title = if (state.editingCabinet == null) "کمد جدید" else "ویرایش کمد",
                name = state.cabinetFormName,
                description = state.cabinetFormDescription,
                selectedColor = state.cabinetFormColor,
                selectedIcon = state.cabinetFormIcon,
                isPinned = state.cabinetFormPinned,
                isSubmitting = state.isSubmittingCabinet,
                showDelete = state.editingCabinet != null,
                onNameChange = viewModel::onCabinetFormNameChange,
                onDescriptionChange = viewModel::onCabinetFormDescriptionChange,
                onColorChange = viewModel::onCabinetFormColorChange,
                onIconChange = viewModel::onCabinetFormIconChange,
                onPinnedChange = viewModel::onCabinetFormPinnedChange,
                onSubmit = viewModel::saveCabinetForm,
                onDelete = {
                    state.editingCabinet?.let(viewModel::requestDeleteCabinet)
                },
            )
        }
    }

    if (state.showManageFoldersSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissManageFoldersSheet) {
            PropertyFoldersManageSheet(
                folders = state.manageFoldersOrder,
                isSavingOrder = state.isSavingFolderOrder,
                onMoveUp = viewModel::moveManageFolderUp,
                onMoveDown = viewModel::moveManageFolderDown,
                onPin = viewModel::toggleFolderPin,
                onEdit = viewModel::openEditFolder,
                onDelete = viewModel::requestDeleteFolder,
                onSaveOrder = viewModel::saveManageFolderOrder,
                onCreateNew = {
                    viewModel.dismissManageFoldersSheet()
                    viewModel.openCreateFolderDialog()
                },
            )
        }
    }

    if (state.showFolderFormSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissFolderFormSheet) {
            PropertyFolderFormSheet(
                title = if (state.editingFolder == null) "زونکن جدید" else "ویرایش زونکن",
                name = state.folderFormName,
                description = state.folderFormDescription,
                selectedColor = state.folderFormColor,
                selectedIcon = state.folderFormIcon,
                isPinned = state.folderFormPinned,
                isSubmitting = state.isSubmittingFolder,
                showDelete = state.editingFolder != null,
                onNameChange = viewModel::onFolderFormNameChange,
                onDescriptionChange = viewModel::onFolderFormDescriptionChange,
                onColorChange = viewModel::onFolderFormColorChange,
                onIconChange = viewModel::onFolderFormIconChange,
                onPinnedChange = viewModel::onFolderFormPinnedChange,
                availableCabinets = state.propertyCabinets,
                selectedCabinetId = state.folderFormCabinetId,
                onCabinetChange = viewModel::onFolderFormCabinetChange,
                onSubmit = viewModel::saveFolderForm,
                onDelete = {
                    state.editingFolder?.let(viewModel::requestDeleteFolder)
                },
            )
        }
    }

    if (state.showCreateDialog) {
        DfModalBottomSheet(
            onDismissRequest = { viewModel.toggleCreate(false) },
            dismissOnScrimOrSwipe = false,
        ) {
            PropertyFormSheet(
                mode = PropertyFormMode.Create,
                form = state.createForm,
                isSubmitting = state.isSubmittingCreate,
                isUploadingImages = state.isUploadingImages,
                onFormChange = viewModel::onCreateFormReplace,
                onGalleryImagesSelected = viewModel::onCreateGalleryImagesSelected,
                onSubmit = viewModel::submitCreate,
                onDismiss = { viewModel.toggleCreate(false) },
            )
        }
    }

    if (state.showExportSheet) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissExportSheet) {
            DfExportSheet(
                title = "خروجی فایل‌های شخصی",
                subtitle = "فایل CRM با فیلترهای فعلی",
                formats = listOf(ExportFormat.XLSX, ExportFormat.JSON, ExportFormat.CSV),
                isExporting = state.isExporting,
                onSelect = { format -> viewModel.exportProperties(context, format) },
                onDismiss = viewModel::dismissExportSheet,
            )
        }
    }

    ExportSecurityGate(
        visible = state.showExportSecuritySheet,
        exportLabel = "فایل‌های شخصی",
        onVerified = { viewModel.onExportSecurityCompleted(context) },
        onDismiss = viewModel::dismissExportSecuritySheet,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    onBack: () -> Unit,
    onContactClick: (Long) -> Unit = {},
    viewModel: PropertyDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = state.detail
    val property = detail?.property
    val snackbar = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val documentPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let { viewModel.uploadDocument(it) } }

    LaunchedEffect(state.pendingSocialShare) {
        val pending = state.pendingSocialShare ?: return@LaunchedEffect
        ContactSocialShare.open(context, pending)
        viewModel.clearPendingSocialShare()
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
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
                state.error != null && detail == null -> {
                    Column {
                        DfDetailPageHeader(
                            title = "جزئیات فایل شخصی",
                            subtitle = "بارگذاری اطلاعات ملک",
                            sectionLabel = DfHeaderSections.CRM,
                            titleIconRes = DfDecorIcons.Building,
                            onBack = onBack,
                        )
                        DfErrorBanner(
                            state.error!!,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                detail != null && property != null -> {
                    PropertyDetailTabbedContent(
                        detail = detail,
                        selectedTab = state.selectedTab,
                        isSubmitting = state.isSubmitting,
                        inlineNotes = state.inlineNotes,
                        onBack = onBack,
                        onTabSelect = viewModel::selectTab,
                        onEdit = { viewModel.toggleEditSheet(true) },
                        onShare = { viewModel.toggleShareSheet(true) },
                        onWhatsApp = { viewModel.toggleShareSheet(true) },
                        onCopyLink = {
                            val publicUrl = detail.publicShare?.shareUrl?.takeIf { it.isNotBlank() }
                            if (publicUrl != null) {
                                copyToClipboard(context, publicUrl)
                                scope.launch { snackbar.showSnackbar("لینک صفحه عمومی کپی شد") }
                            } else {
                                val text = property.link?.takeIf { it.isNotBlank() } ?: property.token.orEmpty()
                                if (text.isNotBlank()) {
                                    copyToClipboard(context, text)
                                    scope.launch { snackbar.showSnackbar("کپی شد") }
                                }
                            }
                        },
                        onOpenLink = property.link?.takeIf { it.isNotBlank() }?.let { link ->
                            { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
                        },
                        onStatusChange = viewModel::changeStatus,
                        onDelete = { viewModel.toggleDeleteDialog(true) },
                        onLinkContact = { viewModel.toggleLinkContactSheet(true) },
                        onContactMatches = { viewModel.toggleContactMatchesSheet(true) },
                        onContactClick = onContactClick,
                        onInlineNotesChange = viewModel::onInlineNotesChange,
                        onSaveNotes = viewModel::saveInlineNotes,
                        onUploadDocument = { documentPicker.launch("*/*") },
                        onDeleteDocument = viewModel::deleteDocument,
                        onToggleFolder = viewModel::togglePropertyFolder,
                        onEditLocation = { viewModel.toggleLocationMapPicker(true) },
                        onFetchNearbyPois = viewModel::fetchNearbyPois,
                        nearbyPoisPayload = state.nearbyPoisPayload,
                        nearbyPoisLoading = state.nearbyPoisLoading,
                        aiSummary = state.aiSummary,
                        isSummarizing = state.isSummarizing,
                        aiIsFallback = state.aiIsFallback,
                        aiModelLabel = state.aiModelLabel,
                        aiSummaryHighlights = state.aiSummaryHighlights,
                        aiNegotiationTip = state.aiNegotiationTip,
                        onGenerateSummary = viewModel::summarizeProperty,
                        onCopySummary = {
                            val text = state.aiSummary
                            if (text.isNotBlank()) {
                                copyToClipboard(context, text)
                                scope.launch { snackbar.showSnackbar("خلاصه کپی شد") }
                            }
                        },
                    )
                }
            }
        }
    }

    if (state.showShareSheet && property != null) {
        val shareOptions = viewModel.propertyShareOptions()
        val preview = PropertyShareFormatter.buildShareText(property, shareOptions)
        val publicShare = detail?.publicShare
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleShareSheet(false) }) {
            DossierShareSheet(
                previewText = preview,
                kind = DossierShareKind.PERSONAL,
                note = state.shareNote,
                includeDivarLink = state.shareIncludeLink,
                publicShareUrl = publicShare?.shareUrl,
                publicShareViewCount = publicShare?.viewCount ?: 0,
                includePublicPageLink = state.shareIncludePublicPage,
                onIncludePublicPageLinkChange = viewModel::onShareIncludePublicPageChange,
                includeAddress = state.shareIncludeAddress,
                includeInternalNotes = state.shareIncludeNotes,
                includeAmenities = state.shareIncludeAmenities,
                onNoteChange = viewModel::onShareNoteChange,
                onIncludeDivarLinkChange = viewModel::onShareIncludeLinkChange,
                onIncludeAddressChange = viewModel::onShareIncludeAddressChange,
                onIncludeInternalNotesChange = viewModel::onShareIncludeNotesChange,
                onIncludeAmenitiesChange = viewModel::onShareIncludeAmenitiesChange,
                onShare = {
                    DossierShareActions.shareText(context, preview)
                },
                onWhatsApp = {
                    DossierShareActions.openWhatsApp(context, preview)
                },
                onBale = {
                    DossierShareActions.openBale(context, preview)
                },
                onTelegram = {
                    DossierShareActions.openTelegram(context, preview)
                },
                onSms = {
                    DossierShareActions.openSms(context, preview)
                },
                onCopy = {
                    DossierShareActions.copyToClipboard(context, preview)
                    scope.launch { snackbar.showSnackbar("متن پیام کپی شد") }
                },
                onCopyPublicLink = publicShare?.shareUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    {
                        copyToClipboard(context, url)
                        scope.launch { snackbar.showSnackbar("لینک صفحه عمومی کپی شد") }
                    }
                },
                onOpenPublicPreview = publicShare?.shareUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
                onManagePublicShare = {
                    viewModel.toggleShareSheet(false)
                    viewModel.togglePublicShareSettingsSheet(true)
                },
                onDismiss = { viewModel.toggleShareSheet(false) },
            )
        }
    }

    if (state.showEditSheet) {
        DfModalBottomSheet(
            onDismissRequest = { viewModel.requestDismissEdit() },
            dismissOnScrimOrSwipe = false,
        ) {
            PropertyFormSheet(
                mode = PropertyFormMode.Edit,
                form = state.editForm,
                isSubmitting = state.isSubmitting,
                isUploadingImages = state.isUploadingImages,
                onFormChange = viewModel::onEditFormReplace,
                onGalleryImagesSelected = viewModel::onEditGalleryImagesSelected,
                onSubmit = viewModel::saveEdit,
                onDismiss = { viewModel.requestDismissEdit() },
            )
        }
    }

    if (state.showDiscardEditDialog) {
        DfConfirmBottomSheet(
            title = "تغییرات ذخیره نشده",
            message = "ویرایش ملک ذخیره نشده است. از تغییرات صرف‌نظر می‌کنید؟",
            confirmText = "صرف‌نظر",
            cancelText = "ادامه ویرایش",
            destructive = true,
            onConfirm = viewModel::confirmDiscardEdit,
            onDismiss = viewModel::cancelDiscardEdit,
        )
    }

    if (state.showLocationMapPicker && property != null) {
        PropertyLocationMapPickerSheet(
            initialLatitude = property.latitude,
            initialLongitude = property.longitude,
            onConfirm = viewModel::updatePropertyLocation,
            onDismiss = { viewModel.toggleLocationMapPicker(false) },
        )
    }

    if (state.showDeleteDialog) {
        DfConfirmBottomSheet(
            title = "حذف فایل شخصی",
            message = "این فایل از لیست شما حذف می‌شود. ادامه می‌دهید؟",
            confirmText = "حذف فایل",
            destructive = true,
            isSubmitting = state.isSubmitting,
            onConfirm = { viewModel.deleteProperty(onBack) },
            onDismiss = { viewModel.toggleDeleteDialog(false) },
        )
    }

    if (state.showPublicShareSettingsSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.togglePublicShareSettingsSheet(false) }) {
            PublicShareSettingsSheet(
                consultantName = state.shareConsultantName,
                consultantPhone = state.shareConsultantPhone,
                welcomeMessage = state.shareWelcomeMessage,
                defaultShareMessage = state.shareDefaultShareMessage,
                isActive = state.sharePublicIsActive,
                showDivarLink = state.sharePublicShowDivarLink,
                showFullAddress = state.sharePublicShowFullAddress,
                showInternalNotes = state.sharePublicShowInternalNotes,
                approximateLocation = state.shareApproximateLocation,
                approximateLocationRadiusM = state.shareApproximateLocationRadiusM,
                showNearbyPois = state.shareShowNearbyPois,
                isSubmitting = state.isSubmitting,
                onConsultantNameChange = viewModel::onShareConsultantNameChange,
                onConsultantPhoneChange = viewModel::onShareConsultantPhoneChange,
                onWelcomeMessageChange = viewModel::onShareWelcomeMessageChange,
                onDefaultShareMessageChange = viewModel::onShareDefaultShareMessageChange,
                onIsActiveChange = viewModel::onSharePublicIsActiveChange,
                onShowDivarLinkChange = viewModel::onSharePublicShowDivarLinkChange,
                onShowFullAddressChange = viewModel::onSharePublicShowFullAddressChange,
                onShowInternalNotesChange = viewModel::onSharePublicShowInternalNotesChange,
                onApproximateLocationChange = viewModel::onShareApproximateLocationChange,
                onApproximateLocationRadiusChange = viewModel::onShareApproximateLocationRadiusChange,
                onShowNearbyPoisChange = viewModel::onShareShowNearbyPoisChange,
                onSave = viewModel::savePublicShareSettings,
                onDismiss = { viewModel.togglePublicShareSettingsSheet(false) },
            )
        }
    }

    if (state.showLinkContactSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleLinkContactSheet(false) }) {
            PropertyLinkContactSheet(
                contactName = state.linkContactName,
                contactPhone = state.linkContactPhone,
                selectedFromCrm = state.linkContactId.isNotBlank(),
                isSubmitting = state.isSubmitting,
                onNameChange = viewModel::onLinkContactNameChange,
                onPhoneChange = viewModel::onLinkContactPhoneChange,
                onPickContact = { viewModel.toggleLinkContactPicker(true) },
                onClearSelection = viewModel::clearLinkContactSelection,
                onSubmit = viewModel::linkContact,
                onDismiss = { viewModel.toggleLinkContactSheet(false) },
            )
        }
    }

    if (state.showLinkContactPicker) {
        ContactPickerSheet(
            onDismiss = { viewModel.toggleLinkContactPicker(false) },
            onContactSelected = viewModel::onLinkContactSelected,
        )
    }

    PropertyContactMatchesSheet(
        visible = state.showContactMatchesSheet,
        matches = state.contactMatchesData,
        isLoading = state.contactMatchesLoading,
        isSubmitting = state.isSubmitting,
        onDismiss = { viewModel.toggleContactMatchesSheet(false) },
        onSuggest = viewModel::suggestContactMatches,
    )

    state.contactSuggestionResult?.let { result ->
        PropertySuggestionResultSheet(
            result = result,
            onDismiss = viewModel::dismissContactSuggestionResult,
            onShare = { item, channel ->
                val message = suggestionMessage(item, result.publicUrl)
                ContactSocialShare.open(
                    context = context,
                    channel = channel,
                    message = message,
                    phone = item.phone,
                    socialLinks = item.socialLinks,
                )
            },
            onCopyText = { text ->
                DossierShareActions.copyToClipboard(context, text)
                scope.launch { snackbar.showSnackbar("متن پیام کپی شد") }
            },
            onOpenPublicLink = { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            },
            onCopyPublicLink = { url ->
                DossierShareActions.copyToClipboard(context, url)
                scope.launch { snackbar.showSnackbar("لینک عمومی کپی شد") }
            },
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("property", text))
}
