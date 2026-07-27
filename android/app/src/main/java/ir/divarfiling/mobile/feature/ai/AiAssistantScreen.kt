package ir.divarfiling.mobile.feature.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onBack: () -> Unit,
) {
    Scaffold(containerColor = DfScreenContainerColor) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                DfHubPageHeader(
                    title = "دستیار AI",
                    subtitle = "هوش مصنوعی مشاور",
                    titleIconRes = DfDecorIcons.Sparkles,
                    onBack = onBack,
                )
            }
            item {
                DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        Text(
                            "در حال توسعه",
                            style = AppTypography.sectionTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.Purple,
                        )
                        Text(
                            "دستیار هوشمند پیام و خلاصه‌سازی آگهی به‌زودی در اپ اندروید فعال می‌شود. " +
                                "فعلاً می‌توانید از قالب پیام‌ها در بخش «بیشتر» برای ارسال حرفه‌ای به مشتری استفاده کنید.",
                            style = AppTypography.bodyDescription,
                            color = DfColors.TextSecondary,
                        )
                        DfPrimaryButton(
                            text = "متوجه شدم",
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
