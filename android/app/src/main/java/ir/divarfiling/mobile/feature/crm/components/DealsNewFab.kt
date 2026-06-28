package ir.divarfiling.mobile.feature.crm.components

import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfExtendedFab

@androidx.compose.runtime.Composable
fun DealsNewFab(
    onClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    DfExtendedFab(
        text = "معامله جدید",
        icon = DfIcons.Plus,
        onClick = onClick,
        modifier = modifier,
    )
}
