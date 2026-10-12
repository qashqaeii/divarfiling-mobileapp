package ir.divarfiling.mobile.feature.contracts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun ContractHandoverWizardScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    onOpenPdf: (Long, Long) -> Unit = { _, _ -> },
    viewModel: ContractHandoverWizardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadPhoto(it) }
    }
    LaunchedEffect(state.snackbar) {
        state.snackbar?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
    }
    LaunchedEffect(state.published) {
        if (state.published) onFinished()
    }
    val step = viewModel.currentStep
    val wizard = state.wizard
    val stepLabel = wizard?.get("display_steps")?.jsonArray?.firstOrNull { el ->
        val key = el.jsonObject["key"]?.jsonPrimitive?.contentOrNull
            ?: el.jsonArray.getOrNull(0)?.jsonPrimitive?.contentOrNull
        key == step
    }?.let { el ->
        el.jsonObject["label"]?.jsonPrimitive?.contentOrNull
            ?: el.jsonArray.getOrNull(1)?.jsonPrimitive?.contentOrNull
    } ?: step
    val enabled = state.canEdit

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            DfDetailPageHeader(
                title = "صورت‌جلسه تحویل",
                subtitle = stepLabel,
                onBack = onBack,
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(padding))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.error?.let { DfErrorBanner(message = it, modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal)) }
            if (!enabled) {
                DfStatusBanner(
                    message = buildString {
                        append("این صورت‌جلسه قابل ویرایش نیست.")
                        state.statusLabel.takeIf { it.isNotBlank() }?.let { append('\n').append(it) }
                    },
                    tone = DfStatusTone.Warning,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.xs),
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                item {
                    HandoverWizardStepper(wizard, step) { viewModel.goToStep(it) }
                }
                item {
                    when (step) {
                        "basic" -> HandoverBasicStepContent(state, enabled, viewModel::updateBasic)
                        "keys" -> HandoverKeysStepContent(
                            state = state,
                            enabled = enabled,
                            onCountDelta = viewModel::incrementKey,
                            onNotes = { i, v -> viewModel.updateKey(i, notes = v) },
                            onCustomTypeChange = viewModel::setCustomKeyType,
                            onAddCustom = viewModel::addCustomKey,
                            onKeyTypeChange = { i, v -> viewModel.updateKey(i, type = v) },
                        )
                        "meters" -> HandoverMetersStepContent(state, enabled, viewModel::updateMeter)
                        "conditions" -> Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            HandoverConditionsStepContent(
                                state = state,
                                enabled = enabled,
                                onStatus = { i, s -> viewModel.updateCondition(i, status = s) },
                                onNote = { i, n -> viewModel.updateCondition(i, note = n) },
                                onMarkAllOk = viewModel::markAllConditionsOk,
                                onReset = viewModel::resetConditions,
                                onUploadPhoto = { photoPicker.launch("image/*") },
                                photoCaption = state.photoCaption,
                                onPhotoCaption = viewModel::setPhotoCaption,
                            )
                            HandoverPhotosGrid(
                                contractId = viewModel.contractId,
                                photos = state.photos,
                                repository = viewModel.repository,
                            )
                        }
                        "equipment" -> HandoverEquipmentStepContent(
                            state = state,
                            enabled = enabled,
                            onChip = viewModel::addEquipmentFromChip,
                            onUpdate = viewModel::updateEquipment,
                            onAddRow = viewModel::addEquipmentRow,
                            onRemove = viewModel::removeEquipment,
                        )
                        "review" -> HandoverReviewStepContent(
                            state = state,
                            enabled = enabled,
                            onEditStep = viewModel::goToStep,
                            onOpenPdf = { onOpenPdf(viewModel.contractId, viewModel.handoverId) },
                            onAckChange = viewModel::setCreatorAck,
                        )
                        else -> Text("مرحله $step", style = AppTypography.bodyDescription)
                    }
                }
                item {
                    val prev = wizard?.get("prev_step")?.jsonPrimitive?.contentOrNull.orEmpty()
                    if (prev.isNotBlank() && step != "review") {
                        DfSecondaryButton(
                            text = "مرحله قبل",
                            onClick = { viewModel.goToStep(prev) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (step == "review") {
                        DfPrimaryButton(
                            text = if (state.isSaving) "در حال ارسال…" else "ارسال برای تأیید طرفین",
                            onClick = { viewModel.saveStep(advance = false, publish = true) },
                            enabled = enabled && !state.isSaving && state.creatorAck && !state.sendDisabled,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else if (enabled) {
                        DfPrimaryButton(
                            text = if (state.isSaving) "ذخیره…" else "ذخیره و ادامه",
                            onClick = { viewModel.saveStep(advance = true) },
                            enabled = !state.isSaving,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
