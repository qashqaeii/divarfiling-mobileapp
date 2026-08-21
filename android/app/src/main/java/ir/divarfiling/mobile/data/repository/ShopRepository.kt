package ir.divarfiling.mobile.data.repository

import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.network.MobileApi
import ir.divarfiling.mobile.core.network.ShopCheckoutData
import ir.divarfiling.mobile.core.network.ShopCheckoutRequest
import ir.divarfiling.mobile.core.network.ShopOrderStatusData
import ir.divarfiling.mobile.core.network.ShopPlansData
import ir.divarfiling.mobile.core.network.requireData
import ir.divarfiling.mobile.core.network.toApiFailure
import ir.divarfiling.mobile.core.network.toUserMessage
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository @Inject constructor(
    private val api: MobileApi,
    private val sessionStore: SessionStore,
    private val json: Json,
    private val licenseRepository: LicenseRepository,
) {
    suspend fun getPlans(): ApiResult<ShopPlansData> = try {
        val response = api.getShopPlans()
        if (!response.ok) ApiResult.Error(
            ir.divarfiling.mobile.core.network.mapApiError(response.code, response.error, null, "دریافت پلن‌ها ناموفق بود"),
            response.code,
        )
        else ApiResult.Success(response.requireData(json))
    } catch (e: Exception) {
        val failure = e.toApiFailure("دریافت پلن‌ها ناموفق بود")
        ApiResult.Error(failure.message, failure.code)
    }

    suspend fun checkout(planId: Long, renewLicenseId: Long?): ApiResult<ShopCheckoutData> = try {
        val response = api.shopCheckout(ShopCheckoutRequest(planId, renewLicenseId))
        if (!response.ok) ApiResult.Error(
            ir.divarfiling.mobile.core.network.mapApiError(response.code, response.error, null, "شروع خرید ناموفق بود"),
            response.code,
        )
        else {
            val data = response.requireData<ShopCheckoutData>(json)
            sessionStore.setPendingOrderId(data.orderId)
            ApiResult.Success(data)
        }
    } catch (e: Exception) {
        val failure = e.toApiFailure("شروع خرید ناموفق بود")
        ApiResult.Error(failure.message, failure.code)
    }

    suspend fun orderStatus(orderId: String): ApiResult<ShopOrderStatusData> {
        if (!ORDER_ID_PATTERN.matches(orderId)) {
            sessionStore.setPendingOrderId(null)
            return ApiResult.Error("شناسه سفارش نامعتبر است.", "VALIDATION_ERROR")
        }
        return try {
            val response = api.shopOrderStatus(orderId)
            if (!response.ok) ApiResult.Error(
                ir.divarfiling.mobile.core.network.mapApiError(response.code, response.error, null, "بررسی سفارش ناموفق بود"),
                response.code,
            )
            else ApiResult.Success(response.requireData(json))
        } catch (e: Exception) {
            val failure = e.toApiFailure("بررسی سفارش ناموفق بود")
            ApiResult.Error(failure.message, failure.code)
        }
    }

    suspend fun verifyPendingAndRefreshLicense(): ApiResult<ShopOrderStatusData?> {
        val orderId = sessionStore.getPendingOrderId() ?: return ApiResult.Success(null)
        if (!ORDER_ID_PATTERN.matches(orderId)) {
            sessionStore.setPendingOrderId(null)
            return ApiResult.Success(null)
        }
        return when (val status = orderStatus(orderId)) {
            is ApiResult.Error -> {
                if (status.code == "ORDER_NOT_FOUND" || status.code == "VALIDATION_ERROR") {
                    sessionStore.setPendingOrderId(null)
                }
                status
            }
            is ApiResult.Success -> {
                when (status.data.status) {
                    "paid" -> {
                        licenseRepository.refreshLicense()
                        sessionStore.setPendingOrderId(null)
                    }
                    "failed", "cancelled" -> sessionStore.setPendingOrderId(null)
                }
                status
            }
        }
    }

    companion object {
        private val ORDER_ID_PATTERN =
            Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    }
}
