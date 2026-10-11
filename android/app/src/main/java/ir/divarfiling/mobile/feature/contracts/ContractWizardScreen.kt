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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    viewModel: ContractWizardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.webBridgeFallback) {
        state.webBridgeFallback?.let {
            onNavigateWebBridge(it)
            viewModel.consumeWebBridge()
        }
    }

    Scaffold(
        containerColor = DfThemeColors.background(),
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
                                canEdit = state.canEdit,
                                pickerEnabled = state.pickerEnabled,
                                onPartyChange = viewModel::updateParty,
                                onPickContact = { viewModel.openPicker(WizardPickerTarget.Party) },
                            )
                            "property" -> WizardPropertyStep(
                                property = state.property,
                                canEdit = state.canEdit,
                                pickerEnabled = state.pickerEnabled,
                                onChange = viewModel::updateProperty,
                                onPickProperty = { viewModel.openPicker(WizardPickerTarget.Property) },
                            )
                            "financial" -> WizardFinancialStep(
                                contractType = state.contractType,
                                lease = state.leaseFinancial,
                                sale = state.saleFinancial,
                                jv = state.jvFinancial,
                                canEdit = state.canEdit,
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
                                canEdit = state.canEdit,
                                onChange = viewModel::updateClauses,
                                onToggleOptional = viewModel::toggleOptionalClause,
                            )
                            "review" -> WizardReviewStep(
                                validationErrors = state.validationErrors,
                                statusLabel = state.statusLabel,
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
                if (state.pickerLoading) {
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

@Composable
private fun WizardPartiesStep(
    parties: List<WizardPartyDraft>,
    canEdit: Boolean,
    pickerEnabled: Boolean,
    onPartyChange: (Int, WizardPartyDraft) -> Unit,
    onPickContact: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (pickerEnabled && canEdit) {
            DfSecondaryButton(text = "انتخاب از CRM", onClick = onPickContact, modifier = Modifier.fillMaxWidth())
        }
        parties.forEachIndexed { index, party ->
            Surface(shape = AppShapes.Card, color = DfThemeColors.surface()) {
                Column(
                    Modifier.padding(AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(party.role, fontWeight = FontWeight.Bold, color = DfThemeColors.primary())
                    WizardField("نوع شخص", party.personType) { onPartyChange(index, party.copy(personType = it)) }
                    WizardField("نام", party.firstName) { onPartyChange(index, party.copy(firstName = it)) }
                    WizardField("نام خانوادگی", party.lastName) { onPartyChange(index, party.copy(lastName = it)) }
                    WizardField("کد ملی", party.nationalId) { onPartyChange(index, party.copy(nationalId = it)) }
                    WizardField("موبایل", party.mobile) { onPartyChange(index, party.copy(mobile = it)) }
                    WizardField("آدرس", party.address) { onPartyChange(index, party.copy(address = it)) }
                    if (party.personType == "legal") {
                        WizardField("نام شرکت", party.companyName) { onPartyChange(index, party.copy(companyName = it)) }
                        WizardField("شناسه ملی شرکت", party.companyNationalId) {
                            onPartyChange(index, party.copy(companyNationalId = it))
                        }
                        WizardField("نام نماینده", party.representativeName) {
                            onPartyChange(index, party.copy(representativeName = it))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WizardPropertyStep(
    property: WizardPropertyDraft,
    canEdit: Boolean,
    pickerEnabled: Boolean,
    onChange: (WizardPropertyDraft) -> Unit,
    onPickProperty: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (pickerEnabled && canEdit) {
            DfSecondaryButton(text = "انتخاب فایل CRM", onClick = onPickProperty, modifier = Modifier.fillMaxWidth())
        }
        if (property.crmPropertyTitle.isNotBlank()) {
            Text("فایل: ${property.crmPropertyTitle}", style = AppTypography.labelLarge)
        }
        WizardField("نوع ملک", property.propertyType) { onChange(property.copy(propertyType = it)) }
        WizardField("کاربری", property.usage) { onChange(property.copy(usage = it)) }
        WizardField("شهر", property.city) { onChange(property.copy(city = it)) }
        WizardField("منطقه", property.district) { onChange(property.copy(district = it)) }
        WizardField("محله", property.neighborhood) { onChange(property.copy(neighborhood = it)) }
        WizardField("آدرس", property.address) { onChange(property.copy(address = it)) }
        WizardField("کد پستی", property.postalCode) { onChange(property.copy(postalCode = it)) }
        WizardField("متراژ", property.area) { onChange(property.copy(area = it)) }
        WizardField("سال ساخت", property.buildYear) { onChange(property.copy(buildYear = it)) }
        WizardField("طبقه", property.floor) { onChange(property.copy(floor = it)) }
        WizardField("واحد", property.unit) { onChange(property.copy(unit = it)) }
        WizardField("اتاق", property.bedrooms) { onChange(property.copy(bedrooms = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            WizardToggle("پارکینگ", property.hasParking) { onChange(property.copy(hasParking = it)) }
            WizardToggle("انباری", property.hasStorage) { onChange(property.copy(hasStorage = it)) }
            WizardToggle("آسانسور", property.hasElevator) { onChange(property.copy(hasElevator = it)) }
        }
    }
}

@Composable
private fun WizardFinancialStep(
    contractType: String,
    lease: WizardLeaseFinancialDraft,
    sale: WizardSaleFinancialDraft,
    jv: WizardJvFinancialDraft,
    canEdit: Boolean,
    onLeaseChange: (WizardLeaseFinancialDraft) -> Unit,
    onSaleChange: (WizardSaleFinancialDraft) -> Unit,
    onJvChange: (WizardJvFinancialDraft) -> Unit,
) {
    when (contractType) {
        "lease" -> Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            WizardField("شروع اجاره", lease.leaseStart) { onLeaseChange(lease.copy(leaseStart = it)) }
            WizardField("پایان اجاره", lease.leaseEnd) { onLeaseChange(lease.copy(leaseEnd = it)) }
            WizardField("رهن (تومان)", lease.deposit) { onLeaseChange(lease.copy(deposit = it)) }
            WizardField("اجاره ماهانه", lease.monthlyRent) { onLeaseChange(lease.copy(monthlyRent = it)) }
            WizardField("روز پرداخت", lease.rentPaymentDay) { onLeaseChange(lease.copy(rentPaymentDay = it)) }
            WizardField("تحویل", lease.deliveryDate) { onLeaseChange(lease.copy(deliveryDate = it)) }
            WizardField("نوع استفاده", lease.usageType) { onLeaseChange(lease.copy(usageType = it)) }
            WizardField("تعداد ساکنین", lease.occupantsCount) { onLeaseChange(lease.copy(occupantsCount = it)) }
        }
        "jv" -> Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            WizardField("سهم مالک (%)", jv.ownerPercent) { onJvChange(jv.copy(ownerPercent = it)) }
            WizardField("سهم سازنده (%)", jv.builderPercent) { onJvChange(jv.copy(builderPercent = it)) }
            WizardField("شروع پروژه", jv.startDate) { onJvChange(jv.copy(startDate = it)) }
            WizardField("مدت تخمینی", jv.durationEstimate) { onJvChange(jv.copy(durationEstimate = it)) }
            WizardField("تحویل", jv.deliveryDate) { onJvChange(jv.copy(deliveryDate = it)) }
            WizardField("آورده مالک", jv.ownerContribution) { onJvChange(jv.copy(ownerContribution = it)) }
            WizardField("آورده سازنده", jv.builderContribution) { onJvChange(jv.copy(builderContribution = it)) }
        }
        else -> Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            WizardField("مبلغ کل (تومان)", sale.totalPrice) { onSaleChange(sale.copy(totalPrice = it)) }
            WizardField("پیش‌پرداخت", sale.downPayment) { onSaleChange(sale.copy(downPayment = it)) }
            WizardField("بیعانه", sale.earnestMoney) { onSaleChange(sale.copy(earnestMoney = it)) }
            WizardField("تاریخ تحویل", sale.deliveryDate) { onSaleChange(sale.copy(deliveryDate = it)) }
            WizardField("تاریخ انتقال سند", sale.transferDate) { onSaleChange(sale.copy(transferDate = it)) }
            WizardField("محل انتقال", sale.transferPlace) { onSaleChange(sale.copy(transferPlace = it)) }
        }
    }
}

@Composable
private fun WizardWitnessesStep(
    witnesses: List<WizardWitnessDraft>,
    canEdit: Boolean,
    onChange: (Int, WizardWitnessDraft) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        witnesses.forEachIndexed { index, w ->
            Surface(shape = AppShapes.Card, color = DfThemeColors.surface()) {
                Column(Modifier.padding(AppSpacing.sm), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(w.witnessRole, fontWeight = FontWeight.SemiBold)
                    WizardField("نام", w.firstName) { onChange(index, w.copy(firstName = it)) }
                    WizardField("نام خانوادگی", w.lastName) { onChange(index, w.copy(lastName = it)) }
                    WizardField("کد ملی", w.nationalId) { onChange(index, w.copy(nationalId = it)) }
                    WizardField("موبایل", w.mobile) { onChange(index, w.copy(mobile = it)) }
                }
            }
        }
    }
}

@Composable
private fun WizardClausesStep(
    clauses: WizardClausesDraft,
    contractType: String,
    options: List<Pair<String, String>>,
    canEdit: Boolean,
    onChange: (WizardClausesDraft) -> Unit,
    onToggleOptional: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        if (options.isNotEmpty()) {
            Text("بندهای اختیاری", fontWeight = FontWeight.Bold)
            options.forEach { (code, title) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Checkbox(
                        checked = code in clauses.selectedOptionalCodes,
                        onCheckedChange = { if (canEdit) onToggleOptional(code) },
                        enabled = canEdit,
                    )
                    Text(title, modifier = Modifier.weight(1f))
                }
            }
        }
        if (contractType == "lease") {
            WizardField("انتقال به غیر", clauses.transferChoice) { onChange(clauses.copy(transferChoice = it)) }
        }
        if (contractType == "sale") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ملک دارای مستأجر است")
                Switch(
                    checked = clauses.propertyHasTenant,
                    onCheckedChange = { if (canEdit) onChange(clauses.copy(propertyHasTenant = it)) },
                    enabled = canEdit,
                )
            }
        }
        HorizontalDivider()
        WizardField("عنوان توافق اختصاصی", clauses.customTitle) { onChange(clauses.copy(customTitle = it)) }
        DfTextField(
            value = clauses.customBody,
            onValueChange = { onChange(clauses.copy(customBody = it)) },
            label = "متن توافق اختصاصی",
            minLines = 4,
            singleLine = false,
            enabled = canEdit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WizardReviewStep(
    validationErrors: List<String>,
    statusLabel: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("مرور نهایی", style = AppTypography.sectionTitle, fontWeight = FontWeight.Bold)
        Text("وضعیت: $statusLabel", color = DfThemeColors.textSecondary())
        if (validationErrors.isEmpty()) {
            Text(
                "پیش‌نویس از نظر اعتبارسنجی اولیه آماده است. ارسال و تأیید طرفین از جزئیات قرارداد یا میزکار انجام می‌شود.",
                style = AppTypography.bodyDescription,
            )
        } else {
            validationErrors.forEach { Text("• $it", color = DfThemeColors.warning()) }
        }
    }
}

@Composable
private fun WizardField(label: String, value: String, onValueChange: (String) -> Unit) {
    DfTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun WizardToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = AppTypography.labelSmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
