package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.network.AiCapabilitiesData
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.NlCommandConfirmRequest
import ir.divarfiling.mobile.core.network.NlCommandConfirmResult
import ir.divarfiling.mobile.core.network.NlCommandParseRequest
import ir.divarfiling.mobile.core.network.NlCommandParseResult
import ir.divarfiling.mobile.core.network.NlCommandResolveContactRequest
import ir.divarfiling.mobile.core.network.NlCommandResolveResult
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toUserMessage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class SmartCommandRepository @Inject constructor(
    private val api: MobileApi,
    private val json: Json,
) {
    suspend fun getCapabilities(): ApiResult<AiCapabilitiesData> = single { api.getAiCapabilities() }

    suspend fun parse(request: NlCommandParseRequest): ApiResult<NlCommandParseResult> =
        single { api.nlCommandParse(request) }

    suspend fun confirm(commandId: String): ApiResult<NlCommandConfirmResult> =
        single { api.nlCommandConfirm(NlCommandConfirmRequest(commandId = commandId)) }

    suspend fun resolveContact(commandId: String, contactId: Long): ApiResult<NlCommandResolveResult> =
        single {
            api.nlCommandResolveContact(
                NlCommandResolveContactRequest(commandId = commandId, contactId = contactId),
            )
        }

    private suspend inline fun <reified T> single(
        crossinline call: suspend () -> ir.divarfiling.mobile.core.network.ApiEnvelope,
    ): ApiResult<T> = try {
        val response = call()
        if (!response.ok) {
            ApiResult.Error(response.error ?: "خطا", code = response.code)
        } else {
            ApiResult.Success(response.requireData(json))
        }
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }
}
