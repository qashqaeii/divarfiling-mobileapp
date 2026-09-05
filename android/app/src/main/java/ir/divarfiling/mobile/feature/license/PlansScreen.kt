package ir.divarfiling.mobile.feature.license

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.ExternalBrowser
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.DateUtils
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfContinueOnWebRow
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.network.ShopPlanDto
import ir.divarfiling.mobile.feature.license.components.AgencyPlanPanel
import ir.divarfiling.mobile.feature.license.components.LicenseRenewalModeToggle
import ir.divarfiling.mobile.feature.license.components.LicenseStatusHero
import ir.divarfiling.mobile.feature.license.components.PersonalPlanCard
import ir.divarfiling.mobile.feature.license.components.PlanModeTabs
import ir.divarfiling.mobile.feature.license.components.PlansCheckoutBottomBar
import ir.divarfiling.mobile.feature.license.components.PlansDiscountSection
import ir.divarfiling.mobile.feature.license.components.PlansSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    onBack: () -> Unit,
    onStartUsing: () -> Unit = {},
    viewModel: PlansViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.consumePendingOrder()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val selectedPlan = state.selectedPlan
    val selectedQuantity = state.selectedQuantity
    val checkoutTotal = state.checkoutTotal
    val showAgencyPanel = state.planMode == PlanMode.Agency && state.agencyPlans.isNotEmpty()

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                PlansCheckoutBottomBar(
                    selectedPlan = selectedPlan,
                    quantity = selectedQuantity,
                    total = checkoutTotal,
                    discountPreview = state.discountPreview,
                    isCheckingOut = state.isCheckingOut,
                    isVerifying = state.isVerifying,
                    canCheckout = state.selectedPlanId != null && state.phoneVerified,
                    showStartUsing = state.license.valid,
                    checkoutLabel = buildCheckoutButtonLabel(selectedPlan, selectedQuantity, checkoutTotal),
                    onCheckout = { viewModel.startCheckout { url -> ExternalBrowser.open(context, url) } },
                    onVerify = viewModel::consumePendingOrder,
                    onStartUsing = onStartUsing,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                DfHubPageHeader(
                    title = "خرید و تمدید لایسنس",
                    subtitle = "دسترسی کامل به فایلینگ، CRM و استخراج",
                    sectionLabel = DfHeaderSections.LICENSE,
                    titleIconRes = DfDecorIcons.Sparkles,
                    onBack = onBack,
                )
            }

            item {
                LicenseStatusHero(
                    license = state.license,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            if (!state.phoneVerified) {
                item {
                    DfStatusBanner(
                        message = "برای خرید، شماره موبایل حساب باید تأیید شده باشد. از تنظیمات پروفایل اقدام کنید.",
                        tone = DfStatusTone.Warning,
                        title = "تأیید موبایل لازم است",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            state.orderStatusMessage?.takeIf { it.isNotBlank() }?.let { statusText ->
                item {
                    DfStatusBanner(
                        message = statusText,
                        tone = when (state.orderStatus) {
                            "paid" -> DfStatusTone.Success
                            "failed", "cancelled" -> DfStatusTone.Error
                            else -> DfStatusTone.Info
                        },
                        title = "وضعیت پرداخت",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            if (state.isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.lg),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = DfThemeColors.primary())
                    }
                }
            }

            if (state.license.canRenew) {
                item {
                    LicenseRenewalModeToggle(
                        renewCurrentLicense = state.renewCurrentLicense,
                        onRenewCurrentLicenseChange = viewModel::setRenewCurrentLicense,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            if (state.personalPlans.isNotEmpty() && state.agencyPlans.isNotEmpty()) {
                item {
                    PlanModeTabs(
                        mode = state.planMode,
                        onModeChange = viewModel::setPlanMode,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            if (showAgencyPanel) {
                if (state.isRenewalCheckout) {
                    item {
                        DfStatusBanner(
                            message = "تمدید فقط برای لایسنس فعلی شما انجام می‌شود. برای خرید چند لایسنس جدید، حالت «خرید لایسنس جدید» را انتخاب کنید.",
                            tone = DfStatusTone.Info,
                            title = "تمدید آژانس",
                            modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                        )
                    }
                }
                item {
                    AgencyPlanPanel(
                        plans = state.agencyPlans,
                        selectedPlanId = state.selectedPlanId,
                        quantity = selectedQuantity,
                        pricingPreview = state.discountPreview,
                        isPricingLoading = state.isPricingLoading,
                        onSelectPlan = viewModel::selectPlan,
                        onQuantityChange = { qty ->
                            state.selectedPlanId?.let { viewModel.setQuantity(it, qty) }
                        },
                        showQuantity = state.canChooseQuantity,
                        enabled = !state.isCheckingOut,
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            } else if (state.personalPlans.isNotEmpty()) {
                item {
                    PlansSectionHeader(
                        title = "پلن‌های شخصی",
                        subtitle = "مناسب مشاور مستقل — یک لایسنس برای خودتان",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
                items(state.personalPlans, key = { "personal-${it.id}" }) { plan ->
                    PersonalPlanCard(
                        plan = plan,
                        selected = plan.id == state.selectedPlanId,
                        onSelect = { viewModel.selectPlan(plan.id) },
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }

            item {
                PlansDiscountSection(
                    discountCode = state.discountCode,
                    onDiscountCodeChange = viewModel::onDiscountCodeChange,
                    onApply = viewModel::applyDiscount,
                    onClear = viewModel::clearDiscount,
                    isApplying = state.isApplyingDiscount,
                    canApply = state.discountCode.isNotBlank() &&
                        state.selectedPlanId != null &&
                        !state.isApplyingDiscount &&
                        !state.isCheckingOut,
                    preview = state.discountPreview,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            item {
                DfContinueOnWebRow(
                    title = "مدیریت کامل اشتراک در وب",
                    subtitle = "داشبورد لایسنس، تخصیص کلید آژانس و فاکتور",
                    url = AppLinks.DASHBOARD_LICENSES,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }
    }
}

private fun buildCheckoutButtonLabel(
    plan: ShopPlanDto?,
    quantity: Int,
    total: Long?,
): String = when {
    plan == null -> "ادامه خرید"
    plan.isAgencyPlan() && total != null && quantity > 1 ->
        "خرید ${DateUtils.toPersianDigits(quantity.toString())} لایسنس · ${FormatUtils.formatPriceToman(total)}"
    plan.isAgencyPlan() && total != null ->
        "خرید لایسنس آژانس · ${FormatUtils.formatPriceToman(total)}"
    total != null -> "خرید ${plan.name} · ${FormatUtils.formatPriceToman(total)}"
    else -> "خرید ${plan.name}"
}
