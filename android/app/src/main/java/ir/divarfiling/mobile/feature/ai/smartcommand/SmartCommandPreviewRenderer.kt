package ir.divarfiling.mobile.feature.ai.smartcommand

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
@Composable
fun SmartCommandPreviewSection(
    lines: List<SmartCommandPreviewLineUi>,
    modifier: Modifier = Modifier,
    title: String = "بررسی قبل از ثبت",
) {
    if (lines.isEmpty()) return
    SmartCommandDetailGroup(title = title, modifier = modifier) {
        lines.forEach { line ->
            SmartCommandPreviewSectionRow(line)
        }
    }
}

@Composable
fun SmartCommandPreviewSectionRow(line: SmartCommandPreviewLineUi) {
    val label = line.label.ifBlank { "جزئیات" }
    val value = line.value.ifBlank { "—" }
    if (line.isMissing || line.isWarning) {
        SmartCommandInsetPanel {
            Text(label, style = AppTypography.labelSmall, color = DfColors.Amber)
            Text(value, style = AppTypography.bodyDescription, color = DfColors.Rose)
        }
    } else {
        SmartCommandKeyValueRow(label = label, value = value)
    }
}

@Composable
fun SmartCommandIncompleteBanner(
    messages: List<String>,
    modifier: Modifier = Modifier,
) {
    if (messages.isEmpty()) return
    SmartCommandInsetPanel(modifier = modifier) {
        Text(
            "برای ثبت، این موارد را تکمیل کنید",
            style = AppTypography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = DfColors.Amber,
        )
        messages.forEach { msg ->
            Text("• $msg", style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
        }
    }
}

@Composable
fun SmartCommandAmbiguousBanner(
    messages: List<String>,
    modifier: Modifier = Modifier,
) {
    if (messages.isEmpty()) return
    SmartCommandInsetPanel(modifier = modifier) {
        Text(
            messages.first(),
            style = AppTypography.bodyDescription,
            color = DfColors.TextSecondary,
        )
    }
}
