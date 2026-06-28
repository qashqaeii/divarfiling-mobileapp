package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsActionBar(
    onNewDeal: () -> Unit,
    onContactsClick: () -> Unit,
    onSalesStagesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DealsActionButton(
            label = "معامله جدید",
            icon = DfIcons.Plus,
            filled = true,
            onClick = onNewDeal,
        )
        DealsActionButton(
            label = "مخاطبین",
            icon = DfIcons.Users,
            onClick = onContactsClick,
        )
        DealsActionButton(
            label = "مراحل فروش",
            icon = DfIcons.Filter,
            onClick = onSalesStagesClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DealsActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    filled: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.CardSmall,
        color = if (filled) DfColors.Purple else DfColors.Surface,
        shadowElevation = if (filled) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (filled) Color.White else DfColors.TextSecondary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = label,
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (filled) Color.White else DfColors.TextPrimary,
                maxLines = 1,
            )
        }
    }
}
