package ir.divarfiling.mobile.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.ApiEnvelope
import ir.divarfiling.mobile.core.network.ContractAttachmentDto
import ir.divarfiling.mobile.core.network.ContractCreateRequest
import ir.divarfiling.mobile.core.network.ContractDetailDto
import ir.divarfiling.mobile.core.network.ContractListItemDto
import ir.divarfiling.mobile.core.network.ContractTimelineData
import ir.divarfiling.mobile.core.network.ContractTimelineItemDto
import ir.divarfiling.mobile.core.network.ContractsListData
import ir.divarfiling.mobile.core.network.DealContractItemDto
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.PaginatedResult
import ir.divarfiling.mobile.core.network.WorkspaceBridgeData
import ir.divarfiling.mobile.core.network.WorkspaceBridgeRequest
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toUserMessage
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ContractsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: MobileApi,
    private val json: Json,
    private val sessionStore: SessionStore,
    @Named("mobile") private val httpClient: OkHttpClient,
) {
    suspend fun listContracts(
        query: String = "",
        status: String? = null,
        type: String? = null,
        dealLinked: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        sort: String = "updated",
        needsAction: Boolean = false,
        page: Int = 1,
    ): ApiResult<Pair<ContractsListData, PaginatedResult<ContractListItemDto>>> {
        return try {
        val response = api.getContracts(
            query = query.ifBlank { null },
            status = status?.ifBlank { null },
            type = type?.ifBlank { null },
            dealLinked = dealLinked?.ifBlank { null },
            dateFrom = dateFrom?.ifBlank { null },
            dateTo = dateTo?.ifBlank { null },
            sort = sort.ifBlank { null },
            needsAction = if (needsAction) "true" else null,
            page = page,
        )
        if (!response.ok) {
            return ApiResult.Error(response.error ?: "خطا", response.code)
        }
        val data = response.requireData<ContractsListData>(json)
        val pageSize = response.meta?.pageSize ?: 20
        val total = response.meta?.total ?: data.items.size
        val hasMore = data.items.isNotEmpty() && page * pageSize < total
        ApiResult.Success(
            data to PaginatedResult(data.items, page, total, hasMore),
        )
        } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
        }
    }

    suspend fun getDetail(contractId: Long): ApiResult<ContractDetailDto> = single {
        api.getContractDetail(contractId)
    }

    suspend fun getTimeline(contractId: Long): ApiResult<List<ContractTimelineItemDto>> = try {
        val response = api.getContractTimeline(contractId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا", response.code)
        else ApiResult.Success(response.requireData<ContractTimelineData>(json).items)
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun getAttachments(contractId: Long): ApiResult<List<ContractAttachmentDto>> = try {
        val response = api.getContractAttachments(contractId)
        if (!response.ok) ApiResult.Error(response.error ?: "خطا", response.code)
        else {
            val items = response.requireData<ir.divarfiling.mobile.core.network.ContractAttachmentsData>(json).items
            ApiResult.Success(items)
        }
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }

    suspend fun uploadAttachment(contractId: Long, uri: Uri, title: String): ApiResult<ContractAttachmentDto> =
        withContext(Dispatchers.IO) {
            try {
                val stream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext ApiResult.Error("فایل قابل خواندن نیست")
                val bytes = stream.use { it.readBytes() }
                val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val name = title.ifBlank { "attachment" }
                val part = MultipartBody.Part.createFormData(
                    "file",
                    name,
                    bytes.toRequestBody(mime.toMediaType()),
                )
                val titlePart = name.toRequestBody("text/plain".toMediaType())
                val response = api.uploadContractAttachment(contractId, part, titlePart)
                if (!response.ok) ApiResult.Error(response.error ?: "خطا", response.code)
                else ApiResult.Success(response.requireData(json))
            } catch (e: Exception) {
                ApiResult.Error(e.toUserMessage("خطای آپلود"))
            }
        }

    suspend fun downloadAttachment(contractId: Long, attachmentId: Long, fileName: String): ApiResult<File> =
        downloadBinary(
            path = "contracts/$contractId/attachments/$attachmentId",
            dirName = "contract_attachments",
            fileName = fileName.ifBlank { "attachment_$attachmentId" },
        )

    suspend fun createContract(
        contractType: String,
        dealId: Long? = null,
    ): ApiResult<DealContractItemDto> = single {
        api.createContract(ContractCreateRequest(contractType = contractType, dealId = dealId))
    }

    suspend fun createWorkspaceBridge(path: String): ApiResult<WorkspaceBridgeData> = single {
        api.createWorkspaceBridge(WorkspaceBridgeRequest(path = path))
    }

    suspend fun downloadPdf(contractId: Long): ApiResult<File> =
        downloadBinary(
            path = "contracts/$contractId/pdf",
            dirName = "contract_pdf",
            fileName = "contract_$contractId.pdf",
        )

    private suspend fun downloadBinary(path: String, dirName: String, fileName: String): ApiResult<File> =
        withContext(Dispatchers.IO) {
            try {
                val base = BuildConfig.API_BASE_URL.trimEnd('/')
                val url = "$base/$path"
                val token = sessionStore.getAccessToken()
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .header("Authorization", "Bearer $token")
                    .header("X-Platform", "android")
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext ApiResult.Error("دریافت فایل ناموفق")
                    }
                    val body = response.body ?: return@withContext ApiResult.Error("فایل خالی")
                    val dir = File(context.cacheDir, dirName).apply { mkdirs() }
                    val out = File(dir, fileName)
                    body.byteStream().use { input -> out.outputStream().use { input.copyTo(it) } }
                    ApiResult.Success(out)
                }
            } catch (e: Exception) {
                ApiResult.Error(e.toUserMessage("خطای دانلود"))
            }
        }

    private suspend inline fun <reified T> single(
        crossinline call: suspend () -> ApiEnvelope,
    ): ApiResult<T> = try {
        val response = call()
        if (!response.ok) ApiResult.Error(response.error ?: "خطا", response.code)
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        ApiResult.Error(e.toUserMessage("خطای شبکه"))
    }
}
