package ir.divarfiling.mobile.feature.contracts

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ContractsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class HandoverPartyCard(
    val index: Int,
    val name: String,
    val roleLabel: String,
    val mobile: String,
)

data class HandoverWizardUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null,
    val snackbar: String? = null,
    val wizard: JsonObject? = null,
    val canEdit: Boolean = true,
    val status: String = "",
    val statusLabel: String = "",
    val pdfAvailable: Boolean = false,
    val sendDisabled: Boolean = false,
    val handoverDate: String = "",
    val handoverTime: String = "",
    val deliveredIdx: Int = 0,
    val receivedIdx: Int = 0,
    val notes: String = "",
    val delivererCards: List<HandoverPartyCard> = emptyList(),
    val receiverCards: List<HandoverPartyCard> = emptyList(),
    val keys: List<HandoverKeyDraft> = emptyList(),
    val customKeyType: String = "",
    val meters: List<HandoverMeterDraft> = emptyList(),
    val conditions: List<HandoverConditionDraft> = emptyList(),
    val conditionStatuses: List<Pair<String, String>> = emptyList(),
    val equipment: List<HandoverEquipmentDraft> = emptyList(),
    val commonEquipment: List<String> = emptyList(),
    val photos: List<HandoverPhotoItem> = emptyList(),
    val photoCaption: String = "",
    val creatorAck: Boolean = false,
    val published: Boolean = false,
)

data class HandoverKeyDraft(val type: String, val count: String, val notes: String, val isCustom: Boolean = false)
data class HandoverMeterDraft(val utility: String, val label: String, val number: String, val reading: String, val notes: String)
data class HandoverConditionDraft(val item: String, val status: String, val note: String)
data class HandoverEquipmentDraft(val name: String, val count: String, val status: String, val notes: String)
data class HandoverPhotoItem(val id: Long, val title: String)

