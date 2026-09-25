package ir.divarfiling.mobile.feature.ai.smartcommand

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard

@Composable
fun SmartCommandPreviewSection(
    lines: List<SmartCommandPreviewLineUi>,
    modifier: Modifier = Modifier,
    title: String = "بررسی قبل از ثبت",
) {
    if (lines.isEmpty()) return
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(title, style = AppTypography.sectionTitle)
            lines.forEach { line ->
                SmartCommandPreviewSectionRow(line)
            }
        }
    }
}

@Composable
fun SmartCommandPreviewSectionRow(line: SmartCommandPreviewLineUi) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (line.label.isNotBlank()) {
            Text(line.label, style = AppTypography.labelSmall, color = DfColors.TextMuted)
        }
        Text(
            line.value.ifBlank { "—" },
            style = AppTypography.bodyDescription,
            color = if (line.isMissing || line.isWarning) DfColors.Rose else DfColors.TextPrimary,
        )
    }
}

@Composable
fun SmartCommandIncompleteBanner(
    messages: List<String>,
    modifier: Modifier = Modifier,
) {
    if (messages.isEmpty()) return
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            messages.forEach { msg ->
                Text(msg, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
            }
        }
    }
}

@Composable
fun SmartCommandAmbiguousBanner(
    messages: List<String>,
    modifier: Modifier = Modifier,
) {
    if (messages.isEmpty()) return
    DfCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.cardPadding)) {
            Text(
                messages.first(),
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
            )
        }
    }
}
