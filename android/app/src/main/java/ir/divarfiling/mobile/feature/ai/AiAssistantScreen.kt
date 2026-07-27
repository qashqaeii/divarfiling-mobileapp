package ir.divarfiling.mobile.feature.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone

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
                DfStatusBanner(
                    message = "فعلاً می‌توانید از قالب پیام‌ها در بخش «بیشتر» برای ارسال حرفه‌ای به مشتری استفاده کنید.",
                    tone = DfStatusTone.Info,
                    title = "در حال توسعه",
                    icon = DfIcons.Sparkles,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
            item {
                DfEmptyState(
                    title = "به‌زودی فعال می‌شود",
                    subtitle = "دستیار هوشمند پیام و خلاصه‌سازی آگهی در نسخه‌های بعدی اپ اندروید در دسترس خواهد بود.",
                    variant = DfEmptyVariant.Locked,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
            item {
                DfSecondaryButton(
                    text = "بازگشت",
                    onClick = onBack,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }
    }
}
