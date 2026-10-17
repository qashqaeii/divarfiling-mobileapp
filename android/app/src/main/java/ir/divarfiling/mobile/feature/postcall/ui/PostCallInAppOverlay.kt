package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.feature.postcall.PostCallEventHub

@Composable
fun PostCallInAppOverlay(
    eventHub: PostCallEventHub,
    onOpenContact: (Long) -> Unit,
) {
    val prompt by eventHub.active.collectAsStateWithLifecycle()
    val active = prompt ?: return
    if (active.phone.isBlank()) return

    PostCallBottomSheetHost(
        metadata = active.metadata,
        onDismissed = { eventHub.dismiss() },
        onOpenContact = onOpenContact,
    )
}
