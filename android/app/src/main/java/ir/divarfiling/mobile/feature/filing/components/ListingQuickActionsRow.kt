package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.R
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme

@Composable
fun ListingQuickActionsRow(
    onSendToContact: () -> Unit,
    onWhatsAppShare: () -> Unit,
    onOpenDivar: (() -> Unit)?,
    onSetReminder: () -> Unit,
    onSaveAsPersonal: () -> Unit,
    showSaveAsPersonal: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val actions = buildList {
        if (showSaveAsPersonal) {
            add(
                QuickActionSpec(
                    label = "شخصی",
                    icon = DfIcons.Building,
                    tint = DfColors.Amber,
                    background = DfColors.AmberLight,
                    onClick = onSaveAsPersonal,
                ),
            )
        }
        add(
            QuickActionSpec(
                label = "ارسال",
                icon = DfIcons.UserPlus,
                tint = DfColors.Purple,
                background = DfColors.PurpleContainer,
                onClick = onSendToContact,
            ),
        )
        add(
            QuickActionSpec(
                label = "واتساپ",
                iconRes = R.drawable.ic_whatsapp,
                tint = DfColors.Green,
                background = DfColors.GreenLight,
                onClick = onWhatsAppShare,
            ),
        )
        onOpenDivar?.let { openDivar ->
            add(
                QuickActionSpec(
                    label = "دیوار",
                    icon = DfIcons.ExternalLink,
                    tint = DfColors.Blue,
                    background = DfColors.BlueLight,
                    onClick = openDivar,
                ),
            )
        }
        add(
            QuickActionSpec(
                label = "یادآور",
                icon = DfIcons.Bell,
                tint = DfColors.PurpleDark,
                background = DfColors.PurpleLight,
                onClick = onSetReminder,
            ),
        )
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
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { action ->
                ListingQuickActionButton(
                    label = action.label,
                    icon = action.icon,
                    iconRes = action.iconRes,
                    tint = action.tint,
                    background = action.background,
                    onClick = action.onClick,
                )
            }
        }
    }
}

private data class QuickActionSpec(
    val label: String,
    val tint: Color,
    val background: Color,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val iconRes: Int? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingQuickActionButton(
    label: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconRes: Int? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 2.dp),
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = background,
            shadowElevation = 0.dp,
            modifier = Modifier.size(52.dp),
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
                    iconRes != null -> Icon(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(22.dp),
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
            onWhatsAppShare = {},
            onOpenDivar = {},
            onSetReminder = {},
            onSaveAsPersonal = {},
        )
    }
}
