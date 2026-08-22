package ir.divarfiling.mobile.feature.crm.templates

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.network.MessageTemplateDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageTemplatesScreen(
    onBack: () -> Unit,
    viewModel: MessageTemplatesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.copiedMessage, state.error) {
        state.copiedMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearCopiedMessage()
        }
        state.error?.let {
            snackbar.showSnackbar(it)
        }
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
            ) {
                item {
                    DfHubPageHeader(
                        title = "قالب پیام",
                        subtitle = "پیام‌های آماده برای ارسال سریع",
                        sectionLabel = DfHeaderSections.CRM,
                        titleIconRes = DfDecorIcons.FileText,
                        onBack = onBack,
                    )
                }
                item {
                    DfStatusBanner(
                        message = "قالب‌ها از میزکار وب همگام می‌شوند؛ اینجا فقط برای کپی سریع در دسترس‌اند.",
                        tone = DfStatusTone.Info,
                        title = "همگام با Workspace",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                state.error?.let { error ->
                    item {
                        DfStatusBanner(
                            message = error,
                            tone = DfStatusTone.Error,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                if (state.isLoading) {
                    item {
                        DfCardListSkeleton(
                            count = 4,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else if (state.templates.isEmpty()) {
                    item {
                        DfEmptyState(
                            title = "قالبی یافت نشد",
                            subtitle = "قالب‌های پیام از میزکار وب همگام می‌شوند.",
                            variant = DfEmptyVariant.Empty,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                } else {
                    items(state.templates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            onCopy = {
                                copyToClipboard(context, template.body)
                                viewModel.onCopied("متن کپی شد")
                            },
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: MessageTemplateDto,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DfCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = DfThemeColors.surface(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                template.title,
                style = AppTypography.cardTitle,
                fontWeight = FontWeight.SemiBold,
                color = DfThemeColors.textPrimary(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (template.category.isNotBlank()) {
                DfBadge(
                    text = template.category,
                    color = DfThemeColors.surfaceVariant(),
                    textColor = DfThemeColors.textSecondary(),
                )
            }
            Text(
                template.body,
                style = AppTypography.bodyDescription,
                color = DfThemeColors.textSecondary(),
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
            )
            DfSecondaryButton(
                text = "کپی متن",
                onClick = onCopy,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("template", text))
}
