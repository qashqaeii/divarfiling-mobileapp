package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton

@Composable
fun PostCallQuickLeadExtras(
    expanded: Boolean,
    budgetMin: String,
    budgetMax: String,
    propertyType: String,
    areas: String,
    enabled: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onBudgetMinChange: (String) -> Unit,
    onBudgetMaxChange: (String) -> Unit,
    onPropertyTypeChange: (String) -> Unit,
    onAreasChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        DfSecondaryButton(
            text = if (expanded) "بستن اطلاعات تکمیلی" else "اطلاعات تکمیلی (اختیاری)",
            onClick = { onExpandedChange(!expanded) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = AppSpacing.sm),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    "نیاز ملکی — در صورت تمایل همین‌جا ثبت می‌شود.",
                    style = AppTypography.labelSmall,
                    color = DfThemeColors.textSecondary(),
                )
                OutlinedTextField(
                    value = budgetMin,
                    onValueChange = onBudgetMinChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("بودجه از (تومان)") },
                    enabled = enabled,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = budgetMax,
                    onValueChange = onBudgetMaxChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("بودجه تا (تومان)") },
                    enabled = enabled,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = propertyType,
                    onValueChange = onPropertyTypeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("نوع ملک") },
                    enabled = enabled,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = areas,
                    onValueChange = onAreasChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("محله‌های موردنظر") },
                    enabled = enabled,
                    minLines = 2,
                )
            }
        }
    }
}
