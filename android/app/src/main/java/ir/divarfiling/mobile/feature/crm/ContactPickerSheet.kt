package ir.divarfiling.mobile.feature.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfContactListSkeleton
import ir.divarfiling.mobile.core.design.components.DfContactRow
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPremiumCard
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSearchField
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSheetSection
import ir.divarfiling.mobile.core.network.ContactDto
import ir.divarfiling.mobile.core.network.DatasetDto
import ir.divarfiling.mobile.core.network.ListingDto
import ir.divarfiling.mobile.data.repository.ApiResult
import ir.divarfiling.mobile.data.repository.CrmRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init { load() }

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
fun SendFilingSheet(
    step: Int,
    datasets: List<DatasetDto>,
    listings: List<ListingDto>,
    note: String,
    isLoading: Boolean,
    isSubmitting: Boolean,
    onNoteChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onDatasetSelected: (String) -> Unit,
    onBackToDatasets: () -> Unit,
    onListingSend: (ListingDto, Boolean) -> Unit,
) {
    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = if (step == 0) "انتخاب فایلینگ" else "انتخاب آگهی",
            subtitle = if (step == 0) "فایل استخراج‌شده را برای ارسال به مخاطب انتخاب کنید" else "آگهی مناسب را به همراه یادداشت ارسال کنید",
            icon = if (step == 0) DfIcons.Folder else DfIcons.Home,
            onClose = onDismiss,
            scrollable = false,
        ) {
            if (step == 1) {
                DfSheetSection(title = "یادداشت ارسال") {
                    OutlinedTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("یادداشت برای مخاطب") },
                        minLines = 2,
                        placeholder = { Text("مثلاً: این ملک مناسب بودجه شماست") },
                    )
                    DfGlassTextButton(text = "بازگشت به لیست فایلینگ", onClick = onBackToDatasets)
                }
            }
            when {
                isLoading -> DfCardListSkeleton(count = 4, itemHeight = 72.dp)
                step == 0 -> {
                    if (datasets.isEmpty()) {
                        DfEmptyState(title = "فایلینگی یافت نشد", subtitle = "ابتدا استخراج انجام دهید")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = AppSpacing.xl),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(datasets, key = { it.id }) { dataset ->
                                DfPremiumCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onDatasetSelected(dataset.id) },
                                ) {
                                    Column(Modifier.padding(4.dp)) {
                                        Text(dataset.name ?: dataset.id, style = AppTypography.cardTitle)
                                        dataset.itemCount.let {
                                            if (it > 0) Text("$it آگهی", style = AppTypography.bodyDescription)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                listings.isEmpty() -> DfEmptyState(title = "آگهی‌ای یافت نشد", subtitle = "")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = AppSpacing.xl),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(listings, key = { it.token }) { listing ->
                            DfPremiumCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(listing.title ?: listing.token, style = AppTypography.cardTitle)
                                    listing.price?.let {
                                        Text(FormatUtils.formatPriceToman(it), style = AppTypography.bodyDescription)
                                    }
                                    listing.district?.let {
                                        Text(it, style = AppTypography.labelSmall, color = DfColors.TextMuted)
                                    }
                                    DfPrimaryButton(
                                        text = "ارسال به مخاطب",
                                        onClick = { onListingSend(listing, false) },
                                        enabled = !isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    DfPrimaryButton(
                                        text = "ارسال + واتساپ",
                                        onClick = { onListingSend(listing, true) },
                                        enabled = !isSubmitting,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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

    DfModalBottomSheet(onDismissRequest = onDismiss) {
        DfSheetScaffold(
            title = "انتخاب مخاطب",
            subtitle = "مخاطب CRM را برای ادامه عملیات انتخاب کنید",
            icon = DfIcons.Users,
            onClose = onDismiss,
            scrollable = false,
        ) {
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
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = AppSpacing.xl),
                    ) {
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
