package ir.divarfiling.mobile.feature.filing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingOwnerPhoneSheet(
    phone: String,
    isSaving: Boolean,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onCall: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    DfSheetScaffold(
        title = "تلفن مالک",
        subtitle = "شماره تماس مالک آگهی را ثبت یا ویرایش کنید",
        icon = DfIcons.Phone,
        onClose = onDismiss,
        footer = {
            DfSheetActions(
                primaryText = if (isSaving) "در حال ذخیره…" else "ذخیره شماره",
                onPrimary = onSave,
                primaryEnabled = !isSaving,
                isSubmitting = isSaving,
                onSecondary = onDismiss,
            )
        },
    ) {
        DfSheetSection(title = "شماره تماس") {
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("شماره موبایل مالک") },
                placeholder = { Text("مثلاً ۰۹۱۲۱۲۳۴۵۶۷") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
            )
            if (phone.isNotBlank()) {
                OwnerCallRow(phone = phone, onCall = onCall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingOwnerCallBanner(
    phone: String,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onCall,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfColors.GreenLight.copy(alpha = 0.65f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(
                    shape = AppShapes.IconContainer,
                    color = DfColors.Green.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = DfIcons.Phone,
                            contentDescription = null,
                            tint = DfColors.Green,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "تماس با مالک",
                        style = AppTypography.labelSmall,
                        color = DfColors.TextMuted,
                    )
                    Text(
                        text = phone,
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfColors.Green,
                    )
                }
            }
            Text(
                text = "تماس",
                style = AppTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DfColors.Green,
            )
        }
    }
}

@Composable
private fun OwnerCallRow(
    phone: String,
    onCall: (String) -> Unit,
) {
    ListingOwnerCallBanner(
        phone = phone,
        onCall = { onCall(phone) },
    )
}
