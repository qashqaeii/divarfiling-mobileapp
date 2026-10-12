package ir.divarfiling.mobile.feature.contracts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private fun JsonObject.str(key: String): String =
    get(key)?.jsonPrimitive?.contentOrNull.orEmpty()

@Composable
fun WizardReviewStepRich(
    reviewContext: JsonObject?,
    validationErrors: List<String>,
    statusLabel: String,
    pdfAvailable: Boolean,
    onEditStep: (String) -> Unit,
    onOpenPdf: () -> Unit,
    onPrepareSend: (() -> Unit)?,
    prepareSendEnabled: Boolean,
    prepareSendLoading: Boolean,
) {
    val ctx = reviewContext
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("مرور نهایی", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        if (statusLabel.isNotBlank()) {
            Text("وضعیت: $statusLabel", color = DfThemeColors.textSecondary())
        }
        val readiness = ctx?.str("readiness").orEmpty()
        val readinessMsg = ctx?.str("readiness_message").ifBlank {
            "اعتبارسنجی قرارداد"
        }
        val readinessDetail = ctx?.str("readiness_detail")
        val tone = when (readiness) {
            "ready" -> DfStatusTone.Success
            "attention" -> DfStatusTone.Warning
            else -> DfStatusTone.Warning
        }
        DfStatusBanner(
            message = readinessMsg,
            detail = readinessDetail?.takeIf { it.isNotBlank() },
            tone = tone,
        )
        ctx?.get("contract_meta")?.jsonObject?.let { meta ->
            val internal = meta.str("internal_number")
            val version = meta.str("version_number")
            if (internal.isNotBlank() || version.isNotBlank()) {
                Text(
                    buildString {
                        if (internal.isNotBlank()) append("شماره: $internal")
                        if (version.isNotBlank()) {
                            if (isNotEmpty()) append(" · ")
                            append("نسخه $version")
                        }
                    },
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textMuted(),
                )
            }
        }
        ctx?.get("issues")?.jsonArray?.forEach { el ->
            val issue = el.jsonObject
            val msg = issue.str("message")
            val step = issue.str("edit_step")
            if (msg.isNotBlank()) {
                Surface(shape = AppShapes.CardSmall, color = DfThemeColors.surface()) {
                    Column(Modifier.padding(AppSpacing.sm)) {
                        Text(msg, style = AppTypography.labelSmall, color = DfThemeColors.warning())
                        if (step.isNotBlank()) {
                            TextButton(onClick = { onEditStep(step) }) { Text("اصلاح") }
                        }
                    }
                }
            }
        }
        validationErrors.forEach { err ->
            Text("• $err", color = DfThemeColors.warning(), style = AppTypography.labelSmall)
        }
        ctx?.get("document_sections")?.jsonArray?.forEach { secEl ->
            val sec = secEl.jsonObject
            val title = sec.str("title")
            val body = sec.str("body")
            if (title.isNotBlank() || body.isNotBlank()) {
                Surface(shape = AppShapes.Card, color = DfThemeColors.surface()) {
                    Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RowWithEdit(title.ifBlank { "بخش قرارداد" }, sec.str("edit_step"), onEditStep)
                        if (body.isNotBlank()) {
                            Text(body, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
                        }
                    }
                }
            }
        }
        if (pdfAvailable) {
            DfSecondaryButton(
                text = "پیش‌نمایش PDF",
                onClick = onOpenPdf,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (onPrepareSend != null && prepareSendEnabled) {
            DfSecondaryButton(
                text = if (prepareSendLoading) "در حال ارسال…" else "آماده‌سازی و ارسال برای طرفین",
                onClick = onPrepareSend,
                enabled = !prepareSendLoading && validationErrors.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RowWithEdit(title: String, editStep: String, onEditStep: (String) -> Unit) {
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, style = AppTypography.labelLarge)
        if (editStep.isNotBlank()) {
            TextButton(onClick = { onEditStep(editStep) }) { Text("ویرایش") }
        }
    }
}
