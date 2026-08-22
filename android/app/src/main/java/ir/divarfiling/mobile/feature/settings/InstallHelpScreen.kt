package ir.divarfiling.mobile.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallHelpScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    fun openWeb(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    DfPullRefresh(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refreshAll,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            item {
                DfHubPageHeader(
                    title = "راهنمای نصب و آپدیت",
                    subtitle = "نصب امن، Play Protect و بروزرسانی نسخه‌ها",
                    sectionLabel = DfHeaderSections.SETTINGS,
                    titleIconRes = DfDecorIcons.Download,
                    userName = state.user?.fullName?.substringBefore(" ") ?: "کاربر",
                    onBack = onBack,
                )
            }
            item {
                HelpCard(
                    title = "روش نصب APK",
                    body = "فایل release را فقط از منبع رسمی بگیرید. اگر هشدار Play Protect دیدید، Install anyway را بزنید. اگر گزینه نیامد، یک‌بار OK بزنید و دوباره فایل APK را باز کنید.",
                )
            }
            item {
                HelpCard(
                    title = "اگر نصب مسدود شد",
                    body = "از Settings > Security > Install unknown apps برای Files یا Telegram اجازه نصب بدهید. در صورت نیاز می‌توانید موقتاً Play Protect را خاموش کنید و بعد از نصب دوباره فعالش کنید.",
                )
            }
            item {
                HelpCard(
                    title = "نسخه مناسب برای نصب",
                    body = "فقط app-release.apk امضاشده را نصب کنید. نسخه debug برای توسعه است و نباید برای مشاور یا مارکت ارسال شود.",
                )
            }
            item {
                HelpCard(
                    title = "آپدیت داخلی اپ",
                    body = "اپ حداکثر هر ۱۲ ساعت نسخه را چک می‌کند. اگر نسخه جدید موجود باشد، دانلود APK و نصب از داخل اپ انجام می‌شود. برای force update یا نسخه زیر حداقل پشتیبانی، بستن جریان آپدیت ممکن نیست.",
                )
            }
            item {
                DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        Text(
                            text = "لینک‌های لازم",
                            style = AppTypography.cardTitle,
                            fontWeight = FontWeight.Bold,
                            color = DfColors.TextPrimary,
                        )
                        DfPrimaryButton(
                            text = "باز کردن سایت اصلی",
                            onClick = { openWeb(AppLinks.SITE) },
                        )
                        DfSecondaryButton(
                            text = "حریم خصوصی",
                            onClick = { openWeb(AppLinks.PRIVACY) },
                        )
                        DfSecondaryButton(
                            text = "قوانین استفاده",
                            onClick = { openWeb(AppLinks.TERMS) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpCard(
    title: String,
    body: String,
) {
    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = title,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.Bold,
                color = DfColors.TextPrimary,
            )
            Text(
                text = body,
                style = AppTypography.bodyDescription,
                color = DfColors.TextSecondary,
            )
        }
    }
}
