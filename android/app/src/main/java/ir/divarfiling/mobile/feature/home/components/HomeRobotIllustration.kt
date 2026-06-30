package ir.divarfiling.mobile.feature.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDecorImage
import ir.divarfiling.mobile.core.design.components.DfDecorSize

@Composable
fun HomeRobotIllustration(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(DfDecorSize.Illustration),
        contentAlignment = Alignment.Center,
    ) {
        DfDecorImage(
            resId = DfDecorIcons.HomeIllustrationRobot,
            size = DfDecorSize.Illustration,
        )
    }
}
