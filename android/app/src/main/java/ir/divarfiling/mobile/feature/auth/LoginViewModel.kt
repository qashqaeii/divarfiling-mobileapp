package ir.divarfiling.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthMode { Password, Otp, Register, Forgot }

enum class AuthStep { Credentials, OtpCode, NewPassword }

data class LoginUiState(
    val mode: AuthMode = AuthMode.Password,
    val step: AuthStep = AuthStep.Credentials,
    val username: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val otpCode: String = "",
    val phoneDisplay: String = "",
    val challengeToken: String = "",
    val resendIn: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private var countdownJob: Job? = null

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onPasswordConfirmChange(value: String) {
        _uiState.update { it.copy(passwordConfirm = value, error = null) }
    }

    fun onOtpChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(otpCode = digits, error = null) }
    }

    fun selectMode(mode: AuthMode) {
        countdownJob?.cancel()
        _uiState.value = LoginUiState(
            mode = mode,
            username = _uiState.value.username,
        )
    }

    fun changePhone() {
        countdownJob?.cancel()
        _uiState.update {
            it.copy(step = AuthStep.Credentials, otpCode = "", challengeToken = "", error = null, resendIn = 0)
        }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "شماره موبایل و رمز عبور را وارد کنید") }
            return
        }
        if (state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(state.username, state.password)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun requestOtp() {
        val state = _uiState.value
        if (state.username.isBlank()) {
            _uiState.update { it.copy(error = "شماره موبایل را وارد کنید") }
            return
        }
        if (state.isLoading) return
        val purpose = when (state.mode) {
            AuthMode.Otp -> "login"
            AuthMode.Register -> "register"
            AuthMode.Forgot -> "password_reset"
            AuthMode.Password -> return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, info = null) }
            when (val result = authRepository.requestOtp(state.username, purpose)) {
                is ApiResult.Success -> {
                    startCountdown(result.data.resendIn ?: 60)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = AuthStep.OtpCode,
                            phoneDisplay = result.data.phoneDisplay.orEmpty(),
                            info = "کد تأیید ارسال شد",
                        )
                    }
                }
                is ApiResult.Error -> {
                    val switchToLogin = result.code == "PHONE_TAKEN"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            mode = if (switchToLogin) AuthMode.Password else it.mode,
                            info = if (switchToLogin) "این شماره قبلاً ثبت شده — با رمز یا OTP وارد شوید." else it.info,
                        )
                    }
                }
            }
        }
    }

    fun resendOtp() {
        val state = _uiState.value
        if (state.resendIn > 0 || state.isLoading) return
        val purpose = when (state.mode) {
            AuthMode.Otp -> "login"
            AuthMode.Register -> "register"
            AuthMode.Forgot -> "password_reset"
            AuthMode.Password -> return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.resendOtp(state.username, purpose)) {
                is ApiResult.Success -> {
                    startCountdown(result.data.resendIn ?: 60)
                    _uiState.update { it.copy(isLoading = false, info = "کد جدید ارسال شد") }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun verifyOtp(onLoggedIn: () -> Unit) {
        val state = _uiState.value
        if (state.otpCode.length < 4) {
            _uiState.update { it.copy(error = "کد تأیید را وارد کنید") }
            return
        }
        if (state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (state.mode) {
                AuthMode.Otp -> when (val result = authRepository.loginWithOtp(state.username, state.otpCode)) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        onLoggedIn()
                    }
                    is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                AuthMode.Register, AuthMode.Forgot -> {
                    val purpose = if (state.mode == AuthMode.Register) "register" else "password_reset"
                    when (val result = authRepository.verifyOtpStep(state.username, purpose, state.otpCode)) {
                        is ApiResult.Success -> _uiState.update {
                            it.copy(
                                isLoading = false,
                                step = AuthStep.NewPassword,
                                challengeToken = result.data.challengeToken.orEmpty(),
                                password = "",
                                passwordConfirm = "",
                                info = "شماره تأیید شد. رمز جدید را تعیین کنید",
                            )
                        }
                        is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
                AuthMode.Password -> Unit
            }
        }
    }

    fun submitNewPassword(onLoggedIn: () -> Unit) {
        val state = _uiState.value
        if (state.password.length < 8) {
            _uiState.update { it.copy(error = "رمز عبور باید حداقل ۸ کاراکتر باشد") }
            return
        }
        if (state.password != state.passwordConfirm) {
            _uiState.update { it.copy(error = "رمز عبور و تکرار آن یکسان نیستند") }
            return
        }
        if (state.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            if (state.mode == AuthMode.Register) {
                when (
                    val result = authRepository.completeRegister(
                        state.username,
                        state.challengeToken,
                        state.password,
                        state.passwordConfirm,
                    )
                ) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        onLoggedIn()
                    }
                    is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            } else {
                when (
                    val result = authRepository.completePasswordReset(
                        state.username,
                        state.challengeToken,
                        state.password,
                        state.passwordConfirm,
                    )
                ) {
                    is ApiResult.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            mode = AuthMode.Password,
                            step = AuthStep.Credentials,
                            password = "",
                            passwordConfirm = "",
                            info = "رمز عبور تغییر کرد. اکنون وارد شوید",
                        )
                    }
                    is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = seconds.coerceAtLeast(0)
            _uiState.update { it.copy(resendIn = remaining) }
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _uiState.update { it.copy(resendIn = remaining) }
            }
        }
    }
}
