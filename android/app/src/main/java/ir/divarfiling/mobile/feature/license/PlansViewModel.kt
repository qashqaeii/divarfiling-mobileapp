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
    val error: String? = null,
    val successMessage: String? = null,
    val orderStatus: String? = null,
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

    fun selectPlan(id: Long) {
        _uiState.update { it.copy(selectedPlanId = id, error = null) }
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
            when (val result = shopRepository.checkout(planId, renewId)) {
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
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            orderStatus = status?.status,
                            successMessage = if (status?.status == "paid") "لایسنس فعال شد" else it.successMessage,
                            error = when (status?.status) {
                                "failed", "cancelled" -> "پرداخت انجام نشد یا لغو شد"
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
}
