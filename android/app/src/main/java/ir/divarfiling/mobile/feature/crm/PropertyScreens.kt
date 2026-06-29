package ir.divarfiling.mobile.feature.crm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfExportSheet
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.export.ExportFormat
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.feature.crm.components.PropertiesSearchFilterPanel
import ir.divarfiling.mobile.feature.crm.components.PropertiesStatsRow
import ir.divarfiling.mobile.feature.crm.components.PropertyCreateSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyDetailContent
import ir.divarfiling.mobile.feature.crm.components.PropertyEditSheet
import ir.divarfiling.mobile.feature.crm.components.PropertyFilters
import ir.divarfiling.mobile.feature.crm.components.PropertyListCard

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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.fabClearance + AppSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "فایل‌های شخصی",
                        subtitle = "مدیریت فایل‌های ملکی و وضعیت معاملات",
                        titleIcon = DfIcons.Building,
                        userName = state.userName,
                        notificationCount = state.notificationBadgeCount,
                        onNotificationsClick = onNavigateNotifications,
                        onMenuClick = onNavigateSettings,
                        onBack = onBack,
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
                        TextButton(
                            onClick = viewModel::openExportSheet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.screenHorizontal),
                        ) {
                            Icon(
                                imageVector = DfIcons.Download,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 6.dp),
                            )
                            Text("خروجی Excel / JSON / CSV")
                        }
                    }
                }
                item {
                    PropertiesSearchFilterPanel(
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = viewModel::search,
                        transactionStatus = state.transactionStatus,
                        dealMode = state.dealMode,
                        propertyType = state.propertyType,
                        onTransactionStatusChange = viewModel::onTransactionStatusChange,
                        onDealModeChange = viewModel::onDealModeChange,
                        onPropertyTypeChange = viewModel::onPropertyTypeChange,
                        onResetFilters = viewModel::clearFilters,
                    )
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
                    item {
                        DfEmptyState(
                            title = "فایل شخصی ثبت نشده",
                            subtitle = "با «فایل جدید» اضافه کنید یا از جزئیات آگهی تبدیل کنید",
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
                }
            }
        }
    }

    if (state.showCreateDialog) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleCreate(false) }) {
            PropertyCreateSheet(
                title = state.createTitle,
                city = state.createCity,
                district = state.createDistrict,
                dealMode = state.createDealMode,
                propertyType = state.createPropertyType,
                area = state.createArea,
                salePrice = state.createPrice,
                deposit = state.createDeposit,
                rent = state.createRent,
                notes = state.createNotes,
                isSubmitting = state.isSubmittingCreate,
                onTitleChange = viewModel::onCreateTitleChange,
                onCityChange = viewModel::onCreateCityChange,
                onDistrictChange = viewModel::onCreateDistrictChange,
                onDealModeChange = viewModel::onCreateDealModeChange,
                onPropertyTypeChange = viewModel::onCreatePropertyTypeChange,
                onAreaChange = viewModel::onCreateAreaChange,
                onSalePriceChange = viewModel::onCreatePriceChange,
                onDepositChange = viewModel::onCreateDepositChange,
                onRentChange = viewModel::onCreateRentChange,
                onNotesChange = viewModel::onCreateNotesChange,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    onBack: () -> Unit,
    viewModel: PropertyDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val property = state.property
    val snackbar = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

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
                property != null -> {
                    PropertyDetailContent(
                        property = property,
                        isSubmitting = state.isSubmitting,
                        onBack = onBack,
                        onEdit = { viewModel.toggleEditSheet(true) },
                        onShare = {
                            sharePropertyText(context, PropertyShareFormatter.buildShareText(property))
                        },
                        onWhatsApp = {
                            openWhatsApp(context, PropertyShareFormatter.buildShareText(property))
                        },
                        onCopyLink = {
                            val text = property.link?.takeIf { it.isNotBlank() } ?: property.token.orEmpty()
                            if (text.isNotBlank()) {
                                copyToClipboard(context, text)
                                scope.launch { snackbar.showSnackbar("کپی شد") }
                            }
                        },
                        onOpenLink = property.link?.takeIf { it.isNotBlank() }?.let { link ->
                            { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) }
                        },
                        onStatusChange = viewModel::changeStatus,
                        onDelete = { viewModel.toggleDeleteDialog(true) },
                    )
                }
            }
        }
    }

    if (state.showEditSheet) {
        DfModalBottomSheet(onDismissRequest = { viewModel.toggleEditSheet(false) }) {
            PropertyEditSheet(
                title = state.editTitle,
                city = state.editCity,
                district = state.editDistrict,
                neighborhood = state.editNeighborhood,
                dealMode = state.editDealMode,
                propertyType = state.editPropertyType,
                transactionStatus = state.editTransactionStatus,
                area = state.editArea,
                rooms = state.editRooms,
                salePrice = state.editPrice,
                deposit = state.editDeposit,
                rent = state.editRent,
                address = state.editAddress,
                notes = state.editNotes,
                isSubmitting = state.isSubmitting,
                onTitleChange = viewModel::onEditTitleChange,
                onCityChange = viewModel::onEditCityChange,
                onDistrictChange = viewModel::onEditDistrictChange,
                onNeighborhoodChange = viewModel::onEditNeighborhoodChange,
                onDealModeChange = viewModel::onEditDealModeChange,
                onPropertyTypeChange = viewModel::onEditPropertyTypeChange,
                onTransactionStatusChange = viewModel::onEditTransactionStatusChange,
                onAreaChange = viewModel::onEditAreaChange,
                onRoomsChange = viewModel::onEditRoomsChange,
                onSalePriceChange = viewModel::onEditPriceChange,
                onDepositChange = viewModel::onEditDepositChange,
                onRentChange = viewModel::onEditRentChange,
                onAddressChange = viewModel::onEditAddressChange,
                onNotesChange = viewModel::onEditNotesChange,
                onSubmit = viewModel::saveEdit,
                onDismiss = { viewModel.toggleEditSheet(false) },
            )
        }
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
}

private fun sharePropertyText(context: Context, message: String) {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            },
            "اشتراک فایل شخصی",
        ),
    )
}

private fun openWhatsApp(context: Context, message: String) {
    val uri = Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("property", text))
}
