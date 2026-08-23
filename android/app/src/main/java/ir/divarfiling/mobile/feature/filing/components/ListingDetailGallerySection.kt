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
import androidx.compose.material3.Text
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
import ir.divarfiling.mobile.core.design.DfIcons

@Composable
fun ListingDetailGallerySection(
    images: List<String>,
    title: String,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onEdit: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onNavigate: (() -> Unit)?,
    modifier: Modifier = Modifier,
    quickActions: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        ListingDetailToolbar(
            title = title,
            isFavorite = isFavorite,
            onBack = onBack,
            onFavoriteToggle = onFavoriteToggle,
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )

        ListingGalleryActionsRow(
            onEdit = onEdit,
            onSaveAsPersonal = onSaveAsPersonal,
            onOpenDivar = onOpenDivar,
            onNavigate = onNavigate,
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )

        quickActions()

        ListingMosaicGallery(
            images = images,
            modifier = Modifier.padding(top = AppSpacing.xxs),
        )
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
                color = DfColors.TextPrimary,
                maxLines = 1,
            )
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    style = AppTypography.labelSmall,
                    color = DfColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Surface(
            onClick = onFavoriteToggle,
            shape = AppShapes.IconContainer,
            color = if (isFavorite) DfColors.RoseLight else DfColors.Surface,
            shadowElevation = AppElevations.subtle,
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = DfIcons.Heart,
                    contentDescription = if (isFavorite) "حذف از علاقه‌مندی" else "افزودن به علاقه‌مندی",
                    tint = if (isFavorite) DfColors.Rose else DfColors.TextMuted,
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
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = DfIcons.ChevronLeft,
                contentDescription = "بازگشت",
                tint = DfColors.TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ListingGalleryActionsRow(
    onEdit: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onNavigate: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            GalleryActionChip(
                label = "ویرایش",
                icon = DfIcons.Pencil,
                tint = DfColors.Purple,
                background = DfColors.PurpleContainer,
                enabled = true,
                onClick = onEdit,
                modifier = Modifier.weight(1f),
            )
            GalleryActionChip(
                label = "فایل شخصی",
                icon = DfIcons.Building,
                tint = DfColors.Amber,
                background = DfColors.AmberLight,
                enabled = true,
                onClick = onSaveAsPersonal,
                modifier = Modifier.weight(1f),
            )
            GalleryActionChip(
                label = "دیوار",
                icon = DfIcons.ExternalLink,
                tint = DfColors.Blue,
                background = DfColors.BlueLight,
                enabled = onOpenDivar != null,
                onClick = { onOpenDivar?.invoke() },
                modifier = Modifier.weight(1f),
            )
            GalleryActionChip(
                label = "مسیریابی",
                icon = DfIcons.Compass,
                tint = DfColors.Green,
                background = DfColors.GreenLight,
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
    val resolvedTint = if (enabled) tint else DfColors.TextMuted
    val resolvedBackground = if (enabled) background else DfColors.SurfaceVariant
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
