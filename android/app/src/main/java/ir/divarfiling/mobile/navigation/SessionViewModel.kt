package ir.divarfiling.mobile.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.data.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    val isLoggedIn = authRepository.isLoggedIn
}
