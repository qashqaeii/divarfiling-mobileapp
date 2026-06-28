package ir.divarfiling.mobile.core.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Transparent scaffold color — use with [DfScreenBackground] so liquid gradients show through. */
val DfScreenContainerColor: Color = Color.Transparent

/**
 * Unified screen backdrop: liquid gradient mesh behind all app content.
 * Wrap screen bodies (inside Scaffold `padding` when present) for a consistent premium look.
 */
@Composable
fun DfScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        DfLiquidBackground()
        content()
    }
}
