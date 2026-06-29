package ir.divarfiling.mobile.feature.crm.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfDecorIcons

data class CrmQuickAction(
    val title: String,
    val onClick: () -> Unit,
    val tint: Color = DfColors.Purple,
    val background: Color = DfColors.PurpleContainer,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
)

@Composable
fun CrmQuickActionsBar(
    actions: List<CrmQuickAction>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        actions.forEach { action ->
            CrmQuickActionItem(
                action = action,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrmQuickActionItem(
    action: CrmQuickAction,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = action.onClick,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        color = DfColors.Surface,
        shadowElevation = AppElevations.subtle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = action.background,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    when {
                        action.iconRes != null -> Image(
                            painter = painterResource(action.iconRes),
                            contentDescription = action.title,
                            modifier = Modifier.size(20.dp),
                            contentScale = ContentScale.Fit,
                        )
                        action.icon != null -> Icon(
                            imageVector = action.icon,
                            contentDescription = action.title,
                            tint = action.tint,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
            Text(
                text = action.title,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DfColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CrmQuickActionsBarPreview() {
    DivarFilingTheme {
        CrmQuickActionsBar(
            actions = listOf(
                CrmQuickAction("فیلتر", icon = DfIcons.Filter, onClick = {}, tint = DfColors.Purple, background = DfColors.PurpleContainer),
                CrmQuickAction("یادداشت", iconRes = DfDecorIcons.StickyNote, onClick = {}, tint = DfColors.Blue, background = DfColors.BlueLight),
                CrmQuickAction("یادآور", iconRes = DfDecorIcons.Upload, onClick = {}, tint = DfColors.Amber, background = DfColors.AmberLight),
                CrmQuickAction("مخاطب", iconRes = DfDecorIcons.ClipboardList, onClick = {}, tint = DfColors.Green, background = DfColors.GreenLight),
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
