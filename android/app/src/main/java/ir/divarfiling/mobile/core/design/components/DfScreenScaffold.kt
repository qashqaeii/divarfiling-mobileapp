package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ir.divarfiling.mobile.core.design.AppSpacing

/**
 * Standard screen chrome: subtle background + optional top bar + snackbar + FAB.
 * Does not change navigation or business logic — layout only.
 */
@Composable
fun DfScreenScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = DfScreenContainerColor,
    contentPadding: PaddingValues? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = topBar,
        snackbarHost = {
            if (snackbarHostState != null) {
                DfSnackbarHost(hostState = snackbarHostState)
            }
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
    ) { innerPadding ->
        DfScreenBackground {
            content(contentPadding ?: innerPadding)
        }
    }
}

/**
 * Hub / list page body slots: header, optional filter strip, then main content.
 */
@Composable
fun DfListScreenLayout(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    filterBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        header()
        filterBar()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            content()
        }
    }
}

/**
 * Detail page body: sticky-feel header slot + scrollable content area.
 */
@Composable
fun DfDetailScreenLayout(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        header()
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal),
            contentAlignment = Alignment.TopStart,
        ) {
            content()
        }
    }
}
