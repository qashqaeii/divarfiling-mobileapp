package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfDecorSize
import ir.divarfiling.mobile.core.design.DivarFilingTheme

data class QuickAction(
    val label: String,
    val tint: Color,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    @DrawableRes val iconRes: Int? = null,
)

@Composable
fun QuickActionsRow(
    actions: List<QuickAction>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
    ) {
        items(actions, key = { it.label }) { action ->
            QuickActionItem(action)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionItem(action: QuickAction) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        modifier = Modifier
            .width(72.dp)
            .padding(vertical = AppSpacing.xxs),
    ) {
        Surface(
            onClick = action.onClick,
            shape = AppShapes.CardSmall,
            color = DfColors.Surface,
            shadowElevation = AppElevations.card,
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    action.iconRes != null -> DfDecorImage(
                        resId = action.iconRes,
                        size = DfDecorSize.Large,
                        contentDescription = action.label,
                    )
                    action.icon != null -> Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = action.tint,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
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
                QuickAction("یادآور جدید", DfColors.Pink, {}, iconRes = DfDecorIcons.StickyNote),
                QuickAction("مخاطب جدید", DfColors.Amber, {}, iconRes = DfDecorIcons.Upload),
                QuickAction("فایل‌ها", DfColors.Blue, {}, iconRes = DfDecorIcons.Layers),
                QuickAction("مخاطبین", DfColors.Purple, {}, iconRes = DfDecorIcons.ClipboardList),
            ),
        )
    }
}
