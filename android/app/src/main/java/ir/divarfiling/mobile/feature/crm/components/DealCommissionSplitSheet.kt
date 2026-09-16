package ir.divarfiling.mobile.feature.crm.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.core.network.DealCommissionSplitSaveRow
import ir.divarfiling.mobile.core.network.DealCommissionSplitsData

@Composable
fun DealCommissionSplitSheet(
    data: DealCommissionSplitsData,
    isSubmitting: Boolean,
    onSave: (List<DealCommissionSplitSaveRow>) -> Unit,
    onDismiss: () -> Unit,
) {
    val members = data.availableMembers
    val initial = remember(data) {
        members.associate { m ->
            val existing = data.splits.firstOrNull { it.memberId == m.memberId }?.percent
            m.memberId to (existing?.toString() ?: "")
        }
    }
    val percents = remember { mutableStateMapOf<Long, String>().apply { putAll(initial) } }
    val total = members.sumOf { percents[it.memberId]?.toDoubleOrNull() ?: 0.0 }

    DfSheetScaffold(
        title = "تقسیم کمیسیون",
        subtitle = "مجموع درصدها باید ۱۰۰ باشد",
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSubmitting) "در حال ذخیره…" else "ذخیره",
                onPrimary = {
                    onSave(
                        members.mapNotNull { m ->
                            val p = percents[m.memberId]?.toDoubleOrNull() ?: return@mapNotNull null
                            DealCommissionSplitSaveRow(memberId = m.memberId, percent = p)
                        },
                    )
                },
                primaryEnabled = !isSubmitting && kotlin.math.abs(total - 100.0) <= 0.01,
                isSubmitting = isSubmitting,
                onSecondary = onDismiss,
            )
        },
    ) {
        Text(
            "کمیسیون کل: ${FormatUtils.formatPriceToman(data.totalCommission)}",
            style = AppTypography.BodySmall,
        )
        Text(
            text = if (kotlin.math.abs(total - 100.0) <= 0.01) "۱۰۰٪ تخصیص داده شده ✓" else "جمع فعلی: ${total.toInt()}٪",
            style = AppTypography.BodySmall,
        )
        members.forEach { member ->
            DfTextField(
                value = percents[member.memberId].orEmpty(),
                onValueChange = { percents[member.memberId] = it },
                label = "${member.name} (٪)",
                enabled = !isSubmitting,
            )
        }
    }
}
