package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfComponentSpec
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize

@Composable
fun ListingDetailGallerySection(
    images: List<String>,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        ListingMosaicGallery(
            images = images,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.sm)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ListingDetailBackButton(onClick = onBack)
            Surface(
                onClick = onFavoriteToggle,
                shape = AppShapes.IconContainer,
                color = DfThemeColors.surface().copy(alpha = 0.92f),
                border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
                shadowElevation = AppElevations.subtle,
                modifier = Modifier.size(DfComponentSpec.MinTouchTargetDp.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = DfIcons.Heart,
                        contentDescription = if (isFavorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی",
                        tint = if (isFavorite) DfThemeColors.error() else DfThemeColors.textMuted(),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingDetailToolbar(
    title: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ListingDetailBackButton(onClick = onBack)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "جزئیات آگهی",
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
                color = DfThemeColors.textPrimary(),
                maxLines = 1,
            )
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Surface(
            onClick = onFavoriteToggle,
            shape = AppShapes.IconContainer,
            color = if (isFavorite) DfThemeColors.errorContainer() else DfThemeColors.surface(),
            border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
            shadowElevation = AppElevations.subtle,
            modifier = Modifier.size(DfComponentSpec.MinTouchTargetDp.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = DfIcons.Heart,
                    contentDescription = if (isFavorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی",
                    tint = if (isFavorite) DfThemeColors.error() else DfThemeColors.textMuted(),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingDetailBackButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = AppShapes.IconContainer,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
        modifier = Modifier.size(DfComponentSpec.MinTouchTargetDp.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = "بازگشت",
                tint = DfThemeColors.textPrimary(),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun ListingGalleryActionsRow(
    onEdit: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onNavigate: (() -> Unit)?,
    isAddedToPersonal: Boolean = false,
    onViewPersonalProperty: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        border = BorderStroke(1.dp, DfThemeColors.outlineSubtle()),
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xxs, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            GalleryActionChip(
                label = "ویرایش",
                icon = DfIcons.Pencil,
                tint = DfThemeColors.primary(),
                background = DfThemeColors.primaryContainer(),
                enabled = true,
                onClick = onEdit,
                modifier = Modifier.weight(1f),
            )
            GalleryActionChip(
                label = if (isAddedToPersonal) "مشاهده فایل شخصی" else "فایل شخصی",
                icon = DfIcons.Building,
                tint = DfThemeColors.warning(),
                background = DfThemeColors.warningContainer(),
                enabled = !isAddedToPersonal || onViewPersonalProperty != null,
                onClick = {
                    if (isAddedToPersonal) {
                        onViewPersonalProperty?.invoke()
                    } else {
                        onSaveAsPersonal()
                    }
                },
                modifier = Modifier.weight(1f),
            )
            GalleryActionChip(
                label = "دیوار",
                icon = DfIcons.ExternalLink,
                tint = DfThemeColors.info(),
                background = DfThemeColors.infoContainer(),
                enabled = onOpenDivar != null,
                onClick = { onOpenDivar?.invoke() },
                modifier = Modifier.weight(1f),
            )
            GalleryActionChip(
                label = "مسیریابی",
                icon = DfIcons.Compass,
                tint = DfThemeColors.success(),
                background = DfThemeColors.successContainer(),
                enabled = onNavigate != null,
                onClick = { onNavigate?.invoke() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryActionChip(
    label: String,
    icon: ImageVector,
    tint: Color,
    background: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedTint = if (enabled) tint else DfThemeColors.textMuted()
    val resolvedBackground = if (enabled) background else DfThemeColors.surfaceVariant()
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = resolvedBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = resolvedTint,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = resolvedTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
