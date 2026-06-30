package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage

@Composable
fun ListingDetailGallerySection(
    images: List<String>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    onCopyLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ListingDetailBackButton(onClick = onBack)
        }

        ListingGalleryActionsRow(
            onEdit = onEdit,
            onSaveAsPersonal = onSaveAsPersonal,
            onCopyLink = onCopyLink,
            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
        )

        ListingMosaicGallery(
            images = images,
            modifier = Modifier.padding(top = AppSpacing.xxs),
        )
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
    onCopyLink: () -> Unit,
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
                icon = DfIcons.SlidersHorizontal,
                tint = DfColors.Purple,
                background = DfColors.PurpleContainer,
                onClick = onEdit,
                modifier = Modifier.weight(1f),
            )
            GalleryActionChip(
                label = "فایل شخصی",
                iconRes = DfDecorIcons.Building,
                tint = DfColors.Amber,
                background = DfColors.AmberLight,
                onClick = onSaveAsPersonal,
                modifier = Modifier.weight(1f),
            )
            GalleryActionChip(
                label = "کپی لینک",
                iconRes = DfDecorIcons.Copy,
                tint = DfColors.Blue,
                background = DfColors.BlueLight,
                onClick = onCopyLink,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryActionChip(
    label: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            when {
                iconRes != null -> DfDecorImage(resId = iconRes, size = 18.dp)
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
