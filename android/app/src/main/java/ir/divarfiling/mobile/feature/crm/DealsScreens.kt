package ir.divarfiling.mobile.feature.crm

import ir.divarfiling.mobile.core.design.DfColors

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfFab
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.design.components.DfTopBar
import ir.divarfiling.mobile.core.network.DealDto
import ir.divarfiling.mobile.core.network.PropertyDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    onBack: () -> Unit = {},
    onDealClick: (Long) -> Unit = {},
    viewModel: DealsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { DfTopBar(title = "معاملات", onBack = onBack) },
        floatingActionButton = {
            DfFab(
                onClick = { viewModel.toggleCreate(true) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                contentDescription = "افزودن معامله",
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    DfSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "جستجوی معامله…",
                        onSearch = viewModel::search,
                    )
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.stages.take(5)) { stage ->
                            val selected = state.selectedStage == stage
                            TextButton(onClick = { viewModel.selectStage(stage) }) {
                                DfBadge(
                                    stage,
                                    color = if (selected) DfColors.PurpleContainer else DfColors.SurfaceVariant,
                                    textColor = if (selected) DfColors.Purple else DfColors.TextSecondary,
                                )
                            }
                        }
                    }
                }
                if (state.pipelineColumns.isNotEmpty()) {
                    item { DfSectionHeader("خلاصه پایپ‌لاین") }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.pipelineColumns) { col ->
                                DfPremiumCard {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(col.stage, style = AppTypography.labelSmall)
                                        Text("${col.count} معامله", style = AppTypography.cardTitle)
                                    }
                                }
                            }
                        }
                    }
                }
                state.error?.let { item { DfErrorBanner(it) } }
                if (state.isLoading && state.deals.isEmpty()) {
                    item { DfCardListSkeleton(count = 5) }
                } else if (state.deals.isEmpty()) {
                    item { DfEmptyState(title = "معامله‌ای نیست", subtitle = "با + معامله جدید بسازید") }
                } else {
                    items(state.deals, key = { it.id }) { deal ->
                        DealRow(deal, onClick = { onDealClick(deal.id) })
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleCreate(false) },
            title = { Text("معامله جدید") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.createCustomerId,
                        onValueChange = viewModel::onCreateCustomerIdChange,
                        label = { Text("شناسه مخاطب") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.createTitle,
                        onValueChange = viewModel::onCreateTitleChange,
                        label = { Text("عنوان") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.createAmount,
                        onValueChange = viewModel::onCreateAmountChange,
                        label = { Text("مبلغ (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = { TextButton(onClick = viewModel::submitCreate) { Text("ثبت") } },
            dismissButton = { TextButton(onClick = { viewModel.toggleCreate(false) }) { Text("انصراف") } },
        )
    }
}

@Composable
private fun DealRow(deal: DealDto, onClick: () -> Unit) {
    DfPremiumCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(deal.title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            deal.stage?.let { DfBadge(it) }
            deal.customerName?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
            deal.amount?.let {
                Text(FormatUtils.formatPriceToman(it), style = AppTypography.bodyDescription, color = DfColors.Purple)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailScreen(
    onBack: () -> Unit,
    onContactClick: (Long) -> Unit = {},
    viewModel: DealDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val deal = state.deal
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        topBar = { DfTopBar(title = deal?.title ?: "جزئیات معامله", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                deal != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            DfPremiumCard {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    deal.stage?.let { DfBadge(it) }
                                    deal.amount?.let {
                                        Text(FormatUtils.formatPriceToman(it), style = AppTypography.sectionTitle, color = DfColors.Purple)
                                    }
                                    deal.commissionAmount?.let {
                                        Text("کمیسیون: ${FormatUtils.formatPriceToman(it)}", style = AppTypography.bodyDescription)
                                    }
                                    deal.customerName?.let {
                                        TextButton(onClick = { deal.customerId?.let(onContactClick) }) {
                                            Text("مخاطب: $it")
                                        }
                                    }
                                    deal.propertyTitle?.let {
                                        Text("ملک: $it", style = AppTypography.bodyDescription)
                                    }
                                    deal.notes?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = AppTypography.bodyDescription)
                                    }
                                }
                            }
                        }
                        item {
                            Text("تغییر مرحله", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                state.stages.forEach { stage ->
                                    TextButton(
                                        onClick = { viewModel.changeStage(stage) },
                                        enabled = deal.stage != stage && !state.isSubmitting,
                                    ) {
                                        Text(stage, style = AppTypography.labelSmall)
                                    }
                                }
                            }
                        }
                        item {
                            DfPrimaryButton("ویرایش", onClick = { viewModel.toggleEditSheet(true) })
                        }
                    }
                }
            }
        }
    }

    if (state.showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { viewModel.toggleEditSheet(false) }, sheetState = sheetState) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(state.editTitle, viewModel::onEditTitleChange, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editAmount, viewModel::onEditAmountChange, label = { Text("مبلغ") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editNotes, viewModel::onEditNotesChange, label = { Text("یادداشت") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                DfPrimaryButton("ذخیره", onClick = viewModel::saveEdit, enabled = !state.isSubmitting)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiesScreen(
    onBack: () -> Unit = {},
    onPropertyClick: (Long) -> Unit = {},
    viewModel: PropertiesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { DfTopBar(title = "املاک CRM", onBack = onBack) },
        floatingActionButton = {
            DfFab(
                onClick = { viewModel.toggleCreate(true) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                contentDescription = "افزودن ملک",
            )
        },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DfSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "جستجوی ملک…",
                        onSearch = viewModel::search,
                    )
                }
                item {
                    DfFilterChipRow(
                        options = listOf(
                            DfFilterOption(null, "همه"),
                            DfFilterOption("فعال", "فعال"),
                            DfFilterOption("در مذاکره", "مذاکره"),
                            DfFilterOption("فروخته شده", "فروخته"),
                        ),
                        selected = state.transactionStatus,
                        onSelect = { viewModel.onTransactionStatusChange(it); viewModel.search() },
                    )
                }
                state.error?.let { item { DfErrorBanner(it) } }
                if (state.isLoading && state.properties.isEmpty()) {
                    item { DfCardListSkeleton(count = 5) }
                } else if (state.properties.isEmpty()) {
                    item { DfEmptyState(title = "ملکی ثبت نشده", subtitle = "با + ملک جدید اضافه کنید") }
                } else {
                    items(state.properties, key = { it.id }) { prop ->
                        PropertyRow(prop, onClick = { onPropertyClick(prop.id) })
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleCreate(false) },
            title = { Text("ملک جدید") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(state.createTitle, viewModel::onCreateTitleChange, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(state.createCity, viewModel::onCreateCityChange, label = { Text("شهر") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(state.createPrice, viewModel::onCreatePriceChange, label = { Text("قیمت فروش") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(onClick = viewModel::submitCreate) { Text("ثبت") } },
            dismissButton = { TextButton(onClick = { viewModel.toggleCreate(false) }) { Text("انصراف") } },
        )
    }
}

@Composable
private fun PropertyRow(property: PropertyDto, onClick: () -> Unit) {
    DfPremiumCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(property.title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
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
                Text(FormatUtils.formatPriceToman(it), style = AppTypography.bodyDescription, color = DfColors.Purple)
            }
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

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        topBar = { DfTopBar(title = property?.title ?: "جزئیات ملک", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when {
                state.isLoading -> DfDetailSkeleton()
                property != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            DfPremiumCard {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    property.transactionStatus?.let { DfBadge(it) }
                                    property.dealMode?.let { DfBadge(it) }
                                    property.salePrice?.let {
                                        Text(FormatUtils.formatPriceToman(it), style = AppTypography.sectionTitle, color = DfColors.Purple)
                                    }
                                    property.area?.let {
                                        Text("$it متر", style = AppTypography.bodyDescription)
                                    }
                                    property.address?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = AppTypography.bodyDescription)
                                    }
                                    property.notes?.takeIf { it.isNotBlank() }?.let {
                                        Text(it, style = AppTypography.bodyDescription)
                                    }
                                }
                            }
                        }
                        item {
                            Text("تغییر وضعیت", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                CrmConstants.PROPERTY_TX_STATUSES.forEach { status ->
                                    TextButton(
                                        onClick = { viewModel.changeStatus(status) },
                                        enabled = property.transactionStatus != status,
                                    ) { Text(status, style = AppTypography.labelSmall) }
                                }
                            }
                        }
                        item { DfPrimaryButton("ویرایش", onClick = { viewModel.toggleEditSheet(true) }) }
                    }
                }
            }
        }
    }

    if (state.showEditSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { viewModel.toggleEditSheet(false) }, sheetState = sheetState) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(state.editTitle, viewModel::onEditTitleChange, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editCity, viewModel::onEditCityChange, label = { Text("شهر") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editPrice, viewModel::onEditPriceChange, label = { Text("قیمت") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.editNotes, viewModel::onEditNotesChange, label = { Text("یادداشت") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                DfPrimaryButton("ذخیره", onClick = viewModel::saveEdit)
            }
        }
    }
}
