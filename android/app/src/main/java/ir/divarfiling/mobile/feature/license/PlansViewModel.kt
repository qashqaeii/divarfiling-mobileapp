package ir.divarfiling.mobile.feature.license

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.ShopCheckoutData
import ir.divarfiling.mobile.core.network.ShopDiscountPreviewData
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
    val planMode: PlanMode = PlanMode.Personal,
    val planQuantities: Map<Long, Int> = emptyMap(),
    val checkout: ShopCheckoutData? = null,
    val license: LicenseState = LicenseState(),
    val isLoading: Boolean = false,
    val isPricingLoading: Boolean = false,
    val isCheckingOut: Boolean = false,
    val isVerifying: Boolean = false,
    val isApplyingDiscount: Boolean = false,
    val discountCode: String = "",
    val discountPreview: ShopDiscountPreviewData? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val orderStatus: String? = null,
    val orderStatusMessage: String? = null,
    val renewCurrentLicense: Boolean = false,
) {
    val selectedPlan: ShopPlanDto?
        get() = plans.firstOrNull { it.id == selectedPlanId }

    val selectedQuantity: Int
        get() {
            val plan = selectedPlan ?: return 1
            return planQuantities[plan.id] ?: plan.defaultQuantity()
        }

    val isRenewalCheckout: Boolean
        get() = renewCurrentLicense && license.canRenew

    val canChooseQuantity: Boolean
        get() {
            val plan = selectedPlan ?: return false
            return plan.isAgencyPlan() && !isRenewalCheckout
        }

    val checkoutTotal: Long?
        get() {
            val plan = selectedPlan ?: return null
            return resolveCheckoutTotal(plan, selectedQuantity, discountPreview)
        }

    val personalPlans: List<ShopPlanDto>
        get() = plans.filter { !it.isAgencyPlan() }

    val agencyPlans: List<ShopPlanDto>
        get() = plans.filter { it.isAgencyPlan() }.sortedBy { planTypeSortKey(it.planType) }
}

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
            licenseRepository.syncDeviceAndRefresh()
            when (val result = shopRepository.getPlans()) {
                is ApiResult.Success -> {
                    val data: ShopPlansData = result.data
                    val visiblePlans = data.plans.filterNot { plan ->
                        plan.planType == "daily" && plan.purchaseBlockReason == "daily_used"
                    }
                    val recommended = visiblePlans.firstOrNull { it.isFeatured && !it.purchaseBlocked }
                        ?: visiblePlans.firstOrNull { !it.purchaseBlocked }
                    val selectedStillVisible = visiblePlans.any { it.id == _uiState.value.selectedPlanId }
                    val defaultQuantities = visiblePlans.associate { it.id to it.defaultQuantity() }
                    val nextSelectedId = when {
                        selectedStillVisible -> _uiState.value.selectedPlanId
                        else -> recommended?.id
                    }
                    val nextSelectedPlan = visiblePlans.firstOrNull { it.id == nextSelectedId }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            plans = visiblePlans,
                            renewableLicenseId = data.renewableLicense?.licenseId,
                            phoneVerified = data.phoneVerified,
                            planQuantities = defaultQuantities + it.planQuantities.filterKeys { id ->
                                visiblePlans.any { plan -> plan.id == id }
                            },
                            selectedPlanId = nextSelectedId,
                            planMode = when {
                                nextSelectedPlan?.isAgencyPlan() == true -> PlanMode.Agency
                                else -> PlanMode.Personal
                            },
                        )
                    }
                    nextSelectedPlan?.takeIf { it.isAgencyPlan() && !it.purchaseBlocked }?.let { plan ->
                        val qty = _uiState.value.planQuantities[plan.id] ?: plan.defaultQuantity()
                        refreshPricingPreview(plan.id, qty)
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
            val quantity = checkoutQuantity(state)
            when (val result = shopRepository.previewDiscount(planId, code, quantity)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isApplyingDiscount = false, discountPreview = result.data, error = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isApplyingDiscount = false, discountPreview = null, error = result.message)
                }
            }
        }
    }

    fun setRenewCurrentLicense(enabled: Boolean) {
        _uiState.update {
            it.copy(
                renewCurrentLicense = enabled && it.license.canRenew,
                discountPreview = null,
                error = null,
            )
        }
    }

    fun setPlanMode(mode: PlanMode) {
        val state = _uiState.value
        if (state.planMode == mode) return
        _uiState.update { it.copy(planMode = mode, discountPreview = null, error = null) }
        val current = state.selectedPlan
        val alreadyInMode = current != null && when (mode) {
            PlanMode.Personal -> !current.isAgencyPlan()
            PlanMode.Agency -> current.isAgencyPlan()
        }
        if (alreadyInMode) {
            if (mode == PlanMode.Agency && current != null && !state.isRenewalCheckout) {
                refreshPricingPreview(current.id, state.selectedQuantity)
            }
            return
        }
        val targetPlans = when (mode) {
            PlanMode.Personal -> state.personalPlans
            PlanMode.Agency -> state.agencyPlans
        }
        val plan = targetPlans.firstOrNull { !it.purchaseBlocked } ?: targetPlans.firstOrNull() ?: return
        selectPlan(plan.id)
    }

    fun selectPlan(id: Long) {
        val plan = _uiState.value.plans.firstOrNull { it.id == id } ?: return
        if (plan.purchaseBlocked) return
        val qty = _uiState.value.planQuantities[id] ?: plan.defaultQuantity()
        _uiState.update {
            it.copy(
                selectedPlanId = id,
                planMode = if (plan.isAgencyPlan()) PlanMode.Agency else PlanMode.Personal,
                planQuantities = it.planQuantities + (id to qty),
                error = null,
                discountPreview = null,
            )
        }
        if (plan.isAgencyPlan() && !_uiState.value.isRenewalCheckout) {
            refreshPricingPreview(id, qty)
        }
    }

    fun setQuantity(planId: Long, quantity: Int) {
        val plan = _uiState.value.plans.firstOrNull { it.id == planId } ?: return
        if (!plan.isAgencyPlan() || plan.purchaseBlocked) return
        val clamped = plan.clampQuantity(quantity)
        _uiState.update {
            it.copy(
                planQuantities = it.planQuantities + (planId to clamped),
                discountPreview = null,
                error = null,
            )
        }
        refreshPricingPreview(planId, clamped)
    }

    private fun refreshPricingPreview(planId: Long, quantity: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPricingLoading = true) }
            when (val result = shopRepository.previewPricing(planId, quantity)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isPricingLoading = false, discountPreview = result.data)
                }
                is ApiResult.Error -> _uiState.update { it.copy(isPricingLoading = false) }
            }
        }
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
        val renewId = if (state.renewCurrentLicense && state.license.canRenew) {
            state.renewableLicenseId ?: state.license.licenseId
        } else {
            null
        }
        val quantity = checkoutQuantity(state)
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true, error = null) }
            when (
                val result = shopRepository.checkout(
                    planId,
                    renewId,
                    state.discountPreview?.code ?: state.discountCode.trim().ifBlank { null },
                    quantity,
                )
            ) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isCheckingOut = false, checkout = result.data) }
                    val url = result.data.payUrl
                    when {
                        result.data.status == "paid" -> {
                            licenseRepository.syncDeviceAndRefresh()
                            refresh()
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
                    licenseRepository.syncDeviceAndRefresh()
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
                            "بررسی پرداخت ناموفق بود. پس از اتصال «بررسی وضعیت» را بزنید."
                        } else {
                            result.message
                        },
                    )
                }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }

    private fun checkoutQuantity(state: PlansUiState): Int {
        if (state.isRenewalCheckout) return 1
        val plan = state.selectedPlan ?: return 1
        return state.planQuantities[plan.id] ?: plan.defaultQuantity()
    }

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