@HiltViewModel
class ContractHandoverWizardViewModel @Inject constructor(
    internal val repository: ContractsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val contractId: Long = savedStateHandle.get<Long>("contractId") ?: 0L
    val handoverId: Long = savedStateHandle.get<Long>("handoverId") ?: 0L
    private val _uiState = MutableStateFlow(HandoverWizardUiState())
    val uiState: StateFlow<HandoverWizardUiState> = _uiState.asStateFlow()

    val currentStep: String
        get() = _uiState.value.wizard?.get("handover_step")?.jsonPrimitive?.contentOrNull ?: "basic"

    init {
        if (contractId > 0 && handoverId > 0) load()
    }

    fun load(step: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getHandoverWizard(contractId, handoverId, step)) {
                is ApiResult.Success -> applyEnvelope(result.data)
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    private fun applyEnvelope(envelope: ContractsRepository.HandoverWizardEnvelope) {
        applyWizard(envelope.wizard)
        _uiState.update {
            it.copy(
                canEdit = envelope.canEdit,
                status = envelope.status,
                statusLabel = envelope.statusLabel,
                pdfAvailable = envelope.pdfAvailable,
                sendDisabled = envelope.sendDisabled,
                published = envelope.published,
            )
        }
    }

    private fun applyWizard(wizard: JsonObject) {
        val keys = wizard["key_rows"]?.jsonArray?.map { el ->
            val o = el.jsonObject
            HandoverKeyDraft(
                type = o.string("type"),
                count = o.string("count").ifBlank { "0" },
                notes = o.string("notes"),
                isCustom = o.string("is_custom") == "true" || o.get("is_custom")?.jsonPrimitive?.contentOrNull == "true",
            )
        }.orEmpty()
        val meters = wizard["meter_rows"]?.jsonArray?.map { el ->
            val o = el.jsonObject
            HandoverMeterDraft(
                utility = o.string("utility"),
                label = o.string("label"),
                number = o.string("meter_number"),
                reading = o.string("reading"),
                notes = o.string("notes"),
            )
        }.orEmpty()
        val conditions = wizard["condition_rows"]?.jsonArray?.map { el ->
            val o = el.jsonObject
            HandoverConditionDraft(
                item = o.string("item"),
                status = o.string("status").ifBlank { "unchecked" },
                note = o.string("note"),
            )
        }.orEmpty()
        val equipment = wizard["equipment_rows"]?.jsonArray?.map { el ->
            val o = el.jsonObject
            HandoverEquipmentDraft(
                name = o.string("name"),
                count = o.string("count").ifBlank { "1" },
                status = o.string("status").ifBlank { "ok" },
                notes = o.string("notes"),
            )
        }.orEmpty().ifEmpty { listOf(HandoverEquipmentDraft("", "1", "ok", "")) }
        val delivererCards = wizard.parsePartyCards("deliverer_party_cards")
        val receiverCards = wizard.parsePartyCards("receiver_party_cards")
        val statuses = wizard["condition_statuses"]?.jsonArray?.mapNotNull { el ->
            val o = el.jsonObject
            val v = o.string("value")
            val l = o.string("label")
            if (v.isNotBlank()) v to l else null
        }.orEmpty()
        val commonEq = wizard["common_equipment"]?.jsonArray?.mapNotNull {
            it.jsonPrimitive.contentOrNull
        }.orEmpty()
        val photos = wizard["photos"]?.jsonArray?.mapNotNull { el ->
            val o = el.jsonObject
            val id = o.get("id")?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: return@mapNotNull null
            HandoverPhotoItem(id, o.string("title"))
        }.orEmpty()
        _uiState.update {
            it.copy(
                isLoading = false,
                wizard = wizard,
                handoverDate = wizard.string("handover_date_display"),
                handoverTime = wizard.string("handover_time_display"),
                deliveredIdx = wizard.string("delivered_idx").toIntOrNull()
                    ?: wizard.get("delivered_idx")?.jsonPrimitive?.intOrNull ?: 0,
                receivedIdx = wizard.string("received_idx").toIntOrNull()
                    ?: wizard.get("received_idx")?.jsonPrimitive?.intOrNull ?: 0,
                notes = wizard.string("notes_value"),
                keys = keys,
                meters = meters,
                conditions = conditions,
                conditionStatuses = statuses,
                equipment = equipment,
                delivererCards = delivererCards,
                receiverCards = receiverCards,
                commonEquipment = commonEq,
                photos = photos,
            )
        }
    }

    fun updateBasic(date: String? = null, time: String? = null, delivered: Int? = null, received: Int? = null, notes: String? = null) {
        _uiState.update {
            it.copy(
                handoverDate = date ?: it.handoverDate,
                handoverTime = time ?: it.handoverTime,
                deliveredIdx = delivered ?: it.deliveredIdx,
                receivedIdx = received ?: it.receivedIdx,
                notes = notes ?: it.notes,
            )
        }
    }

    fun updateKey(index: Int, count: String? = null, notes: String? = null, type: String? = null) {
        _uiState.update { state ->
            val list = state.keys.toMutableList()
            if (index !in list.indices) return@update state
            val cur = list[index]
            list[index] = cur.copy(
                count = count ?: cur.count,
                notes = notes ?: cur.notes,
                type = type ?: cur.type,
            )
            state.copy(keys = list)
        }
    }

    fun incrementKey(index: Int, delta: Int) {
        val cur = _uiState.value.keys.getOrNull(index)?.count?.toIntOrNull() ?: 0
        updateKey(index, count = (cur + delta).coerceAtLeast(0).toString())
    }

    fun setCustomKeyType(value: String) = _uiState.update { it.copy(customKeyType = value) }

    fun addCustomKey() {
        val type = _uiState.value.customKeyType.trim()
        if (type.isBlank()) return
        _uiState.update {
            it.copy(
                keys = it.keys + HandoverKeyDraft(type, "1", "", isCustom = true),
                customKeyType = "",
            )
        }
    }

    fun updateMeter(index: Int, number: String? = null, reading: String? = null, notes: String? = null) {
        _uiState.update { state ->
            val list = state.meters.toMutableList()
            if (index !in list.indices) return@update state
            val cur = list[index]
            list[index] = cur.copy(
                number = number ?: cur.number,
                reading = reading ?: cur.reading,
                notes = notes ?: cur.notes,
            )
            state.copy(meters = list)
        }
    }

    fun updateCondition(index: Int, status: String? = null, note: String? = null) {
        _uiState.update { state ->
            val list = state.conditions.toMutableList()
            if (index !in list.indices) return@update state
            val cur = list[index]
            list[index] = cur.copy(status = status ?: cur.status, note = note ?: cur.note)
            state.copy(conditions = list)
        }
    }

    fun markAllConditionsOk() {
        _uiState.update { state ->
            state.copy(conditions = state.conditions.map { it.copy(status = "ok") })
        }
    }

    fun resetConditions() {
        _uiState.update { state ->
            state.copy(conditions = state.conditions.map { it.copy(status = "unchecked", note = "") })
        }
    }

    fun updateEquipment(index: Int, name: String? = null, count: String? = null, status: String? = null, notes: String? = null) {
        _uiState.update { state ->
            val list = state.equipment.toMutableList()
            if (index !in list.indices) return@update state
            val cur = list[index]
            list[index] = cur.copy(
                name = name ?: cur.name,
                count = count ?: cur.count,
                status = status ?: cur.status,
                notes = notes ?: cur.notes,
            )
            state.copy(equipment = list)
        }
    }

    fun addEquipmentFromChip(name: String) {
        _uiState.update { state ->
            if (state.equipment.any { it.name == name }) return@update state
            state.copy(equipment = state.equipment + HandoverEquipmentDraft(name, "1", "ok", ""))
        }
    }

    fun addEquipmentRow() {
        _uiState.update { it.copy(equipment = it.equipment + HandoverEquipmentDraft("", "1", "ok", "")) }
    }

    fun removeEquipment(index: Int) {
        _uiState.update { state ->
            val list = state.equipment.toMutableList()
            if (index !in list.indices) return@update state
            list.removeAt(index)
            state.copy(equipment = list.ifEmpty { listOf(HandoverEquipmentDraft("", "1", "ok", "")) })
        }
    }

    fun setPhotoCaption(value: String) = _uiState.update { it.copy(photoCaption = value) }

    fun setCreatorAck(value: Boolean) = _uiState.update { it.copy(creatorAck = value) }

    fun uploadPhoto(uri: Uri) {
        if (!_uiState.value.canEdit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true) }
            val caption = _uiState.value.photoCaption
            when (
                val result = repository.uploadAttachment(
                    contractId,
                    uri,
                    title = caption,
                    attachmentType = "property_photo",
                    handoverId = handoverId,
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            snackbar = "عکس آپلود شد",
                            photoCaption = "",
                        )
                    }
                    load(currentStep)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isUploadingPhoto = false, snackbar = result.message)
                }
            }
        }
    }

    fun goToStep(step: String) {
        if (step.isBlank() || step == currentStep) return
        load(step)
    }

    fun saveStep(advance: Boolean = true, publish: Boolean = false) {
        if (_uiState.value.isSaving) return
        if (!_uiState.value.canEdit && !publish) return
        val step = currentStep
        if (publish && (!_uiState.value.creatorAck || _uiState.value.sendDisabled)) return
        val payload = buildJsonObject {
            when (step) {
                "basic" -> {
                    put("handover_date", _uiState.value.handoverDate)
                    put("handover_time", _uiState.value.handoverTime)
                    put("delivered_by_idx", _uiState.value.deliveredIdx)
                    put("received_by_idx", _uiState.value.receivedIdx)
                    put("notes", _uiState.value.notes)
                }
                "keys" -> put(
                    "keys",
                    buildJsonArray {
                        _uiState.value.keys.forEach { k ->
                            add(
                                buildJsonObject {
                                    put("type", k.type)
                                    put("count", k.count.toIntOrNull() ?: 0)
                                    put("notes", k.notes)
                                },
                            )
                        }
                    },
                )
                "meters" -> put(
                    "meters",
                    buildJsonArray {
                        _uiState.value.meters.forEach { m ->
                            add(
                                buildJsonObject {
                                    put("utility", m.utility)
                                    put("meter_number", m.number)
                                    put("reading", m.reading)
                                    put("notes", m.notes)
                                },
                            )
                        }
                    },
                )
                "conditions" -> put(
                    "conditions",
                    buildJsonArray {
                        _uiState.value.conditions.forEach { c ->
                            add(
                                buildJsonObject {
                                    put("item", c.item)
                                    put("status", c.status)
                                    put("note", c.note)
                                },
                            )
                        }
                    },
                )
                "equipment" -> put(
                    "equipment",
                    buildJsonArray {
                        _uiState.value.equipment.filter { it.name.isNotBlank() }.forEach { e ->
                            add(
                                buildJsonObject {
                                    put("name", e.name)
                                    put("count", e.count)
                                    put("status", e.status)
                                    put("notes", e.notes)
                                },
                            )
                        }
                    },
                )
                "review" -> { /* publish only */ }
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (
                val result = repository.patchHandoverWizard(
                    contractId,
                    handoverId,
                    step,
                    payload,
                    publish = publish,
                )
            ) {
                is ApiResult.Success -> {
                    applyEnvelope(result.data)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            snackbar = when {
                                publish -> "صورت‌جلسه ارسال شد"
                                else -> "ذخیره شد"
                            },
                        )
                    }
                    if (advance && !publish) {
                        val next = result.data.wizard.string("next_step")
                        if (next.isNotBlank() && next != step) load(next)
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }

    fun consumeSnackbar() = _uiState.update { it.copy(snackbar = null) }

    private fun JsonObject.parsePartyCards(key: String): List<HandoverPartyCard> =
        get(key)?.jsonArray?.mapNotNull { el ->
            val o = el.jsonObject
            val idx = o.get("index")?.jsonPrimitive?.intOrNull ?: return@mapNotNull null
            HandoverPartyCard(
                index = idx,
                name = o.string("name").ifBlank {
                    listOf(o.string("first_name"), o.string("last_name")).filter { it.isNotBlank() }.joinToString(" ")
                },
                roleLabel = o.string("role_label"),
                mobile = o.string("mobile"),
            )
        }.orEmpty()

    private fun JsonObject.string(key: String): String =
        get(key)?.jsonPrimitive?.contentOrNull.orEmpty()
}
