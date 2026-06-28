package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfActionButton

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
    ) {
        DfActionButton(
            text = "معامله جدید",
            icon = DfIcons.Plus,
            filled = true,
            onClick = onNewDeal,
        )
        DfActionButton(
            text = "مخاطبین",
            icon = DfIcons.Users,
            onClick = onContactsClick,
        )
        DfActionButton(
            text = "مراحل فروش",
            icon = DfIcons.Filter,
            onClick = onSalesStagesClick,
        )
    }
}
