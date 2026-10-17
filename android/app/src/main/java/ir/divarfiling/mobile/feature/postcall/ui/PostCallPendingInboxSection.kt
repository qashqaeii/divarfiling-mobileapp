package ir.divarfiling.mobile.feature.postcall.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dagger.hilt.android.EntryPointAccessors
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.feature.postcall.PostCallAssistantActivity
import ir.divarfiling.mobile.feature.postcall.PostCallEntryPoint
import ir.divarfiling.mobile.feature.postcall.PostCallFeatureFlags
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.PostCallUiCopy
import ir.divarfiling.mobile.feature.postcall.PendingPostCall

@Composable
fun PostCallPendingInboxSection(modifier: Modifier = Modifier) {
    if (!PostCallFeatureFlags.PENDING_INBOX_ENABLED) return
    val context = LocalContext.current
    val store = remember(context) {
        EntryPointAccessors.fromApplication(context, PostCallEntryPoint::class.java).pendingCallStore()
    }
    val items by store.items.collectAsState()
    if (items.isEmpty()) return

    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                "تماس‌های نیازمند ثبت",
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "تماس‌هایی که بدون ثبت نتیجه بسته شده‌اند.",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
            )
            items.take(5).forEach { item ->
                PendingCallRow(item = item, onComplete = {
                    val meta = store.toMetadata(item) ?: return@PendingCallRow
                    val intent = PostCallAssistantActivity.intent(context, meta, fromNotification = false)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                })
            }
        }
    }
}

@Composable
private fun PendingCallRow(item: PendingPostCall, onComplete: () -> Unit) {
    val kind = runCatching { PostCallLogic.CallEndKind.valueOf(item.callEndKind) }.getOrNull()
    val kindLabel = kind?.let { PostCallUiCopy.callKindShort(it) } ?: "تماس"
    val title = item.contactName?.takeIf { it.isNotBlank() } ?: item.phone
    val timeLabel = item.callLogDateMs?.let { DateUtils.formatTimeFromMillis(it) }
        ?: DateUtils.formatTimeFromMillis(item.createdAtMs)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onComplete),
        shape = RoundedCornerShape(14.dp),
        color = DfThemeColors.surfaceVariant().copy(alpha = 0.35f),
    ) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Text(title, style = AppTypography.labelLarge, fontWeight = FontWeight.Medium)
            Text(
                "$kindLabel · $timeLabel",
                style = AppTypography.labelSmall,
                color = DfThemeColors.textSecondary(),
                modifier = Modifier.padding(top = AppSpacing.xxs),
            )
            Text(
                "تکمیل ثبت",
                style = AppTypography.labelSmall,
                color = DfColors.Purple,
                modifier = Modifier.padding(top = AppSpacing.xs),
            )
        }
    }
}
