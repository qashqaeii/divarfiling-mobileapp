package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.feature.extract.components.ExtractSectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingOwnerPhoneCard(
    phone: String,
    isSaving: Boolean,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCall: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtractSectionCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("شماره تماس مالک") },
                placeholder = { Text("مثلاً ۰۹۱۲۱۲۳۴۵۶۷") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                enabled = !isSaving,
                trailingIcon = {
                    if (phone.isNotBlank()) {
                        IconButton(onClick = { onCall(phone) }) {
                            Icon(
                                imageVector = DfIcons.Phone,
                                contentDescription = "تماس",
                                tint = DfColors.Green,
                            )
                        }
                    }
                },
            )
            DfPrimaryButton(
                text = if (isSaving) "…" else "ذخیره",
                onClick = onSave,
                enabled = !isSaving,
            )
        }
    }
}
