package ir.divarfiling.mobile.feature.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.ShopCheckoutData
import ir.divarfiling.mobile.core.network.ShopPlanDto
import ir.divarfiling.mobile.core.network.ShopPlansData
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.LicenseRepository
import ir.divarfiling.mobile.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlansUiState(
    val plans: List<ShopPlanDto> = emptyList(),
    val renewableLicenseId: Long? = null,
    val phoneVerified: Boolean = true,
    val selectedPlanId: Long? = null,
    val checkout: ShopCheckoutData? = null,
    val license: LicenseState = LicenseState(),
    val isLoading: Boolean = false,
    val isCheckingOut: Boolean = false,
    val isVerifying: Boolean = false,
    val isApplyingDiscount: Boolean = false,
    val discountCode: String = "",
    val discountPreview: ir.divarfiling.mobile.core.network.ShopDiscountPreviewData? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val orderStatus: String? = null,
    val orderStatusMessage: String? = null,
)

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val shopRepository: ShopRepository,
    private val licenseRepository: LicenseRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.licenseState.collect { license ->
                _uiState.update { it.copy(license = license) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            licenseRepository.refreshLicense()
            when (val result = shopRepository.getPlans()) {
                is ApiResult.Success -> {
                    val data: ShopPlansData = result.data
                    val recommended = data.plans.firstOrNull { it.isFeatured && !it.purchaseBlocked } ?: data.plans.firstOrNull { !it.purchaseBlocked }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            plans = data.plans,
                            renewableLicenseId = data.renewableLicense?.licenseId,
                            phoneVerified = data.phoneVerified,
                            selectedPlanId = it.selectedPlanId ?: recommended?.id,
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onDiscountCodeChange(value: String) {
        _uiState.update { it.copy(discountCode = value, error = null) }
    }

    fun clearDiscount() {
        _uiState.update { it.copy(discountPreview = null, discountCode = "", error = null) }
    }

    fun applyDiscount() {
        val state = _uiState.value
        val planId = state.selectedPlanId ?: return
        val code = state.discountCode.trim()
        if (code.isBlank() || state.isApplyingDiscount) return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingDiscount = true, error = null) }
            when (val result = shopRepository.previewDiscount(planId, code)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isApplyingDiscount = false, discountPreview = result.data, error = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isApplyingDiscount = false, discountPreview = null, error = result.message)
                }
            }
        }
    }

    fun selectPlan(id: Long) {
        _uiState.update { it.copy(selectedPlanId = id, error = null, discountPreview = null) }
    }

    fun startCheckout(onPayUrl: (String) -> Unit) {
        val state = _uiState.value
        val planId = state.selectedPlanId ?: return
        val plan = state.plans.firstOrNull { it.id == planId } ?: return
        if (plan.purchaseBlocked || state.isCheckingOut) return
        if (!state.phoneVerified) {
            _uiState.update { it.copy(error = "برای خرید، ابتدا شماره موبایل حساب را تأیید کنید") }
            return
        }
        val renewId = if (state.license.canRenew) state.renewableLicenseId ?: state.license.licenseId else null
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true, error = null) }
            when (val result = shopRepository.checkout(planId, renewId, state.discountPreview?.code ?: state.discountCode.trim().ifBlank { null })) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isCheckingOut = false, checkout = result.data) }
                    val url = result.data.payUrl
                    when {
                        result.data.status == "paid" -> {
                            licenseRepository.refreshLicense()
                            _uiState.update { it.copy(successMessage = "لایسنس فعال شد") }
                        }
                        !url.isNullOrBlank() -> onPayUrl(url)
                        else -> _uiState.update {
                            it.copy(error = "لینک پرداخت دریافت نشد. وضعیت سفارش را بررسی کنید.")
                        }
                    }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isCheckingOut = false, error = result.message) }
            }
        }
    }

    fun consumePendingOrder() {
        if (_uiState.value.isVerifying) return
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true) }
            when (val result = shopRepository.verifyPendingAndRefreshLicense()) {
                is ApiResult.Success -> {
                    val status = result.data
                    val message = orderStatusMessageFa(status?.status, missing = status == null)
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            orderStatus = status?.status ?: "missing",
                            orderStatusMessage = message,
                            successMessage = if (status?.status == "paid") "لایسنس فعال شد" else it.successMessage,
                            error = when (status?.status) {
                                "failed" -> "پرداخت ناموفق بود"
                                "cancelled" -> "پرداخت لغو شد"
                                else -> null
                            },
                        )
                    }
                    if (status?.status == "paid") refresh()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isVerifying = false,
                        error = if (result.code == "NETWORK_ERROR" || result.message.contains("اتصال")) {
                            "بررسی پرداخت ناموفق بود. پس از اتصال «بررسی وضعیت پرداخت» را بزنید."
                        } else {
                            result.message
                        },
                    )
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    companion object {
        fun orderStatusMessageFa(status: String?, missing: Boolean = false): String = when {
            missing -> "سفارش در انتظار یافت نشد. اگر پرداخت کرده‌اید کمی بعد دوباره تلاش کنید."
            status == "paid" -> "پرداخت تأیید شد و لایسنس به‌روز شد"
            status == "pending" -> "پرداخت هنوز در انتظار تأیید است"
            status == "failed" -> "پرداخت ناموفق بود"
            status == "cancelled" -> "پرداخت لغو شد"
            else -> "وضعیت سفارش مشخص نیست"
        }
    }
}
