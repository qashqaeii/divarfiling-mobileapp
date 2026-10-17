package ir.divarfiling.mobile.feature.postcall.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfTextField

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
            compact = true,
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
                DfTextField(
                    value = budgetMin,
                    onValueChange = onBudgetMinChange,
                    label = "بودجه از (تومان)",
                    enabled = enabled,
                    singleLine = true,
                )
                DfTextField(
                    value = budgetMax,
                    onValueChange = onBudgetMaxChange,
                    label = "بودجه تا (تومان)",
                    enabled = enabled,
                    singleLine = true,
                )
                DfTextField(
                    value = propertyType,
                    onValueChange = onPropertyTypeChange,
                    label = "نوع ملک",
                    placeholder = "آپارتمان، ویلا، …",
                    enabled = enabled,
                    singleLine = true,
                )
                DfTextField(
                    value = areas,
                    onValueChange = onAreasChange,
                    label = "محله‌های موردنظر",
                    enabled = enabled,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                )
            }
        }
    }
}
