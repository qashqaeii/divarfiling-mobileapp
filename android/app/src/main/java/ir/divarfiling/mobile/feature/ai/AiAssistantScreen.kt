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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfFilterChipRow
import ir.divarfiling.mobile.core.design.components.DfFilterOption
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    onBack: () -> Unit,
    viewModel: AiAssistantViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val quotaExhausted = state.quota?.let { it.enabled && it.remaining <= 0 } == true
    val aiDisabled = state.quota?.enabled == false

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        title = "دستیار AI",
                        subtitle = "پیش‌نویس پیام و خلاصه آگهی در جریان واقعی کار",
                        titleIconRes = DfDecorIcons.Sparkles,
                        onBack = onBack,
                    )
                }

                state.contextLabel?.let { label ->
                    item {
                        DfStatusBanner(
                            message = label,
                            tone = DfStatusTone.Info,
                            title = "زمینه آماده",
                            icon = DfIcons.Sparkles,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }

                when {
                    aiDisabled -> item {
                        DfStatusBanner(
                            message = "AI فعلاً برای این حساب فعال نیست. پیش‌نویس جایگزین همچنان در دسترس است.",
                            tone = DfStatusTone.Warning,
                            title = "AI غیرفعال",
                            icon = DfIcons.Sparkles,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                    quotaExhausted -> item {
                        DfStatusBanner(
                            message = "سهمیه امروز تمام شده است. می‌توانید فردا دوباره تلاش کنید یا از نسخه جایگزین استفاده کنید.",
                            tone = DfStatusTone.Warning,
                            title = "سهمیه تمام شده",
                            icon = DfIcons.Sparkles,
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }

                state.quota?.let { quota ->
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            DfStatusBanner(
                                message = if (quota.enabled) {
                                    "${quota.remaining} از ${quota.limit} درخواست باقی مانده است"
                                } else {
                                    "سرویس AI برای این حساب خاموش است"
                                },
                                tone = when {
                                    !quota.enabled -> DfStatusTone.Warning
                                    quota.remaining > 0 -> DfStatusTone.Success
                                    else -> DfStatusTone.Warning
                                },
                                title = quota.planLabel ?: "سهمیه AI",
                            )
                        }
                    }
                }

                item {
                    DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            Text(text = "نوع کار")
                            DfFilterChipRow(
                                options = listOf(
                                    DfFilterOption(AiMode.Draft, "پیش‌نویس پیام"),
                                    DfFilterOption(AiMode.Summarize, "خلاصه آگهی"),
                                ),
                                selected = state.mode,
                                onSelect = viewModel::onModeChange,
                            )

                            OutlinedTextField(
                                value = state.contactId,
                                onValueChange = viewModel::onContactIdChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("شناسه مخاطب") },
                                singleLine = true,
                                enabled = !state.isSubmitting,
                                shape = AppShapes.Field,
                            )
                            OutlinedTextField(
                                value = state.listingToken,
                                onValueChange = viewModel::onListingTokenChange,
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("توکن آگهی") },
                                singleLine = true,
                                enabled = !state.isSubmitting,
                                shape = AppShapes.Field,
                            )

                            if (state.mode == AiMode.Draft) {
                                Text(text = "لحن پیام")
                                DfFilterChipRow(
                                    options = AiAssistantViewModel.toneOptions.map {
                                        DfFilterOption(it.value, it.label)
                                    },
                                    selected = state.tone,
                                    onSelect = viewModel::onToneChange,
                                )
                                OutlinedTextField(
                                    value = state.notes,
                                    onValueChange = viewModel::onNotesChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("نکات تکمیلی") },
                                    supportingText = {
                                        Text("مثل بودجه، زمان بازدید یا جزئیات خاص")
                                    },
                                    minLines = 3,
                                    enabled = !state.isSubmitting,
                                    shape = AppShapes.Field,
                                )
                            }

                            DfPrimaryButton(
                                text = when (state.mode) {
                                    AiMode.Draft -> "ساخت پیش‌نویس"
                                    AiMode.Summarize -> "خلاصه‌سازی آگهی"
                                },
                                onClick = viewModel::runPrimaryAction,
                                loading = state.isSubmitting || state.isLoading,
                                enabled = !state.isSubmitting,
                            )
                            if (state.draftText.isNotBlank() || state.summaryText.isNotBlank()) {
                                DfSecondaryButton(
                                    text = "تولید مجدد",
                                    onClick = viewModel::regenerate,
                                    enabled = !state.isSubmitting && !state.isLoading,
                                )
                            }
                        }
                    }
                }

                state.draftText.takeIf { it.isNotBlank() }?.let { draft ->
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                DfStatusBanner(
                                    message = draft,
                                    tone = if (state.draftIsFallback) DfStatusTone.Warning else DfStatusTone.Success,
                                    title = if (state.draftIsFallback) {
                                        "پیش‌نویس جایگزین"
                                    } else {
                                        "پیش‌نویس آماده ارسال"
                                    },
                                )
                                DfSecondaryButton(
                                    text = "کپی متن",
                                    onClick = {
                                        copyToClipboard(context, draft)
                                        viewModel.showMessage("متن پیش‌نویس کپی شد")
                                    },
                                )
                            }
                        }
                    }
                }

                state.summaryText.takeIf { it.isNotBlank() }?.let { summary ->
                    item {
                        DfCard(modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            ) {
                                DfStatusBanner(
                                    message = summary,
                                    tone = if (state.summaryIsFallback) DfStatusTone.Warning else DfStatusTone.Info,
                                    title = if (state.summaryIsFallback) {
                                        "خلاصه جایگزین"
                                    } else {
                                        "خلاصه آگهی"
                                    },
                                )
                                DfSecondaryButton(
                                    text = "کپی خلاصه",
                                    onClick = {
                                        copyToClipboard(context, summary)
                                        viewModel.showMessage("خلاصه آگهی کپی شد")
                                    },
                                )
                            }
                        }
                    }
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
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("ai_draft", text))
}
