package ir.divarfiling.mobile.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.data.repository.AuthRepository
import ir.divarfiling.mobile.data.repository.ShopRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
    private val shopRepository: ShopRepository,
) : ViewModel() {
    val isLoggedIn = authRepository.isLoggedIn

    fun rememberPendingOrder(orderId: String) {
        val normalized = orderId.trim()
        if (!ORDER_ID.matches(normalized)) return
        viewModelScope.launch { sessionStore.setPendingOrderId(normalized) }
    }

    fun consumePendingPayment() {
        viewModelScope.launch { shopRepository.verifyPendingAndRefreshLicense() }
    }

    companion object {
        private val ORDER_ID =
            Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
    }
}
