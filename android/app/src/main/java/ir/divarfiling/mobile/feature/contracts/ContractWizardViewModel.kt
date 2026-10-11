package ir.divarfiling.mobile.feature.contracts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.network.ContractWizardGetDto
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
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
    val pickerLoading: Boolean = false,
    val pickerItems: List<ContractWizardPickerItemDto> = emptyList(),
    val pickerQuery: String = "",
    val pickerTarget: WizardPickerTarget? = null,
)

enum class WizardPickerTarget { Party, Property }

@HiltViewModel
class ContractWizardViewModel @Inject constructor(
    private val repository: ContractsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val contractId: Long = savedStateHandle.get<Long>("contractId") ?: 0L
    private val _uiState = MutableStateFlow(ContractWizardUiState(contractId = contractId))
    val uiState: StateFlow<ContractWizardUiState> = _uiState.asStateFlow()

    init {
        if (contractId > 0) load()
    }

    fun load(stepKey: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getWizard(contractId, stepKey)) {
                is ApiResult.Success -> applyWizard(result.data)
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    private fun applyWizard(dto: ContractWizardGetDto) {
        val roles = defaultRolesForType(dto.contractType)
        val optional = parseOptionalClauses(dto.clauseUi)
        val stepIndex = dto.displaySteps.indexOfFirst { it.key == dto.currentStep }.coerceAtLeast(0)
        _uiState.update {
            it.copy(
                isLoading = false,
                contractId = dto.contractId,
                contractType = dto.contractType,
                contractTypeLabel = dto.contractTypeLabel,
                internalNumber = dto.internalNumber,
                statusLabel = dto.statusLabel,
                canEdit = dto.canEdit,
                pickerEnabled = dto.pickerEnabled,
                displaySteps = dto.displaySteps,
                stepIndex = stepIndex,
                validationErrors = dto.validationErrors,
                updatedAt = dto.updatedAt,
                workspaceWizardPath = dto.workspacePaths.wizard,
                parties = partiesFromWizardData(dto.wizardData, roles),
                property = propertyFromWizardData(dto.wizardData),
                leaseFinancial = leaseFinancialFromWizard(dto.wizardData),
                saleFinancial = saleFinancialFromWizard(dto.wizardData),
                jvFinancial = jvFinancialFromWizard(dto.wizardData),
                witnesses = witnessesFromWizard(dto.wizardData),
                clauses = clausesFromWizard(dto.wizardData),
                optionalClauseOptions = optional,
            )
        }
    }

    private fun parseOptionalClauses(clauseUi: kotlinx.serialization.json.JsonObject?): List<Pair<String, String>> {
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
        if (index !in steps.indices) return
        _uiState.update { it.copy(stepIndex = index) }
        load(steps[index].key)
    }

    fun goNext() {
        val next = _uiState.value.stepIndex + 1
        if (next < _uiState.value.displaySteps.size) goToStep(next)
    }

    fun goPrevious() {
        val prev = _uiState.value.stepIndex - 1
        if (prev >= 0) goToStep(prev)
    }

    fun updateParty(index: Int, party: WizardPartyDraft) {
        _uiState.update { state ->
            val list = state.parties.toMutableList()
            if (index in list.indices) list[index] = party
            state.copy(parties = list)
        }
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
        val step = currentStepKey ?: return
        if (step == "review") {
            if (advance) goNext()
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
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            updatedAt = result.data.updatedAt,
                            validationErrors = result.data.validationErrors,
                            snackbar = "ذخیره شد",
                        )
                    }
                    if (advance) goNext() else load(step)
                }
                is ApiResult.Error -> {
                    if (result.code == "CONFLICT") {
                        _uiState.update {
                            it.copy(isSaving = false, error = result.message)
                        }
                        load(step)
                    } else {
                        _uiState.update {
                            it.copy(isSaving = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    private fun buildStepPayload(step: String): JsonElement? {
        val state = _uiState.value
        return when (step) {
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
    }

    fun openPicker(target: WizardPickerTarget) {
        _uiState.update { it.copy(pickerTarget = target, pickerQuery = "", pickerItems = emptyList()) }
        searchPicker("")
    }

    fun closePicker() = _uiState.update { it.copy(pickerTarget = null, pickerItems = emptyList()) }

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
            WizardPickerTarget.Party -> {
                val idx = _uiState.value.parties.indexOfFirst { it.customerId.isBlank() && it.firstName.isBlank() }
                    .takeIf { it >= 0 } ?: 0
                val current = _uiState.value.parties.getOrNull(idx) ?: WizardPartyDraft()
                updateParty(
                    idx,
                    current.copy(
                        customerId = item.id.toString(),
                        firstName = item.displayTitle().substringBefore(" ").ifBlank { item.displayTitle() },
                        lastName = item.displayTitle().substringAfter(" ", ""),
                        mobile = item.phone.orEmpty(),
                    ),
                )
            }
            WizardPickerTarget.Property -> {
                updateProperty(
                    _uiState.value.property.copy(
                        crmPropertyId = item.id.toString(),
                        crmPropertyTitle = item.displayTitle(),
                        address = item.displaySubtitle(),
                    ),
                )
            }
            null -> Unit
        }
        closePicker()
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
