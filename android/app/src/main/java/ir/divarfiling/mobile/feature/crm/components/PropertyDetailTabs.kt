package ir.divarfiling.mobile.feature.crm.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.network.CustomerDocumentDto
import ir.divarfiling.mobile.core.network.ListingFeatureGroupDto
import ir.divarfiling.mobile.core.network.PropertyContactLinkDto
import ir.divarfiling.mobile.core.network.PropertyDetailData
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.PropertyDetailTab

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
    onLinkContact: () -> Unit,
    onContactClick: (Long) -> Unit,
    onInlineNotesChange: (String) -> Unit,
    onSaveNotes: () -> Unit,
    onUploadDocument: () -> Unit,
    onDeleteDocument: (Long) -> Unit,
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
            PropertyDetailHero(
                property = property,
                contactCount = detail.contactCount,
                highlights = detail.listingHighlights,
                dealAccent = dealAccent,
                statusColor = statusColor,
                statusBg = statusBg,
                txStatus = txStatus,
                onBack = onBack,
            )
        }

        item {
            PropertyDetailQuickActions(
                onEdit = onEdit,
                onShare = onShare,
                onWhatsApp = onWhatsApp,
                onCopyLink = onCopyLink,
                onOpenLink = onOpenLink,
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

        item {
            when (selectedTab) {
                PropertyDetailTab.OVERVIEW -> PropertyDossierPanel(
                    property = property,
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
                PropertyDetailSummarySidebar(
                    property = property,
                    contactCount = detail.contactCount,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }

        if (detail.canEdit) {
            item {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenHorizontal),
                    enabled = !isSubmitting,
                ) {
                    Text("حذف ملک", color = DfColors.OverdueAccent)
                }
            }
        }
    }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertyDetailHero(
    property: PropertyDto,
    contactCount: Int,
    highlights: List<String>,
    dealAccent: Color,
    statusColor: Color,
    statusBg: Color,
    txStatus: String,
    onBack: () -> Unit,
) {
    val cover = property.images.firstOrNull()?.takeIf { it.isNotBlank() }
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
        ) {
            if (cover != null) {
                DfAsyncImage(
                    url = cover,
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                DfListingImage(
                    thumbnailUrl = null,
                    images = property.images,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(dealAccent.copy(alpha = 0.25f), DfColors.PurpleContainer),
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
                            listOf(Color.Black.copy(alpha = 0.1f), Color.Black.copy(alpha = 0.72f)),
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
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = property.title,
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val location = PropertyFilters.locationLabel(property)
                if (location != "—") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(DfIcons.MapPin, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(14.dp))
                        Text(location, style = AppTypography.labelSmall, color = Color.White.copy(alpha = 0.9f))
                    }
                }
                property.salePrice?.let {
                    Text(
                        text = FormatUtils.formatPriceToman(it),
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.BlueLight,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    property.area?.let {
                        PropertyHeroKpi(
                            label = PropertyFilters.formatArea(it).orEmpty(),
                            icon = DfIcons.Ruler,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    property.rooms?.takeIf { it.isNotBlank() }?.let {
                        PropertyHeroKpi(label = "$it اتاق", icon = DfIcons.Bed, modifier = Modifier.weight(1f))
                    }
                    PropertyHeroKpi(
                        label = "$contactCount مخاطب",
                        icon = DfIcons.Users,
                        modifier = Modifier.weight(1f),
                    )
                    PropertyFilters.jalaliUpdated(property)?.let {
                        PropertyHeroKpi(label = it, icon = DfIcons.Calendar, modifier = Modifier.weight(1f))
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    property.dealMode?.let { PropertyDetailBadge(it, dealAccent, dealAccent.copy(alpha = 0.25f)) }
                    PropertyDetailBadge(txStatus, statusColor, statusBg.copy(alpha = 0.85f))
                    property.publishStatus?.let {
                        PropertyDetailBadge(it, Color.White, Color.White.copy(alpha = 0.2f))
                    }
                    property.propertyType?.let {
                        PropertyDetailBadge(it, Color.White, Color.White.copy(alpha = 0.2f))
                    }
                }
                if (highlights.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        highlights.take(6).forEach { tag ->
                            PropertyDetailBadge(tag, Color.White, Color.White.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyHeroKpi(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = Color.White.copy(alpha = 0.12f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(12.dp))
            Text(
                text = label,
                style = AppTypography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PropertyDetailTabBar(
    tabs: List<PropertyDetailTab>,
    selectedTab: PropertyDetailTab,
    onTabSelect: (PropertyDetailTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        edgePadding = 0.dp,
        divider = {},
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelect(tab) },
                text = {
                    Text(
                        text = tab.label,
                        style = AppTypography.labelLarge,
                        fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Medium,
                    )
                },
            )
        }
    }
}

@Composable
private fun PropertyDossierPanel(
    property: PropertyDto,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)) {
        Text(
            text = "پرونده کامل ملک",
            style = AppTypography.cardTitle,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "تمام اطلاعات ثبت‌شده برای معرفی و پیگیری",
            style = AppTypography.bodyDescription,
            color = DfColors.TextMuted,
        )
        PropertyDossierGroup(
            title = "معامله و قیمت",
            icon = DfIcons.Tag,
            rows = buildList {
                property.dealMode?.let { add("نوع معامله" to it) }
                add("وضعیت" to (property.transactionStatus ?: "فعال"))
                property.publishStatus?.let { add("انتشار" to it) }
                property.salePrice?.let { add("فروش" to FormatUtils.formatPriceToman(it)) }
                property.deposit?.let { add("رهن" to FormatUtils.formatPriceToman(it)) }
                property.rent?.let { add("اجاره" to FormatUtils.formatPriceToman(it)) }
            },
        )
        PropertyDossierGroup(
            title = "مشخصات فنی",
            icon = DfIcons.Ruler,
            rows = buildList {
                property.propertyType?.let { add("نوع ملک" to it) }
                PropertyFilters.formatArea(property.area)?.let { add("متراژ" to it) }
                property.rooms?.takeIf { it.isNotBlank() }?.let { add("اتاق" to it) }
                PropertyFilters.formatFloor(property.floor, property.totalFloors)?.let { add("طبقه" to it) }
                property.buildYear?.let { add("سال ساخت" to it.toString()) }
                add("پارکینگ" to if (property.hasParking) "دارد" else "—")
                add("انباری" to if (property.hasStorage) "دارد" else "—")
                add("آسانسور" to if (property.hasElevator) "دارد" else "—")
            },
        )
        PropertyDossierGroup(
            title = "موقعیت",
            icon = DfIcons.MapPin,
            rows = buildList {
                add("نمایش" to PropertyFilters.locationLabel(property))
                property.neighborhood?.takeIf { it.isNotBlank() }?.let { add("محله" to it) }
                property.city?.takeIf { it.isNotBlank() }?.let { add("شهر" to it) }
                property.district?.takeIf { it.isNotBlank() }?.let { add("منطقه" to it) }
                property.address?.takeIf { it.isNotBlank() }?.let { add("آدرس" to it) }
            },
        )
        property.amenities?.takeIf { it.isNotBlank() }?.let { amenities ->
            PropertyDossierGroup(
                title = "امکانات",
                icon = DfIcons.Star,
                rows = listOf("توضیحات" to amenities),
            )
        }
        PropertyDossierGroup(
            title = "زمان‌بندی",
            icon = DfIcons.Calendar,
            rows = buildList {
                property.createdAt?.let { add("ثبت" to DateUtils.formatJalaliDateTime(it)) }
                property.updatedAt?.let { add("بروزرسانی" to DateUtils.formatJalaliDateTime(it)) }
                if (property.images.isNotEmpty()) add("تصاویر" to "${property.images.size} عدد")
            },
        )
    }
}

@Composable
private fun PropertyDossierGroup(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
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
                Icon(icon, null, tint = DfColors.Purple, modifier = Modifier.size(16.dp))
                Text(title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            }
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                    Text(
                        value,
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 3,
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
            Text("مخاطبین مرتبط", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            if (canEdit) {
                TextButton(onClick = onLinkContact) { Text("پیوند مخاطب") }
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
    DfPremiumCard(onClick = onClick) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(link.customerName, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                link.role?.let { DfBadge(it) }
            }
            link.phone?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                link.dealType?.let { DfBadge(it) }
                link.interestLevel?.takeIf { it.isNotBlank() }?.let { DfBadge(it) }
                if (link.isPrimary) DfBadge("اصلی")
            }
            link.notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfColors.TextMuted)
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
    val profile = detail.featureProfile
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap)) {
        Text("مشخصات آگهی منبع", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
        if (profile == null || !profile.hasDetails) {
            if (detail.listingHighlights.isEmpty()) {
                DfEmptyState(
                    title = "مشخصات تکمیلی ندارد",
                    subtitle = "این فایل به آگهی دیوار متصل نیست یا ویژگی ثبت نشده",
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    detail.listingHighlights.forEach { DfBadge(it) }
                }
            }
            return
        }
        profile.core.filter { !it.value.isNullOrBlank() && it.value != "—" }.forEach { item ->
            PropertySpecRow(item.label ?: item.key.orEmpty(), item.value.orEmpty())
        }
        profile.groups.forEach { group ->
            PropertyFeatureGroupCard(group)
        }
    }
}

@Composable
private fun PropertyFeatureGroupCard(group: ListingFeatureGroupDto) {
    DfPremiumCard {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(group.title ?: "جزئیات", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            group.items.forEach { item ->
                PropertySpecRow(item.label ?: item.key.orEmpty(), item.value.orEmpty())
            }
        }
    }
}

@Composable
private fun PropertySpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
        Text(value, style = AppTypography.labelLarge, fontWeight = FontWeight.Medium)
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
        Text("یادداشت مالک", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
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
            Text("مدارک", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            if (canEdit && !maskSensitive) {
                TextButton(onClick = onUpload, enabled = !isSubmitting) { Text("آپلود مدرک") }
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
                document.uploadedAt?.let {
                    Text(
                        DateUtils.formatJalaliDateTime(it),
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
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
    property: PropertyDto,
    contactCount: Int,
    modifier: Modifier = Modifier,
) {
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
            PropertySpecRow("مخاطبین", contactCount.toString())
            property.createdAt?.let {
                PropertySpecRow("ثبت", DateUtils.formatJalaliDateTime(it))
            }
        }
    }
}

@Composable
fun PropertyLinkContactSheet(
    contactId: String,
    role: String,
    isSubmitting: Boolean,
    onContactIdChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text("پیوند مخاطب به ملک", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
        Text(
            "شناسه مخاطب را از صفحه مخاطبین وارد کنید",
            style = AppTypography.bodyDescription,
            color = DfColors.TextMuted,
        )
        OutlinedTextField(
            value = contactId,
            onValueChange = onContactIdChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("شناسه مخاطب") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        val roles = listOf("مالک", "خریدار", "مستاجر", "پیشنهادی", "معرف")
        roles.forEach { r ->
            DfSheetOptionRow(
                label = r,
                selected = role == r,
                onClick = { onRoleChange(r) },
            )
        }
        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isSubmitting) "در حال ثبت…" else "پیوند مخاطب")
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
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
