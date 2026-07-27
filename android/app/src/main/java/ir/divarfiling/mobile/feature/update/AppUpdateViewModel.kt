package ir.divarfiling.mobile.feature.update

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.divarfiling.mobile.BuildConfig
import ir.divarfiling.mobile.core.network.AppVersionData
import ir.divarfiling.mobile.core.update.ApkInstaller
import ir.divarfiling.mobile.core.update.AppUpdatePreferences
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.AppUpdateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class AppUpdatePhase {
    Idle,
    Checking,
    Available,
    AwaitingInstallPermission,
    Downloading,
    ReadyToInstall,
    Installing,
    UpToDate,
    Error,
}

data class AppUpdateUiState(
    val phase: AppUpdatePhase = AppUpdatePhase.Idle,
    val visible: Boolean = false,
    val forceUpdate: Boolean = false,
    val version: AppVersionData? = null,
    val progress: Float = 0f,
    val progressLabel: String = "",
    val message: String? = null,
    val error: String? = null,
    val manualCheck: Boolean = false,
)

@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    private val repository: AppUpdateRepository,
    private val preferences: AppUpdatePreferences,
    private val apkInstaller: ApkInstaller,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    private var downloadJob: Job? = null
    private var downloadedFile: File? = null
    private var pendingInstallAfterPermission = false

    fun checkOnLaunch() {
        viewModelScope.launch {
            val lastCheck = preferences.getLastCheckAt()
            val due = System.currentTimeMillis() - lastCheck >= AppUpdatePreferences.SOFT_CHECK_INTERVAL_MS
            if (!due && _uiState.value.phase == AppUpdatePhase.Idle) return@launch
            checkInternal(manual = false, ignoreDismiss = false)
        }
    }

    fun checkManually() {
        viewModelScope.launch {
            checkInternal(manual = true, ignoreDismiss = true)
        }
    }

    private suspend fun checkInternal(manual: Boolean, ignoreDismiss: Boolean) {
        _uiState.update {
            it.copy(
                phase = AppUpdatePhase.Checking,
                manualCheck = manual,
                error = null,
                message = if (manual) "در حال بررسی نسخه…" else it.message,
                visible = manual || it.visible,
            )
        }
        when (val result = repository.checkForUpdate()) {
            is ApiResult.Success -> {
                preferences.markCheckedNow()
                val data = result.data
                val localNeedsUpdate = data.versionCode > BuildConfig.VERSION_CODE && data.available
                val updateAvailable = data.updateAvailable || localNeedsUpdate
                if (!updateAvailable) {
                    _uiState.update {
                        it.copy(
                            phase = if (manual) AppUpdatePhase.UpToDate else AppUpdatePhase.Idle,
                            visible = manual,
                            version = data,
                            forceUpdate = false,
                            message = "شما آخرین نسخه را دارید (v${BuildConfig.VERSION_NAME})",
                        )
                    }
                    return
                }
                val dismissed = preferences.getDismissedVersionCode()
                val forceEffective = data.forceUpdate ||
                    BuildConfig.VERSION_CODE < data.minSupportedVersionCode
                if (!ignoreDismiss && !forceEffective && dismissed >= data.versionCode) {
                    _uiState.update {
                        it.copy(phase = AppUpdatePhase.Idle, visible = false, version = data)
                    }
                    return
                }
                _uiState.update {
                    it.copy(
                        phase = AppUpdatePhase.Available,
                        visible = true,
                        version = data,
                        forceUpdate = forceEffective,
                        message = null,
                        error = null,
                        progress = 0f,
                    )
                }
            }
            is ApiResult.Error -> {
                _uiState.update {
                    it.copy(
                        phase = AppUpdatePhase.Error,
                        visible = manual,
                        error = result.message,
                        message = null,
                    )
                }
            }
        }
    }

    fun dismissSoftUpdate() {
        val version = _uiState.value.version ?: return
        if (_uiState.value.forceUpdate) return
        viewModelScope.launch {
            preferences.dismissVersion(version.versionCode)
            _uiState.update {
                it.copy(visible = false, phase = AppUpdatePhase.Idle, progress = 0f)
            }
        }
    }

    fun startUpdate() {
        val version = _uiState.value.version ?: return
        if (version.apkUrl.isBlank()) {
            _uiState.update { it.copy(error = "لینک دانلود در دسترس نیست") }
            return
        }
        if (!apkInstaller.canInstallPackages()) {
            pendingInstallAfterPermission = true
            _uiState.update { it.copy(phase = AppUpdatePhase.AwaitingInstallPermission) }
            return
        }
        beginDownload(version)
    }

    fun onInstallPermissionResult(granted: Boolean) {
        if (!granted) {
            _uiState.update {
                it.copy(
                    phase = AppUpdatePhase.Available,
                    error = "برای نصب آپدیت، اجازه «نصب از منابع ناشناس» لازم است.",
                )
            }
            return
        }
        val version = _uiState.value.version ?: return
        if (pendingInstallAfterPermission || downloadedFile?.exists() == true) {
            pendingInstallAfterPermission = false
            if (downloadedFile?.exists() == true) {
                launchInstaller()
            } else {
                beginDownload(version)
            }
        }
    }

    fun openInstallPermissionSettings(): Intent = apkInstaller.installPermissionSettingsIntent()

    fun installNow() {
        if (!apkInstaller.canInstallPackages()) {
            pendingInstallAfterPermission = true
            _uiState.update { it.copy(phase = AppUpdatePhase.AwaitingInstallPermission) }
            return
        }
        launchInstaller()
    }

    fun clearManualMessage() {
        if (_uiState.value.phase == AppUpdatePhase.UpToDate || _uiState.value.phase == AppUpdatePhase.Error) {
            _uiState.update {
                it.copy(visible = false, phase = AppUpdatePhase.Idle, message = null, error = null)
            }
        }
    }

    private fun beginDownload(version: AppVersionData) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    phase = AppUpdatePhase.Downloading,
                    progress = 0f,
                    progressLabel = "۰٪",
                    error = null,
                    visible = true,
                )
            }
            when (
                val result = repository.downloadApk(
                    url = version.apkUrl,
                    filename = version.apkFilename.ifBlank { "divar-filing-update.apk" },
                    expectedSha256 = version.apkSha256,
                ) { progress ->
                    val pct = (progress.fraction * 100).toInt().coerceIn(0, 100)
                    _uiState.update { state ->
                        state.copy(
                            progress = progress.fraction,
                            progressLabel = "$pct٪",
                        )
                    }
                }
            ) {
                is ApiResult.Success -> {
                    downloadedFile = result.data
                    _uiState.update {
                        it.copy(
                            phase = AppUpdatePhase.ReadyToInstall,
                            progress = 1f,
                            progressLabel = "۱۰۰٪",
                        )
                    }
                    launchInstaller()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            phase = AppUpdatePhase.Error,
                            error = result.message,
                            visible = true,
                        )
                    }
                }
            }
        }
    }

    private fun launchInstaller() {
        val file = downloadedFile ?: return
        if (!file.exists()) {
            _uiState.update {
                it.copy(phase = AppUpdatePhase.Error, error = "فایل آپدیت پیدا نشد؛ دوباره دانلود کنید.")
            }
            return
        }
        _uiState.update { it.copy(phase = AppUpdatePhase.Installing) }
        try {
            appContext.startActivity(apkInstaller.installApk(file))
            _uiState.update { it.copy(phase = AppUpdatePhase.ReadyToInstall) }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    phase = AppUpdatePhase.Error,
                    error = e.message ?: "نمی‌توان نصب‌کننده را باز کرد",
                )
            }
        }
    }
}
