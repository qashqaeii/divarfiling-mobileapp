package ir.divarfiling.mobile.feature.crm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfConfirmBottomSheet
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfExtendedFab
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterChipSection
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPillChip
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSearchFilterPanel
import ir.divarfiling.mobile.core.design.components.DfSectionHeader
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.components.PropertiesStatsRow
import ir.divarfiling.mobile.feature.crm.components.PropertyCreateSheet
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
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
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
                }
                item {
                    DfSearchFilterPanel(
                        query = state.query,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = viewModel::search,
                        searchPlaceholder = "جستجو در فایل‌های شخصی…",
                        filters = {
                            DfFilterChipSection(label = "وضعیت معامله") {
                                DfFilterChipRow(
                                    options = buildList {
                                        add(DfFilterOption<String?>(null, "همه وضعیت‌ها"))
                                        addAll(PropertyConstants.TX_STATUSES.map { DfFilterOption(it, it) })
                                    },
                                    selected = state.transactionStatus,
                                    onSelect = {
                                        viewModel.onTransactionStatusChange(it)
                                        viewModel.search()
                                    },
                                )
                            }
                            DfFilterChipSection(label = "نوع معامله") {
                                DfFilterChipRow(
                                    options = buildList {
                                        add(DfFilterOption<String?>(null, "همه معاملات"))
                                        addAll(PropertyConstants.DEAL_MODES.map { DfFilterOption(it, it) })
                                    },
                                    selected = state.dealMode,
                                    onSelect = {
                                        viewModel.onDealModeChange(it)
                                        viewModel.search()
                                    },
                                )
                            }
                            DfFilterChipSection(label = "نوع ملک") {
                                DfFilterChipRow(
                                    options = buildList {
                                        add(DfFilterOption<String?>(null, "همه انواع"))
                                        addAll(PropertyConstants.PROPERTY_TYPES.map { DfFilterOption(it, it) })
                                    },
                                    selected = state.propertyType,
                                    onSelect = {
                                        viewModel.onPropertyTypeChange(it)
                                        viewModel.search()
                                    },
                                )
                            }
                        },
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
    val context = LocalContext.current
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

@Composable
private fun PropertyDetailContent(
    property: PropertyDto,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onWhatsApp: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenLink: (() -> Unit)?,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val dealAccent = PropertyFilters.dealModeAccent(property)
    val txStatus = property.transactionStatus ?: "فعال"
    val (statusColor, statusBg) = PropertyFilters.txStatusColors(txStatus)
    val cover = property.images.firstOrNull()?.takeIf { it.isNotBlank() }

    LazyColumn(
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
            ) {
                if (cover != null) {
                    DfAsyncImage(
                        url = cover,
                        contentDescription = property.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(dealAccent.copy(alpha = 0.35f), DfColors.PurpleContainer),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = PropertyFilters.propertyTypeIcon(property.propertyType),
                            contentDescription = null,
                            tint = dealAccent,
                            modifier = Modifier.size(56.dp),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                            ),
                        ),
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = AppSpacing.sm, end = AppSpacing.screenHorizontal)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)),
                ) {
                    Icon(DfIcons.ChevronLeft, contentDescription = "بازگشت", tint = Color.White)
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    property.dealMode?.let { PropertyDetailBadge(it, dealAccent, dealAccent.copy(alpha = 0.2f)) }
                    PropertyDetailBadge(txStatus, statusColor, statusBg)
                    property.propertyType?.let { PropertyDetailBadge(it, DfColors.TextSecondary, DfColors.SurfaceVariant) }
                    property.publishStatus?.takeIf { it.isNotBlank() }?.let {
                        PropertyDetailBadge(it, PropertyFilters.publishDotColor(it), DfColors.SurfaceVariant)
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(property.title, style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
                val location = PropertyFilters.locationLabel(property)
                if (location != "—") {
                    Text(location, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                }
            }
        }

        item {
            PropertyPriceBoard(property = property, accent = dealAccent)
        }

        item {
            PropertySpecsSection(property = property)
        }

        if (property.hasParking || property.hasStorage || property.hasElevator) {
            item {
                PropertyAmenitiesCard(property = property)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                PropertyQuickActionCard(
                    label = "ویرایش",
                    icon = DfIcons.File,
                    background = DfColors.PurpleContainer,
                    iconTint = DfColors.Purple,
                    onClick = onEdit,
                    modifier = Modifier.size(width = 96.dp, height = 88.dp),
                )
                PropertyQuickActionCard(
                    label = "اشتراک",
                    icon = DfIcons.Share2,
                    background = DfColors.BlueLight,
                    iconTint = DfColors.Blue,
                    onClick = onShare,
                    modifier = Modifier.size(width = 96.dp, height = 88.dp),
                )
                PropertyQuickActionCard(
                    label = "واتساپ",
                    iconRes = R.drawable.ic_whatsapp,
                    background = DfColors.GreenLight,
                    iconTint = DfColors.Green,
                    onClick = onWhatsApp,
                    modifier = Modifier.size(width = 96.dp, height = 88.dp),
                )
                PropertyQuickActionCard(
                    label = "کپی لینک",
                    icon = DfIcons.Copy,
                    background = DfColors.AmberLight,
                    iconTint = DfColors.Amber,
                    onClick = onCopyLink,
                    modifier = Modifier.size(width = 96.dp, height = 88.dp),
                )
                if (onOpenLink != null) {
                    PropertyQuickActionCard(
                        label = "دیوار",
                        icon = DfIcons.ExternalLink,
                        background = DfColors.BlueLight,
                        iconTint = DfColors.Blue,
                        onClick = onOpenLink,
                        modifier = Modifier.size(width = 96.dp, height = 88.dp),
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text("تغییر وضعیت", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    CrmConstants.PROPERTY_TX_STATUSES.forEach { status ->
                        DfPillChip(
                            label = status,
                            selected = property.transactionStatus == status,
                            onClick = { if (!isSubmitting) onStatusChange(status) },
                        )
                    }
                }
            }
        }

        property.address?.takeIf { it.isNotBlank() }?.let { address ->
            item {
                DfPremiumCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(DfIcons.MapPin, null, tint = DfColors.Purple, modifier = Modifier.size(16.dp))
                            Text("آدرس", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                        }
                        Text(address, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                    }
                }
            }
        }

        property.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            item {
                DfPremiumCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("یادداشت", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                        Text(notes, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
                    }
                }
            }
        }

        property.token?.takeIf { it.isNotBlank() }?.let { token ->
            item {
                DfPremiumCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("شناسه فایل", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                            Text(token, style = AppTypography.bodyDescription, fontWeight = FontWeight.Medium)
                        }
                        PropertyFilters.jalaliUpdated(property)?.let {
                            Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                        }
                    }
                }
            }
        }

        item {
            TextButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal),
                enabled = !isSubmitting,
            ) {
                Text("حذف فایل شخصی", color = DfColors.OverdueAccent)
            }
        }
    }
}

