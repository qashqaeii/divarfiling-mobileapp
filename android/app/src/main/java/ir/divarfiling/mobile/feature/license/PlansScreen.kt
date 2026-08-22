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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.ExternalBrowser
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.FormatUtils
import ir.divarfiling.mobile.core.design.components.DfContinueOnWebRow
import ir.divarfiling.mobile.core.design.components.DfDecorIcons
import ir.divarfiling.mobile.core.design.components.DfGlassTextButton
import ir.divarfiling.mobile.core.design.components.DfHeaderSections
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.design.components.DfTextField
import ir.divarfiling.mobile.feature.license.components.DiscountApplyButton
import ir.divarfiling.mobile.feature.license.components.LicensePlanCard
import ir.divarfiling.mobile.feature.license.components.LicenseStatusHero

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

    val selectedPlan = state.plans.firstOrNull { it.id == state.selectedPlanId }

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(top = AppSpacing.xs, bottom = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                if (state.license.valid) {
                    DfPrimaryButton(text = "شروع استفاده", onClick = onStartUsing)
                }
                DfPrimaryButton(
                    text = when {
                        selectedPlan != null -> "خرید ${selectedPlan.name}"
                        state.license.valid || state.license.canRenew -> "تمدید / ادامه خرید"
                        else -> "ادامه خرید"
                    },
                    onClick = {
                        viewModel.startCheckout { url -> ExternalBrowser.open(context, url) }
                    },
                    loading = state.isCheckingOut,
                    enabled = state.selectedPlanId != null && !state.isCheckingOut,
                )
                DfSecondaryButton(
                    text = "بررسی وضعیت",
                    onClick = viewModel::consumePendingOrder,
                    loading = state.isVerifying,
                    enabled = !state.isVerifying,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 160.dp),
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

            item {
                Column(
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(
                        text = "پلن‌های موجود",
                        style = AppTypography.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    Text(
                        text = "یک پلن را انتخاب کنید؛ قیمت‌ها مستقیم از سرور خوانده می‌شود.",
                        style = AppTypography.bodyDescription,
                        color = DfThemeColors.textSecondary(),
                    )
                }
            }

            items(state.plans, key = { it.id }) { plan ->
                LicensePlanCard(
                    plan = plan,
                    selected = plan.id == state.selectedPlanId,
                    onSelect = { viewModel.selectPlan(plan.id) },
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(
                        text = "کد تخفیف",
                        style = AppTypography.cardTitle,
                        fontWeight = FontWeight.Bold,
                        color = DfThemeColors.textPrimary(),
                    )
                    DfTextField(
                        value = state.discountCode,
                        onValueChange = viewModel::onDiscountCodeChange,
                        label = "کد تخفیف",
                        enabled = !state.isCheckingOut && !state.isApplyingDiscount,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DiscountApplyButton(
                            text = if (state.isApplyingDiscount) "…" else "اعمال",
                            onClick = viewModel::applyDiscount,
                            enabled = state.discountCode.isNotBlank() && state.selectedPlanId != null,
                            loading = state.isApplyingDiscount,
                        )
                        if (state.discountPreview != null) {
                            DfGlassTextButton(text = "حذف", onClick = viewModel::clearDiscount)
                        }
                    }
                    state.discountPreview?.let { preview ->
                        val before = preview.baseFinalPrice ?: preview.originalPrice
                        val after = preview.finalPrice
                        if (before != null && after != null) {
                            Text(
                                text = "قبل: ${FormatUtils.formatPriceToman(before)}  ←  بعد: ${FormatUtils.formatPriceToman(after)}",
                                style = AppTypography.bodyDescription,
                                color = DfThemeColors.textSecondary(),
                            )
                        }
                    }
                }
            }

            item {
                DfContinueOnWebRow(
                    title = "مدیریت کامل اشتراک در وب",
                    subtitle = "داشبورد لایسنس، فاکتور و تمدید پیشرفته",
                    url = AppLinks.DASHBOARD_LICENSES,
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
            }
        }
    }
}
