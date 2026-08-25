package ir.divarfiling.mobile.feature.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.ExportSecurityRepository
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExportSecurityStep {
    Password,
    Otp,
}

data class ExportSecurityUiState(
    val step: ExportSecurityStep = ExportSecurityStep.Password,
    val exportLabel: String = "",
    val password: String = "",
    val otpCode: String = "",
    val phoneDisplay: String = "",
    val passwordChallenge: String? = null,
    val resendIn: Int = 0,
    val isBusy: Boolean = false,
    val error: String? = null,
    val isVisible: Boolean = false,
    val verified: Boolean = false,
)

@HiltViewModel
class ExportSecurityViewModel @Inject constructor(
    private val repository: ExportSecurityRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExportSecurityUiState())
    val uiState: StateFlow<ExportSecurityUiState> = _uiState.asStateFlow()

    private var resendJob: Job? = null

    fun open(label: String) {
        resendJob?.cancel()
        _uiState.value = ExportSecurityUiState(
            isVisible = true,
            exportLabel = label,
        )
    }

    fun dismiss() {
        resendJob?.cancel()
        _uiState.value = ExportSecurityUiState()
    }

    fun consumeVerified() {
        _uiState.update { it.copy(verified = false) }
    }

    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }

    fun onOtpChange(value: String) = _uiState.update { it.copy(otpCode = value, error = null) }

    fun submitPassword() {
        val password = _uiState.value.password.trim()
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "رمز عبور حساب را وارد کنید.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            when (val result = repository.verifyPassword(password)) {
                is ApiResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            step = ExportSecurityStep.Otp,
                            passwordChallenge = data.passwordChallenge,
                            phoneDisplay = data.phoneDisplay.orEmpty(),
                            otpCode = "",
                        )
                    }
                    startResendTimer(data.resendIn ?: 60)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isBusy = false, error = result.message)
                }
            }
        }
    }

    fun submitOtp() {
        val state = _uiState.value
        val code = state.otpCode.trim()
        val challenge = state.passwordChallenge
        if (code.isBlank()) {
            _uiState.update { it.copy(error = "کد پیامک را وارد کنید.") }
            return
        }
        if (challenge.isNullOrBlank()) {
            _uiState.update {
                it.copy(step = ExportSecurityStep.Password, error = "نشست منقضی شده. دوباره رمز را وارد کنید.")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            when (val result = repository.verifyOtp(challenge, code)) {
                is ApiResult.Success -> {
                    resendJob?.cancel()
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            isVisible = false,
                            verified = true,
                            password = "",
                            otpCode = "",
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isBusy = false, error = result.message)
                }
            }
        }
    }

    fun resendOtp() {
        val challenge = _uiState.value.passwordChallenge ?: return
        if (_uiState.value.resendIn > 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, error = null) }
            when (val result = repository.resendOtp(challenge)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isBusy = false,
                            passwordChallenge = result.data.passwordChallenge ?: challenge,
                            phoneDisplay = result.data.phoneDisplay.orEmpty(),
                        )
                    }
                    startResendTimer(result.data.resendIn ?: 60)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isBusy = false, error = result.message)
                }
            }
        }
    }

    fun backToPassword() {
        resendJob?.cancel()
        _uiState.update {
            it.copy(
                step = ExportSecurityStep.Password,
                otpCode = "",
                resendIn = 0,
                error = null,
            )
        }
    }

    private fun startResendTimer(seconds: Int) {
        resendJob?.cancel()
        _uiState.update { it.copy(resendIn = seconds.coerceAtLeast(0)) }
        resendJob = viewModelScope.launch {
            var remaining = seconds.coerceAtLeast(0)
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _uiState.update { it.copy(resendIn = remaining) }
            }
        }
    }
}