@Composable
private fun PropertyDetailBadge(text: String, color: Color, bg: Color) {
    Surface(shape = AppShapes.Chip, color = bg) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = AppTypography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PropertyPriceBoard(property: PropertyDto, accent: Color) {
    DfPremiumCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            property.salePrice?.let {
                PropertyPriceCell("فروش", FormatUtils.formatPriceToman(it), accent, Modifier.weight(1f))
            }
            property.deposit?.let {
                PropertyPriceCell("رهن", FormatUtils.formatPriceToman(it), DfColors.Blue, Modifier.weight(1f))
            }
            property.rent?.let {
                PropertyPriceCell("اجاره", FormatUtils.formatPriceToman(it), DfColors.Amber, Modifier.weight(1f))
            }
            if (property.salePrice == null && property.deposit == null && property.rent == null) {
                Text("قیمت ثبت نشده", style = AppTypography.bodyDescription, color = DfColors.TextMuted)
            }
        }
    }
}

@Composable
private fun PropertyPriceCell(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
        Text(value, style = AppTypography.labelLarge, fontWeight = FontWeight.Bold, color = color, maxLines = 2)
    }
}

@Composable
private fun PropertySpecsSection(property: PropertyDto) {
    PropertySpecsCard(property = property)
}

@Composable
private fun PropertySpecsCard(property: PropertyDto) {
    val specs = buildList {
        PropertyFilters.formatArea(property.area)?.let { add("متراژ" to it) }
        property.rooms?.takeIf { it.isNotBlank() }?.let { add("اتاق" to it) }
        PropertyFilters.formatFloor(property.floor, property.totalFloors)?.let { add("طبقه" to it) }
        property.buildYear?.let { add("سال ساخت" to it.toString()) }
        property.propertyType?.takeIf { it.isNotBlank() }?.let { add("نوع" to it) }
        val location = PropertyFilters.locationLabel(property)
        if (location != "—") add("موقعیت" to location)
    }
    if (specs.isEmpty()) return

    DfPremiumCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text("مشخصات", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            specs.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    row.forEach { (label, value) ->
                        PropertySpecCell(label, value, Modifier.weight(1f))
                    }
                    if (row.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PropertySpecCell(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = AppShapes.Chip,
        color = DfColors.SurfaceVariant,
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Text(value, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfColors.TextPrimary)
        }
    }
}

@Composable
private fun PropertyAmenitiesCard(property: PropertyDto) {
    DfPremiumCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Text("امکانات", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                if (property.hasParking) PropertyAmenityChip("پارکینگ", DfIcons.Car)
                if (property.hasStorage) PropertyAmenityChip("انباری", DfIcons.Folder)
                if (property.hasElevator) PropertyAmenityChip("آسانسور", DfIcons.Building)
            }
        }
    }
}

@Composable
private fun PropertyAmenityChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(shape = AppShapes.Chip, color = DfColors.GreenLight) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = DfColors.Green, modifier = Modifier.size(14.dp))
            Text(label, style = AppTypography.labelSmall, color = DfColors.Green, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyQuickActionCard(
    label: String,
    onClick: () -> Unit,
    background: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconRes: Int? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Card,
        color = background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                icon != null -> Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                iconRes != null -> Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = DfColors.TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
