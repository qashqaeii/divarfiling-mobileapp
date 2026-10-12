package ir.divarfiling.mobile.feature.contracts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContractWizardGetDto
import ir.divarfiling.mobile.core.network.ContractWizardPatchDto
import ir.divarfiling.mobile.core.network.ContractWizardPickerItemDto
import ir.divarfiling.mobile.core.network.ContractWizardStepDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ContractsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class ContractWizardUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val snackbar: String? = null,
    val contractId: Long = 0,
    val contractType: String = "",
    val contractTypeLabel: String = "",
    val internalNumber: String = "",
    val statusLabel: String = "",
    val canEdit: Boolean = false,
    val pickerEnabled: Boolean = true,
    val displaySteps: List<ContractWizardStepDto> = emptyList(),
    val stepIndex: Int = 0,
    val validationErrors: List<String> = emptyList(),
    val updatedAt: String? = null,
    val workspaceWizardPath: String = "",
    val webBridgeFallback: String? = null,
    val parties: List<WizardPartyDraft> = emptyList(),
    val property: WizardPropertyDraft = WizardPropertyDraft(),
    val leaseFinancial: WizardLeaseFinancialDraft = WizardLeaseFinancialDraft(),
    val saleFinancial: WizardSaleFinancialDraft = WizardSaleFinancialDraft(),
    val jvFinancial: WizardJvFinancialDraft = WizardJvFinancialDraft(),
    val witnesses: List<WizardWitnessDraft> = emptyList(),
    val clauses: WizardClausesDraft = WizardClausesDraft(),
    val optionalClauseOptions: List<Pair<String, String>> = emptyList(),
    val clauseUi: JsonObject? = null,
    val status: String = "",
    val isActionLoading: Boolean = false,
    val pickerLoading: Boolean = false,
    val pickerItems: List<ContractWizardPickerItemDto> = emptyList(),
    val pickerQuery: String = "",
    val pickerTarget: WizardPickerTarget? = null,
    val pickerPartyIndex: Int? = null,
    val pickerApplying: Boolean = false,
    val pendingStepIndex: Int? = null,
    val showSaveBeforeLeaveDialog: Boolean = false,
    val choices: JsonObject = buildJsonObject { },
    val clauseWarnings: List<String> = emptyList(),
    val showPrepareSendConfirm: Boolean = false,
    val reviewContext: JsonObject? = null,
    val pdfAvailable: Boolean = false,
    val openPdf: Boolean = false,
    val showVersionConflictDialog: Boolean = false,
)

enum class WizardPickerTarget { Party, Property }

private val PATCHABLE_WIZARD_STEPS = setOf("parties", "property", "financial", "witnesses", "clauses")

