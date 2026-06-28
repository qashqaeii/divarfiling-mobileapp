package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfGlassButtonVariant
import ir.divarfiling.mobile.core.design.components.liquidGlassSurface

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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlassSurface(shape = AppShapes.Hero, elevation = 8.dp)
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                rowActions.forEach { action ->
                    CrmQuickActionItem(
                        action = action,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowActions.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
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
            .heightIn(min = 92.dp)
            .liquidGlassSurface(
                shape = AppShapes.CardSmall,
                variant = DfGlassButtonVariant.Secondary,
                elevation = 4.dp,
            )
            .clickable(onClick = action.onClick)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .liquidGlassSurface(
                    shape = AppShapes.IconContainer,
                    variant = DfGlassButtonVariant.Accent,
                    accent = DfColors.Purple,
                    elevation = 3.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = DfColors.Purple,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = action.title,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = DfColors.TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = action.subtitle,
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.Normal,
            color = DfColors.TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
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
