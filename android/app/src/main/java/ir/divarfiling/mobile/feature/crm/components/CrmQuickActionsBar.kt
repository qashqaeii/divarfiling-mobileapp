package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class CrmQuickAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun CrmQuickActionsBar(
    actions: List<CrmQuickAction>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(AppElevations.card, AppShapes.Card, ambientColor = DfColors.Shadow),
        shape = AppShapes.Card,
        color = DfColors.Surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(horizontal = AppSpacing.xxs, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEachIndexed { index, action ->
                if (index > 0) {
                    VerticalDivider(
                        modifier = Modifier.height(48.dp),
                        color = DfColors.OutlineSubtle,
                        thickness = 1.dp,
                    )
                }
                CrmQuickActionItem(
                    action = action,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CrmQuickActionItem(
    action: CrmQuickAction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clickable(onClick = action.onClick)
            .padding(horizontal = AppSpacing.xxs, vertical = AppSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .padding(bottom = 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = AppShapes.IconContainer,
                color = DfColors.PurpleContainer,
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = DfColors.Purple,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Text(
            text = action.title,
            style = AppTypography.bottomNav,
            fontWeight = FontWeight.SemiBold,
            color = DfColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            text = action.subtitle,
            style = AppTypography.labelSmall,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CrmQuickActionsBarPreview() {
    DivarFilingTheme {
        CrmQuickActionsBar(
            actions = listOf(
                CrmQuickAction("فیلتر پیشرفته", "جستجوی دقیق", DfIcons.Filter) {},
                CrmQuickAction("یادداشت سریع", "ثبت یادداشت", DfIcons.File) {},
                CrmQuickAction("یادآور جدید", "تنظیم یادآور", DfIcons.AlarmClock) {},
                CrmQuickAction("مخاطب جدید", "افزودن سریع", DfIcons.UserPlus) {},
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
