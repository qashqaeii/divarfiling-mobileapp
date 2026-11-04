package ir.divarfiling.mobile.feature.contentstudio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.feature.contentstudio.ui.PcsZoomablePosterPreview
import ir.divarfiling.mobile.feature.contentstudio.ui.TemplatePickerSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentStudioScreen(
    onBack: () -> Unit,
    viewModel: ContentStudioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { msg ->
            if (state.previewBitmap == null || !state.assetsLoading) {
                snackbar.showSnackbar(msg)
            }
        }
    }

    TemplatePickerSheet(
        visible = state.showTemplatePicker,
        templates = state.templates,
        selectedTemplateId = state.selectedTemplateId,
        onSelect = viewModel::selectTemplate,
        onDismiss = { viewModel.toggleTemplatePicker(false) },
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("استودیوی تولید محتوا", style = AppTypography.titleSmall)
                        Text(
                            state.projectTitle.ifBlank { "فایل شخصی" },
                            style = AppTypography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(DfIcons.ChevronLeft, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveSelection,
                        enabled = state.canSaveSelection && !state.saving,
                    ) {
                        Text(if (state.saving) "…" else "ذخیره انتخاب")
                    }
                    TextButton(
                        onClick = viewModel::exportPng,
                        enabled = state.previewBitmap != null && !state.exporting && !state.assetsLoading,
                    ) {
                        Text(if (state.exporting) "…" else "PNG")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = DfThemeColors.background(),
    ) { padding ->
        when {
            state.loading -> LoadingBlock(Modifier.fillMaxSize().padding(padding))
            state.errorCode == "MANIFEST_VERSION_MISMATCH" || state.errorCode == "MANIFEST_UNAVAILABLE" -> {
                DfEmptyState(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.screenHorizontal),
                    title = "Manifest در دسترس نیست",
                    subtitle = state.error ?: "نسخه قالب با سرور هماهنگ نیست.",
                    actionLabel = "تلاش مجدد",
                    onAction = viewModel::refresh,
                    variant = DfEmptyVariant.Offline,
                )
            }
            state.scene != null && state.property != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenHorizontal),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        DfSecondaryButton(
                            text = "قالب‌ها",
                            onClick = { viewModel.toggleTemplatePicker(true) },
                            modifier = Modifier.weight(1f),
                        )
                        listOf("story", "post", "square").forEach { fmt ->
                            DfPrimaryButton(
                                text = when (fmt) {
                                    "story" -> "استوری"
                                    "post" -> "پست"
                                    else -> "مربع"
                                },
                                onClick = { viewModel.selectFormat(fmt) },
                                modifier = Modifier.weight(1f),
                                enabled = state.selectedFormat != fmt,
                            )
                        }
                    }
                    if (state.assetsLoading) {
                        LoadingBlock(Modifier.weight(1f))
                    } else {
                        PcsZoomablePosterPreview(
                            bitmap = state.previewBitmap,
                            overlayLoading = false,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
            }
            state.error != null -> {
                DfEmptyState(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(AppSpacing.screenHorizontal),
                    title = "خطا",
                    subtitle = state.error.orEmpty(),
                    actionLabel = "تلاش مجدد",
                    onAction = viewModel::refresh,
                    variant = DfEmptyVariant.Error,
                )
            }
            else -> {
                DfEmptyState(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    title = "صحنه بارگذاری نشد",
                    subtitle = "قالب یا Manifest نامعتبر است.",
                    actionLabel = "بارگذاری مجدد",
                    onAction = viewModel::refresh,
                )
            }
        }
    }
}

@Composable
private fun LoadingBlock(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text("در حال بارگذاری…", modifier = Modifier.padding(top = AppSpacing.md))
    }
}
