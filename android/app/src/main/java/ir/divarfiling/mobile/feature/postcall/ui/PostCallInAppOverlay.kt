package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.feature.postcall.PostCallEventHub
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCallInAppOverlay(
    eventHub: PostCallEventHub,
    onOpenContact: (Long) -> Unit,
) {
    val prompt by eventHub.active.collectAsStateWithLifecycle()
    val active = prompt ?: return
    if (active.phone.isBlank()) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { eventHub.dismiss() },
        sheetState = sheetState,
    ) {
        PostCallSheetContent(
            metadata = active.metadata,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    eventHub.dismiss()
                }
            },
            onOpenContact = onOpenContact,
        )
    }
}
