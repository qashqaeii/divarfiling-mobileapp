package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.feature.postcall.PostCallCallMetadata
import ir.divarfiling.mobile.feature.postcall.PostCallLogic
import ir.divarfiling.mobile.feature.postcall.PostCallUiMode

@Preview(name = "Compact · Existing", showBackground = true)
@Composable
private fun PreviewCompactExisting() {
    DivarFilingTheme {
        Surface {
            PostCallCompactCard(
                metadata = previewMetadata(PostCallLogic.CallEndKind.Answered),
                mode = PostCallUiMode.Existing,
                contact = null,
                displayName = "علی محمدی",
                onDismiss = {},
                onPrimaryAction = {},
                onRetryResolve = {},
                modifier = Modifier.padding(AppSpacing.md),
            )
        }
    }
}

@Preview(name = "Compact · Loading", showBackground = true)
@Composable
private fun PreviewCompactLoading() {
    DivarFilingTheme {
        Surface {
            PostCallCompactCard(
                metadata = previewMetadata(PostCallLogic.CallEndKind.Missed),
                mode = PostCallUiMode.Loading,
                contact = null,
                displayName = null,
                onDismiss = {},
                onPrimaryAction = {},
                onRetryResolve = {},
                modifier = Modifier.padding(AppSpacing.md),
            )
        }
    }
}

@Preview(name = "Compact · Ambiguous", showBackground = true)
@Composable
private fun PreviewCompactAmbiguous() {
    DivarFilingTheme {
        Surface {
            PostCallCompactCard(
                metadata = previewMetadata(PostCallLogic.CallEndKind.Outgoing),
                mode = PostCallUiMode.Ambiguous,
                contact = null,
                displayName = null,
                onDismiss = {},
                onPrimaryAction = {},
                onRetryResolve = {},
                modifier = Modifier.padding(AppSpacing.md),
            )
        }
    }
}

private fun previewMetadata(kind: PostCallLogic.CallEndKind) = PostCallCallMetadata(
    phone = "09121234567",
    callEndKind = kind,
    callLogDateMs = System.currentTimeMillis() - 120_000L,
    durationSec = 95L,
    callLogType = null,
)
