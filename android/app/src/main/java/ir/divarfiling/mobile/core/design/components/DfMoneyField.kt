package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ir.divarfiling.mobile.core.design.FormatUtils

@Composable
fun DfMoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    enabled: Boolean = true,
    helperText: String? = "تومان",
    errorText: String? = null,
) {
    val display = FormatUtils.formatGroupedInput(value)
    DfTextField(
        value = display,
        onValueChange = { incoming ->
            val parsed = FormatUtils.parseLocalizedLong(incoming)
            onValueChange(parsed?.toString().orEmpty())
        },
        modifier = modifier,
        label = label,
        helperText = helperText,
        errorText = errorText,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}
