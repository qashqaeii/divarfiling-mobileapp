package ir.divarfiling.mobile.core.design.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.DfThemeColors

@Composable
fun rememberDfSnackbarHostState(): SnackbarHostState = remember { SnackbarHostState() }

@Composable
fun DfSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { data ->
        DfSnackbar(snackbarData = data)
    }
}

@Composable
fun DfSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    containerColor: Color = DfThemeColors.surface(),
    contentColor: Color = DfThemeColors.textPrimary(),
    actionColor: Color = DfThemeColors.primary(),
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = actionColor,
        dismissActionContentColor = DfThemeColors.textSecondary(),
    )
}

suspend fun SnackbarHostState.showDfMessage(
    message: String,
    actionLabel: String? = null,
) = showSnackbar(
    message = message,
    actionLabel = actionLabel,
    withDismissAction = true,
)
