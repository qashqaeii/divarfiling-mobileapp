package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfMoreAction
import ir.divarfiling.mobile.core.design.components.DfMoreActionsSheet
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@Composable
fun ListingQuickActionsRow(
    onSendToContact: () -> Unit,
    onShare: () -> Unit,
    onOwnerPhone: () -> Unit,
    onWhatsAppShare: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onSetReminder: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    onOpenAi: (() -> Unit)? = null,
    showSaveAsPersonal: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var showMore by remember { mutableStateOf(false) }
    val visible = listOf(
        QuickActionSpec(
            label = "ارسال",
            icon = DfIcons.UserPlus,
            tint = DfColors.Purple,
            background = DfColors.PurpleContainer,
            onClick = onSendToContact,
        ),
        QuickActionSpec(
            label = "اشتراک",
            icon = DfIcons.Share2,
            tint = DfColors.TextSecondary,
            background = DfColors.SurfaceVariant,
            onClick = onShare,
        ),
        QuickActionSpec(
            label = "تلفن مالک",
            icon = DfIcons.Phone,
            tint = DfColors.TextSecondary,
            background = DfColors.SurfaceVariant,
            onClick = onOwnerPhone,
        ),
        QuickActionSpec(
            label = "بیشتر",
            icon = DfIcons.MoreVertical,
            tint = DfColors.TextSecondary,
            background = DfColors.SurfaceVariant,
            onClick = { showMore = true },
        ),
    )
    val moreActions = buildList {
        add(DfMoreAction("واتساپ", onWhatsAppShare, DfIcons.MessageCircle))
        add(DfMoreAction("یادآور", onSetReminder, DfIcons.Bell))
        if (showSaveAsPersonal) add(DfMoreAction("ذخیره شخصی", onSaveAsPersonal, DfIcons.ClipboardList))
        onOpenAi?.let { add(DfMoreAction("دستیار AI", it, DfIcons.Sparkles)) }
        onOpenDivar?.let { add(DfMoreAction("مشاهده در دیوار", it, DfIcons.ExternalLink)) }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        shape = AppShapes.Card,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            visible.forEach { action ->
                ListingQuickActionButton(
                    label = action.label,
                    icon = action.icon,
                    iconRes = action.iconRes,
                    tintIconRes = action.tintIconRes,
                    tint = action.tint,
                    background = action.background,
                    onClick = action.onClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    DfMoreActionsSheet(
        visible = showMore,
        onDismiss = { showMore = false },
        actions = moreActions,
    )
}

private data class QuickActionSpec(
    val label: String,
    val tint: Color,
    val background: Color,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
    val tintIconRes: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingQuickActionButton(
    label: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    tintIconRes: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.padding(horizontal = 2.dp),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = background,
            shadowElevation = 0.dp,
            modifier = Modifier.size(44.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    icon != null -> Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(22.dp),
                    )
                    iconRes != null && tintIconRes -> Icon(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(22.dp),
                    )
                    iconRes != null -> Image(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(22.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
        Text(
            text = label,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DfColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ListingQuickActionsRowPreview() {
    DivarFilingTheme {
        ListingQuickActionsRow(
            onSendToContact = {},
            onShare = {},
            onOwnerPhone = {},
            onWhatsAppShare = {},
            onOpenDivar = {},
            onSetReminder = {},
            onSaveAsPersonal = {},
        )
    }
}
