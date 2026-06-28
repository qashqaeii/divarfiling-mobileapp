package ir.divarfiling.mobile.feature.filing.components

import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfExtendedFab

@androidx.compose.runtime.Composable
fun FilingExtractFab(
    onClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    DfExtendedFab(
        text = "استخراج جدید",
        icon = DfIcons.Plus,
        onClick = onClick,
        modifier = modifier,
    )
}
