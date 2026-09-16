package ir.divarfiling.mobile.feature.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import ir.divarfiling.mobile.core.design.components.DfTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.components.DfBadge
import ir.divarfiling.mobile.core.design.components.DfCard
import ir.divarfiling.mobile.core.design.components.DfCardListSkeleton
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfEmptyState
import ir.divarfiling.mobile.core.design.components.DfEmptyVariant
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfModalBottomSheet
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfPullRefresh
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSheetActions
import ir.divarfiling.mobile.core.design.components.DfSheetScaffold
import ir.divarfiling.mobile.core.design.components.DfSoftChip
import ir.divarfiling.mobile.core.design.DfColors
import ir.divarfiling.mobile.core.network.AgencyAdvisorDto
import ir.divarfiling.mobile.core.network.AgencyInvitationDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMembersScreen(
    onBack: () -> Unit,
    onAdvisorClick: (Long) -> Unit = {},
    openInviteOnStart: Boolean = false,
    viewModel: AgencyAdvisorsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val pad = teamHorizontalPadding()

    LaunchedEffect(openInviteOnStart) {
        if (openInviteOnStart) viewModel.openInvite()
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    if (state.showInvite) {
        DfModalBottomSheet(onDismissRequest = viewModel::dismissInvite) {
            DfSheetScaffold(
                title = "دعوت مشاور",
                subtitle = "پیامک دعوت برای عضو جدید",
                onClose = viewModel::dismissInvite,
                footer = {
                    DfSheetActions(
                        primaryText = if (state.inviteSubmitting) "در حال ارسال…" else "ارسال دعوت",
                        onPrimary = viewModel::submitInvite,
                        primaryEnabled = !state.inviteSubmitting,
                        isSubmitting = state.inviteSubmitting,
                        onSecondary = viewModel::dismissInvite,
                    )
                },
            ) {
                OutlinedTextField(
                    value = state.invitePhone,
                    onValueChange = viewModel::onInvitePhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("موبایل") },
                    singleLine = true,
                )
            }
        }
    }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TeamAmbientBackground()
            DfPullRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.load() },
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    contentPadding = teamListContentPadding(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                ) {
                    item {
                        DfHubPageHeader(
                            title = "مشاوران",
                            subtitle = "${DateUtils.toPersianDigits(state.advisors.size.toString())} عضو",
                            sectionLabel = DfHeaderSections.TEAM,
                            titleIconRes = DfDecorIcons.Users,
                            onBack = onBack,
                        )
                    }
                    item {
                        DfTextField(
                            value = state.query,
                            onValueChange = viewModel::onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = pad),
                            label = "جستجو",
                            placeholder = "نام یا موبایل مشاور…",
                            singleLine = true,
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = pad),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        ) {
                            listOf(
                                AgencyAdvisorFilter.ALL to "همه",
                                AgencyAdvisorFilter.ACTIVE to "فعال",
                                AgencyAdvisorFilter.NO_SEAT to "بدون صندلی",
                                AgencyAdvisorFilter.INVITED to "دعوت‌شده",
                            ).forEach { (key, label) ->
                                DfSoftChip(
                                    text = label,
                                    selected = state.statusFilter == key,
                                    onClick = { viewModel.setStatusFilter(key) },
                                )
                            }
                        }
                    }
                    item {
                        DfPrimaryButton(
                            text = "دعوت مشاور",
                            onClick = viewModel::openInvite,
                            modifier = Modifier.padding(horizontal = pad),
                        )
                    }
                    when {
                        state.isLoading -> item {
                            DfCardListSkeleton(modifier = Modifier.padding(horizontal = pad))
                        }
                        state.advisors.isEmpty() && state.invitations.isEmpty() -> item {
                            DfEmptyState(
                                title = "هنوز عضوی به تیم اضافه نشده",
                                subtitle = "با دعوت مشاور، تیم خود را بسازید.",
                                variant = DfEmptyVariant.Empty,
                                modifier = Modifier.padding(horizontal = pad),
                            )
                        }
                        else -> {
                            if (state.statusFilter == AgencyAdvisorFilter.INVITED || state.invitations.isNotEmpty()) {
                                items(state.invitations, key = { "inv-${it.id}" }) { inv ->
                                    TeamInvitationCompactCard(
                                        invitation = inv,
                                        onResend = { viewModel.resendInvitation(inv.id) },
                                        onCancel = { viewModel.cancelInvitation(inv.id) },
                                        modifier = Modifier.padding(horizontal = pad),
                                    )
                                }
                            }
                            items(state.advisors, key = { it.id }) { advisor ->
                                TeamAdvisorCompactCard(
                                    advisor = advisor,
                                    onClick = { onAdvisorClick(advisor.id) },
                                    modifier = Modifier.padding(horizontal = pad),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
