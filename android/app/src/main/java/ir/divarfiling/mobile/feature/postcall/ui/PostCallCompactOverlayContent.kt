package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dagger.hilt.android.EntryPointAccessors
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.network.PostCallContactBriefDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.feature.postcall.PostCallEntryPoint
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
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
    val callEndKind = metadata.callEndKind
    val context = LocalContext.current
    val crm = remember(context) {
        EntryPointAccessors.fromApplication(context, PostCallEntryPoint::class.java).crmRepository()
    }
    var mode by remember { mutableStateOf(PostCallUiMode.Loading) }
    var contact by remember { mutableStateOf<PostCallContactBriefDto?>(null) }
    var displayName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(phone) {
        mode = PostCallUiMode.Loading
        val result = withContext(Dispatchers.IO) { crm.resolvePostCall(phone) }
        when (result) {
            is ApiResult.Success -> {
                mode = when (result.data.status) {
                    "existing_contact" -> PostCallUiMode.Existing
                    "new_contact" -> PostCallUiMode.New
                    else -> PostCallUiMode.New
                }
                contact = result.data.contact
                displayName = result.data.contact?.fullName
            }
            is ApiResult.Error -> mode = PostCallUiMode.Error
        }
    }

    val kindLabel = when (callEndKind) {
        PostCallLogic.CallEndKind.Missed -> "تماس بی‌پاسخ"
        PostCallLogic.CallEndKind.Outgoing -> "تماس خروجی"
        PostCallLogic.CallEndKind.Answered -> "تماس ورودی"
        PostCallLogic.CallEndKind.Ignored -> "تماس"
    }
    val statusLabel = when (mode) {
        PostCallUiMode.Existing -> "در CRM"
        PostCallUiMode.New -> "مخاطب جدید"
        PostCallUiMode.Loading -> "در حال بررسی…"
        else -> "فایلینگ دیوار"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        shape = RoundedCornerShape(20.dp),
        color = DfThemeColors.surface(),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    color = DfColors.PurpleContainer,
                ) {
                    Icon(
                        DfIcons.Phone,
                        contentDescription = null,
                        tint = DfColors.Purple,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = AppSpacing.sm),
                ) {
                    Text(
                        displayName ?: phone,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                    Text(
                        "$kindLabel · $statusLabel",
                        style = AppTypography.labelSmall,
                        color = DfThemeColors.textSecondary(),
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(DfIcons.X, contentDescription = "بستن", tint = DfThemeColors.textSecondary())
                }
            }
            contact?.customerType?.takeIf { it.isNotBlank() }?.let {
                Text("نقش: $it", style = AppTypography.labelSmall, color = DfColors.Purple)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                DfPrimaryButton(
                    text = if (mode == PostCallUiMode.Existing) "ثبت سریع" else "ثبت مخاطب",
                    onClick = onExpand,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = DfThemeColors.surfaceVariant().copy(alpha = 0.5f),
                ) {
                    Text(
                        "بستن",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        style = AppTypography.labelLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }
}
