package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.network.PostCallContactBriefDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallEntryPoint
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.PostCallUiMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PostCallCompactOverlayContent(
    metadata: PostCallCallMetadata,
    onDismiss: () -> Unit,
    onExpand: () -> Unit,
) {
    val phone = metadata.phone
    val context = LocalContext.current
    val crm = remember(context) {
        EntryPointAccessors.fromApplication(context, PostCallEntryPoint::class.java).crmRepository()
    }
    var resolveAttempt by remember(phone) { mutableIntStateOf(0) }
    var mode by remember(phone) { mutableStateOf(PostCallUiMode.Loading) }
    var contact by remember(phone) { mutableStateOf<PostCallContactBriefDto?>(null) }
    var displayName by remember(phone) { mutableStateOf<String?>(null) }

    LaunchedEffect(phone, resolveAttempt) {
        mode = PostCallUiMode.Loading
        val result = withContext(Dispatchers.IO) { crm.resolvePostCall(phone) }
        when (result) {
            is ApiResult.Success -> {
                mode = PostCallLogic.resolveStatusToMode(result.data.status)
                contact = result.data.contact
                displayName = result.data.contact?.fullName
            }
            is ApiResult.Error -> mode = PostCallUiMode.Error
        }
    }

    PostCallCompactCard(
        metadata = metadata,
        mode = mode,
        contact = contact,
        displayName = displayName,
        onDismiss = onDismiss,
        onPrimaryAction = onExpand,
        onRetryResolve = { resolveAttempt++ },
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
    )
}
