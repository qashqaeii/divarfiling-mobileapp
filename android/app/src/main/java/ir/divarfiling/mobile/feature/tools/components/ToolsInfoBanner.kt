package ir.divarfiling.mobile.feature.tools.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DivarFilingTheme
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone

@Composable
fun ToolsInfoBanner(
    modifier: Modifier = Modifier,
) {
    DfStatusBanner(
        message = "این ابزارها به شما کمک می‌کنند در معاملات املاک با دقت بیشتر تصمیم بگیرید.",
        tone = DfStatusTone.Info,
        title = "دقت بالا، تصمیم بهتر",
        icon = DfIcons.Sparkles,
        modifier = modifier.padding(horizontal = AppSpacing.screenHorizontal),
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ToolsInfoBannerPreview() {
    DivarFilingTheme {
        ToolsInfoBanner()
    }
}
