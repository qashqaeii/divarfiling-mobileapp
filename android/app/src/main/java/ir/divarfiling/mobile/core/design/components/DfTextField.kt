package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors

/**
 * Unified outlined text field — RTL-friendly, consistent shape/colors.
 */
@Composable
fun DfTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val isError = !errorText.isNullOrBlank()
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            label = label?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
            placeholder = placeholder?.let {
                {
                    Text(
                        it,
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textMuted(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            shape = AppShapes.Field,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DfThemeColors.primary(),
                unfocusedBorderColor = DfThemeColors.outline(),
                errorBorderColor = DfThemeColors.error(),
                focusedLabelColor = DfThemeColors.primary(),
                cursorColor = DfThemeColors.primary(),
                focusedTextColor = DfThemeColors.textPrimary(),
                unfocusedTextColor = DfThemeColors.textPrimary(),
                focusedContainerColor = DfThemeColors.surface(),
                unfocusedContainerColor = DfThemeColors.surface(),
            ),
        )
        val caption = errorText?.takeIf { it.isNotBlank() } ?: helperText
        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                style = AppTypography.meta,
                color = if (isError) DfThemeColors.error() else DfThemeColors.textMuted(),
                modifier = Modifier.padding(
                    start = AppSpacing.sm,
                    top = AppSpacing.xxs,
                    end = AppSpacing.sm,
                ),
            )
        }
    }
}
