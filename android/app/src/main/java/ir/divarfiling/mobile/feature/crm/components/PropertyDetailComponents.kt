package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.DfListingImage
import ir.divarfiling.mobile.core.design.components.DfSheetOptionRow
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface
import ir.divarfiling.mobile.core.network.PropertyDto
import ir.divarfiling.mobile.feature.crm.CrmConstants

@Composable
fun PropertyDetailContent(
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        item {
            PropertyDetailGallery(
                cover = cover,
                images = property.images,
                property = property,
                dealAccent = dealAccent,
                statusColor = statusColor,
                statusBg = statusBg,
                txStatus = txStatus,
                onBack = onBack,
            )
        }

        item {
            PropertyDetailSummaryCard(
                property = property,
                dealAccent = dealAccent,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }

        item {
            PropertyDetailPriceBoard(
                property = property,
                accent = dealAccent,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }

        item {
            PropertyDetailSpecsGrid(
                property = property,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }

        if (property.hasParking || property.hasStorage || property.hasElevator) {
            item {
                PropertyDetailAmenities(
                    property = property,
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
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }

        item {
            PropertyTxStatusSection(
                currentStatus = txStatus,
                isSubmitting = isSubmitting,
                onStatusChange = onStatusChange,
                modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
            )
        }

        property.address?.takeIf { it.isNotBlank() }?.let { address ->
            item {
                PropertyDetailInfoCard(
                    iconRes = DfDecorIcons.MapPin,
                    title = "آدرس",
                    body = address,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }

        property.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            item {
                PropertyDetailInfoCard(
                    iconRes = DfDecorIcons.StickyNote,
                    title = "یادداشت",
                    body = notes,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }

        property.token?.takeIf { it.isNotBlank() }?.let { token ->
            item {
                PropertyDetailMetaCard(
                    token = token,
                    updatedLabel = PropertyFilters.jalaliUpdated(property),
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PropertyDetailGallery(
    cover: String?,
    images: List<String>,
    property: PropertyDto,
    dealAccent: Color,
    statusColor: Color,
    statusBg: Color,
    txStatus: String,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
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
                images = images,
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
                        listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.6f)),
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
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                property.dealMode?.let { PropertyDetailBadge(it, dealAccent, dealAccent.copy(alpha = 0.25f)) }
                PropertyDetailBadge(txStatus, statusColor, statusBg.copy(alpha = 0.85f))
                property.propertyType?.let {
                    PropertyDetailBadge(it, Color.White, Color.White.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
private fun PropertyDetailSummaryCard(
    property: PropertyDto,
    dealAccent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text("خلاصه فایل", style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold, color = DfColors.TextMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                PropertySummaryChip(
                    icon = DfIcons.Tag,
                    label = "معامله",
                    value = property.dealMode ?: "—",
                    accent = dealAccent,
                    modifier = Modifier.weight(1f),
                )
                PropertySummaryChip(
                    icon = DfIcons.Zap,
                    label = "وضعیت",
                    value = property.transactionStatus ?: "فعال",
                    accent = PropertyFilters.txStatusColors(property.transactionStatus).first,
                    modifier = Modifier.weight(1f),
                )
            }
            property.publishStatus?.takeIf { it.isNotBlank() }?.let { pub ->
                PropertySummaryChip(
                    icon = DfIcons.ExternalLink,
                    label = "انتشار",
                    value = pub,
                    accent = PropertyFilters.publishDotColor(pub),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PropertySummaryChip(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = accent.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
            Column {
                Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                Text(value, style = AppTypography.labelLarge, fontWeight = FontWeight.Bold, color = accent, maxLines = 1)
            }
        }
    }
}

@Composable
fun PropertyDetailPriceBoard(
    property: PropertyDto,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text("قیمت‌گذاری", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
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
            }
            if (property.salePrice == null && property.deposit == null && property.rent == null) {
                Text("قیمت ثبت نشده", style = AppTypography.bodyDescription, color = DfColors.TextMuted)
            }
        }
    }
}

@Composable
private fun PropertyPriceCell(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = color.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
            Text(value, style = AppTypography.labelLarge, fontWeight = FontWeight.Bold, color = color, maxLines = 2)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PropertyDetailSpecsGrid(
    property: PropertyDto,
    modifier: Modifier = Modifier,
) {
    val specs = PropertyFilters.allSpecs(property)
    if (specs.isEmpty()) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text("مشخصات ملک", style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                specs.forEach { (label, value) ->
                    PropertySpecTile(label = label, value = value, icon = specIcon(label))
                }
            }
        }
    }
}

@Composable
private fun PropertySpecTile(label: String, value: String, icon: ImageVector) {
    Surface(
        shape = AppShapes.CardSmall,
        color = DfColors.SurfaceVariant.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .liquidGlassSurface(shape = AppShapes.IconContainer, variant = DfGlassButtonVariant.Secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = DfColors.Purple, modifier = Modifier.size(14.dp))
            }
            Column {
                Text(label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                Text(value, style = AppTypography.labelLarge, fontWeight = FontWeight.SemiBold, color = DfColors.TextPrimary)
            }
        }
    }
}

private fun specIcon(label: String): ImageVector = when {
    label.contains("متراژ") -> DfIcons.Ruler
    label.contains("اتاق") -> DfIcons.Bed
    label.contains("طبقه") -> DfIcons.Building
    label.contains("ساخت") -> DfIcons.Calendar
    label.contains("موقعیت") -> DfIcons.MapPin
    label.contains("نوع") -> DfIcons.Home
    else -> DfIcons.Tag
}

@Composable
fun PropertyDetailAmenities(
    property: PropertyDto,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
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
private fun PropertyAmenityChip(label: String, icon: ImageVector) {
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

@Composable
fun PropertyDetailQuickActions(
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onWhatsApp: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenLink: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        PropertyQuickAction(label = "ویرایش", icon = DfIcons.File, bg = DfColors.PurpleContainer, tint = DfColors.Purple, onClick = onEdit, modifier = Modifier.weight(1f))
        PropertyQuickAction(label = "اشتراک", iconRes = DfDecorIcons.Share2, bg = DfColors.BlueLight, tint = DfColors.Blue, onClick = onShare, modifier = Modifier.weight(1f))
        PropertyQuickAction(label = "واتساپ", iconRes = R.drawable.ic_whatsapp, tintIconRes = true, bg = DfColors.GreenLight, tint = DfColors.Green, onClick = onWhatsApp, modifier = Modifier.weight(1f))
        PropertyQuickAction(label = "کپی", icon = DfIcons.Copy, bg = DfColors.AmberLight, tint = DfColors.Amber, onClick = onCopyLink, modifier = Modifier.weight(1f))
        if (onOpenLink != null) {
            PropertyQuickAction(label = "دیوار", icon = DfIcons.ExternalLink, bg = DfColors.BlueLight, tint = DfColors.Blue, onClick = onOpenLink, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyQuickAction(
    label: String,
    onClick: () -> Unit,
    bg: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    tintIconRes: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Card,
        color = bg.copy(alpha = 0.65f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            when {
                icon != null -> Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
                iconRes != null && tintIconRes -> Icon(painterResource(iconRes), null, tint = tint, modifier = Modifier.size(18.dp))
                iconRes != null -> Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    contentScale = ContentScale.Fit,
                )
            }
            Text(label, style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold, color = tint)
        }
    }
}

@Composable
fun PropertyTxStatusSection(
    currentStatus: String,
    isSubmitting: Boolean,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("وضعیت معامله", style = AppTypography.labelSmall, fontWeight = FontWeight.SemiBold, color = DfColors.TextMuted)
            if (isSubmitting) {
                Text("در حال به‌روزرسانی…", style = AppTypography.labelSmall, color = DfColors.Purple)
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card,
            color = DfColors.Surface,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                CrmConstants.PROPERTY_TX_STATUSES.forEach { status ->
                    DfSheetOptionRow(
                        label = status,
                        selected = status == currentStatus,
                        onClick = { if (!isSubmitting && status != currentStatus) onStatusChange(status) },
                        icon = PropertyFilters.txStatusIcon(status),
                        trailing = if (status == currentStatus) "فعلی" else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyDetailInfoCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                when {
                    iconRes != null -> DfDecorImage(resId = iconRes, size = 16.dp)
                    icon != null -> Icon(icon, null, tint = DfColors.Purple, modifier = Modifier.size(16.dp))
                }
                Text(title, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
            }
            Text(body, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
        }
    }
}

@Composable
private fun PropertyDetailMetaCard(
    token: String,
    updatedLabel: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.SurfaceVariant.copy(alpha = 0.45f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("شناسه فایل", style = AppTypography.labelSmall, color = DfColors.TextMuted)
                Text(token, style = AppTypography.bodyDescription, fontWeight = FontWeight.Medium)
            }
            updatedLabel?.let {
                Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
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
