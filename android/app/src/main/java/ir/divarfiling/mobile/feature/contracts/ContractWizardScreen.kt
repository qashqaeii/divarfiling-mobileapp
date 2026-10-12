package ir.divarfiling.mobile.feature.contracts

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfDetailPageHeader
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfErrorBanner
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.components.DfTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractWizardScreen(
    onBack: () -> Unit,
    onNavigateWebBridge: (String) -> Unit,
    onFinished: () -> Unit,
    onOpenPdf: (Long, String) -> Unit = { _, _ -> },
    viewModel: ContractWizardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let {
            snackbarHost.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    LaunchedEffect(state.webBridgeFallback) {
        state.webBridgeFallback?.let {
            onNavigateWebBridge(it)
            viewModel.consumeWebBridge()
        }
    }

    LaunchedEffect(state.openPdf) {
        if (state.openPdf) {
            onOpenPdf(
                state.contractId,
                state.internalNumber.ifBlank { state.contractTypeLabel },
            )
            viewModel.consumeOpenPdf()
        }
    }

    if (state.showPrepareSendConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPrepareSendConfirm,
            title = { Text("ارسال قرارداد") },
            text = {
                Text(
                    "قرارداد برای طرفین آماده و دعوت‌نامه ارسال می‌شود. این عمل قابل بازگشت ساده نیست. ادامه می‌دهید؟",
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmPrepareSend) {
                    Text("تأیید و ارسال")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPrepareSendConfirm) {
                    Text("انصراف")
                }
            },
        )
    }

    if (state.showVersionConflictDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissVersionConflictDialog,
            title = { Text("تعارض نسخه") },
            text = {
                Text(
                    "نسخه جدیدتری از قرارداد روی سرور ذخیره شده است. برای ادامه، نسخه سرور را بارگذاری کنید یا ویرایش محلی را نگه دارید.",
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::reloadWizardAfterConflict) {
                    Text("بارگذاری از سرور")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissVersionConflictDialog) {
                    Text("ادامه ویرایش محلی")
                }
            },
        )
    }

    if (state.showSaveBeforeLeaveDialog) {
        AlertDialog(
            onDismissRequest = viewModel::cancelLeaveStep,
            title = { Text("ذخیره تغییرات") },
            text = {
                Text("قبل از رفتن به مرحله دیگر، تغییرات این مرحله ذخیره شود.")
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmSaveAndLeaveStep) {
                    Text("ذخیره و ادامه")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelLeaveStep) {
                    Text("انصراف")
                }
            },
        )
    }

    Scaffold(
        containerColor = DfThemeColors.background(),
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            DfDetailPageHeader(
                title = "تکمیل قرارداد",
                subtitle = listOfNotNull(
                    state.internalNumber.takeIf { it.isNotBlank() },
                    state.contractTypeLabel.takeIf { it.isNotBlank() },
                ).joinToString(" · "),
                onBack = onBack,
            )
        },
        bottomBar = {
            if (!state.isLoading && state.error == null) {
                WizardBottomBar(
                    stepIndex = state.stepIndex,
                    stepTotal = state.displaySteps.size,
                    isSaving = state.isSaving,
                    canEdit = state.canEdit,
                    onPrevious = viewModel::goPrevious,
                    onSave = { viewModel.saveCurrentStep(advance = false) },
                    onSaveNext = { viewModel.saveCurrentStep(advance = true) },
                    onFinish = onFinished,
                    isReview = viewModel.currentStepKey == "review",
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("در حال بارگذاری ویزارد…", color = DfThemeColors.textSecondary())
                }
            }
            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    DfErrorBanner(message = state.error.orEmpty())
                    DfSecondaryButton(text = "تلاش مجدد", onClick = { viewModel.load() }, modifier = Modifier.fillMaxWidth())
                    if (state.workspaceWizardPath.isNotBlank()) {
                        TextButton(onClick = viewModel::openWebFallback) {
                            Text("باز کردن در میزکار (fallback)")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        horizontal = AppSpacing.screenHorizontal,
                        vertical = AppSpacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    item {
                        WizardStepper(
                            steps = state.displaySteps,
                            activeIndex = state.stepIndex,
                            onStepClick = viewModel::goToStep,
                        )
                    }
                    if (state.validationErrors.isNotEmpty()) {
                        item {
                            Surface(
                                shape = AppShapes.Card,
                                color = DfThemeColors.warningContainer().copy(alpha = 0.5f),
                            ) {
                                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("موارد ناقص", style = AppTypography.labelLarge, fontWeight = FontWeight.Bold)
                                    state.validationErrors.take(8).forEach { err ->
                                        Text("• $err", style = AppTypography.labelSmall, color = DfThemeColors.textSecondary())
                                    }
                                }
                            }
                        }
                    }
                    item {
                        when (viewModel.currentStepKey) {
                            "parties" -> WizardPartiesStep(
                                parties = state.parties,
                                contractType = state.contractType,
                                canEdit = state.canEdit,
                                pickerEnabled = state.pickerEnabled,
                                choices = state.choices,
                                onPartyChange = viewModel::updateParty,
                                onPickContact = viewModel::openPickerForParty,
                                onAddParty = viewModel::addParty,
                                onRemoveParty = viewModel::removeParty,
                                onClearPartyCustomer = viewModel::clearPartyCustomer,
                            )
                            "property" -> WizardPropertyStep(
                                property = state.property,
                                canEdit = state.canEdit,
                                pickerEnabled = state.pickerEnabled,
                                choices = state.choices,
                                onChange = viewModel::updateProperty,
                                onPickProperty = viewModel::openPickerForProperty,
                            )
                            "financial" -> WizardFinancialStep(
                                contractType = state.contractType,
                                lease = state.leaseFinancial,
                                sale = state.saleFinancial,
                                jv = state.jvFinancial,
                                canEdit = state.canEdit,
                                choices = state.choices,
                                onLeaseChange = viewModel::updateLeaseFinancial,
                                onSaleChange = viewModel::updateSaleFinancial,
                                onJvChange = viewModel::updateJvFinancial,
                            )
                            "witnesses" -> WizardWitnessesStep(
                                witnesses = state.witnesses,
                                canEdit = state.canEdit,
                                onChange = viewModel::updateWitness,
                            )
                            "clauses" -> WizardClausesStep(
                                clauses = state.clauses,
                                contractType = state.contractType,
                                options = state.optionalClauseOptions,
                                clauseSections = clauseSectionsFromUi(state.clauseUi),
                                clauseWarnings = state.clauseWarnings,
                                canEdit = state.canEdit,
                                onChange = viewModel::updateClauses,
                                onToggleOptional = viewModel::toggleOptionalClause,
                            )
                            "review" -> WizardReviewStepRich(
                                reviewContext = state.reviewContext,
                                validationErrors = state.validationErrors,
                                statusLabel = state.statusLabel,
                                pdfAvailable = state.pdfAvailable,
                                onEditStep = viewModel::goToStepByKey,
                                onOpenPdf = viewModel::requestOpenPdf,
                                onPrepareSend = if (state.canEdit && state.status == "draft") {
                                    viewModel::requestPrepareSend
                                } else {
                                    null
                                },
                                prepareSendEnabled = state.canEdit,
                                prepareSendLoading = state.isActionLoading,
                            )
                            else -> DfEmptyState(title = "مرحله نامشخص", subtitle = "از stepper مرحله دیگری را انتخاب کنید")
                        }
                    }
                }
            }
        }
    }

    if (state.pickerTarget != null) {
        DfModalBottomSheet(onDismissRequest = viewModel::closePicker) {
            DfSheetScaffold(
                title = if (state.pickerTarget == WizardPickerTarget.Party) "انتخاب مخاطب CRM" else "انتخاب فایل CRM",
                onClose = viewModel::closePicker,
                scrollable = false,
            ) {
                DfTextField(
                    value = state.pickerQuery,
                    onValueChange = viewModel::onPickerQueryChange,
                    label = "جستجو",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.pickerApplying) {
                    Text("در حال بارگذاری مخاطب…", color = DfThemeColors.textMuted())
                } else if (state.pickerLoading) {
                    Text("در حال جستجو…", color = DfThemeColors.textMuted())
                } else if (state.pickerItems.isEmpty()) {
                    DfEmptyState(title = "نتیجه‌ای نیست", subtitle = "عبارت دیگری را امتحان کنید")
                } else {
                    state.pickerItems.forEach { item ->
                        Surface(
                            onClick = { viewModel.applyPickerItem(item) },
                            shape = AppShapes.CardSmall,
                            color = DfThemeColors.surfaceVariant().copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        ) {
                            Column(Modifier.padding(AppSpacing.sm)) {
                                Text(item.displayTitle(), fontWeight = FontWeight.SemiBold)
                                if (item.displaySubtitle().isNotBlank()) {
                                    Text(
                                        item.displaySubtitle(),
                                        style = AppTypography.labelSmall,
                                        color = DfThemeColors.textMuted(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardStepper(
    steps: List<ir.divarfiling.mobile.core.network.ContractWizardStepDto>,
    activeIndex: Int,
    onStepClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        steps.forEachIndexed { index, step ->
            val selected = index == activeIndex
            DfSoftChip(
                text = step.label.ifBlank { step.key },
                selected = selected,
                onClick = { onStepClick(index) },
            )
        }
    }
}

@Composable
private fun WizardBottomBar(
    stepIndex: Int,
    stepTotal: Int,
    isSaving: Boolean,
    canEdit: Boolean,
    onPrevious: () -> Unit,
    onSave: () -> Unit,
    onSaveNext: () -> Unit,
    onFinish: () -> Unit,
    isReview: Boolean,
) {
    Surface(shadowElevation = 6.dp, color = DfThemeColors.surface()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (stepIndex > 0) {
                DfSecondaryButton(
                    text = "قبلی",
                    onClick = onPrevious,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
            if (isReview) {
                DfPrimaryButton(
                    text = "بازگشت به جزئیات",
                    onClick = onFinish,
                    modifier = Modifier.weight(2f),
                )
            } else {
                DfSecondaryButton(
                    text = if (isSaving) "…" else "ذخیره",
                    onClick = onSave,
                    enabled = canEdit && !isSaving,
                    modifier = Modifier.weight(1f),
                )
                DfPrimaryButton(
                    text = if (isSaving) "در حال ذخیره…" else if (stepIndex >= stepTotal - 2) "ذخیره و مرور" else "ذخیره و بعدی",
                    onClick = onSaveNext,
                    enabled = canEdit && !isSaving,
                    modifier = Modifier.weight(2f),
                )
            }
        }
    }
}
