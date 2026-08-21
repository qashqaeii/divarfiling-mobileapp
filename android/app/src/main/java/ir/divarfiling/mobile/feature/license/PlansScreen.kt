package ir.divarfiling.mobile.feature.license

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.divarfiling.mobile.core.AppLinks
import ir.divarfiling.mobile.core.ExternalBrowser
import ir.divarfiling.mobile.core.design.AppElevations
import ir.divarfiling.mobile.core.design.AppShapes
import ir.divarfiling.mobile.core.design.AppSpacing
import ir.divarfiling.mobile.core.design.AppTypography
import ir.divarfiling.mobile.core.design.DfIcons
import ir.divarfiling.mobile.core.design.DfThemeColors
import ir.divarfiling.mobile.core.design.components.DfContinueOnWebRow
import ir.divarfiling.mobile.core.design.components.DfHubPageHeader
import ir.divarfiling.mobile.core.design.components.DfPrimaryButton
import ir.divarfiling.mobile.core.design.components.DfScreenContainerColor
import ir.divarfiling.mobile.core.design.components.DfSecondaryButton
import ir.divarfiling.mobile.core.design.components.DfStatusBanner
import ir.divarfiling.mobile.core.design.components.DfStatusTone
import ir.divarfiling.mobile.core.network.ShopPlanDto
import java.text.NumberFormat
import java.util.Locale

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
            if (event == Lifecycle.Event.ON_RESUME) viewModel.consumePendingOrder()
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

    Scaffold(
        containerColor = DfScreenContainerColor,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.screenHorizontal)
                    .padding(bottom = AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                if (state.successMessage != null || state.license.valid) {
                    DfPrimaryButton(text = "شروع استفاده", onClick = onStartUsing)
                }
                DfPrimaryButton(
                    text = if (state.license.valid || state.license.canRenew) "تمدید / ادامه خرید" else "ادامه خرید",
                    onClick = {
                        viewModel.startCheckout { url -> ExternalBrowser.open(context, url) }
                    },
                    loading = state.isCheckingOut,
                    enabled = state.selectedPlanId != null && !state.isCheckingOut,
                )
                DfSecondaryButton(
                    text = "بررسی وضعیت پرداخت",
                    onClick = viewModel::consumePendingOrder,
                    loading = state.isVerifying,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            item {
                DfHubPageHeader(
                    title = "خرید و تمدید لایسنس",
                    subtitle = "قیمت‌ها از سرور خوانده می‌شود",
                    titleIcon = DfIcons.Sparkles,
                    onBack = onBack,
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
            if (state.license.expiringSoon && state.license.valid) {
                item {
                    DfStatusBanner(
                        message = "چند روز تا انقضای لایسنس مانده است. تمدید کنید تا دسترسی قطع نشود.",
                        tone = DfStatusTone.Warning,
                        title = "نزدیک به انقضا",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }
            if (!state.license.valid) {
                item {
                    DfStatusBanner(
                        message = "بدون لایسنس فعال، فایلینگ و استخراج قفل است. یک پلن انتخاب کنید.",
                        tone = DfStatusTone.Locked,
                        title = "لایسنس فعال نیست",
                        modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                    )
                }
            }
            items(state.plans, key = { it.id }) { plan ->
                PlanCard(
                    plan = plan,
                    selected = plan.id == state.selectedPlanId,
                    onSelect = { viewModel.selectPlan(plan.id) },
                    modifier = Modifier.padding(horizontal = AppSpacing.screenHorizontal),
                )
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

@Composable
private fun PlanCard(
    plan: ShopPlanDto,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = remember { NumberFormat.getInstance(Locale("fa", "IR")) }
    Surface(
        onClick = { if (!plan.purchaseBlocked) onSelect() },
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        color = DfThemeColors.surface(),
        shadowElevation = if (selected) AppElevations.raised else AppElevations.subtle,
        tonalElevation = AppElevations.none,
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(plan.name, style = AppTypography.cardTitle, fontWeight = FontWeight.Bold)
                if (plan.isFeatured) {
                    Text("پیشنهادی", style = AppTypography.labelSmall, color = DfThemeColors.primary(), fontWeight = FontWeight.SemiBold)
                }
            }
            plan.durationLabel?.let { Text(it, style = AppTypography.meta, color = DfThemeColors.textSecondary()) }
            plan.tagline?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTypography.bodyDescription, color = DfThemeColors.textSecondary())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                if (plan.hasDiscount && plan.originalPrice != null) {
                    Text(
                        "${format.format(plan.originalPrice)} تومان",
                        style = AppTypography.meta,
                        color = DfThemeColors.textMuted(),
                    )
                }
                Text(
                    "${format.format(plan.finalPrice ?: 0)} تومان",
                    style = AppTypography.sectionTitle,
                    fontWeight = FontWeight.Bold,
                    color = DfThemeColors.textPrimary(),
                )
            }
            if (plan.purchaseBlocked) {
                Text(plan.purchaseBlockMessage.orEmpty(), style = AppTypography.meta, color = DfThemeColors.error())
            }
        }
    }
}
