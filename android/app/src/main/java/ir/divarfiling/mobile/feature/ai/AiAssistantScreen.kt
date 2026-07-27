package ir.divarfiling.mobile.feature.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfDetailSkeleton
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onBack: () -> Unit,
    viewModel: AiAssistantViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        DfPullRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
        ) {
            if (state.isLoading) {
                DfDetailSkeleton()
                return@DfPullRefresh
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "دستیار AI",
                        subtitle = "پیش‌نویس پیام برای مخاطب یا آگهی",
                        titleIconRes = DfDecorIcons.Sparkles,
                        onBack = onBack,
                    )
                }
                item {
                    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                            Text("سهمیه باقی‌مانده", style = AppTypography.labelSmall, color = DfColors.TextSecondary)
                            Text(
                                "${state.quota?.remaining ?: 0} از ${state.quota?.limit ?: 0}",
                                style = AppTypography.sectionTitle,
                            )
                            state.quota?.planLabel?.let {
                                Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                            }
                        }
                    }
                }
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        OutlinedTextField(
                            value = state.contactId,
                            onValueChange = viewModel::onContactIdChange,
                            label = { Text("شناسه مخاطب (اختیاری)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.listingToken,
                            onValueChange = viewModel::onListingTokenChange,
                            label = { Text("توکن آگهی (اختیاری)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.intent,
                            onValueChange = viewModel::onIntentChange,
                            label = { Text("نوع پیام") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = viewModel::onNotesChange,
                            label = { Text("یادداشت") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                        )
                        DfPrimaryButton(
                            text = "تولید پیش‌نویس",
                            onClick = viewModel::generateDraft,
                            loading = state.isSubmitting,
                        )
                    }
                }
                if (state.draftText.isNotBlank()) {
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            Column(Modifier.padding(AppSpacing.cardPadding), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                Text("پیش‌نویس", style = AppTypography.cardTitle)
                                Text(state.draftText, style = AppTypography.bodyDescription, color = DfColors.TextSecondary)
                                DfPrimaryButton(
                                    text = "کپی متن",
                                    onClick = {
                                        copyToClipboard(context, state.draftText)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("ai-draft", text))
}
