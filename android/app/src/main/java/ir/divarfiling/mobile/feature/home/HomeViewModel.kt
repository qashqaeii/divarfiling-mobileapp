package ir.divarfiling.mobile.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val licenseState: StateFlow<ir.divarfiling.mobile.core.license.LicenseState> =
        authRepository.licenseState
}
