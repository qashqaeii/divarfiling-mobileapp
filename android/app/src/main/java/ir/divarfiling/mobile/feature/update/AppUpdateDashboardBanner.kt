package ir.divarfiling.mobile.feature.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton

@Composable
fun AppUpdateDashboardBanner(
    state: AppUpdateUiState,
    onDownloadClick: () -> Unit,
    onInAppUpdateClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (!state.updateRequired || state.version == null) return

    val latestVersion = state.version.versionName.ifBlank { "جدید" }
    val accent = if (state.forceUpdate) DfColors.OverdueAccent else DfColors.Purple

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.CardSmall,
        color = accent.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(AppShapes.IconContainer)
                        .background(
                            Brush.linearGradient(
                                listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.12f)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        DfIcons.Download,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = if (state.forceUpdate) "به‌روزرسانی ضروری اپ" else "نسخه جدید اپ منتشر شده",
                        style = AppTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.TextPrimary,
                    )
                    Text(
                        text = "نسخه فعلی شما v${BuildConfig.VERSION_NAME} — آخرین نسخه v$latestVersion. " +
                            "لطفاً حتماً نسخه جدید را دانلود و نصب کنید.",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextSecondary,
                    )
                }
            }

            DfPrimaryButton(
                text = "دانلود نسخه جدید",
                onClick = onDownloadClick,
                modifier = Modifier.fillMaxWidth(),
            )

            if (onInAppUpdateClick != null && state.phase != AppUpdatePhase.Downloading) {
                DfSecondaryButton(
                    text = "دانلود مستقیم در اپ",
                    onClick = onInAppUpdateClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
