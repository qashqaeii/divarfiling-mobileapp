package ir.divarfiling.mobile.feature.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet

@Composable
fun ExportSecurityGate(
    visible: Boolean,
    exportLabel: String,
    onVerified: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: ExportSecurityViewModel = hiltViewModel(),
) {
    val securityState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(visible, exportLabel) {
        if (visible) {
            viewModel.open(exportLabel)
        } else {
            viewModel.dismiss()
        }
    }

    if (!visible && !securityState.isVisible) return

    DfModalBottomSheet(
        onDismissRequest = {
            viewModel.dismiss()
            onDismiss()
        },
    ) {
        ExportSecuritySheet(
            onVerified = onVerified,
            onDismiss = {
                viewModel.dismiss()
                onDismiss()
            },
            viewModel = viewModel,
        )
    }
}
