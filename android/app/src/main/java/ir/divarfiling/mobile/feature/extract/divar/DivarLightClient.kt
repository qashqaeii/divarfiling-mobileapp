package ir.divarfiling.mobile.feature.extract.divar

import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.license.ExtractLightLimits
import ir.divarfiling.mobile.feature.extract.ExtractCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class RawExtractedItem(
    val token: String,
    val raw: JsonElement,
    val thumbnailUrl: String? = null,
    val imageUrls: List<String> = emptyList(),
)

@Singleton
class DivarLightClient @Inject constructor(
    @Named("divar") private val client: OkHttpClient,
    private val json: Json,
) {
    private val listHeaders = mapOf(
        "accept" to "application/json, text/plain, */*",
        "content-type" to "application/json",
        "origin" to "https://divar.ir",
        "referer" to "https://divar.ir/",
        "user-agent" to MOBILE_USER_AGENT,
        "x-render-type" to "CSR",
        "x-standard-divar-error" to "true",
    )

    private val detailHeaders = mapOf(
        "accept" to "application/json-filled",
        "origin" to "https://divar.ir",
        "referer" to "https://divar.ir/",
        "user-agent" to DESKTOP_USER_AGENT,
        "x-render-type" to "CSR",
    )

    suspend fun collectItems(
        filters: ExtractFilters,
        onProgress: (Int, Int) -> Unit,
        isCancelled: () -> Boolean,
    ): List<RawExtractedItem> = withContext(Dispatchers.IO) {
        val maxItems = filters.maxItems.coerceIn(1, ExtractLightLimits.MAX_ITEMS)
        val (tokens, listThumbnails) = fetchPostTokens(filters, maxItems, isCancelled)
        val limitedTokens = tokens.take(maxItems)
        onProgress(0, limitedTokens.size)

        val firstPass = fetchDetailsForTokens(
            tokens = limitedTokens,
            listThumbnails = listThumbnails,
            onProgress = onProgress,
            isCancelled = isCancelled,
        )
        if (isCancelled()) return@withContext firstPass.values.toList()

        val missing = limitedTokens.filter { it !in firstPass }
        val retried = if (missing.isNotEmpty()) {
            fetchDetailsForTokens(
                tokens = missing,
                listThumbnails = listThumbnails,
                onProgress = { completed, _ ->
                    onProgress((firstPass.size + completed).coerceAtMost(limitedTokens.size), limitedTokens.size)
                },
                isCancelled = isCancelled,
            )
        } else {
            emptyMap()
        }

        limitedTokens.mapNotNull { token -> firstPass[token] ?: retried[token] }
    }

    private suspend fun fetchDetailsForTokens(
        tokens: List<String>,
        listThumbnails: Map<String, String>,
        onProgress: (Int, Int) -> Unit,
        isCancelled: () -> Boolean,
    ): Map<String, RawExtractedItem> {
        if (tokens.isEmpty()) return emptyMap()
        val semaphore = Semaphore(ExtractLightLimits.MAX_CONCURRENT)
        var completed = 0
        return coroutineScope {
            tokens.map { token ->
                async {
                    if (isCancelled()) return@async null
                    semaphore.withPermit {
                        delay(ExtractLightLimits.MIN_DELAY_MS)
                        val detail = fetchDetailEnriched(token) ?: return@withPermit null
                        val imageUrls = DivarImageExtractor.extractImageUrls(detail)
                        val fallbackThumb = listThumbnails[token]
                        val thumbnailUrl = imageUrls.firstOrNull() ?: fallbackThumb
                        val mergedUrls = buildList {
                            thumbnailUrl?.let { add(it) }
                            addAll(imageUrls.filter { it != thumbnailUrl })
                        }
                        token to RawExtractedItem(
                            token = token,
                            raw = detail,
                            thumbnailUrl = thumbnailUrl,
                            imageUrls = mergedUrls,
                        )
                    }?.also {
                        completed++
                        onProgress(completed, tokens.size)
                    }
                }
            }.awaitAll().filterNotNull().toMap()
        }
    }

    private fun fetchPostTokens(
        filters: ExtractFilters,
        maxItems: Int,
        isCancelled: () -> Boolean,
    ): Pair<List<String>, Map<String, String>> {
        val tokens = mutableListOf<String>()
        val listThumbnails = mutableMapOf<String, String>()
        var page = 1
        var searchUid: String? = null
        var lastPostDate: String? = null
        var cumulativeWidgets = 0
        var viewedTokens: String? = null
        var consecutiveEmpty = 0

        while (tokens.size < maxItems && !isCancelled()) {
            val payload = buildSearchPayload(
                page, lastPostDate, searchUid, cumulativeWidgets, viewedTokens, filters,
            )
            val body = json.encodeToString(JsonObject.serializer(), payload)
                .toRequestBody("application/json".toMediaType())

            val response = executeWithRetry {
                val request = Request.Builder()
                    .url("${BuildConfig.DIVAR_API_HOST}/v8/postlist/w/search")
                    .post(body)
                    .apply { listHeaders.forEach { (k, v) -> header(k, v) } }
                    .build()
                client.newCall(request).execute()
            } ?: break

            if (!response.isSuccessful) break

            val responseBody = response.body?.string() ?: break
            val root = json.parseToJsonElement(responseBody).jsonObject
            val listWidgets = root["list_widgets"]?.jsonArray ?: break

            val pageTokens = listWidgets.mapNotNull { widget ->
                val obj = widget.asObject() ?: return@mapNotNull null
                if (obj.str("widget_type") != "POST_ROW") return@mapNotNull null
                val data = obj["data"]?.asObject() ?: return@mapNotNull null
                val token = data.str("token")
                    ?: data["action"]?.asObject()?.get("payload")?.asObject()?.str("token")
                token?.let { tok ->
                    DivarImageExtractor.extractListThumbnail(data)?.let { thumb ->
                        listThumbnails[tok] = thumb
                    }
                }
                token
            }

            if (pageTokens.isEmpty()) {
                consecutiveEmpty++
                if (consecutiveEmpty >= 2) break
            } else {
                consecutiveEmpty = 0
                val existing = tokens.toSet()
                val newOnes = pageTokens.filter { it !in existing }
                val remaining = maxItems - tokens.size
                tokens.addAll(newOnes.take(remaining))

                val first = listWidgets.first().asObject()
                val last = listWidgets.last().asObject()
                val firstInfo = first?.nested("action_log", "server_side_info", "info")
                val lastInfo = last?.nested("action_log", "server_side_info", "info")
                lastPostDate = lastInfo?.str("sort_date") ?: firstInfo?.str("sort_date") ?: lastPostDate
                searchUid = firstInfo?.nested("extra_data")?.str("search_uid") ?: searchUid
                cumulativeWidgets += listWidgets.size

                val paginationData = root["pagination_data"]?.asObject()
                viewedTokens = paginationData?.str("viewed_tokens") ?: viewedTokens
            }

            if (tokens.size >= maxItems) break
            page++
            if (page > MAX_LIST_PAGES) break
            Thread.sleep(ExtractLightLimits.MIN_DELAY_MS)
        }

        return tokens.distinct() to listThumbnails
    }

    private fun fetchDetailEnriched(token: String): JsonElement? {
        val detail = fetchDetail(token) ?: return null
        val detailObj = detail.jsonObject.toMutableMap()
        fetchBusinessLazyWidgets(detail, token)?.let { widgets ->
            detailObj["business_lazy_widget_list"] = widgets
        }
        return JsonObject(detailObj)
    }

    private fun fetchDetail(token: String): JsonElement? {
        val response = executeWithRetry {
            val request = Request.Builder()
                .url("${BuildConfig.DIVAR_API_HOST}/v8/posts-v2/web/$token")
                .get()
                .apply { detailHeaders.forEach { (k, v) -> header(k, v) } }
                .build()
            client.newCall(request).execute()
        } ?: return null
        if (!response.isSuccessful) return null
        val body = response.body?.string() ?: return null
        return json.parseToJsonElement(body)
    }

    private fun fetchBusinessLazyWidgets(detail: JsonElement, token: String): JsonElement? {
        val (path, body) = findBusinessLazyRequest(detail, token) ?: return null
        val url = if (path.startsWith("http")) path else "${BuildConfig.DIVAR_API_HOST}$path"

        val response = executeWithRetry {
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .apply { listHeaders.forEach { (k, v) -> header(k, v) } }
                .build()
            client.newCall(request).execute()
        } ?: return null
        if (!response.isSuccessful) return null
        val responseBody = response.body?.string() ?: return null
        val root = json.parseToJsonElement(responseBody).jsonObject
        return root["widget_list"]
    }

    private fun findBusinessLazyRequest(detail: JsonElement, token: String): Pair<String, String>? {
        val detailObj = detail.jsonObject
        val sections = detailObj["sections"]?.jsonArray ?: return fallbackBusinessLazyRequest(detailObj, token)

        for (section in sections) {
            val sec = section.asObject() ?: continue
            if (sec.str("section_name") != "BUSINESS_SECTION") continue
            val widgets = sec["widgets"]?.jsonArray ?: continue
            for (widget in widgets) {
                val w = widget.asObject() ?: continue
                if (w.str("widget_type") != "LAZY_SECTION") continue
                val data = w["data"]?.asObject() ?: continue
                val path = data.str("rest_request_path")?.trim().orEmpty()
                if (path.isBlank()) continue
                val reqData = data["request_data"]?.asObject()
                val body = if (reqData != null) {
                    buildJsonObject { put("request_data", reqData) }
                } else {
                    defaultBusinessLazyBody(tokenFromDetail(detailObj, token))
                }
                return path to json.encodeToString(JsonObject.serializer(), body)
            }
        }
        return fallbackBusinessLazyRequest(detailObj, token)
    }

    private fun fallbackBusinessLazyRequest(detailObj: JsonObject, token: String): Pair<String, String>? {
        val businessType = detailObj["webengage"]?.asObject()?.str("business_type")?.lowercase().orEmpty()
        if (businessType != "premium-panel" && businessType != "premium_panel") return null
        val path = "/v8/premium-user/post-page/business-data/$token/lazy"
        val body = defaultBusinessLazyBody(tokenFromDetail(detailObj, token))
        return path to json.encodeToString(JsonObject.serializer(), body)
    }

    private fun tokenFromDetail(detailObj: JsonObject, fallback: String): String =
        detailObj["webengage"]?.asObject()?.str("token")?.trim()?.takeIf { it.isNotEmpty() } ?: fallback

    private fun defaultBusinessLazyBody(token: String): JsonObject = buildJsonObject {
        put("request_data", buildJsonObject {
            put("@type", "type.googleapis.com/premium_panel.GetPostBusinessLazyWidgetsRequest.RequestData")
            put("post_token", token)
        })
    }

    private fun buildSearchPayload(
        page: Int,
        lastPostDate: String?,
        searchUid: String?,
        cumulativeWidgets: Int,
        viewedTokens: String?,
        filters: ExtractFilters,
    ): JsonObject {
        val formData = buildJsonObject {
            put("category", buildJsonObject {
                put("str", buildJsonObject { put("value", filters.category) })
            })
            if (filters.districtIds.isNotEmpty()) {
                put("districts", buildJsonObject {
                    put("repeated_string", buildJsonObject {
                        put("value", buildJsonArray {
                            filters.districtIds.forEach { add(it) }
                        })
                    })
                })
            }
            applyAdvancedFilters(filters)
        }

        val searchQuery = filters.searchQuery?.trim().orEmpty()

        val searchData = buildJsonObject {
            put("form_data", buildJsonObject { put("data", formData) })
            put("server_payload", buildJsonObject {
                put("@type", "type.googleapis.com/widgets.SearchData.ServerPayload")
                put("additional_form_data", buildJsonObject {
                    put("data", buildJsonObject {
                        put("sort", buildJsonObject {
                            put("str", buildJsonObject { put("value", filters.sort) })
                        })
                    })
                })
            })
            if (searchQuery.isNotBlank()) {
                put("query", searchQuery)
            }
        }

        val basePayload = buildJsonObject {
            put("city_ids", buildJsonArray { add(filters.cityId) })
            put("pagination_data", buildJsonObject {
                put("@type", "type.googleapis.com/post_list.PaginationData")
                put("page", page)
                put("layer_page", page)
                put("cumulative_widgets_count", cumulativeWidgets)
                if (!lastPostDate.isNullOrBlank()) put("last_post_date", lastPostDate)
                if (!searchUid.isNullOrBlank()) put("search_uid", searchUid)
                if (!viewedTokens.isNullOrBlank()) put("viewed_tokens", viewedTokens)
            })
            put("disable_recommendation", false)
            put("map_state", buildJsonObject {
                put("camera_info", buildJsonObject { put("bbox", buildJsonObject {}) })
            })
            put("search_data", searchData)
        }

        if (searchQuery.isNotBlank()) {
            return buildJsonObject {
                basePayload.forEach { (k, v) -> put(k, v) }
                put("source_view", "SEARCH_BAR_QUERY_SUGGESTION")
            }
        }

        if (!ExtractCategories.needsCategoryViewFlags(filters.category)) {
            return basePayload
        }

        return buildJsonObject {
            basePayload.forEach { (k, v) -> put(k, v) }
            put("source_view", "CATEGORY")
            put("map_state", buildJsonObject {
                put("camera_info", buildJsonObject { put("bbox", buildJsonObject {}) })
                put("page_state", "HALF_STATE")
            })
        }
    }

    private fun kotlinx.serialization.json.JsonObjectBuilder.applyAdvancedFilters(filters: ExtractFilters) {
        val adv = filters.advanced
        val isRent = ExtractCategories.isRentCategory(filters.category)
        if (isRent) {
            putNumRange("credit", adv.depositMin, adv.depositMax)
            putNumRange("rent", adv.rentMin, adv.rentMax)
        } else {
            putNumRange("price", adv.priceMin, adv.priceMax)
        }
        putNumRange("size", adv.areaMin?.toLong(), adv.areaMax?.toLong())
        putNumRange("building-year", adv.yearMin?.toLong(), adv.yearMax?.toLong())
        if (adv.rooms.isNotEmpty()) {
            put("rooms", buildJsonObject {
                put("repeated_string", buildJsonObject {
                    put("value", buildJsonArray { adv.rooms.forEach { add(it) } })
                })
            })
        }
        val advertiser = adv.advertiserFilter.trim().lowercase()
        if (advertiser == "personal" || advertiser == "genuine_personal") {
            put("business-type", buildJsonObject {
                put("repeated_string", buildJsonObject {
                    put("value", buildJsonArray { add("personal") })
                })
            })
        }
    }

    private fun kotlinx.serialization.json.JsonObjectBuilder.putNumRange(
        key: String,
        min: Long?,
        max: Long?,
    ) {
        if (min == null && max == null) return
        put(key, buildJsonObject {
            put("number_range", buildJsonObject {
                min?.let { put("minimum", it) }
                max?.let { put("maximum", it) }
            })
        })
    }

    private inline fun executeWithRetry(block: () -> okhttp3.Response): okhttp3.Response? {
        var last: okhttp3.Response? = null
        repeat(MAX_RETRIES) { attempt ->
            try {
                last?.close()
                val resp = block()
                if (resp.isSuccessful || resp.code !in RETRYABLE_CODES) return resp
                resp.close()
            } catch (_: Exception) {
                // retry
            }
            if (attempt < MAX_RETRIES - 1) {
                Thread.sleep((RETRY_BACKOFF_MS * (attempt + 1)).toLong())
            }
        }
        return try { block() } catch (_: Exception) { null }
    }

    private fun JsonElement.asObject(): JsonObject? = runCatching { jsonObject }.getOrNull()

    private fun JsonObject.str(key: String): String? =
        runCatching { get(key)?.jsonPrimitive?.content }.getOrNull()?.takeIf { it.isNotBlank() }

    private fun JsonObject.nested(vararg keys: String): JsonObject? {
        var current: JsonObject? = this
        for (key in keys) {
            current = current?.get(key)?.asObject() ?: return null
        }
        return current
    }

    companion object {
        private const val MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        private const val MAX_LIST_PAGES = 80
        private const val MAX_RETRIES = 3
        private const val RETRY_BACKOFF_MS = 400L
        private val RETRYABLE_CODES = setOf(429, 500, 502, 503, 504)
    }
}
