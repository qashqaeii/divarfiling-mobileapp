package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfAsyncImage
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.filing.ListingImageUtils
import ir.divarfiling.mobile.core.network.ListingDetailDto
import ir.divarfiling.mobile.core.network.PropertyFolderDto
import ir.divarfiling.mobile.feature.crm.components.PropertyFolderIconBadge
import ir.divarfiling.mobile.feature.crm.components.ZonkanBrandIcon

@Composable
fun ListingPersonalFolderPickerSheet(
    listing: ListingDetailDto,
    folders: List<PropertyFolderDto>,
    searchQuery: String,
    isLoadingFolders: Boolean,
    foldersError: String?,
    selectedFolderId: Long?,
    choiceExplicit: Boolean,
    withoutFolderSelected: Boolean,
    showCreateForm: Boolean,
    newFolderName: String,
    isCreatingFolder: Boolean,
    createFolderError: String?,
    isSubmitting: Boolean,
    transferError: String?,
    folderAssignFailed: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectFolder: (Long) -> Unit,
    onSelectWithoutFolder: () -> Unit,
    onOpenCreateForm: () -> Unit,
    onDismissCreateForm: () -> Unit,
    onNewFolderNameChange: (String) -> Unit,
    onCreateFolder: () -> Unit,
    onRetryLoadFolders: () -> Unit,
    onConfirm: () -> Unit,
    onRetryFolderAssign: () -> Unit,
    onDismiss: () -> Unit,
) {
    val filteredFolders = folders.filter { folder ->
        if (searchQuery.isBlank()) return@filter true
        val q = searchQuery.trim()
        folder.name.contains(q, ignoreCase = true) ||
            folder.cabinet?.name?.contains(q, ignoreCase = true) == true
    }
    val grouped = filteredFolders.groupBy { folder ->
        folder.cabinet?.name?.takeIf { it.isNotBlank() } ?: "بدون کمد"
    }
    val canConfirm = choiceExplicit && !isLoadingFolders && !isCreatingFolder
    val folderSelectionEnabled = !folderAssignFailed
    val primaryText = when {
        isSubmitting -> "لطفاً صبر کنید…"
        folderAssignFailed -> "تلاش مجدد برای ثبت در زونکن"
        else -> "انتقال به فایل‌های شخصی"
    }

    DfSheetScaffold(
        title = "انتقال به فایل‌های شخصی",
        subtitle = "زونکن مقصد این فایل را انتخاب کنید.",
        icon = DfIcons.Building,
        onClose = if (!isSubmitting) onDismiss else null,
        bodyHeightFraction = 0.72f,
        footer = {
            DfSheetActions(
                primaryText = primaryText,
                onPrimary = if (folderAssignFailed) onRetryFolderAssign else onConfirm,
                primaryEnabled = if (folderAssignFailed) !isSubmitting else canConfirm,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        ListingTransferSummaryRow(listing = listing)

        transferError?.takeIf { it.isNotBlank() }?.let { message ->
            DfErrorBanner(message)
        }

        if (showCreateForm) {
            PersonalFolderCreateInlineForm(
                name = newFolderName,
                isSubmitting = isCreatingFolder,
                error = createFolderError,
                onNameChange = onNewFolderNameChange,
                onSubmit = onCreateFolder,
                onCancel = onDismissCreateForm,
            )
        } else {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = { Text("جست‌وجوی زونکن") },
                placeholder = { Text("نام زونکن یا کمد") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSubmitting && !isLoadingFolders && folderSelectionEnabled,
            )

            WithoutFolderOptionRow(
                selected = withoutFolderSelected,
                enabled = !isSubmitting && folderSelectionEnabled,
                onClick = onSelectWithoutFolder,
            )

            when {
                isLoadingFolders -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.lg),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp,
                            color = DfThemeColors.primary(),
                        )
                    }
                }
                foldersError != null -> {
                    DfErrorBanner(foldersError)
                    DfSecondaryButton(
                        text = "تلاش مجدد",
                        onClick = onRetryLoadFolders,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                filteredFolders.isEmpty() && folders.isEmpty() -> {
                    DfEmptyState(
                        title = "هنوز زونکنی ندارید",
                        subtitle = "می‌توانید زونکن بسازید یا بدون زونکن منتقل کنید.",
                        variant = DfEmptyVariant.Empty,
                        actionLabel = "ساخت زونکن",
                        onAction = onOpenCreateForm,
                    )
                }
                filteredFolders.isEmpty() -> {
                    Text(
                        text = "زونکنی با این جست‌وجو پیدا نشد.",
                        style = AppTypography.bodyDescription,
                        color = DfColors.TextMuted,
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    ) {
                        grouped.forEach { (cabinetLabel, cabinetFolders) ->
                            if (grouped.size > 1) {
                                Text(
                                    text = cabinetLabel,
                                    style = AppTypography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DfColors.TextMuted,
                                    modifier = Modifier.padding(top = AppSpacing.xs, bottom = 2.dp),
                                )
                            }
                            cabinetFolders.forEach { folder ->
                                PersonalFolderSelectRow(
                                    folder = folder,
                                    selected = choiceExplicit && selectedFolderId == folder.id,
                                    enabled = !isSubmitting && folderSelectionEnabled,
                                    onClick = { onSelectFolder(folder.id) },
                                )
                            }
                        }
                    }
                }
            }

            if (folderSelectionEnabled) {
                DfGlassTextButton(
                    text = "+ زونکن جدید",
                    onClick = onOpenCreateForm,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ListingTransferSummaryRow(listing: ListingDetailDto) {
    val thumb = ListingImageUtils.buildGalleryUrls(listing).firstOrNull()
    val meta = buildList {
        listing.area?.takeIf { it > 0 }?.let { add("${it.toInt()} متر") }
        when {
            listing.rent != null || listing.deposit != null -> {
                listing.deposit?.takeIf { it > 0 }?.let { add("رهن ${FormatUtils.formatPriceShort(it)}") }
                listing.rent?.takeIf { it > 0 }?.let { add("اجاره ${FormatUtils.formatPriceShort(it)}") }
            }
            listing.price?.takeIf { it > 0 } != null -> {
                add(FormatUtils.formatPriceShort(listing.price!!))
            }
        }
    }.joinToString(" · ")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!thumb.isNullOrBlank()) {
            DfAsyncImage(
                url = thumb,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                shape = AppShapes.CardSmall,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = listing.title?.takeIf { it.isNotBlank() } ?: "فایل شخصی",
                style = AppTypography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = DfColors.TextPrimary,
            )
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    HorizontalDivider(color = DfColors.OutlineSubtle)
}

@Composable
private fun WithoutFolderOptionRow(
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) DfThemeColors.primary() else DfColors.OutlineSubtle
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.CardSmall,
        color = if (selected) DfThemeColors.primaryContainer().copy(alpha = 0.35f) else DfColors.SurfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ZonkanBrandIcon(size = 28.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "بدون زونکن — فقط در فایل‌های شخصی",
                    style = AppTypography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = DfColors.TextPrimary,
                )
            }
            if (selected) {
                Icon(
                    imageVector = DfIcons.Check,
                    contentDescription = null,
                    tint = DfThemeColors.primary(),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun PersonalFolderSelectRow(
    folder: PropertyFolderDto,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = parseFolderColorHex(folder.color)
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.CardSmall,
        color = if (selected) color.copy(alpha = 0.12f) else DfColors.SurfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, if (selected) color.copy(alpha = 0.55f) else DfColors.OutlineSubtle),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PropertyFolderIconBadge(
                faIcon = folder.icon,
                color = color,
                size = 36.dp,
                iconSize = 16.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = buildString {
                    folder.cabinet?.name?.takeIf { it.isNotBlank() }?.let {
                        append(it)
                        append(" · ")
                    }
                    append("${folder.propertyCount} فایل")
                }
                Text(
                    text = subtitle,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                Icon(
                    imageVector = DfIcons.Check,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun PersonalFolderCreateInlineForm(
    name: String,
    isSubmitting: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    DfSheetSection(title = "زونکن جدید") {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("نام زونکن") },
            placeholder = { Text("مثلاً آپارتمان‌های شمال") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isSubmitting,
        )
        error?.takeIf { it.isNotBlank() }?.let { DfErrorBanner(it) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            DfPrimaryButton(
                text = if (isSubmitting) "در حال ساخت…" else "ساخت زونکن",
                onClick = onSubmit,
                enabled = !isSubmitting && name.trim().isNotBlank(),
                loading = isSubmitting,
                modifier = Modifier.weight(1f),
            )
            DfGlassTextButton(text = "بازگشت", onClick = onCancel)
        }
    }
}

private fun parseFolderColorHex(hex: String): Color {
    val cleaned = hex.trim().removePrefix("#")
    if (cleaned.length != 6) return DfColors.Purple
    return try {
        Color(
            red = cleaned.substring(0, 2).toInt(16) / 255f,
            green = cleaned.substring(2, 4).toInt(16) / 255f,
            blue = cleaned.substring(4, 6).toInt(16) / 255f,
        )
    } catch (_: Exception) {
        DfColors.Purple
    }
}