@HiltViewModel
class ContractWizardViewModel @Inject constructor(
    private val repository: ContractsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contractId: Long = savedStateHandle.get<Long>("contractId") ?: 0L
    private val _uiState = MutableStateFlow(ContractWizardUiState(contractId = contractId))
    val uiState: StateFlow<ContractWizardUiState> = _uiState.asStateFlow()

    private val savedStepSnapshots = mutableMapOf<String, String>()

    init {
        if (contractId > 0) load()
    }

    fun load(stepKey: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getWizard(contractId, stepKey)) {
                is ApiResult.Success -> applyWizard(result.data, replaceDrafts = true)
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    private fun applyWizard(dto: ContractWizardGetDto, replaceDrafts: Boolean) {
        val roles = defaultRolesForType(dto.contractType)
        val optional = parseOptionalClauses(dto.clauseUi)
        val stepIndex = dto.displaySteps.indexOfFirst { it.key == dto.currentStep }.coerceAtLeast(0)
        _uiState.update { state ->
            val next = state.copy(
                isLoading = false,
                contractId = dto.contractId,
                contractType = dto.contractType,
                contractTypeLabel = dto.contractTypeLabel,
                internalNumber = dto.internalNumber,
                statusLabel = dto.statusLabel,
                status = dto.status,
                canEdit = dto.canEdit,
                clauseUi = dto.clauseUi,
                pickerEnabled = dto.pickerEnabled,
                displaySteps = dto.displaySteps,
                stepIndex = stepIndex,
                validationErrors = dto.validationErrors,
                updatedAt = dto.updatedAt,
                workspaceWizardPath = dto.workspacePaths.wizard,
                optionalClauseOptions = optional,
                choices = dto.choices,
                clauseWarnings = parseClauseWarnings(dto.clauseUi),
                reviewContext = dto.reviewContext,
                pdfAvailable = dto.pdfAvailable,
            )
            if (!replaceDrafts) next else next.copy(
                parties = partiesFromWizardData(dto.wizardData, roles),
                property = propertyFromWizardData(dto.wizardData),
                leaseFinancial = leaseFinancialFromWizard(dto.wizardData),
                saleFinancial = saleFinancialFromWizard(dto.wizardData),
                jvFinancial = jvFinancialFromWizard(dto.wizardData),
                witnesses = witnessesFromWizard(dto.wizardData),
                clauses = mergeClauseDraftFromWizard(dto.wizardData, dto.clauseUi),
            )
        }
        if (replaceDrafts) {
            refreshAllSnapshots(_uiState.value)
        }
    }

    private fun parseClauseWarnings(clauseUi: JsonObject?): List<String> {
        val arr = clauseUi?.get("clause_step_warnings")?.jsonArray ?: return emptyList()
        return arr.mapNotNull { it.jsonPrimitive.contentOrNull?.takeIf { w -> w.isNotBlank() } }
    }

    fun goToStepByKey(key: String) {
        val idx = _uiState.value.displaySteps.indexOfFirst { it.key == key }
        if (idx >= 0) goToStep(idx)
    }

    fun requestPrepareSend() {
        if (_uiState.value.validationErrors.isNotEmpty()) return
        _uiState.update { it.copy(showPrepareSendConfirm = true) }
    }

    fun dismissPrepareSendConfirm() = _uiState.update { it.copy(showPrepareSendConfirm = false) }

    fun dismissVersionConflictDialog() = _uiState.update { it.copy(showVersionConflictDialog = false) }

    fun reloadWizardAfterConflict() {
        dismissVersionConflictDialog()
        load(currentStepKey)
    }

    fun confirmPrepareSend() {
        _uiState.update { it.copy(showPrepareSendConfirm = false) }
        prepareSend()
    }

    private fun refreshClauseUi() {
        viewModelScope.launch {
            when (val result = repository.getWizard(contractId, "clauses")) {
                is ApiResult.Success -> {
                    val dto = result.data
                    _uiState.update { state ->
                        state.copy(
                            clauseUi = dto.clauseUi,
                            clauseWarnings = parseClauseWarnings(dto.clauseUi),
                            optionalClauseOptions = parseOptionalClauses(dto.clauseUi),
                            clauses = mergeClauseDraftFromWizard(dto.wizardData, dto.clauseUi),
                        )
                    }
                    markSnapshotSaved("clauses")
                }
                is ApiResult.Error -> Unit
            }
        }
    }

    private fun parseOptionalClauses(clauseUi: JsonObject?): List<Pair<String, String>> {
        val arr = clauseUi?.get("available_clauses")?.jsonArray ?: return emptyList()
        return arr.mapNotNull { el ->
            val o = el.jsonObject
            val code = o["code"]?.jsonPrimitive?.contentOrNull.orEmpty()
            val title = o["title"]?.jsonPrimitive?.contentOrNull
                ?: o["label"]?.jsonPrimitive?.contentOrNull
                ?: code
            if (code.isBlank()) null else code to title
        }
    }

    val currentStepKey: String?
        get() {
            val steps = _uiState.value.displaySteps
            val idx = _uiState.value.stepIndex
            return steps.getOrNull(idx)?.key
        }

    fun goToStep(index: Int) {
        val steps = _uiState.value.displaySteps
        if (index !in steps.indices || index == _uiState.value.stepIndex) return
        if (_uiState.value.isSaving) return
        val step = currentStepKey
        if (step == "review" || !_uiState.value.canEdit || step !in PATCHABLE_WIZARD_STEPS) {
            setStepIndex(index)
            return
        }
        if (isCurrentStepDirty()) {
            _uiState.update {
                it.copy(pendingStepIndex = index, showSaveBeforeLeaveDialog = true)
            }
        } else {
            setStepIndex(index)
        }
    }

    fun confirmSaveAndLeaveStep() {
        val target = _uiState.value.pendingStepIndex ?: return
        _uiState.update {
            it.copy(showSaveBeforeLeaveDialog = false)
        }
        saveCurrentStep(targetStepIndex = target)
    }

    fun cancelLeaveStep() {
        _uiState.update {
            it.copy(pendingStepIndex = null, showSaveBeforeLeaveDialog = false)
        }
    }

    fun goNext() {
        val next = _uiState.value.stepIndex + 1
        if (next < _uiState.value.displaySteps.size) goToStep(next)
    }

    fun goPrevious() {
        val prev = _uiState.value.stepIndex - 1
        if (prev >= 0) goToStep(prev)
    }

    private fun setStepIndex(index: Int) {
        _uiState.update { it.copy(stepIndex = index, pendingStepIndex = null) }
        val key = _uiState.value.displaySteps.getOrNull(index)?.key
        if (key == "review") {
            load("review")
        }
    }

    fun requestOpenPdf() = _uiState.update { it.copy(openPdf = true) }

    fun consumeOpenPdf() = _uiState.update { it.copy(openPdf = false) }

    fun updateParty(index: Int, party: WizardPartyDraft) {
        _uiState.update { state ->
            val list = state.parties.toMutableList()
            if (index in list.indices) list[index] = party
            state.copy(parties = list)
        }
    }

    fun addParty(role: String) {
        if (role.isBlank()) return
        _uiState.update { it.copy(parties = it.parties + WizardPartyDraft(role = role)) }
    }

    fun removeParty(index: Int) {
        val state = _uiState.value
        if (!canRemoveWizardParty(state.parties, index, state.contractType)) return
        _uiState.update {
            it.copy(parties = it.parties.filterIndexed { i, _ -> i != index })
        }
    }

    fun clearPartyCustomer(index: Int) {
        val party = _uiState.value.parties.getOrNull(index) ?: return
        updateParty(index, party.copy(customerId = ""))
    }

    fun updateProperty(property: WizardPropertyDraft) = _uiState.update { it.copy(property = property) }
    fun updateLeaseFinancial(f: WizardLeaseFinancialDraft) = _uiState.update { it.copy(leaseFinancial = f) }
    fun updateSaleFinancial(f: WizardSaleFinancialDraft) = _uiState.update { it.copy(saleFinancial = f) }
    fun updateJvFinancial(f: WizardJvFinancialDraft) = _uiState.update { it.copy(jvFinancial = f) }
    fun updateWitness(index: Int, witness: WizardWitnessDraft) {
        _uiState.update { state ->
            val list = state.witnesses.toMutableList()
            if (index in list.indices) list[index] = witness
            state.copy(witnesses = list)
        }
    }

    fun updateClauses(clauses: WizardClausesDraft) = _uiState.update { it.copy(clauses = clauses) }

    fun toggleOptionalClause(code: String) {
        _uiState.update { state ->
            val next = state.clauses.selectedOptionalCodes.toMutableSet()
            if (code in next) next.remove(code) else next.add(code)
            state.copy(clauses = state.clauses.copy(selectedOptionalCodes = next))
        }
    }

    fun saveCurrentStep(advance: Boolean = false) {
        val target = if (advance) {
            (_uiState.value.stepIndex + 1).takeIf {
                it < _uiState.value.displaySteps.size
            }
        } else {
            null
        }
        saveCurrentStep(targetStepIndex = target)
    }

    private fun saveCurrentStep(targetStepIndex: Int? = null) {
        if (_uiState.value.isSaving) return
        val step = currentStepKey ?: return
        if (step == "review") {
            targetStepIndex?.let { setStepIndex(it) }
            return
        }
        if (step !in PATCHABLE_WIZARD_STEPS) {
            targetStepIndex?.let { setStepIndex(it) }
            return
        }
        if (!isCurrentStepDirty()) {
            targetStepIndex?.let { setStepIndex(it) }
            clearLeavePrompt()
            return
        }
        val payload = buildStepPayload(step) ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (
                val result = repository.patchWizard(
                    contractId,
                    step,
                    payload,
                    _uiState.value.updatedAt,
                )
            ) {
                is ApiResult.Success -> {
                    mergePatchResult(step, result.data)
                    markSnapshotSaved(step)
                    if (step == "clauses") {
                        refreshClauseUi()
                    }
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            snackbar = "ذخیره شد",
                        )
                    }
                    val navigateTo = targetStepIndex ?: _uiState.value.pendingStepIndex
                    if (navigateTo != null && navigateTo in _uiState.value.displaySteps.indices) {
                        setStepIndex(navigateTo)
                    }
                    clearLeavePrompt()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            snackbar = if (result.code == "CONFLICT") null else result.message,
                            showVersionConflictDialog = result.code == "CONFLICT",
                            pendingStepIndex = if (result.code == "CONFLICT") null else it.pendingStepIndex,
                            showSaveBeforeLeaveDialog = result.code != "CONFLICT" && it.pendingStepIndex != null,
                        )
                    }
                }
            }
        }
    }

    private fun clearLeavePrompt() {
        _uiState.update {
            it.copy(pendingStepIndex = null, showSaveBeforeLeaveDialog = false)
        }
    }

    private fun mergePatchResult(step: String, dto: ContractWizardPatchDto) {
        val wizard = dto.wizardData
        val roles = defaultRolesForType(_uiState.value.contractType)
        _uiState.update { state ->
            val merged = when (step) {
                "parties" -> state.copy(parties = partiesFromWizardData(wizard, roles))
                "property" -> state.copy(property = propertyFromWizardData(wizard))
                "financial" -> when (state.contractType) {
                    "lease" -> state.copy(leaseFinancial = leaseFinancialFromWizard(wizard))
                    "jv" -> state.copy(jvFinancial = jvFinancialFromWizard(wizard))
                    else -> state.copy(saleFinancial = saleFinancialFromWizard(wizard))
                }
                "witnesses" -> state.copy(witnesses = witnessesFromWizard(wizard))
                "clauses" -> state.copy(clauses = mergeClauseDraftFromWizard(wizard, state.clauseUi))
                else -> state
            }
            merged.copy(
                updatedAt = dto.updatedAt,
                validationErrors = dto.validationErrors,
            )
        }
        markSnapshotSaved(step)
    }

    private fun buildStepPayload(step: String): JsonElement? =
        buildStepPayload(_uiState.value, step)

    private fun buildStepPayload(state: ContractWizardUiState, step: String): JsonElement? =
        when (step) {
            "parties" -> partiesToJson(state.parties)
            "property" -> propertyToJson(state.property)
            "financial" -> when (state.contractType) {
                "lease" -> leaseFinancialToJson(state.leaseFinancial)
                "jv" -> jvFinancialToJson(state.jvFinancial)
                else -> saleFinancialToJson(state.saleFinancial)
            }
            "witnesses" -> witnessesToJson(state.witnesses)
            "clauses" -> clausesToJson(state.clauses, state.contractType)
            else -> null
        }

    private fun stepPayloadFingerprint(state: ContractWizardUiState, step: String): String? =
        buildStepPayload(state, step)?.toString()

    private fun isCurrentStepDirty(): Boolean {
        val step = currentStepKey ?: return false
        if (step !in PATCHABLE_WIZARD_STEPS) return false
        val current = stepPayloadFingerprint(_uiState.value, step) ?: return false
        return current != savedStepSnapshots[step]
    }

    private fun refreshAllSnapshots(state: ContractWizardUiState) {
        PATCHABLE_WIZARD_STEPS.forEach { step ->
            stepPayloadFingerprint(state, step)?.let { savedStepSnapshots[step] = it }
        }
    }

    private fun markSnapshotSaved(step: String) {
        stepPayloadFingerprint(_uiState.value, step)?.let { savedStepSnapshots[step] = it }
    }

    fun openPickerForParty(partyIndex: Int) {
        if (partyIndex !in _uiState.value.parties.indices) return
        _uiState.update {
            it.copy(
                pickerTarget = WizardPickerTarget.Party,
                pickerPartyIndex = partyIndex,
                pickerQuery = "",
                pickerItems = emptyList(),
            )
        }
        searchPicker("")
    }

    fun openPickerForProperty() {
        _uiState.update {
            it.copy(
                pickerTarget = WizardPickerTarget.Property,
                pickerPartyIndex = null,
                pickerQuery = "",
                pickerItems = emptyList(),
            )
        }
        searchPicker("")
    }

    fun closePicker() = _uiState.update {
        it.copy(
            pickerTarget = null,
            pickerPartyIndex = null,
            pickerItems = emptyList(),
            pickerApplying = false,
        )
    }

    fun onPickerQueryChange(query: String) {
        _uiState.update { it.copy(pickerQuery = query) }
        searchPicker(query)
    }

    private fun searchPicker(query: String) {
        val target = _uiState.value.pickerTarget ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(pickerLoading = true) }
            val result = when (target) {
                WizardPickerTarget.Party -> repository.searchWizardCustomers(query)
                WizardPickerTarget.Property -> repository.searchWizardProperties(query)
            }
            _uiState.update { state ->
                when (result) {
                    is ApiResult.Success -> state.copy(pickerLoading = false, pickerItems = result.data.results)
                    is ApiResult.Error -> state.copy(pickerLoading = false, error = result.message)
                }
            }
        }
    }

    fun applyPickerItem(item: ContractWizardPickerItemDto) {
        when (_uiState.value.pickerTarget) {
            WizardPickerTarget.Party -> applyPartyPickerItem(item)
            WizardPickerTarget.Property -> applyPropertyPickerItem(item)
            null -> Unit
        }
    }

    private fun applyPartyPickerItem(item: ContractWizardPickerItemDto) {
        val idx = _uiState.value.pickerPartyIndex
        if (idx == null || idx !in _uiState.value.parties.indices) {
            closePicker()
            return
        }
        val role = _uiState.value.parties[idx].role
        viewModelScope.launch {
            _uiState.update { it.copy(pickerApplying = true) }
            when (val result = repository.getWizardCustomerParty(item.id)) {
                is ApiResult.Success -> {
                    val draft = partyDraftFromCustomerPayload(role, result.data.party)
                    updateParty(idx, draft)
                    _uiState.update { it.copy(pickerApplying = false) }
                    closePicker()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            pickerApplying = false,
                            snackbar = result.message,
                        )
                    }
                }
            }
        }
    }

    private fun applyPropertyPickerItem(item: ContractWizardPickerItemDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(pickerApplying = true) }
            when (val result = repository.getWizardPropertyPayload(item.id)) {
                is ApiResult.Success -> {
                    updateProperty(propertyDraftFromWizardPayload(result.data.property))
                    _uiState.update { it.copy(pickerApplying = false) }
                    closePicker()
                }
                is ApiResult.Error -> {
                    updateProperty(
                        _uiState.value.property.copy(
                            crmPropertyId = item.id.toString(),
                            crmPropertyTitle = item.displayTitle(),
                            address = item.displaySubtitle(),
                        ),
                    )
                    _uiState.update {
                        it.copy(pickerApplying = false, snackbar = result.message)
                    }
                    closePicker()
                }
            }
        }
    }

    fun prepareSend() {
        if (_uiState.value.isActionLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActionLoading = true) }
            when (val result = repository.contractAction(contractId, "prepare_send")) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            snackbar = "قرارداد برای طرفین ارسال شد",
                            status = result.data.status,
                            statusLabel = result.data.statusLabel,
                            canEdit = result.data.canEdit,
                            validationErrors = result.data.validationErrors,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isActionLoading = false, snackbar = result.message)
                    }
                }
            }
        }
    }

    fun openWebFallback() {
        val path = _uiState.value.workspaceWizardPath
        if (path.isBlank()) return
        viewModelScope.launch {
            when (val bridge = repository.createWorkspaceBridge(path)) {
                is ApiResult.Success -> _uiState.update { it.copy(webBridgeFallback = bridge.data.bridgeUrl) }
                is ApiResult.Error -> _uiState.update { it.copy(error = bridge.message) }
            }
        }
    }

    fun consumeWebBridge() = _uiState.update { it.copy(webBridgeFallback = null) }
    fun consumeSnackbar() = _uiState.update { it.copy(snackbar = null) }
}
