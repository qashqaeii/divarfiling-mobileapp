package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme

data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
    val onClick: () -> Unit,
)

@Composable
fun QuickActionsRow(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        actions.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                row.forEach { action ->
                    QuickActionItem(action, modifier = Modifier.weight(1f))
                }
                repeat(3 - row.size) {
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionItem(action: QuickAction, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.titleSubtitleGap),
        modifier = modifier.padding(vertical = AppSpacing.xxs),
    ) {
        Surface(
            onClick = action.onClick,
            shape = AppShapes.IconContainer,
            color = action.background,
            shadowElevation = AppElevations.subtle,
            modifier = Modifier.size(52.dp),
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = action.tint,
                modifier = Modifier
                    .padding(AppSpacing.sm)
                    .size(26.dp),
            )
        }
        Text(
            text = action.label,
            style = AppTypography.bottomNav,
            color = DfColors.TextSecondary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun QuickActionsRowPreview() {
    DivarFilingTheme {
        QuickActionsRow(
            actions = listOf(
                QuickAction("تحلیل", DfIcons.TrendingDown, DfColors.Green, DfColors.GreenLight) {},
                QuickAction("مخاطبین", DfIcons.Users, DfColors.Purple, DfColors.PurpleContainer) {},
                QuickAction("فایل‌ها", DfIcons.Folder, DfColors.Blue, DfColors.BlueLight) {},
                QuickAction("مخاطب جدید", DfIcons.Plus, DfColors.Amber, DfColors.AmberLight) {},
                QuickAction("یادآور", DfIcons.Bell, DfColors.Pink, DfColors.PinkLight) {},
            ),
        )
    }
}
