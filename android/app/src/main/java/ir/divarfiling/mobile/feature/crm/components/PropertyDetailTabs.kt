package ir.divarfiling.mobile.feature.crm.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.DfComponentSpec
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfNbaAction
import ir.divarfiling.mobile.core.design.components.DfNbaCard
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.FeatureProfilePanels
import ir.divarfiling.mobile.core.network.CustomerDocumentDto
import ir.divarfiling.mobile.core.network.PropertyContactLinkDto
import ir.divarfiling.mobile.core.network.PropertyDetailData
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.PropertyDetailTab
import ir.divarfiling.mobile.feature.filing.components.ListingAiSummarySection

@Composable
fun PropertyDetailTabbedContent(
    detail: PropertyDetailData,
    selectedTab: PropertyDetailTab,
    isSubmitting: Boolean,
    inlineNotes: String,
    onBack: () -> Unit,
    onTabSelect: (PropertyDetailTab) -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onWhatsApp: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenLink: (() -> Unit)?,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: (() -> Unit)? = null,
    onCreateDeal: (() -> Unit)? = null,
    onTeamShare: (() -> Unit)? = null,
    onAgencySpace: (() -> Unit)? = null,
    agencySpaceProvenance: ir.divarfiling.mobile.core.network.AgencySpaceProvenanceDto? = null,
    onOpenSpaceItem: ((Long) -> Unit)? = null,
    onLinkContact: () -> Unit,
    onContactMatches: () -> Unit = {},
    onContactClick: (Long) -> Unit,
    onInlineNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit,
    onUploadDocument: () -> Unit,
    onDeleteDocument: (Long) -> Unit,
    onToggleFolder: (folderId: Long, add: Boolean) -> Unit = { _, _ -> },
    onEditLocation: () -> Unit = {},
    onFetchNearbyPois: (refresh: Boolean) -> Unit = {},
    nearbyPoisPayload: ir.divarfiling.mobile.core.network.NearbyPoisPayloadDto? = null,
    nearbyPoisLoading: Boolean = false,
    aiSummary: String = "",
    isSummarizing: Boolean = false,
    aiIsFallback: Boolean = false,
    aiModelLabel: String = "",
    aiSummaryHighlights: List<String> = emptyList(),
    aiNegotiationTip: String = "",
    onGenerateSummary: () -> Unit = {},
    onCopySummary: () -> Unit = {},
) {
    val property = detail.property
    val tabs = buildPropertyTabs(detail)
    val dealAccent = PropertyFilters.dealModeAccent(property)
    val txStatus = property.transactionStatus ?: "فعال"
    val (statusColor, statusBg) = PropertyFilters.txStatusColors(txStatus)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item {
            PropertyDetailMediaHeader(
                property = property,
                contactCount = detail.contactCount,
                dealAccent = dealAccent,
                statusColor = statusColor,
                statusBg = statusBg,
                txStatus = txStatus,
                onBack = onBack,
            )
        }

        if (agencySpaceProvenance != null) {
            item {
                ir.divarfiling.mobile.feature.team.AgencySpaceProvenanceBadge(
                    provenance = agencySpaceProvenance,
                    onOpenSpaceItem = onOpenSpaceItem,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }

        item {
            PropertyDetailQuickActions(
                onEdit = onEdit,
                onShare = onShare,
                onWhatsApp = onWhatsApp,
                onCopyLink = onCopyLink,
                onOpenLink = onOpenLink,
                onDelete = if (detail.canEdit) onDelete else null,
                onDuplicate = if (detail.canEdit) onDuplicate else null,
                onCreateDeal = onCreateDeal,
                onTeamShare = if (detail.canEdit) onTeamShare else null,
                onAgencySpace = onAgencySpace,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }

        item {
            val hasOwner = detail.contacts.isNotEmpty() || !property.ownerName.isNullOrBlank()
            DfNbaCard(
                compact = true,
                action = if (!hasOwner) {
                    DfNbaAction(
                        title = "مالک مشخص نیست",
                        subtitle = "برای پیگیری و معامله، مالک را وصل کنید",
                        cta = "اتصال مالک",
                        onClick = onLinkContact,
                    )
                } else {
                    DfNbaAction(
                        title = "تطبیق با مشتری",
                        subtitle = "مشتریان هم‌خوان با این فایل",
                        cta = "پیشنهادها",
                        onClick = onContactMatches,
                    )
                },
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }

        item {
            PropertyDetailTabBar(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelect = onTabSelect,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }

        if (detail.canEdit && detail.availableFolders.isNotEmpty()) {
            item {
                PropertyFolderPicker(
                    property = property,
                    availableFolders = detail.availableFolders,
                    isSubmitting = isSubmitting,
                    onToggle = onToggleFolder,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }

        item {
            when (selectedTab) {
                PropertyDetailTab.OVERVIEW -> PropertyDossierPanel(
                    detail = detail,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
                PropertyDetailTab.CONTACTS -> PropertyContactsPanel(
                    contacts = detail.contacts,
                    canEdit = detail.canEdit,
                    onLinkContact = onLinkContact,
                    onContactClick = onContactClick,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
                PropertyDetailTab.ACTIVITY -> ContactActivityTimeline(
                    activities = detail.activities,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
                PropertyDetailTab.SPECS -> PropertySpecsPanel(
                    detail = detail,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
                PropertyDetailTab.NOTES -> PropertyNotesPanel(
                    notes = inlineNotes,
                    amenities = property.amenities,
                    maskSensitive = detail.maskSensitive,
                    canEdit = detail.canEdit,
                    isSubmitting = isSubmitting,
                    onNotesChange = onInlineNotesChange,
                    onSave = onSaveNotes,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
                PropertyDetailTab.DOCS -> PropertyDocumentsPanel(
                    documents = detail.documents,
                    canEdit = detail.canEdit,
                    maskSensitive = detail.maskSensitive,
                    isSubmitting = isSubmitting,
                    onUpload = onUploadDocument,
                    onDelete = onDeleteDocument,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }

        if (selectedTab == PropertyDetailTab.OVERVIEW) {
            item {
                PropertyTxStatusSection(
                    currentStatus = txStatus,
                    isSubmitting = isSubmitting,
                    onStatusChange = onStatusChange,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
            item {
                PropertyLocationSection(
                    property = property,
                    canEdit = detail.canEdit,
                    isSubmitting = isSubmitting,
                    onEditLocation = onEditLocation,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
            item {
                PropertyNavigationSection(
                    mapNavigation = detail.mapNavigation,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
            item {
                PropertyNearbyPoisSection(
                    property = property,
                    nearbyPoisCtx = detail.nearbyPoisCtx,
                    payload = nearbyPoisPayload,
                    isLoading = nearbyPoisLoading,
                    canEdit = detail.canEdit,
                    onFetch = onFetchNearbyPois,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
            item {
                PropertyFileAccessPanel(
                    detail = detail,
                    onTeamShare = onTeamShare,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
            item {
                ListingAiSummarySection(
                    title = "خلاصه هوشمند فایل",
                    summary = aiSummary,
                    isLoading = isSummarizing,
                    isFallback = aiIsFallback,
                    modelLabel = aiModelLabel.takeIf { it.isNotBlank() },
                    highlights = aiSummaryHighlights,
                    negotiationTip = aiNegotiationTip,
                    emptyHint = "خلاصه حرفه‌ای این ملک را برای ارائه به مشتری و مذاکره تولید کنید.",
                    onGenerate = onGenerateSummary,
                    onCopy = onCopySummary,
                    onOpenAssistant = onGenerateSummary,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }
    }
}

private fun propertyLocationDossierRows(property: PropertyDto): List<Pair<String, String>> =
    buildList {
        property.city?.takeIf { it.isNotBlank() }?.let { add("شهر" to it) }
        property.district?.takeIf { it.isNotBlank() }?.let { add("منطقه" to it) }
        property.neighborhood?.takeIf { it.isNotBlank() }?.let { add("محله" to it) }
        property.postalCode?.takeIf { it.isNotBlank() }?.let { add("کد پستی" to it) }
        property.address?.takeIf { it.isNotBlank() }?.let { add("آدرس" to it) }
    }

private fun propertyFacadeType(property: PropertyDto): String? =
    property.featureData["نمای ساختمان"]?.takeIf { it.isNotBlank() }

private fun propertyBoolDetailLabel(enabled: Boolean): String? =
    if (enabled) PropertyAmenityResolver.label(true) else null

private fun propertyFeatureAmenityRows(property: PropertyDto): List<Pair<String, String>> =
    buildList {
        propertyBoolDetailLabel(property.hasWardrobe)?.let { add("کمد دیواری" to it) }
        propertyBoolDetailLabel(property.hasTwoCorner)?.let { add("دو نبش" to it) }
        propertyBoolDetailLabel(property.hasTwoAccess)?.let { add("دو معبر" to it) }
    }

private fun propertyDecorationDossierRows(property: PropertyDto): List<Pair<String, String>> =
    buildList {
        propertyBoolDetailLabel(property.hasWallpaper)?.let { add("کاغذ دیواری" to it) }
        propertyBoolDetailLabel(property.hasKnauf)?.let { add("کناف" to it) }
        propertyBoolDetailLabel(property.hasLighting)?.let { add("نورپردازی" to it) }
    }

private fun buildPropertyTabs(detail: PropertyDetailData): List<PropertyDetailTab> {
    val hasSpecs = detail.featureProfile?.hasDetails == true || detail.listingHighlights.isNotEmpty()
    return buildList {
        add(PropertyDetailTab.OVERVIEW)
        add(PropertyDetailTab.CONTACTS)
        add(PropertyDetailTab.ACTIVITY)
        if (hasSpecs) add(PropertyDetailTab.SPECS)
        add(PropertyDetailTab.NOTES)
        add(PropertyDetailTab.DOCS)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmartMatchEntryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = Color.Transparent,
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.Card)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            DfThemeColors.primary().copy(alpha = 0.12f),
                            DfThemeColors.info().copy(alpha = 0.08f),
                        ),
                    ),
                )
                .padding(AppSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DfThemeColors.primaryContainer()),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        DfIcons.Sparkles,
                        contentDescription = null,
                        tint = DfThemeColors.primary(),
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "پیشنهاد هوشمند",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        "مشتریان هم‌خوان با این فایل را پیدا و پیشنهاد کنید",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    DfIcons.ChevronLeft,
                    contentDescription = null,
                    tint = DfThemeColors.primary(),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyDetailTabBar(
    tabs: List<PropertyDetailTab>,
    selectedTab: PropertyDetailTab,
    onTabSelect: (PropertyDetailTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            Surface(
                onClick = { onTabSelect(tab) },
                shape = AppShapes.Chip,
                color = if (selected) DfThemeColors.primaryContainer() else DfThemeColors.surfaceVariant(),
                border = BorderStroke(
                    1.dp,
                    if (selected) DfThemeColors.primary().copy(alpha = 0.35f) else DfThemeColors.outlineSubtle(),
                ),
                modifier = Modifier.defaultMinSize(minHeight = DfComponentSpec.MinTouchTargetDp.dp),
            ) {
                Text(
                    text = tab.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = AppTypography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) DfThemeColors.onPrimaryContainer() else DfThemeColors.textSecondary(),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PropertyDossierPanel(
    detail: PropertyDetailData,
    modifier: Modifier = Modifier,
) {
    val property = detail.property
    val profile = detail.featureProfile
    val parking = PropertyAmenityResolver.effectiveParking(property, profile)
    val storage = PropertyAmenityResolver.effectiveStorage(property, profile)
    val elevator = PropertyAmenityResolver.effectiveElevator(property, profile)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        PropertyAmenitiesPanel(
            parking = parking,
            storage = storage,
            elevator = elevator,
        )

        PropertyDossierGroup(
            title = "معامله و قیمت",
            iconRes = DfDecorIcons.Coins,
            rows = buildList {
                property.dealMode?.let { add("نوع معامله" to it) }
                property.publishStatus?.let { add("انتشار" to it) }
                property.salePrice?.let { add("فروش" to FormatUtils.formatPriceToman(it)) }
                property.deposit?.let { add("رهن" to FormatUtils.formatPriceToman(it)) }
                property.rent?.let { add("اجاره" to FormatUtils.formatPriceToman(it)) }
            },
        )
        PropertyDossierGroup(
            title = "موقعیت",
            icon = DfIcons.MapPin,
            rows = propertyLocationDossierRows(property),
        )
        PropertyDossierGroup(
            title = "مشخصات فنی",
            iconRes = DfDecorIcons.Ruler,
            rows = buildList {
                property.propertyType?.let { add("نوع ملک" to it) }
                PropertyFilters.formatArea(property.area)?.let { add("متراژ بنا" to it) }
                PropertyFilters.formatArea(property.landArea)?.let { add("متراژ زمین" to it) }
                PropertyFilters.formatArea(property.frontageWidth)?.let { add("دهنه ملک" to it) }
                property.rooms?.takeIf { it.isNotBlank() }?.let { add("اتاق" to it) }
                PropertyFilters.formatFloor(property.floor, property.totalFloors)?.let { add("طبقه" to it) }
                property.buildYear?.let { add("سال ساخت" to it.toString()) }
                propertyFacadeType(property)?.let { add("نمای ساختمان" to it) }
                add("پارکینگ" to PropertyAmenityResolver.label(parking))
                add("انباری" to PropertyAmenityResolver.label(storage))
                add("آسانسور" to PropertyAmenityResolver.label(elevator))
            },
        )
        PropertyDossierGroup(
            title = "امکانات",
            icon = DfIcons.Star,
            rows = propertyFeatureAmenityRows(property),
        )
        PropertyDossierGroup(
            title = "رفاهی و تزئینات",
            icon = DfIcons.Sparkles,
            rows = propertyDecorationDossierRows(property),
        )
        property.amenities?.takeIf { it.isNotBlank() }?.let { amenities ->
            PropertyDossierGroup(
                title = "سایر امکانات",
                icon = DfIcons.Star,
                rows = listOf("توضیحات" to amenities),
            )
        }
        PropertyDossierGroup(
            title = "زمان‌بندی",
            icon = DfIcons.Calendar,
            rows = buildList {
                property.createdAt?.let { ts ->
                    DateUtils.formatJalaliDateTime(ts)?.let { add("ثبت" to it) }
                }
                property.updatedAt?.let { ts ->
                    DateUtils.formatJalaliDateTime(ts)?.let { add("بروزرسانی" to it) }
                }
                if (property.images.isNotEmpty()) add("تصاویر" to "${property.images.size} عدد")
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertyAmenitiesPanel(
    parking: Boolean?,
    storage: Boolean?,
    elevator: Boolean?,
) {
    DfPremiumCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfDecorImage(resId = DfDecorIcons.Car, size = 16.dp)
                Text("امکانات کلیدی", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                PropertyAmenityTile("پارکینگ", DfDecorIcons.Car, parking, Modifier.weight(1f))
                PropertyAmenityTile("انباری", DfDecorIcons.Storage, storage, Modifier.weight(1f))
                PropertyAmenityTile("آسانسور", DfDecorIcons.Elevator, elevator, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PropertyAmenityTile(
    label: String,
    iconRes: Int,
    value: Boolean?,
    modifier: Modifier = Modifier,
) {
    val isYes = value == true
    val isNo = value == false
    val color = when {
        isYes -> DfThemeColors.success()
        isNo -> DfThemeColors.textMuted()
        else -> DfThemeColors.warning()
    }
    val bg = when {
        isYes -> DfThemeColors.successContainer()
        isNo -> DfThemeColors.surfaceVariant()
        else -> DfThemeColors.warningContainer()
    }
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = bg,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            DfDecorImage(resId = iconRes, size = 18.dp)
            Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textSecondary(), maxLines = 1)
            Text(
                PropertyAmenityResolver.label(value),
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun PropertyDossierGroup(
    title: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconRes: Int? = null,
) {
    if (rows.isEmpty()) return
    DfPremiumCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when {
                    iconRes != null -> DfDecorImage(resId = iconRes, size = 16.dp)
                    icon != null -> Icon(icon, null, tint = DfThemeColors.primary(), modifier = Modifier.size(16.dp))
                }
                Text(title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            }
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    HorizontalDivider(color = DfThemeColors.outlineSubtle())
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(label, style = AppTypography.labelSmall, color = DfThemeColors.textMuted(), modifier = Modifier.weight(0.42f))
                    Text(
                        value,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(0.58f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyContactsPanel(
    contacts: List<PropertyContactLinkDto>,
    canEdit: Boolean,
    onLinkContact: () -> Unit,
    onContactClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("مخاطبین مرتبط", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold, color = DfThemeColors.textPrimary())
                Text(
                    "مالک، خریدار و پیگیری‌های متصل به این فایل",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
            if (canEdit) {
                TextButton(onClick = onLinkContact) {
                    Text("پیوند", color = DfThemeColors.primary(), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (contacts.isEmpty()) {
            DfEmptyState(
                title = "مخاطبی پیوند نشده",
                subtitle = "مخاطب موجود را به این فایل وصل کنید",
            )
        } else {
            contacts.forEach { link ->
                PropertyContactCard(link = link, onClick = { onContactClick(link.customerId) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyContactCard(
    link: PropertyContactLinkDto,
    onClick: () -> Unit,
) {
    val accent = DfThemeColors.primary()
    DfPremiumCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(72.dp)
                    .background(accent, AppShapes.Chip),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(link.customerName, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold, color = DfThemeColors.textPrimary())
                link.role?.let { DfBadge(it) }
            }
            link.phone?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.labelLarge, fontWeight = FontWeight.Medium, color = DfThemeColors.textSecondary())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                link.dealType?.let { DfBadge(it) }
                link.interestLevel?.takeIf { it.isNotBlank() }?.let { DfBadge(it) }
                if (link.isPrimary) DfBadge("اصلی")
            }
            link.notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfThemeColors.textMuted(), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertySpecsPanel(
    detail: PropertyDetailData,
    modifier: Modifier = Modifier,
) {
    val property = detail.property
    val profile = detail.featureProfile
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)) {
        PropertyAmenitiesPanel(
            parking = PropertyAmenityResolver.effectiveParking(property, profile),
            storage = PropertyAmenityResolver.effectiveStorage(property, profile),
            elevator = PropertyAmenityResolver.effectiveElevator(property, profile),
        )
        FeatureProfilePanels(
            profile = profile,
            highlights = detail.listingHighlights,
            title = "مشخصات کامل آگهی منبع",
            subtitle = "ساختمان، امکانات، سند، شرایط سکونت و تأسیسات",
            modifier = Modifier.fillMaxWidth(),
            emptyMessage = "این فایل به آگهی دیوار متصل نیست یا ویژگی تفصیلی ثبت نشده.",
            applyHorizontalPadding = false,
        )
    }
}

@Composable
private fun PropertySpecRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted, modifier = Modifier.weight(0.42f))
        Text(
            value,
            style = AppTypography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.58f),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PropertyNotesPanel(
    notes: String,
    amenities: String?,
    maskSensitive: Boolean,
    canEdit: Boolean,
    isSubmitting: Boolean,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DfDecorImage(resId = DfDecorIcons.StickyNote, size = 18.dp)
            Text("یادداشت مالک", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
        }
        if (maskSensitive) {
            DfEmptyState(
                title = "یادداشت مخفی است",
                subtitle = "در اشتراک محرمانه یادداشت‌ها نمایش داده نمی‌شوند",
            )
            return
        }
        if (canEdit) {
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                placeholder = { Text("یادداشت‌های معرفی و پیگیری…") },
            )
            Button(
                onClick = onSave,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isSubmitting) "در حال ذخیره…" else "ذخیره یادداشت")
            }
        } else if (notes.isBlank()) {
            DfEmptyState(title = "یادداشتی ثبت نشده", subtitle = "از ویرایش فایل می‌توانید یادداشت اضافه کنید")
        } else {
            Text(notes, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
        }
        amenities?.takeIf { it.isNotBlank() }?.let {
            DfPremiumCard {
                Column(modifier = Modifier.padding(AppSpacing.sm)) {
                    Text("امکانات متنی", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                    Text(it, style = AppTypography.bodyDescription)
                }
            }
        }
    }
}

@Composable
private fun PropertyDocumentsPanel(
    documents: List<CustomerDocumentDto>,
    canEdit: Boolean,
    maskSensitive: Boolean,
    isSubmitting: Boolean,
    onUpload: () -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DfDecorImage(resId = DfDecorIcons.FileText, size = 18.dp)
                Text("مدارک", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            }
            if (canEdit && !maskSensitive) {
                TextButton(onClick = onUpload, enabled = !isSubmitting) {
                    DfDecorImage(
                        resId = DfDecorIcons.Upload,
                        size = 16.dp,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text("آپلود مدرک")
                }
            }
        }
        if (maskSensitive) {
            DfEmptyState(title = "مدارک مخفی", subtitle = "در اشتراک محرمانه مدارک نمایش داده نمی‌شوند")
            return
        }
        if (documents.isEmpty()) {
            DfEmptyState(title = "مدرکی ثبت نشده", subtitle = "قرارداد، سند یا تصویر را آپلود کنید")
        } else {
            documents.forEach { doc ->
                PropertyDocumentCard(
                    document = doc,
                    canDelete = canEdit,
                    onOpen = {
                        doc.fileUrl?.takeIf { it.isNotBlank() }?.let { url ->
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    },
                    onDelete = { onDelete(doc.id) },
                )
            }
        }
    }
}

@Composable
private fun PropertyDocumentCard(
    document: CustomerDocumentDto,
    canDelete: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    DfPremiumCard(onClick = onOpen) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(document.title, style = AppTypography.cardTitle)
                document.uploadedAt?.let { uploadedAt ->
                    DateUtils.formatJalaliDateTime(uploadedAt)?.let { formatted ->
                        Text(
                            formatted,
                            style = AppTypography.labelSmall,
                            color = DfColors.TextMuted,
                        )
                    }
                }
            }
            if (canDelete) {
                TextButton(onClick = onDelete) {
                    Text("حذف", color = DfColors.OverdueAccent)
                }
            }
        }
    }
}

@Composable
private fun PropertyDetailSummarySidebar(
    detail: PropertyDetailData,
    modifier: Modifier = Modifier,
) {
    val property = detail.property
    val profile = detail.featureProfile
    DfPremiumCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("خلاصه پرونده", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            PropertySpecRow("نوع", property.propertyType ?: "—")
            PropertySpecRow("معامله", property.dealMode ?: "—")
            PropertySpecRow("وضعیت", property.transactionStatus ?: "فعال")
            PropertySpecRow("انتشار", property.publishStatus ?: "—")
            PropertySpecRow(
                "مالک",
                listOfNotNull(
                    property.ownerName?.takeIf { it.isNotBlank() },
                    property.phone?.takeIf { it.isNotBlank() },
                ).joinToString(" · ").ifBlank { "—" },
            )
            PropertySpecRow("مخاطبین", detail.contactCount.toString())
            PropertySpecRow(
                "امکانات",
                listOfNotNull(
                    PropertyAmenityResolver.effectiveParking(property, profile).takeIf { it == true }?.let { "پارکینگ" },
                    PropertyAmenityResolver.effectiveStorage(property, profile).takeIf { it == true }?.let { "انباری" },
                    PropertyAmenityResolver.effectiveElevator(property, profile).takeIf { it == true }?.let { "آسانسور" },
                ).joinToString(" · ").ifBlank { "—" },
            )
            property.createdAt?.let { createdAt ->
                DateUtils.formatJalaliDateTime(createdAt)?.let { formatted ->
                    PropertySpecRow("ثبت", formatted)
                }
            }
        }
    }
}

@Composable
fun PropertyLinkContactSheet(
    contactName: String,
    contactPhone: String,
    selectedFromCrm: Boolean,
    isSubmitting: Boolean,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPickContact: () -> Unit,
    onClearSelection: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val canSubmit = (contactName.isNotBlank() && contactPhone.isNotBlank()) || selectedFromCrm

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(AppShapes.IconContainer)
                    .background(DfColors.PurpleContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(DfIcons.User, contentDescription = null, tint = DfColors.Purple, modifier = Modifier.size(20.dp))
            }
            Column {
                Text("اتصال مالک", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                Text(
                    "مالک را از مخاطبین انتخاب کنید یا نام و شماره را دستی وارد کنید",
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                )
            }
        }

        DfSecondaryButton(
            text = if (selectedFromCrm) "تغییر مخاطب از CRM" else "انتخاب از مخاطبین CRM",
            onClick = onPickContact,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )

        if (selectedFromCrm && (contactName.isNotBlank() || contactPhone.isNotBlank())) {
            DfPremiumCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            contactName.ifBlank { "مخاطب انتخاب‌شده" },
                            style = AppTypography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (contactPhone.isNotBlank()) {
                            Text(contactPhone, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
                        }
                        Text(
                            "از مخاطبین CRM",
                            style = AppTypography.labelSmall,
                            color = DfColors.Purple,
                        )
                    }
                    TextButton(onClick = onClearSelection, enabled = !isSubmitting) {
                        Text("حذف")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = DfColors.OutlineSubtle)
            Text("یا ثبت دستی", style = AppTypography.labelSmall, color = DfColors.TextMuted)
            HorizontalDivider(modifier = Modifier.weight(1f), color = DfColors.OutlineSubtle)
        }

        OutlinedTextField(
            value = contactName,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("نام مالک") },
            placeholder = { Text("مثلاً علی محمدی") },
            singleLine = true,
            enabled = !isSubmitting,
        )
        OutlinedTextField(
            value = contactPhone,
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("شماره تماس مالک") },
            placeholder = { Text("مثلاً ۰۹۱۲۱۲۳۴۵۶۷") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            enabled = !isSubmitting,
        )

        Text(
            "در صورت نبودن در CRM، مخاطب جدید ساخته و با نقش مالک به این ملک اضافه می‌شود.",
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
        )

        DfPrimaryButton(
            text = if (isSubmitting) "در حال اتصال…" else "اتصال مالک",
            onClick = onSubmit,
            enabled = !isSubmitting && canSubmit,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), enabled = !isSubmitting) {
            Text("انصراف")
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
