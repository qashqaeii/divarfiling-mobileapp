package ir.divarfiling.mobile.feature.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {
    val licenseState: Flow<ir.divarfiling.mobile.core.license.LicenseState> =
        authRepository.licenseState
}
