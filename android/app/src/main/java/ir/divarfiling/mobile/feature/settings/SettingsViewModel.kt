package ir.divarfiling.mobile.feature.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.core.datastore.SessionStore
import ir.divarfiling.mobile.core.license.LicenseState
import ir.divarfiling.mobile.core.network.NotificationPrefsDto
import ir.divarfiling.mobile.core.network.UserDto
import ir.divarfiling.mobile.core.avatar.AvatarCatalog
import ir.divarfiling.mobile.core.avatar.AvatarPickerLogic
import ir.divarfiling.mobile.core.util.PhoneNormalizer
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.AuthRepository
import ir.divarfiling.mobile.data.repository.DashboardRepository
import ir.divarfiling.mobile.data.repository.LicenseRepository
import ir.divarfiling.mobile.data.repository.SettingsRepository
import ir.divarfiling.mobile.feature.postcall.PostCallController
import ir.divarfiling.mobile.feature.postcall.PostCallFeatureFlags
import ir.divarfiling.mobile.feature.postcall.permission.PostCallHealthSnapshot
import ir.divarfiling.mobile.feature.postcall.permission.PostCallPermissionSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class SettingsUiState(
    val user: UserDto? = null,
    val license: LicenseState = LicenseState(),
    val notificationPrefs: NotificationPrefsDto = NotificationPrefsDto(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val isSavingPrefs: Boolean = false,
    val showProfileSheet: Boolean = false,
    val showAvatarPicker: Boolean = false,
    val reopenProfileAfterAvatarPicker: Boolean = false,
    val pendingAvatarKey: String? = null,
    val isSavingAvatarKey: Boolean = false,
    val avatarPickerTouched: Boolean = false,
    val canSaveAvatarPicker: Boolean = false,
    val editFullName: String = "",
    val editPhone: String = "",
    val deviceId: String = "",
    val successMessage: String? = null,
    val error: String? = null,
    val notificationBadgeCount: Int = 0,
    val postCallEnabled: Boolean = false,
    val postCallPermissions: PostCallPermissionSnapshot? = null,
    val postCallStatus: String? = null,
    val postCallBusy: Boolean = false,
    val postCallHealth: PostCallHealthSnapshot? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val licenseRepository: LicenseRepository,
    private val dashboardRepository: DashboardRepository,
    private val sessionStore: SessionStore,
    private val postCallController: PostCallController,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(authRepository.licenseState, authRepository.isLoggedIn) { license, loggedIn ->
                license to loggedIn
            }.collect { (license, _) ->
                _uiState.update { it.copy(license = license) }
            }
        }
        viewModelScope.launch {
            when (val result = dashboardRepository.getDashboard()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(notificationBadgeCount = result.data.notificationsUnread) }
                is ApiResult.Error -> Unit
            }
        }
        refreshAll()
        refreshPostCallSettings()
    }

    fun refreshPostCallSettings() {
        if (!PostCallFeatureFlags.ENABLED) return
        viewModelScope.launch {
            val enabled = postCallController.isEnabledForCurrentUser()
            val snap = postCallController.permissionSnapshot()
            _uiState.update {
                it.copy(
                    postCallEnabled = enabled,
                    postCallPermissions = snap,
                    postCallStatus = buildPostCallStatus(enabled, snap),
                    postCallHealth = if (enabled) postCallController.healthSnapshot() else null,
                )
            }
        }
    }

    suspend fun enablePostCallAfterPermissions() {
        _uiState.update { it.copy(postCallBusy = true) }
        postCallController.setEnabled(true)
        refreshPostCallSettings()
        _uiState.update { it.copy(postCallBusy = false) }
    }

    fun disablePostCall() {
        viewModelScope.launch {
            _uiState.update { it.copy(postCallBusy = true) }
            postCallController.setEnabled(false)
            refreshPostCallSettings()
            _uiState.update { it.copy(postCallBusy = false) }
        }
    }

    private fun buildPostCallStatus(enabled: Boolean, snap: PostCallPermissionSnapshot): String? {
        if (!enabled) return null
        if (!snap.phoneStateGranted) return "برای تشخیص تماس، مجوز وضعیت تماس لازم است."
        if (!snap.callLogRuntimeAvailable) {
            return "بدون گزارش تماس، شماره فقط در صورت ارسال مستقیم توسط سیستم قابل تشخیص است."
        }
        if (!snap.canShowOverlay && !snap.canNotify) {
            return "پنجره شناور یا اعلان را فعال کنید تا پس از تماس یادآور ببینید."
        }
        if (snap.isXiaomiFamilyDevice &&
            snap.miuiBackgroundPopupStatus ==
            ir.divarfiling.mobile.feature.postcall.permission.MiuiBackgroundPopupStatus.NeedsManualConfirmation
        ) {
            return "در گوشی شیائومی، مجوز پنجره پس‌زمینه MIUI را جدا از Overlay استاندارد بررسی کنید."
        }
        return null
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = it.user != null, isLoading = it.user == null, error = null) }
            licenseRepository.syncDeviceAndRefresh()
            when (val dashboard = dashboardRepository.getDashboard()) {
                is ApiResult.Success ->
                    _uiState.update { it.copy(notificationBadgeCount = dashboard.data.notificationsUnread) }
                is ApiResult.Error -> Unit
            }
            when (val profile = settingsRepository.getProfile()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        user = profile.data,
                        editFullName = profile.data.fullName,
                        editPhone = profile.data.phone.orEmpty(),
                    ).recomputeAvatarSave()
                }
                is ApiResult.Error -> _uiState.update { it.copy(error = profile.message) }
            }
            when (val prefs = settingsRepository.getNotificationPrefs()) {
                is ApiResult.Success -> _uiState.update { it.copy(notificationPrefs = prefs.data) }
                is ApiResult.Error -> if (_uiState.value.error == null) {
                    _uiState.update { it.copy(error = prefs.message) }
                }
            }
            _uiState.update {
                it.copy(deviceId = sessionStore.getDeviceId().orEmpty())
            }
            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    fun toggleProfileSheet(show: Boolean) {
        _uiState.update {
            val user = it.user
            it.copy(
                showProfileSheet = show,
                editFullName = user?.fullName ?: it.editFullName,
                editPhone = user?.phone.orEmpty().ifBlank { it.editPhone },
            )
        }
    }

    fun openAvatarPicker(fromProfileEdit: Boolean = false) {
        _uiState.update { state ->
            val seed = AvatarPickerLogic.seedKey(state.user)
            val touched = AvatarPickerLogic.initialPickerTouched(state.user)
            state.copy(
                showProfileSheet = if (fromProfileEdit) false else state.showProfileSheet,
                reopenProfileAfterAvatarPicker = fromProfileEdit,
                showAvatarPicker = true,
                pendingAvatarKey = seed,
                avatarPickerTouched = touched,
            ).recomputeAvatarSave()
        }
    }

    fun dismissAvatarPicker() {
        _uiState.update { state ->
            state.copy(
                showAvatarPicker = false,
                showProfileSheet = state.showProfileSheet || state.reopenProfileAfterAvatarPicker,
                reopenProfileAfterAvatarPicker = false,
            )
        }
    }

    fun onPendingAvatarKeyChange(key: String) {
        _uiState.update {
            it.copy(
                pendingAvatarKey = AvatarCatalog.normalizeKey(key),
                avatarPickerTouched = true,
            ).recomputeAvatarSave()
        }
    }

    private fun SettingsUiState.recomputeAvatarSave(): SettingsUiState {
        return copy(
            canSaveAvatarPicker = AvatarPickerLogic.canSave(
                user,
                pendingAvatarKey,
                avatarPickerTouched,
            ),
        )
    }

    fun saveAvatarKey() {
        val state = _uiState.value
        if (!state.canSaveAvatarPicker) return
        val key = state.pendingAvatarKey ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingAvatarKey = true, error = null) }
            when (val result = settingsRepository.setAvatarKey(key)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSavingAvatarKey = false,
                        user = result.data,
                        showAvatarPicker = false,
                        reopenProfileAfterAvatarPicker = false,
                        successMessage = "آواتار شما تغییر کرد",
                    ).recomputeAvatarSave()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingAvatarKey = false, error = result.message)
                }
            }
        }
    }

    fun onEditFullNameChange(value: String) = _uiState.update { it.copy(editFullName = value) }
    fun onEditPhoneChange(value: String) = _uiState.update { it.copy(editPhone = PhoneNormalizer.normalize(value)) }

    fun saveProfile() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, error = null) }
            when (val result = settingsRepository.updateProfile(state.editFullName, state.editPhone)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isSavingProfile = false,
                        user = result.data,
                        showProfileSheet = false,
                        successMessage = "پروفایل ذخیره شد",
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingProfile = false, error = result.message)
                }
            }
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, error = null) }
            val bytes = withContext(Dispatchers.IO) { readAvatarBytes(uri) }
            if (bytes == null) {
                _uiState.update { it.copy(isUploadingAvatar = false, error = "خواندن تصویر ناموفق بود") }
                return@launch
            }
            when (val result = settingsRepository.uploadAvatar(bytes)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isUploadingAvatar = false,
                        user = result.data,
                        successMessage = "عکس پروفایل ذخیره شد",
                    ).recomputeAvatarSave()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isUploadingAvatar = false, error = result.message)
                }
            }
        }
    }

    fun removeAvatar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, error = null) }
            when (val result = settingsRepository.deleteAvatar()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isUploadingAvatar = false,
                        user = result.data,
                        successMessage = "عکس پروفایل حذف شد",
                    ).recomputeAvatarSave()
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isUploadingAvatar = false, error = result.message)
                }
            }
        }
    }

    private fun readAvatarBytes(uri: Uri): ByteArray? {
        return appContext.contentResolver.openInputStream(uri)?.use { input ->
            val bitmap = BitmapFactory.decodeStream(input) ?: return null
            val maxSide = 720
            val scale = maxOf(bitmap.width, bitmap.height).toFloat() / maxSide
            val prepared = if (scale > 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width / scale).toInt().coerceAtLeast(1),
                    (bitmap.height / scale).toInt().coerceAtLeast(1),
                    true,
                )
            } else {
                bitmap
            }
            ByteArrayOutputStream().use { out ->
                prepared.compress(Bitmap.CompressFormat.JPEG, 85, out)
                out.toByteArray()
            }
        }
    }

    fun updatePref(transform: (NotificationPrefsDto) -> NotificationPrefsDto) {
        val next = transform(_uiState.value.notificationPrefs)
        _uiState.update { it.copy(notificationPrefs = next, isSavingPrefs = true) }
        viewModelScope.launch {
            when (val result = settingsRepository.updateNotificationPrefs(next)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(notificationPrefs = result.data, isSavingPrefs = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isSavingPrefs = false, error = result.message)
                }
            }
        }
    }

    fun onDigestHourChange(hour: Int) {
        updatePref { it.copy(digestHour = hour.coerceIn(0, 23)) }
    }

    fun clearMessage() = _uiState.update { it.copy(successMessage = null, error = null) }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}
