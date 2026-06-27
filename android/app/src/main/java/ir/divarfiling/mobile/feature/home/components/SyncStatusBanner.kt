package ir.divarfiling.mobile.feature.home.components

import ir.divarfiling.mobile.core.design.DfColors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.DfShapes

@Composable
fun SyncStatusBanner(
    isSyncing: Boolean,
    pendingCount: Int,
    modifier: Modifier = Modifier,
) {
    if (!isSyncing && pendingCount == 0) return

    val (bg, fg, message) = when {
        isSyncing -> Triple(
            DfColors.BlueLight,
            DfColors.Blue,
            "در حال همگام‌سازی…",
        )
        else -> Triple(
            DfColors.AmberLight,
            DfColors.Amber,
            "$pendingCount عملیات در صف آفلاین",
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = DfShapes.Chip,
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = fg,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = fg,
            )
        }
    }
}
