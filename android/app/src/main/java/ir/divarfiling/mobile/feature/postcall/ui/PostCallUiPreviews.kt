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

@Preview(name = "Compact · Answered", showBackground = true)
@Composable
private fun PreviewCompactAnswered() {
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

@Preview(name = "Compact · Missed", showBackground = true)
@Composable
private fun PreviewCompactMissed() {
    DivarFilingTheme {
        Surface {
            PostCallCompactCard(
                metadata = previewMetadata(PostCallLogic.CallEndKind.Missed, durationSec = 0L),
                mode = PostCallUiMode.Existing,
                contact = null,
                displayName = "مشتری ناشناس",
                onDismiss = {},
                onPrimaryAction = {},
                onRetryResolve = {},
                modifier = Modifier.padding(AppSpacing.md),
            )
        }
    }
}

@Preview(name = "Compact · Outgoing", showBackground = true)
@Composable
private fun PreviewCompactOutgoing() {
    DivarFilingTheme {
        Surface {
            PostCallCompactCard(
                metadata = previewMetadata(PostCallLogic.CallEndKind.Outgoing),
                mode = PostCallUiMode.New,
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

@Preview(name = "Kind banner · Answered", showBackground = true)
@Composable
private fun PreviewKindBannerAnswered() {
    DivarFilingTheme {
        Surface(modifier = Modifier.padding(AppSpacing.md)) {
            PostCallKindBanner(
                metadata = previewMetadata(PostCallLogic.CallEndKind.Answered),
                phone = "09121234567",
            )
        }
    }
}

@Preview(name = "Kind banner · Missed", showBackground = true)
@Composable
private fun PreviewKindBannerMissed() {
    DivarFilingTheme {
        Surface(modifier = Modifier.padding(AppSpacing.md)) {
            PostCallKindBanner(
                metadata = previewMetadata(PostCallLogic.CallEndKind.Missed, durationSec = 0L),
                phone = "09129876543",
            )
        }
    }
}

private fun previewMetadata(
    kind: PostCallLogic.CallEndKind,
    durationSec: Long = 95L,
) = PostCallCallMetadata(
    phone = "09121234567",
    callEndKind = kind,
    callLogDateMs = System.currentTimeMillis() - 120_000L,
    durationSec = durationSec,
    callLogType = null,
)
