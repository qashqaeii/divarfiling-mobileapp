package ir.divarfiling.mobile.feature.postcall.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallViewModel
import kotlinx.coroutines.launch

/**
 * مسیر واحد خروج کاربر: Back، Swipe، لمس بیرون و دکمه بستن همگی [onUserDismiss] را صدا می‌زنند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCallBottomSheetHost(
    metadata: PostCallCallMetadata,
    onDismissed: () -> Unit,
    onOpenContact: (Long) -> Unit,
    viewModel: PostCallViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val dismissFromUser: () -> Unit = {
        viewModel.onDismissWithoutSave()
        scope.launch {
            sheetState.hide()
            onDismissed()
        }
    }

    BackHandler(onBack = dismissFromUser)

    ModalBottomSheet(
        onDismissRequest = dismissFromUser,
        sheetState = sheetState,
    ) {
        PostCallSheetContent(
            metadata = metadata,
            onDismiss = dismissFromUser,
            onOpenContact = onOpenContact,
            viewModel = viewModel,
        )
    }
}
