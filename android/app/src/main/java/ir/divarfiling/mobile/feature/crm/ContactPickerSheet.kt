package ir.divarfiling.mobile.feature.crm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.components.DfContactRow
import ir.divarfiling.mobile.core.design.components.DfContactListSkeleton
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactPickerUiState(
    val contacts: List<ContactDto> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ContactPickerViewModel @Inject constructor(
    private val crmRepository: CrmRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactPickerUiState())
    val uiState: StateFlow<ContactPickerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load(query: String = _uiState.value.query) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = crmRepository.getContacts(query = query, pageSize = 100)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(contacts = result.data.items, isLoading = false)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        load(query)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerSheet(
    onDismiss: () -> Unit,
    onContactSelected: (ContactDto) -> Unit,
    viewModel: ContactPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "انتخاب مخاطب",
                style = AppTypography.sectionTitle,
                fontWeight = FontWeight.Bold,
            )
            DfSearchField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = "جستجوی نام یا تلفن…",
            )
            when {
                state.isLoading -> DfContactListSkeleton(count = 5)
                state.contacts.isEmpty() -> DfEmptyState(
                    title = "مخاطبی یافت نشد",
                    subtitle = "ابتدا یک مخاطب در CRM ثبت کنید",
                )
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.contacts, key = { it.id }) { contact ->
                            DfContactRow(
                                name = contact.fullName,
                                phone = contact.phone,
                                status = contact.status,
                                customerType = contact.customerType,
                                onClick = { onContactSelected(contact) },
                            )
                        }
                    }
                }
            }
        }
    }
}
