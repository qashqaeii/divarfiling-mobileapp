package ir.divarfiling.mobile.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfShapes
import ir.divarfiling.mobile.core.design.DfSpacing
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
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = DfSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(DfSpacing.md),
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Surface(
            onClick = action.onClick,
            shape = DfShapes.CardSmall,
            color = action.background,
            shadowElevation = 1.dp,
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = action.tint,
                modifier = Modifier
                    .padding(14.dp)
                    .size(28.dp),
            )
        }
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelSmall,
            color = DfColors.TextSecondary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun QuickActionsRowPreview() {
    DivarFilingTheme {
        QuickActionsRow(
            actions = listOf(
                QuickAction("نقشه", DfIcons.MapPin, DfColors.Green, DfColors.GreenLight) {},
                QuickAction("مخاطبین", DfIcons.Users, DfColors.Purple, DfColors.PurpleContainer) {},
            ),
        )
    }
}
