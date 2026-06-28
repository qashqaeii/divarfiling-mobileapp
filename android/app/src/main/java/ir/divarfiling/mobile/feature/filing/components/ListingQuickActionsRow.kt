package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

@Composable
fun ListingQuickActionsRow(
    onSendToContact: () -> Unit,
    onWhatsAppShare: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onSetReminder: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        ListingQuickActionCard(
            label = "فایل شخصی",
            icon = DfIcons.Building,
            background = DfColors.AmberLight,
            iconTint = DfColors.Amber,
            onClick = onSaveAsPersonal,
            modifier = Modifier.weight(1f),
        )
        ListingQuickActionCard(
            label = "ارسال به مخاطب",
            icon = DfIcons.UserPlus,
            background = DfColors.PurpleContainer,
            iconTint = DfColors.Purple,
            onClick = onSendToContact,
            modifier = Modifier.weight(1f),
        )
        ListingQuickActionCard(
            label = "اشتراک واتساپ",
            iconRes = R.drawable.ic_whatsapp,
            background = DfColors.GreenLight,
            iconTint = DfColors.Green,
            onClick = onWhatsAppShare,
            modifier = Modifier.weight(1f),
        )
        if (onOpenDivar != null) {
            ListingQuickActionCard(
                label = "مشاهده در دیوار",
                icon = DfIcons.ExternalLink,
                background = DfColors.BlueLight,
                iconTint = DfColors.Blue,
                onClick = onOpenDivar,
                modifier = Modifier.weight(1f),
            )
        }
        ListingQuickActionCard(
            label = "ثبت یادآور",
            icon = DfIcons.Bell,
            background = DfColors.PurpleLight,
            iconTint = DfColors.PurpleDark,
            onClick = onSetReminder,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingQuickActionCard(
    label: String,
    onClick: () -> Unit,
    background: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
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
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
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
                fontWeight = FontWeight.SemiBold,
                color = DfColors.TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
