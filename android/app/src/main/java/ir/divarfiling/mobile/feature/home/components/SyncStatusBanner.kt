package ir.divarfiling.mobile.feature.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone

@Composable
fun SyncStatusBanner(
    isSyncing: Boolean,
    pendingCount: Int,
    modifier: Modifier = Modifier,
) {
    if (!isSyncing && pendingCount == 0) return

    if (isSyncing) {
        DfStatusBanner(
            message = "در حال همگام‌سازی…",
            tone = DfStatusTone.Info,
            modifier = modifier,
        )
    } else {
        DfStatusBanner(
            message = "$pendingCount عملیات در صف آفلاین",
            tone = DfStatusTone.Warning,
            title = "آفلاین",
            modifier = modifier,
        )
    }
}
